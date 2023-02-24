package th.co.dv.p2p.common.kafka.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import th.co.dv.p2p.common.models.BuyerVendorModel
import th.co.dv.p2p.common.models.ContractModel
import th.co.dv.p2p.corda.base.models.*

/**
 * Data class to keep new state that processed and old state before process
 * that we will using to update related state in other service
 * and using for rollback case
 *
 * The model will stream to kafka for subscriber that need to use the data for update it self
 *
 * @property previousState : The existing state before process
 * @property nextState: The state after process
 * @property relatedServices: All services the related
 * @property priority: priority of state
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventStateModel(
        val previousState: AllStates? = null,
        val nextState: AllStates,
        val relatedServices: List<String>,
        val priority: Int = 1,
        val command: String? = null
)


/**
 * Data class that keep states datas
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AllStates(
        var invoices: List<InvoiceModel>? = null,
        var invoiceItems: List<InvoiceItemModel>? = null,
        var purchaseOrders: List<PurchaseOrderModel>? = null,
        var purchaseItems: List<PurchaseItemModel>? = null,
        var goodsReceiveds: List<GoodsReceivedModel>? = null,
        var goodsReceivedItems: List<GoodsReceivedItemModel>? = null,
        var creditNotes: List<CreditNoteModel>? = null,
        var creditNoteItems: List<CreditNoteItemModel>? = null,
        var debitNotes: List<DebitNoteModel>? = null,
        var debitNoteItems: List<DebitNoteItemModel>? = null,
        var requests: List<RequestModel>? = null,
        var requestItems: List<RequestItemModel>? = null,
        var payments: List<PaymentModel>? = null,
        var taxDocuments: List<TaxDocumentModel>? = null,
        var buyerVendors: List<BuyerVendorModel>? = null,
        var contracts: List<ContractModel>? = null,
        var financeableDocuments: List<FinanceableDocumentModel>? = null,
        var repaymentRequests: List<RepaymentRequestModel>? = null,
        var repaymentHistories: List<RepaymentHistoryModel>? = null,
        var loans: List<LoanModel>? = null,
        var loanProfiles: List<LoanProfileModel>? = null
)
