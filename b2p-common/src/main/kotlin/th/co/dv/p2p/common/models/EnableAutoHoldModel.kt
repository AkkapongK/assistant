package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class EnableAutoHoldModel (
        var id: Long? = null,
        var sponsor: String? = null,
        var vendorTaxId: String? = null,
        var holdRemark: String? = null,
        var unholdRemark: String? = null
)