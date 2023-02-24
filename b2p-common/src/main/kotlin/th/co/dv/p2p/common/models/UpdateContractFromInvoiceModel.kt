package th.co.dv.p2p.common.models

import th.co.dv.p2p.corda.base.models.InvoiceModel

/**
 * This class is use to transfer invoice data to update contract on po-service
 *
 * @param nextInvoiceModel : invoice after update data
 * @param previousInvoiceModel : invoice before update data
 */
data class UpdateContractFromInvoiceModel(
    val nextInvoiceModel: InvoiceModel? = null,
    val previousInvoiceModel: InvoiceModel? = null
)