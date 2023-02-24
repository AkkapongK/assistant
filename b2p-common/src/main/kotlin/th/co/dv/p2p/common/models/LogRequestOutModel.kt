package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import th.co.dv.p2p.common.annotations.NoArgConstructor

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgConstructor
data class LogRequestOutModel(
        val status: String? = null,
        val message: List<String>? = emptyList(),
        val response_timestamp: String
)