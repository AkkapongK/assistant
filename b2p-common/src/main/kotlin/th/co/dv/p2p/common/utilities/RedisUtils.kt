package th.co.dv.p2p.common.utilities

import th.co.dv.p2p.common.constants.COLON
import th.co.dv.p2p.common.constants.PIPE_LINE
import th.co.dv.p2p.common.constants.SPACE
import th.co.dv.p2p.common.models.ContractModel
import th.co.dv.p2p.common.utilities.StringUtility.Companion.toHex
import th.co.dv.p2p.corda.base.models.*

object RedisUtils {

    private const val MODEL = "Model"

    /**
     * Method for complete prefix key that used to store in Redis
     * the prefix is [Service name]:[The key that make state unique (can difference in each flow)]:
     *
     * @param serviceName: Name of service
     * @param entity: Entity model that use for get unique state key
     * @param getKey: Function to generate key (return in String)
     */
    fun <T> completePrefixKey(serviceName: String, entity: T, getKey: (T) -> String): String {
        return serviceName + COLON + getKey(entity) + COLON
    }

    fun <T> completeKey(serviceName: String, entity: T, getKey: (T) -> String, transactionId: String): String {
        return completePrefixKey(serviceName, entity, getKey) + transactionId
    }

    /** PURCHASE **/
    val getPurchaseHeaderKeyDefault = { purchaseOrder: PurchaseOrderModel ->
        val uniqueForState = buildUniquePurchaseOrderKey(purchaseOrder.businessPlaceTaxNumber, purchaseOrder.purchaseOrderNumber)
        uniqueForState?.let { modifyClassName(PurchaseOrderModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniquePurchaseOrderKey(businessPlaceTaxNumber: String?, purchaseOrderNumber: String?): String? {
        if (containsNull(listOf(businessPlaceTaxNumber, purchaseOrderNumber))) return null
        return "$businessPlaceTaxNumber$PIPE_LINE${purchaseOrderNumber.toHex()}"
    }

    val getPurchaseItemKeyDefault = { purchaseItem: PurchaseItemModel ->
        val uniqueForState = buildUniquePurchaseItemKey(purchaseItem.purchaseOrderLinearId, purchaseItem.poItemNo)
        uniqueForState?.let { modifyClassName(PurchaseItemModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniquePurchaseItemKey(purchaseOrderLinearId: String?, poItemNo: String?): String? {
        if (containsNull(listOf(purchaseOrderLinearId, poItemNo))) return null
        return "$purchaseOrderLinearId$PIPE_LINE${poItemNo.toHex()}"
    }

    /** GOODS **/
    val getGoodsReceivedHeaderKeyDefault = { goodsReceived: GoodsReceivedModel ->
        val uniqueForState = buildUniqueGoodsReceivedKey(goodsReceived.companyTaxNumber, goodsReceived.vendorTaxNumber, goodsReceived.externalId)
        uniqueForState?.let { modifyClassName(GoodsReceivedModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueGoodsReceivedKey(companyTaxNumber: String?, vendorTaxNumber: String?, externalId: String?): String? {
        if (containsNull(listOf(companyTaxNumber, vendorTaxNumber, externalId))) return null
        return "$companyTaxNumber$COLON$vendorTaxNumber$COLON${externalId.toHex()}"
    }

    val getGoodsReceivedItemKeyDefault = { goodsReceivedItem: GoodsReceivedItemModel ->
        val uniqueForState = buildUniqueGoodsReceivedItemKey(goodsReceivedItem.goodsReceivedLinearId, goodsReceivedItem.externalId)
        uniqueForState?.let { modifyClassName(GoodsReceivedItemModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueGoodsReceivedItemKey(goodsReceivedLinearId: String?, externalId: String?): String? {
        if (containsNull(listOf(goodsReceivedLinearId, externalId))) return null
        return "$goodsReceivedLinearId$COLON${externalId.toHex()}"
    }

    /** INVOICE **/
    val getInvoiceHeaderKeyDefault = { invoice: InvoiceModel ->
        val uniqueForState = buildUniqueInvoiceKey(invoice.companyTaxNumber, invoice.vendorTaxNumber, invoice.externalId, invoice.invoiceDate)
        uniqueForState?.let { modifyClassName(InvoiceModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueInvoiceKey(companyTaxNumber: String?, vendorTaxNumber: String?, externalId: String?, invoiceDate: String?): String? {
        val invoiceYear = getYearFromDate(invoiceDate)
        if (containsNull(listOf(companyTaxNumber, vendorTaxNumber, externalId, invoiceYear))) return null
        return "$companyTaxNumber$PIPE_LINE$vendorTaxNumber$PIPE_LINE${externalId.toHex()}$PIPE_LINE$invoiceYear"
    }

    val getInvoiceItemKeyDefault = { invoiceItem: InvoiceItemModel ->
        val uniqueForState = buildUniqueInvoiceItemKey(invoiceItem.invoiceLinearId, invoiceItem.externalId)
        uniqueForState?.let { modifyClassName(InvoiceItemModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueInvoiceItemKey(invoiceLinearId: String?, externalId: String?): String? {
        if (containsNull(listOf(invoiceLinearId, externalId))) return null
        return "$invoiceLinearId$PIPE_LINE${externalId.toHex()}"
    }

    /** CREDIT NOTE **/
    val getCreditNoteHeaderKeyDefault = { creditNote: CreditNoteModel ->
        val uniqueForState = buildUniqueCreditNoteKey(creditNote.companyTaxNumber, creditNote.vendorTaxNumber, creditNote.externalId)
        uniqueForState?.let { modifyClassName(CreditNoteModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueCreditNoteKey(companyTaxNumber: String?, vendorTaxNumber: String?, externalId: String?): String? {
        if (containsNull(listOf(companyTaxNumber, vendorTaxNumber, externalId))) return null
        return "$companyTaxNumber$COLON$vendorTaxNumber$COLON${externalId.toHex()}"
    }

    val getCreditNoteItemKeyDefault = { creditNoteItem: CreditNoteItemModel ->
        val uniqueForState = buildUniqueCreditNoteItemKey(creditNoteItem.creditNoteLinearId, creditNoteItem.externalId)
        uniqueForState?.let { modifyClassName(CreditNoteItemModel::class.java.simpleName) + COLON + uniqueForState }
    }

    fun buildUniqueCreditNoteItemKey(creditNoteLinearId: String?, externalId: String?): String? {
        if (containsNull(listOf(creditNoteLinearId, externalId))) return null
        return "$creditNoteLinearId$COLON${externalId.toHex()}"
    }

    /** DEBIT NOTE **/
    val getDebitNoteHeaderKeyDefault = { debitNote: DebitNoteModel ->
        val uniqueForState = buildUniqueDebitNoteKey(debitNote.companyTaxNumber, debitNote.vendorTaxNumber, debitNote.externalId)
        uniqueForState?.let { modifyClassName(DebitNoteModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueDebitNoteKey(companyTaxNumber: String?, vendorTaxNumber: String?, externalId: String?): String? {
        if (containsNull(listOf(companyTaxNumber, vendorTaxNumber, externalId))) return null
        return "$companyTaxNumber$COLON$vendorTaxNumber$COLON${externalId.toHex()}"
    }

    val getDebitNoteItemKeyDefault = { debitNoteItem: DebitNoteItemModel ->
        val uniqueForState = buildUniqueDebitNoteItemKey(debitNoteItem.debitNoteLinearId, debitNoteItem.externalId)
        uniqueForState?.let { modifyClassName(DebitNoteItemModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueDebitNoteItemKey(debitNoteLinearId: String?,externalId: String?): String? {
        if (containsNull(listOf(debitNoteLinearId, externalId))) return null
        return "$debitNoteLinearId$COLON${externalId.toHex()}"
    }

    /** REQUEST **/
    val getRequestHeaderKeyDefault = { request: RequestModel ->
        val uniqueForState = buildUniqueRequestKey(request.companyTaxNumber, request.vendorTaxNumber, request.externalId)
        uniqueForState?.let { modifyClassName(RequestModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueRequestKey(companyTaxNumber: String?, vendorTaxNumber: String?, externalId: String?): String? {
        if (containsNull(listOf(companyTaxNumber, vendorTaxNumber, externalId))) return null
        return "$companyTaxNumber$COLON$vendorTaxNumber$COLON${externalId.toHex()}"
    }

    val getRequestItemKeyDefault = { requestItem: RequestItemModel ->
        val uniqueForState = buildUniqueRequestItemKey(requestItem.requestLinearId, requestItem.externalId)
        uniqueForState?.let { modifyClassName(RequestItemModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueRequestItemKey(requestLinearId: String?, externalId: String?): String? {
        if (containsNull(listOf(requestLinearId, externalId))) return null
        return "$requestLinearId$COLON${externalId.toHex()}"
    }

    /** CONTRACT **/
    val getContractKeyDefault = { contract: ContractModel ->
        val uniqueForState = buildUniqueContractKey(contract.sponsor, contract.contractNumber)
        uniqueForState?.let { modifyClassName(ContractModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueContractKey(sponsor: String?,contractNumber: String?): String? {
        if (containsNull(listOf(sponsor, contractNumber))) return null
        return "$sponsor$PIPE_LINE${contractNumber.toHex()}"
    }

    /** PAYMENT **/
    val getPaymentHeaderKeyDefault = { payment: PaymentModel ->
        val uniqueForState = buildUniquePaymentKey(payment.companyTaxNumber, payment.vendorTaxNumber, payment.paymentNumber)
        uniqueForState?.let { modifyClassName(PaymentModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniquePaymentKey(companyTaxNumber: String?, vendorTaxNumber: String?, paymentNumber: String?): String? {
        if (containsNull(listOf(companyTaxNumber, vendorTaxNumber, paymentNumber))) return null
        return "$companyTaxNumber$COLON$vendorTaxNumber$COLON${paymentNumber.toHex()}"
    }

    /** TAX DOCUMENT **/
    val getTaxDocumentKeyDefault = { taxDocument: TaxDocumentModel ->
        val uniqueForState = buildUniqueTaxDocumentKey(taxDocument.companyTaxNumber, taxDocument.vendorTaxNumber, taxDocument.documentNumber, taxDocument.documentDate, taxDocument.vendorBranchCode)
        uniqueForState?.let { modifyClassName(TaxDocumentModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueTaxDocumentKey(companyTaxNumber: String?, vendorTaxNumber: String?, documentNumber: String?, documentDate: String?, vendorBranchCode: String?): String? {
        val taxDocumentYear = getYearFromDate(documentDate)
        if (containsNull(listOf(companyTaxNumber, vendorTaxNumber, documentNumber, taxDocumentYear, vendorBranchCode))) return null
        return "$companyTaxNumber$COLON$vendorTaxNumber$COLON$taxDocumentYear$COLON$vendorBranchCode$COLON${documentNumber.toHex()}"
    }

    /** FINANCING **/
    val getFinanceableDocumentKeyDefault = { financeableDocumentModel: FinanceableDocumentModel ->
        val uniqueForState = buildUniqueFinanceableDocumentKey(
            financeableDocumentModel.documentType,
            financeableDocumentModel.vendorTaxNumber,
            financeableDocumentModel.buyerTaxId,
            financeableDocumentModel.externalId
        )
        uniqueForState?.let { modifyClassName(FinanceableDocumentModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueFinanceableDocumentKey(documentType: String?, vendorTaxNumber: String?, buyerTaxId: String?, externalId: String?): String? {
        if (containsNull(listOf(documentType, vendorTaxNumber, buyerTaxId, externalId))) return null
        return "$documentType$COLON$vendorTaxNumber$COLON$buyerTaxId$COLON${externalId.toHex()}"
    }

    val getRepaymentRequestKeyDefault = { repaymentRequestModel: RepaymentRequestModel ->
        val uniqueForState = repaymentRequestModel.repaymentRequestId?.toHex()
        uniqueForState?.let { modifyClassName(RepaymentRequestModel::class.java.simpleName) + COLON + it }
    }

    val getRepaymentHistoryKeyDefault = { repaymentHistory: RepaymentHistoryModel ->
        val uniqueForState = repaymentHistory.repaymentResultId?.toHex()
        uniqueForState?.let { modifyClassName(RepaymentHistoryModel::class.java.simpleName) + COLON + it }
    }

    val getLoanKeyDefault = { loanModel: LoanModel ->
        val uniqueForState = loanModel.externalId?.toHex()
        uniqueForState?.let { modifyClassName(LoanModel::class.java.simpleName) + COLON + it }
    }

    val getLoanProfileKeyDefault = { loanProfileModel: LoanProfileModel ->
        val uniqueForState = buildUniqueLoanProfileKey(
            loanProfileModel.borrowerTaxId,
            loanProfileModel.lenderCode,
            loanProfileModel.lenderFinancingProduct
        )
        uniqueForState?.let { modifyClassName(LoanProfileModel::class.java.simpleName) + COLON + it }
    }

    fun buildUniqueLoanProfileKey(borrowerTaxId: String?, lenderCode: String?, lenderFinancingProduct: String?): String? {
        if (containsNull(listOf(borrowerTaxId, lenderCode, lenderFinancingProduct))) return null
        return "$lenderCode$COLON$lenderFinancingProduct$COLON${borrowerTaxId.toHex()}"
    }

    private fun modifyClassName(simpleName: String): String {
        return simpleName.replace(MODEL, SPACE).trim()
    }

    private fun containsNull(values: List<String?>): Boolean {
        return values.any { it == null }
    }

}