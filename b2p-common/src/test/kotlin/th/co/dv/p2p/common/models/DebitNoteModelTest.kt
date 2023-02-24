package th.co.dv.p2p.common.models

import io.mockk.spyk
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.corda.base.models.DebitNoteModel
import java.math.BigDecimal
import kotlin.test.assertEquals

class DebitNoteModelTest {

    @Test
    fun `test display`() {
        // case: debitNoteModel lifecycle is null
        var debitNoteModel = DebitNoteModel()
        var isSeller = false
        var expectDebitNoteModel = DebitNoteModel()
        var result = debitNoteModel.display(isSeller)
        assertEquals(expectDebitNoteModel, result)

        // case: debitNoteModel lifecycle is match with debit note status, buyer
        debitNoteModel = DebitNoteModel(lifecycle = "PENDING_BUYER")
        expectDebitNoteModel = DebitNoteModel(lifecycle = "PENDING_BUYER", status = "Pending Clarification")
        result = debitNoteModel.display(isSeller)
        assertEquals(expectDebitNoteModel, result)

        // case: debitNoteModel lifecycle is match with debit note status, seller
        isSeller = true
        expectDebitNoteModel = DebitNoteModel(lifecycle = "PENDING_BUYER", status = "Verifying")
        result = debitNoteModel.display(isSeller)
        assertEquals(expectDebitNoteModel, result)

        // case: debitNoteModel lifecycle is match with many debit note status
        debitNoteModel = DebitNoteModel(lifecycle = "PENDING_SELLER")
        expectDebitNoteModel = DebitNoteModel(lifecycle = "PENDING_SELLER", status = "Request to Resubmit")
        result = debitNoteModel.display(isSeller)
        assertEquals(expectDebitNoteModel, result)

        debitNoteModel = DebitNoteModel(lifecycle = "PENDING_SELLER", rdSubmittedDate = "10/10/2020")
        expectDebitNoteModel = DebitNoteModel(lifecycle = "PENDING_SELLER", rdSubmittedDate = "10/10/2020", status = "Request to Cancel")
        result = debitNoteModel.display(isSeller)
        assertEquals(expectDebitNoteModel, result)
    }

    @Test
    fun `test generateStatus`() {
        val debitNoteModel = spyk<DebitNoteModel>()

        // case: this lifecycle displayName not has comma, rdSubmittedDate is null
        var lifecycle = "ISSUED"
        var isSeller = true
        var rdSubmittedDate: String? = null
        var result = callMethod<DebitNoteModel, String>(debitNoteModel, "generateStatus", lifecycle, isSeller, rdSubmittedDate)
        assertEquals("Submitted", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is null
        lifecycle = "PENDING_SELLER"
        isSeller = false
        result = callMethod<DebitNoteModel, String>(debitNoteModel, "generateStatus", lifecycle, isSeller, rdSubmittedDate)
        assertEquals("Request to Resubmit", result)

        // case: this lifecycle displayName has comma, rdSubmittedDate is not null
        rdSubmittedDate = "10/10/2020"
        result = callMethod<DebitNoteModel, String>(debitNoteModel, "generateStatus", lifecycle, isSeller, rdSubmittedDate)
        assertEquals("Request to Cancel", result)
    }

    @Test
    fun `Test updateWithholdingTax`() {
        val debiteNoteModel = DebitNoteModel()

        val result = debiteNoteModel.updateWithholdingTax(BigDecimal.TEN)
        assertEquals(BigDecimal.TEN, result.withholdingTaxTotal)
    }

}