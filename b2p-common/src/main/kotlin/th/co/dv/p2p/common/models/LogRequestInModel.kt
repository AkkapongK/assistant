package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import th.co.dv.p2p.common.annotations.NoArgConstructor

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgConstructor
data class LogRequestInModel(
    val url: String,
    val method: String,
    val headers: String,
    val queryString: String? = null,
    val body:String? = null,
    val request_timestamp: String
)
