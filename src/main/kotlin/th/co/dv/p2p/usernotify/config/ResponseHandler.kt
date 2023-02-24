package th.co.dv.p2p.usernotify.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.models.ResponseModel
import th.co.dv.p2p.common.utilities.BaseResponseHandler

@ControllerAdvice
class ResponseHandler : BaseResponseHandler(), ResponseBodyAdvice<Any> {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ResponseHandler::class.java)
        private val className = ResponseHandler::class.java.simpleName
    }

    override fun getService(): Services {
        return Services.USER_NOTIFY
    }

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleException(ex: Exception): Any {
        logger.warn("$className.handleException ", ex)
        return getErrorResponseSpecific(ex)
    }


    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return true
    }

    override fun beforeBodyWrite(
            body: Any?,
            returnType: MethodParameter,
            selectedContentType: MediaType,
            selectedConverterType: Class<out HttpMessageConverter<*>>,
            request: ServerHttpRequest,
            response: ServerHttpResponse): Any? {
        val httpResult = responseModel(body, returnType.annotatedElement)
        val httpStatus = getHttpStatusFromResponseModel(httpResult)
        response.setStatusCode(httpStatus)
        return httpResult
    }
    /**
     * method for get http status from response body
     * @param response api response body
     */
    @Suppress("UNCHECKED_CAST")
    private fun getHttpStatusFromResponseModel(response: Any?): HttpStatus {
        response ?: return HttpStatus.NO_CONTENT
        return try {
            when (response) {
                is ResponseModel<*> -> HttpStatus.valueOf(response.statusCode ?: 200)
                is Collection<*> -> {
                    val responses = (response as List<ResponseModel<*>>)
                    getHttpStatusFromResponseModel(responses.maxByOrNull { it.statusCode!! })
                }
                else -> HttpStatus.OK // in case body not ResponseModel return 200
            }
        } catch (e: Exception) {
            HttpStatus.OK
        }
    }
}