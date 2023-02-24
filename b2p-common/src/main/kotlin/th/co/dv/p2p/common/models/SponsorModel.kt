package th.co.dv.p2p.common.models

import java.util.*

data class SponsorModel (
    var id: Long? = null,
    var sponsor: String? = null,
    var sponsorName: String? = null,
    var sponsorDescription: String? = null,
    var sponsorLogoImageUrl: String? = null,
    var sponsorCoverImage: String? = null,
    var supplierHomeUrl: String? = null,
    var sponsorFlagRd: Boolean = false,
    var invoiceFinancingFormUrl: String? = null,
    var isActive: Boolean = false,
    var createdBy: String? = null,
    var createdDate: Date? = null,
    var updatedBy: String? = null,
    var updatedDate: Date? = null,
    var sponsorOrder: Long? = null,
    var b2cFlag: Boolean? = null,
    var supplierManualUrl: String? = null,
    var scbFeeChargeTo: String? = null,
    var nonScbFeeChargeTo: String? = null
)
