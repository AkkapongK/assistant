package th.co.dv.p2p.usernotify.utility

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.util.LinkedMultiValueMap
import th.co.dv.p2p.common.utilities.AuthorizationUtils.INTERFACE_AUTHORIZATION
import th.co.dv.p2p.common.utilities.SYSTEM_USERNAME
import th.co.dv.p2p.usernotify.models.UserAnswerModel
import th.co.dv.p2p.usernotify.utility.web.RequestUtility
import kotlin.reflect.full.declaredMemberProperties

object CommonUtility {

    /**
     * Method for complete parameter for send request to service
     *
     */
    fun Map<String, Any?>.completeRequestParameter(): LinkedMultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()

        this.forEach { (field, value) ->
            // skip if null
            if (value == null) return@forEach
            when (value) {
                is Collection<*> -> value.forEach { params.add(field, it.toString()) }
                else -> params.add(field, value.toString())
            }

        }
        return params
    }

    /**
     * Method for get username from user authorization in security context
     * and default to "system" when user is INTERFACE or have an error
     */
    fun getUsernameFromSecurityContext(): String {
        return try {
            RequestUtility.getUserAuthorization().username.let {
                when (it) {
                    INTERFACE_AUTHORIZATION.username -> SYSTEM_USERNAME
                    else -> it
                }
            }
        } catch (e: Exception) {
            SYSTEM_USERNAME
        }
    }

    /**
     * This function use for get value of any object via property name
     * @param instance: object that we need to get value
     * @param propertyName: property name of object
     * @return value
     */
    fun <R : Any?> readProperty(instance: Any, propertyName: String): String {
        val clazz = instance.javaClass.kotlin
        @Suppress("UNCHECKED_CAST")
        val propertyValue = clazz.declaredMemberProperties.first { it.name == propertyName }.get(instance)
        return when (propertyValue != null) {
            true -> propertyValue.toString()
            else -> ""
        }
    }

    fun String.toArgument() = this.split(" ").map { it.trim() }.toMutableList()
    fun List<String>.concatString() = this.joinToString(" ")

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    inline fun <reified T> mapperModel(listData: String, innerClass: Class<*>? = null): T {
        val mapper = jacksonObjectMapper()
        val model = mapper.readValue(listData, T::class.java)
        val isList = model is List<*>

        val finalModel = when (isList) {

            true -> (model as List<*>).map { mapper.readValue(mapper.writeValueAsString(it), innerClass) }
            false -> model

        } as T

        return finalModel
    }

    fun List<UserAnswerModel>.toObjectString(): String {
        val mapper = jacksonObjectMapper()
        return mapper.writeValueAsString(this)
    }



}