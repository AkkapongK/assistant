package th.co.dv.p2p.common.models

import th.co.dv.p2p.common.utilities.RabbitMQUtility.formatLegalName
import th.co.dv.p2p.corda.base.models.PartyModel
import java.util.*

data class SellerModel(
        var taxId: String? = null,

        var beneficiaryNotification: String? = null,

        var paymentAdviceEmail: String? = null,

        var legalName: String? = null,

        var url: String? = null,

        val createdDate: Date? = null,

        val updatedDate: Date? = null,

        var rdActiveStartDate: Date? = null,

        var rdActiveEndDate: Date? = null,

        var acceptMarketConduct: Boolean? = null,

        var acceptTermAndCon: Boolean? = null,

        var acceptPDPA: Boolean? = null,

        var company: CompanyModel? = null
) {

    fun getPartyModel(): PartyModel? {
        return (this.legalName ?: this.company?.legalName)?.let {
            PartyModel(legalName = formatLegalName(it))
        }
    }
}