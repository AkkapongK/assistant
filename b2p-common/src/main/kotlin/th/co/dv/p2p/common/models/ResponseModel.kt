package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.netty.handler.codec.http.HttpResponseStatus
import th.co.dv.p2p.common.enums.ServiceErrorClassType

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseModel<T>(
        val statusCode: Int? = null,
        val message: String? = null,
        val error: ErrorObject? = null,
        val transactionId: String? = null,
        val data: T? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorObject(
        val code: String? = null,
        val type: String? = null,
        val message: String? = null,
        val rootCause: String? = null)

data class ErrorInfoDto(
        val httpResponseStatus: HttpResponseStatus,
        val errorClassType: ServiceErrorClassType,
        val errorMessage: String? = null
)

class IllegalDataException(msg: String) :IllegalArgumentException(msg)