package th.co.dv.p2p.common.models

import th.co.dv.p2p.corda.base.models.PaymentModel

/**
 * Model for generate withholding tax certificate
 */
data class GenerateWhtCertificateModel(
    val payment: PaymentModel? = null,
    val additionalData: AdditionalWhtCertInformationModel? = null
)
