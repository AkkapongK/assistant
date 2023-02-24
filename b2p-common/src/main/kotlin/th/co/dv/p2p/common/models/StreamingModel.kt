package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import th.co.dv.p2p.common.base.enums.MessageType
import th.co.dv.p2p.common.annotations.NoArgConstructor
import th.co.dv.p2p.common.enums.Services

/**
 * This class is use to transfer any data from corda to kafka (producer) and kafka to external system (consumer).
 * About property will explain as below
 * [id]: Unique key of data, now we put linearId.
 * [command]: Command name that use in corda transaction. eg: Issue, Approve, ..etc
 * [data]: State models from corda transaction
 * [type]: Type of state. eg: Invoice, CreditNote, Payment, ..etc
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgConstructor
data class StreamingModel<T>(
    var sponsor: String? = null,
    var id: String,
    var command: String,
    var data: T? = null,
    var type: String,
    val messageType: MessageType? = null,
    val message: String? = null,
    val redisKey: String? = null,
    val relatedServices: List<Services> = emptyList()
) {
    companion object {
        const val PURCHASE_ORDER = "PurchaseOrder"
        const val PURCHASE_ITEM = "PurchaseItem"
        const val GOODS_RECEIVED = "GoodsReceived"
        const val GOODS_RECEIVED_ITEM = "GoodsReceivedItem"
        const val INVOICE = "Invoice"
        const val INVOICE_ITEM = "InvoiceItem"
        const val DEBIT_NOTE = "DebitNote"
        const val DEBIT_NOTE_ITEM = "DebitNoteItem"
        const val CREDIT_NOTE = "CreditNote"
        const val CREDIT_NOTE_ITEM = "CreditNoteItem"
        const val PAYMENT = "Payment"
        const val REQUEST = "Request"
        const val REQUEST_ITEM = "RequestItem"
        const val TAX_DOCUMENT = "TaxDocument"
        const val CONTRACT = "Contract"
    }
}