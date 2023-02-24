package th.co.dv.p2p.common.models

data class ManageVendorSearchModel (
    val vendorName: String? = null,
    val vendorTaxId: String? = null,
    val vendorCode: String? = null,
    val isHold: Boolean? = null,
    val isActive: Boolean? = null,
    val page: Int = 1,
    val pageSize: Int = 10
)