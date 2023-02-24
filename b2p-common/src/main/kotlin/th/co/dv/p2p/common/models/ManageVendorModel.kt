package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ManageVendorModel (
    val vendorName: String? = null,
    val vendorTaxId: String? = null,
    val vendorCode: String? = null,
    val numberOfBuyer: Int? = null,
    val holdFlag: Int? = null,
    val inActiveFlag: Int? = null
)