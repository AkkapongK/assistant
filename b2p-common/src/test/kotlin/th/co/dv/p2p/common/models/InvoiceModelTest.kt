package th.co.dv.p2p.common.models

import io.mockk.spyk
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.corda.base.models.InvoiceModel
import kotlin.test.assertEquals

class InvoiceModelTest {

    @Test
    fun `test display`() {
        // case: invoiceModel lifecycle is null
        var invoiceModel = InvoiceModel()
        var isSeller = false
        var expectInvoiceModel = InvoiceModel()
        var result = invoiceModel.display(isSeller)
        assertEquals(expectInvoiceModel, result)

        // case: invoiceModel lifecycle is match with invoice status, buyer
        invoiceModel = InvoiceModel(lifecycle = "FINANCED")
        expectInvoiceModel = InvoiceModel(lifecycle = "FINANCED", status = "Waiting Payment Due Date", matchingStatus = "Waiting Payment Due Date")
        result = invoiceModel.display(isSeller)
        assertEquals(expectInvoiceModel, result)

        // case: invoiceModel lifecycle is match with invoice status, seller
        isSeller = true
        expectInvoiceModel = InvoiceModel(lifecycle = "FINANCED", status = "Financed", matchingStatus = "Waiting Payment Due Date")
        result = invoiceModel.display(isSeller)
        assertEquals(expectInvoiceModel, result)

        // case: invoiceModel lifecycle is match with many invoice status
        invoiceModel = InvoiceModel(lifecycle = "PENDING_SELLER")
        expectInvoiceModel = InvoiceModel(lifecycle = "PENDING_SELLER", status = "Request to Resubmit", matchingStatus = "Request Invoice Resubmission")
        result = invoiceModel.display(isSeller)
        assertEquals(expectInvoiceModel, result)

        invoiceModel = InvoiceModel(lifecycle = "PENDING_SELLER", rdSubmittedDate = "10/10/2020")
        expectInvoiceModel = InvoiceModel(lifecycle = "PENDING_SELLER", rdSubmittedDate = "10/10/2020", status = "Request to Cancel", matchingStatus = "Request to Cancel")
        result = invoiceModel.display(isSeller)
        assertEquals(expectInvoiceModel, result)
    }

    @Test
    fun `test generateStatus`() {
        val invoiceModel = spyk<InvoiceModel>()
        // case: this lifecycle displayName not has comma, rdSubmittedDate is null
        var lifecycle = "ISSUED"
        var isSeller = true
        var rdSubmittedDate: String? = null

        var result = callMethod<InvoiceModel, String>(invoiceModel, "generateStatus", lifecycle, isSeller, rdSubmittedDate)
        assertEquals("Submitted", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is null
        lifecycle = "PENDING_SELLER"
        isSeller = false
        result = callMethod<InvoiceModel, String>(invoiceModel, "generateStatus", lifecycle, isSeller, rdSubmittedDate)
        assertEquals("Request to Resubmit", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is not null
        rdSubmittedDate = "10/10/2020"
        result = callMethod<InvoiceModel, String>(invoiceModel, "generateStatus", lifecycle, isSeller, rdSubmittedDate)
        assertEquals("Request to Cancel", result)
    }

    @Test
    fun `test generateMatcherStatus`() {
        val invoiceModel = spyk<InvoiceModel>()
        // case: this lifecycle displayName not has comma, rdSubmittedDate is null
        var lifecycle = "ISSUED"
        var rdSubmittedDate: String? = null

        var result = callMethod<InvoiceModel, String>(invoiceModel, "generateMatcherStatus", lifecycle, rdSubmittedDate)
        assertEquals("Submitted", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is null
        lifecycle = "PENDING_SELLER"
        result = callMethod<InvoiceModel, String>(invoiceModel, "generateMatcherStatus", lifecycle, rdSubmittedDate)
        assertEquals("Request Invoice Resubmission", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is not null
        rdSubmittedDate = "10/10/2020"
        result = callMethod<InvoiceModel, String>(invoiceModel, "generateMatcherStatus", lifecycle, rdSubmittedDate)
        assertEquals("Request to Cancel", result)
    }

}