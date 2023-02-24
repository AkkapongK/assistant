package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ScfCodeMappingModel(
    val taxId: String? = null,
    val type: String? = null,
    val scfCode: String? = null,
    val corpId: String? = null
)
