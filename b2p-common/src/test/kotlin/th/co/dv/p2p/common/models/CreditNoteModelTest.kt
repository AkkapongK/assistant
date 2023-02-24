package th.co.dv.p2p.common.models

import io.mockk.spyk
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.corda.base.models.CreditNoteModel
import java.math.BigDecimal
import kotlin.test.assertEquals

class CreditNoteModelTest {

    @Test
    fun `test display`() {
        // case: creditNoteModel lifecycle is null
        var creditNoteModel = CreditNoteModel()
        var isSeller = false
        var expectCreditNoteModel = CreditNoteModel()
        var result = creditNoteModel.display(isSeller)
        assertEquals(expectCreditNoteModel, result)

        // case: creditNoteModel lifecycle is match with credit note status, buyer
        creditNoteModel = CreditNoteModel(lifecycle = "PARTIAL")
        expectCreditNoteModel = CreditNoteModel(lifecycle = "PARTIAL", status = "Verifying", matchingStatus = "Partial Return GR")
        result = creditNoteModel.display(isSeller)
        assertEquals(expectCreditNoteModel, result)

        // case: creditNoteModel lifecycle is match with credit note status, seller
        isSeller = true
        result = creditNoteModel.display(isSeller)
        assertEquals(expectCreditNoteModel, result)

        // case: creditNoteModel lifecycle is match with many credit note status
        creditNoteModel = CreditNoteModel(lifecycle = "REJECTED")
        expectCreditNoteModel = CreditNoteModel(lifecycle = "REJECTED", status = "Request to Resubmit", matchingStatus = "Request Credit Note Resubmission")
        result = creditNoteModel.display(isSeller)
        assertEquals(expectCreditNoteModel, result)

        creditNoteModel = CreditNoteModel(lifecycle = "REJECTED", rdSubmittedDate = "10/10/2020")
        expectCreditNoteModel = CreditNoteModel(lifecycle = "REJECTED", rdSubmittedDate = "10/10/2020", status = "Request to Cancel", matchingStatus = "Request to Cancel")
        result = creditNoteModel.display(isSeller)
        assertEquals(expectCreditNoteModel, result)
    }

    @Test
    fun `test generateStatus`() {
        val creditNoteModel = spyk<CreditNoteModel>()

        // case: this lifecycle displayName not has comma, rdSubmittedDate is null
        var lifecycle = "ISSUED"
        var isSeller = true
        var rdSubmittedDate: String? = null
        var result = callMethod<CreditNoteModel, String>(creditNoteModel, "generateStatus", lifecycle, isSeller, rdSubmittedDate)
        assertEquals("Submitted", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is null
        lifecycle = "REJECTED"
        isSeller = false
        result = callMethod<CreditNoteModel, String>(creditNoteModel, "generateStatus", lifecycle, isSeller, rdSubmittedDate)
        assertEquals("Request to Resubmit", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is not null
        rdSubmittedDate = "10/10/2020"
        result = callMethod<CreditNoteModel, String>(creditNoteModel, "generateStatus", lifecycle, isSeller, rdSubmittedDate)
        assertEquals("Request to Cancel", result)
    }

    @Test
    fun `test generateMatcherStatus`() {
        val creditNoteModel = spyk<CreditNoteModel>()

        // case: this lifecycle displayName not has comma, rdSubmittedDate is null
        var lifecycle = "ISSUED"
        var rdSubmittedDate: String? = null
        var result = callMethod<CreditNoteModel, String>(creditNoteModel, "generateMatcherStatus", lifecycle, rdSubmittedDate)
        assertEquals("Submitted", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is null
        lifecycle = "REJECTED"
        result = callMethod<CreditNoteModel, String>(creditNoteModel, "generateMatcherStatus", lifecycle, rdSubmittedDate)
        assertEquals("Request Credit Note Resubmission", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is not null
        rdSubmittedDate = "10/10/2020"
        result = callMethod<CreditNoteModel, String>(creditNoteModel, "generateMatcherStatus", lifecycle, rdSubmittedDate)
        assertEquals("Request to Cancel", result)
    }

    @Test
    fun `Test updateWithholdingTax`() {
        val creditNoteModel = CreditNoteModel()

        val result = creditNoteModel.updateWithholdingTax(BigDecimal.TEN)
        assertEquals(BigDecimal.TEN, result.withholdingTaxTotal)
    }

    @Test
    fun `Test updatePurchaseOrderHeaderNumber`() {
        val creditNoteModel = CreditNoteModel()

        val result = creditNoteModel.updatePurchaseOrderHeaderNumber("PO-001")
        assertEquals("PO-001", result.purchaseOrderHeaderNumber)
    }
}