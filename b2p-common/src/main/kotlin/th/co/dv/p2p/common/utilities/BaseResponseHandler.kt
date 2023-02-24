package th.co.dv.p2p.common.utilities

import io.netty.handler.codec.http.HttpResponseStatus
import net.corda.core.internal.rootCause
import org.springframework.security.access.AccessDeniedException
import th.co.dv.p2p.common.constants.DOT
import th.co.dv.p2p.common.enums.ServiceErrorClassType
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.enums.Status
import th.co.dv.p2p.common.exceptions.*
import th.co.dv.p2p.common.models.ErrorInfoDto
import th.co.dv.p2p.common.models.ErrorObject
import th.co.dv.p2p.common.models.PageModel
import th.co.dv.p2p.common.models.ResponseModel
import th.co.dv.p2p.common.utilities.search.paging.PagableList
import th.co.dv.p2p.corda.base.IllegalFlowException
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.ParameterizedType
import javax.persistence.PersistenceException
import kotlin.reflect.KClass

abstract class BaseResponseHandler {

    companion object {
        /**
         * Method for get error info from Exception class
         * @return ErrorInfoDto
         */
        fun getErrorInfo(exception: Exception): ErrorInfoDto {
            return when {
                exception.hasCause(IllegalFlowException::class) -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.BAD_REQUEST,
                        errorClassType = ServiceErrorClassType.VALIDATION,
                        errorMessage = exception.message)
                exception.hasCause(IllegalArgumentException::class) -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.BAD_REQUEST,
                        errorClassType = ServiceErrorClassType.ILLEGAL_ARGUMENT)
                exception.hasCause(SendToKafkaException::class) -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        errorClassType = ServiceErrorClassType.KAFKA)
                exception.hasCause(SendToRabbitException::class) -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        errorClassType = ServiceErrorClassType.RABBITMQ)
                exception.hasCause(ExternalServiceException::class) -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        errorClassType = ServiceErrorClassType.EXTERNAL_SERVICE,
                        errorMessage = exception.message)
                exception.hasCause(PersistenceException::class) -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        errorClassType = ServiceErrorClassType.DATABASE)
                exception.hasCause(LockingRecordException::class) -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.LOCKED,
                        errorClassType = ServiceErrorClassType.LOCKED_RECORD,
                        errorMessage = exception.message)
                exception.hasCause(AuthorizationException::class) -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.UNAUTHORIZED,
                        errorClassType = ServiceErrorClassType.VALIDATION,
                        errorMessage = exception.message
                )
                exception.hasCause(AuthorizationPermissionException::class) -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.FORBIDDEN,
                        errorClassType = ServiceErrorClassType.AUTHORIZATION_PERMISSION,
                        errorMessage = exception.message
                )
                exception.hasCause(AccessDeniedException::class) -> ErrorInfoDto(
                    httpResponseStatus = HttpResponseStatus.UNAUTHORIZED,
                    errorClassType = ServiceErrorClassType.AUTHORIZATION_PERMISSION,
                    errorMessage = exception.message
                )
                else -> ErrorInfoDto(
                        httpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        errorClassType = ServiceErrorClassType.UNKNOWN)
            }
        }

        private fun Throwable.hasCause(clazz: KClass<*>): Boolean {
            return listOf(this, this.rootCause).any { clazz.isInstance(it) }
        }
    }

    protected abstract fun getService(): Services

    protected fun getErrorResponseSpecific(exception: Exception, errorMessage: String? = null): ResponseModel<ErrorObject> {

        val errorInfo = getErrorInfo(exception)
        val message = errorMessage
                ?: errorInfo.errorMessage
                ?: errorInfo.httpResponseStatus.reasonPhrase()
        val errorObject = getErrorObject(
                code = "${getService().code}-${errorInfo.errorClassType.code}",
                type = getService().name,
                message = message)

        return getErrorResponseSpecificObject(statusCode = errorInfo.httpResponseStatus.code(), errorMessage = message, errorObject = errorObject)
    }

    private fun getErrorResponseSpecificObject(statusCode: Int = HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), errorMessage: String?, errorObject: ErrorObject? = null):
            ResponseModel<ErrorObject> {

        return ResponseModel(
                statusCode = statusCode,
                message = errorMessage,
                error = errorObject
        )
    }

    /**
     * Method for create ErrorObject from [code], [type] and [message]
     *
     * @param code: Our system error code
     * @param type: Error class
     * @param message: full error message
     */
    private fun getErrorObject(code: String, type: String, message: String): ErrorObject {
        return ErrorObject(
                code = code,
                type = type,
                message = message
        )
    }

    private fun isAnnotatedResponseModel(annotatedElement: AnnotatedElement): Boolean {
        return annotatedElement.getAnnotation(th.co.dv.p2p.common.annotations.ResponseModel::class.java) != null
    }

    private fun isAnnotatedPageModel(annotatedElement: AnnotatedElement): Boolean {
        return annotatedElement.getAnnotation(th.co.dv.p2p.common.annotations.PageModel::class.java) != null
    }

    /**
     * Method for generate response data in Model
     */
    protected fun responseModel(
            body: Any?,
            annotatedElement: AnnotatedElement,
            selectedFields: List<String> = emptyList()): Any? {
        return if (isAnnotatedResponseModel(annotatedElement) ) {
            ResponseModel(
                    statusCode = HttpResponseStatus.OK.code(),
                    message = Status.SUCCESS.value,
                    data = pageModel(body, annotatedElement, selectedFields))
        } else body?.let { selectField(selectedFields, it, it::class.java) }
    }

    /**
     * Method for generate response data in Page model
     */
    private fun pageModel(
            body: Any?,
            annotatedElement: AnnotatedElement,
            selectedFields: List<String>): Any? {
        return when  {
            isAnnotatedPageModel(annotatedElement).not() -> body?.let { selectField(selectedFields, it, it::class.java) }
            body is PagableList<*> -> PageModel(
                    pageSize = body.getPageSize(),
                    page = body.getPage(),
                    totalRecordsAvailable = body.getTotalSize().toLong(),
                    rows =  body.getData().map{ it?.let{ selectField(selectedFields, it, it::class.java) } }
            )
            body is List<*> -> PageModel(
                    pageSize =  body.size,
                    page = 1,
                    totalRecordsAvailable = body.size.toLong(),
                    rows = body.map{ it?.let{ selectField(selectedFields, it, it::class.java) } }
            )
            else -> body?.let { selectField(selectedFields, it, it::class.java) }
        }
    }

    /**
     * This method will return data that only has data in [fields]
     *
     * @param fields: list of field name that we want to return, we will return everything if this list is empty
     * @param originalData: input data
     * @param originalDataClass: class of input data
     *
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> selectField(fields: List<String>, originalData: T, originalDataClass: Class<*>): T {
        // when fields is empty, we will send all data
        if (fields.isEmpty()) return originalData

        // get only fields of this object
        // example: fields = [linearId, items.unitPrice, items.quantity, siteName]
        // this function will resolved to [linearId, items, siteName]
        val thisFields = fields.map {
            it.split(DOT).first()
        }.distinct()

        // create new instance of originalData
        val newObj = originalDataClass.getDeclaredConstructor().newInstance()

        // loop over select fields of this object
        thisFields.forEach {fieldName ->
            // get Field by select field name, skip if not found
            val targetField = originalDataClass.declaredFields.find{ it.name == fieldName } ?: return@forEach

            // set targetField accessible to true so we can get data directly from Field.get method
            targetField.isAccessible = true

            // get data of this field, skip if data is null
            var data = targetField.get(originalData) ?: return@forEach

            // get type of this Field
            var targetFieldType = targetField.type

            // get only fields of this field name
            // example:
            //   fields = [linearId, items.unitPrice, items.quantity, siteName]
            //   fieldName = items
            // this function will resolved to [unitPrice, quantity]
            val dataFields = fields.mapNotNull{
                if (it.indexOf(fieldName + DOT) == 0) it.replaceFirst(fieldName + DOT, "") else null
            }

            // if [dataFields] is empty we will add original data of this field
            // if [dataFields] is not empty will call [selectField] with this field data as input
            if (dataFields.isNotEmpty()) {

                // if data is List we will loop this data
                if (List::class.java.isAssignableFrom(targetFieldType)) {
                    val listType = targetField.genericType as ParameterizedType
                    targetFieldType = listType.actualTypeArguments[0] as Class<*>
                    data = (data as List<*>).map { selectField(dataFields, it, targetFieldType) }
                } else {
                    data = selectField(dataFields, data, targetFieldType)
                }
            }

            // set data to new instance
            newObj.setFieldValue(fieldName, data)

        }

        return newObj as T
    }
}