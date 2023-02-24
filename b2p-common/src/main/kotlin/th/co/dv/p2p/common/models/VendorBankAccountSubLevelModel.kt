package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class VendorBankAccountSubLevelModel(
    var buyerTaxId: String? = null,
    var buyerCode: String? = null,
    var vendorTaxId: String? = null,
    var vendorCode: String? = null,
    var subKey: String? = null,
    var receivableBankAccountNo: String? = null,
    var receivableBankCode: String? = null,
    var receivableBankBranchCode: String? = null
)
