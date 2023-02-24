package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SellerInfoModel(
    val sponsor: String? = null,
    val vendorTaxId: String? = null,
    val vendorNameTH: String? = null,
    val vendorNameEN: String? = null,
    val vendorContactName: String? = null,
    val vendorContactPhone: String? = null,
    val vendorContactEmail: String? = null,
    val whtFormType: String? = null,
    val paymentAdviceEmail: String? = null,
    val branchInfo: List<BranchModel> = emptyList(),
    val userInfo: List<UserModel> = emptyList(),
    val vendorCodeInfo: List<VendorCodeInfoModel> = emptyList()
)
