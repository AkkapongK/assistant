package th.co.dv.p2p.common.models

import th.co.dv.p2p.common.utilities.RabbitMQUtility.formatLegalName
import th.co.dv.p2p.corda.base.models.PartyModel
import java.util.*

data class BuyerModel (
        var taxId: String? = null,

        var code: String? = null,

        var sponsor: String? = null,

        var legalName: String? = null,

        var payableBankAccountNo: String? = null,

        var payableBankCode: String? = null,

        var payableBankBranchCode: String? = null,

        var paymentCalendar: String? = null,

        var cmsCalendar: String? = null,

        var whtSignatory: String? = null,

        var url: String? = null,

        var createdDate: Date? = null,

        var updatedDate: Date? = null,

        var company: CompanyModel? = null
) {
    fun getPartyModel(): PartyModel? {
        return (this.legalName ?: this.company?.legalName)?.let {
            PartyModel(legalName = formatLegalName(it))
        }
    }
}