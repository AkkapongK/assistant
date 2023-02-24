package th.co.dv.p2p.common.utilities

import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import org.junit.Test
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.models.ContractModel
import th.co.dv.p2p.corda.base.models.*
import kotlin.test.assertNull

class RedisUtilsTest {

    @Test
    fun testCompletePrefixKey() {
        val redis = spyk<RedisUtils>()
        val entity = InvoiceModel()
        val getKey = { _: InvoiceModel -> "MIDDLE-KEY" }

        val result = redis.completePrefixKey(Services.INVOICE.name, entity, getKey)
        assertEquals("INVOICE:MIDDLE-KEY:", result)
    }

    @Test
    fun testCompleteKey() {
        val redis = spyk<RedisUtils>()
        val entity = InvoiceModel()
        val getKey = { _: InvoiceModel -> "MIDDLE-KEY" }

        val result = redis.completeKey(Services.INVOICE.name, entity, getKey, "TX001")
        assertEquals("INVOICE:MIDDLE-KEY:TX001", result)
    }

    @Test
    fun testGetPurchaseHeaderKeyDefault() {
        val redis = spyk<RedisUtils>()
        val purchaseOrder = PurchaseOrderModel(purchaseOrderNumber = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", businessPlaceTaxNumber = "12500199")
        var result = redis.getPurchaseHeaderKeyDefault(purchaseOrder)
        assertEquals("PurchaseOrder:12500199|21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getPurchaseHeaderKeyDefault(purchaseOrder.copy(purchaseOrderNumber = null))
        assertNull(result)
    }

    @Test
    fun testGetPurchaseItemKeyDefault() {
        val redis = spyk<RedisUtils>()
        val purchaseItem = PurchaseItemModel(poItemNo = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", purchaseOrderLinearId = "po-linear-01")
        var result = redis.getPurchaseItemKeyDefault(purchaseItem)
        assertEquals("PurchaseItem:po-linear-01|21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getPurchaseItemKeyDefault(purchaseItem.copy(poItemNo = null))
        assertNull(result)
    }

    @Test
    fun testGetGoodsReceivedHeaderKeyDefault() {
        val redis = spyk<RedisUtils>()
        val goodsReceived = GoodsReceivedModel(externalId = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", vendorTaxNumber = "1000010", companyTaxNumber = "12500199")
        var result = redis.getGoodsReceivedHeaderKeyDefault(goodsReceived)
        assertEquals("GoodsReceived:12500199:1000010:21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getGoodsReceivedHeaderKeyDefault(goodsReceived.copy(externalId = null))
        assertNull(result)
    }

    @Test
    fun testGetDefaultItemKey() {
        val redis = spyk<RedisUtils>()
        val goodsReceivedItem = GoodsReceivedItemModel(externalId = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", goodsReceivedLinearId = "gr-01")
        var result = redis.getGoodsReceivedItemKeyDefault(goodsReceivedItem)
        assertEquals("GoodsReceivedItem:gr-01:21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getGoodsReceivedItemKeyDefault(goodsReceivedItem.copy(externalId = null))
        assertNull(result)
    }

    @Test
    fun testGetInvoiceHeaderKeyDefault() {
        val date = DateUtility.convertStringToDate("2022-03-17 00:00:00", DateUtility.WS_DATE_TIME_FORMAT)?.toInstant()?.stringify()
        val redis = spyk<RedisUtils>()
        val invoice = InvoiceModel(externalId = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", vendorTaxNumber = "1000010", companyTaxNumber = "12500199", invoiceDate = date)
        var result = redis.getInvoiceHeaderKeyDefault(invoice)
        assertEquals("Invoice:12500199|1000010|21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996|2022", result)

        // Case some key is null
        result = redis.getInvoiceHeaderKeyDefault(invoice.copy(externalId = null))
        assertNull(result)
    }

    @Test
    fun testGetInvoiceItemKeyDefault() {
        val redis = spyk<RedisUtils>()
        val invoiceItem = InvoiceItemModel(externalId = "!@#\$%^&*()_+=- ~+๑๒๔ู฿๖", invoiceLinearId = "invoice-linear-01")
        var result = redis.getInvoiceItemKeyDefault(invoiceItem)
        assertEquals("InvoiceItem:invoice-linear-01|21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getInvoiceItemKeyDefault(invoiceItem.copy(externalId = null))
        assertNull(result)
    }

    @Test
    fun testGetCreditNoteHeaderKeyDefault() {
        val redis = spyk<RedisUtils>()
        val creditNote = CreditNoteModel(externalId = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", vendorTaxNumber = "1000010", companyTaxNumber = "12500199")
        var result = redis.getCreditNoteHeaderKeyDefault(creditNote)
        assertEquals("CreditNote:12500199:1000010:21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getCreditNoteHeaderKeyDefault(creditNote.copy(externalId = null))
        assertNull(result)
    }

    @Test
    fun testGetCreditNoteItemKeyDefault() {
        val redis = spyk<RedisUtils>()
        val creditNoteItem = CreditNoteItemModel(externalId = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", creditNoteLinearId = "cn-01")
        var result = redis.getCreditNoteItemKeyDefault(creditNoteItem)
        assertEquals("CreditNoteItem:cn-01:21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getCreditNoteItemKeyDefault(creditNoteItem.copy(externalId = null))
        assertNull(result)
    }

    @Test
    fun testGetDebitNoteHeaderKeyDefault() {
        val redis = spyk<RedisUtils>()
        val debitNote = DebitNoteModel(externalId = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", vendorTaxNumber = "1000010", companyTaxNumber = "12500199")
        var result = redis.getDebitNoteHeaderKeyDefault(debitNote)
        assertEquals("DebitNote:12500199:1000010:21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getDebitNoteHeaderKeyDefault(debitNote.copy(externalId = null))
        assertNull(result)
    }

    @Test
    fun testGetDebitNoteItemKeyDefault() {
        val redis = spyk<RedisUtils>()
        val debitNoteItem = DebitNoteItemModel(externalId = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", debitNoteLinearId = "dn-01")
        var result = redis.getDebitNoteItemKeyDefault(debitNoteItem)
        assertEquals("DebitNoteItem:dn-01:21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getDebitNoteItemKeyDefault(debitNoteItem.copy(externalId = null))
        assertNull(result)
    }

    @Test
    fun testGetContractKeyDefault() {
        val redis = spyk<RedisUtils>()
        val contract = ContractModel(contractNumber = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", sponsor = "DV")
        var result = redis.getContractKeyDefault(contract)
        assertEquals("Contract:DV|21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getContractKeyDefault(contract.copy(contractNumber = null))
        assertNull(result)
    }


    @Test
    fun testGetPaymentKeyDefault() {
        val redis = spyk<RedisUtils>()
        val payment = PaymentModel(paymentNumber = "!@#$%^&*()_+=- ~+๑๒๔ู฿๖", vendorTaxNumber = "1000010", companyTaxNumber = "12500199")
        var result = redis.getPaymentHeaderKeyDefault(payment)
        assertEquals("Payment:12500199:1000010:21402324255e262a28295f2b3d2d207e2be0b991e0b992e0b994e0b8b9e0b8bfe0b996", result)

        // Case some key is null
        result = redis.getPaymentHeaderKeyDefault(payment.copy(paymentNumber = null))
        assertNull(result)
    }

    @Test
    fun testGetTaxDocumentKeyDefault() {
        val redis = spyk<RedisUtils>()
        val date = DateUtility.convertStringToDate("2022-04-26 00:00:00", DateUtility.WS_DATE_TIME_FORMAT)?.toInstant()?.stringify()
        val taxDocument = TaxDocumentModel(companyTaxNumber = "CTN111", vendorTaxNumber = "VTN222", documentNumber = "DOC333", documentDate = date, vendorBranchCode = "VBC444")
        var result = redis.getTaxDocumentKeyDefault(taxDocument)
        assertEquals("TaxDocument:CTN111:VTN222:2022:VBC444:444f43333333", result)

        // Case some key is null
        result = redis.getTaxDocumentKeyDefault(taxDocument.copy(companyTaxNumber = null))
        assertNull(result)
    }

    @Test
    fun testGetRequestDefaultHeaderKey() {
        val requestUtility = spyk<RedisUtils>()
        var request = RequestModel(externalId = "!@#$%^&*()!@#1234567890+=", vendorTaxNumber = "1000010", companyTaxNumber = "12500199")
        var result = requestUtility.getRequestHeaderKeyDefault(request)
        assertEquals("Request:12500199:1000010:21402324255e262a2829214023313233343536373839302b3d", result)

        // Case some key is null
        result = requestUtility.getRequestHeaderKeyDefault(request.copy(companyTaxNumber = null))
        assertNull(result)

        request = RequestModel(externalId = "!@#$%^&*()!@#1234567890+=", vendorTaxNumber = "1000010", companyTaxNumber = "12500199")
        result = requestUtility.getRequestHeaderKeyDefault(request)
        assertEquals("Request:12500199:1000010:21402324255e262a2829214023313233343536373839302b3d", result)
    }

    @Test
    fun testGetRequestItemDefaultItemKey() {
        val requestUtility = spyk<RedisUtils>()
        var requestItem = RequestItemModel(externalId = "!@#$%^&*()!@#1234567890+=", requestLinearId = "RQ-01")
        var result = requestUtility.getRequestItemKeyDefault(requestItem)
        assertEquals("RequestItem:RQ-01:21402324255e262a2829214023313233343536373839302b3d", result)

        // Case some key is null
        result = requestUtility.getRequestItemKeyDefault(requestItem.copy(externalId = null))
        assertNull(result)

        requestItem = RequestItemModel(externalId = "!@#$%^&*()!@#1234567890+=", requestLinearId = "RQ-01")
        result = requestUtility.getRequestItemKeyDefault(requestItem)
        assertEquals("RequestItem:RQ-01:21402324255e262a2829214023313233343536373839302b3d", result)
    }

    @Test
    fun testGetFinanceableDocumentKeyDefault() {
        val redis = spyk<RedisUtils>()
        val financeableDocumentModel = FinanceableDocumentModel(documentType = "PO", vendorTaxNumber = "VTAX-01", buyerTaxId = "BTAX-01", externalId = "001")
        var result = redis.getFinanceableDocumentKeyDefault(financeableDocumentModel)
        assertEquals("FinanceableDocument:PO:VTAX-01:BTAX-01:303031", result)

        // Case some key is null
        result = redis.getFinanceableDocumentKeyDefault(financeableDocumentModel.copy(documentType = null))
        assertNull(result)
    }

    @Test
    fun testGetRepaymentRequestKeyDefault() {
        val redis = spyk<RedisUtils>()
        val repaymentRequestModel = RepaymentRequestModel(repaymentRequestId = "001")
        var result = redis.getRepaymentRequestKeyDefault(repaymentRequestModel)
        assertEquals("RepaymentRequest:303031", result)

        // Case repaymentRequestId is null
        result = redis.getRepaymentRequestKeyDefault(repaymentRequestModel.copy(repaymentRequestId = null))
        assertNull(result)
    }

    @Test
    fun testGetRepaymentHistoryKeyDefault() {
        val redis = spyk<RedisUtils>()
        val repaymentHistoryModel = RepaymentHistoryModel(repaymentResultId = "001")
        var result = redis.getRepaymentHistoryKeyDefault(repaymentHistoryModel)
        assertEquals("RepaymentHistory:303031", result)

        // Case repaymentResultId is null
        result = redis.getRepaymentHistoryKeyDefault(repaymentHistoryModel.copy(repaymentResultId = null))
        assertNull(result)
    }

    @Test
    fun testGetLoanKeyDefault() {
        val redis = spyk<RedisUtils>()
        val loanModel = LoanModel(externalId = "001")
        var result = redis.getLoanKeyDefault(loanModel)
        assertEquals("Loan:303031", result)

        // Case externalId is null
        result = redis.getLoanKeyDefault(loanModel.copy(externalId = null))
        assertNull(result)
    }

    @Test
    fun testGetLoanProfileKeyDefault() {
        val redis = spyk<RedisUtils>()
        val loanProfileModel = LoanProfileModel(lenderCode = "LC", lenderFinancingProduct = "LFP", borrowerTaxId = "001")
        var result = redis.getLoanProfileKeyDefault(loanProfileModel)
        assertEquals("LoanProfile:LC:LFP:303031", result)

        // Case some key is null
        result = redis.getLoanProfileKeyDefault(loanProfileModel.copy(borrowerTaxId = null))
        assertNull(result)
    }

}