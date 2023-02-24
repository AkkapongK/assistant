package th.co.dv.p2p.common.models

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.enums.State
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.utilities.isListEqualsByFieldName
import th.co.dv.p2p.corda.base.models.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomValidateFieldModelTest {

    @Test
    fun `test validateField`(){
        mockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")

        //state PurchaseOrder
        val customValidateFieldModel = CustomValidateFieldModel(state = State.PurchaseOrder.name)
        val purchaseOrderModels = listOf<PurchaseOrderModel>()
        val purchaseItemModels = listOf<PurchaseItemModel>()
        every { isListEqualsByFieldName(purchaseOrderModels, purchaseItemModels, "poFieldName") } returns true
        every { isListEqualsByFieldName(purchaseOrderModels, purchaseItemModels, "poFieldName2") } returns false
        var result = listOf(
                customValidateFieldModel.copy(fieldName = "poFieldName", errorMessage = "poFieldName invalid"),
                customValidateFieldModel.copy(fieldName = "poFieldName2", errorMessage = "poFieldName2 invalid"))
                .validateField(AllStates(purchaseOrders = purchaseOrderModels, purchaseItems = purchaseItemModels))
        assertEquals(listOf("poFieldName2 invalid"), result)

        //state GoodsReceived
        val customValidateFieldModel2 = CustomValidateFieldModel(state = State.GoodsReceived.name)
        val goodsReceivedModels = listOf<GoodsReceivedModel>()
        val goodsReceivedItemModels = listOf<GoodsReceivedItemModel>()
        every { isListEqualsByFieldName(goodsReceivedModels, goodsReceivedItemModels, "grFieldName") } returns true
        every { isListEqualsByFieldName(goodsReceivedModels, goodsReceivedItemModels, "grFieldName2") } returns false
        every { isListEqualsByFieldName(goodsReceivedModels, goodsReceivedItemModels, "grFieldName3") } returns false
        result = listOf(
                customValidateFieldModel2.copy(fieldName = "grFieldName", errorMessage = "grFieldName invalid"),
                customValidateFieldModel2.copy(fieldName = "grFieldName2", errorMessage = "grFieldName2 invalid"),
                customValidateFieldModel2.copy(fieldName = "grFieldName3", errorMessage = "grFieldName3 invalid"))
                .validateField(AllStates(goodsReceiveds = goodsReceivedModels, goodsReceivedItems = goodsReceivedItemModels))
        assertEquals(listOf("grFieldName2 invalid", "grFieldName3 invalid"), result)

        //state CreditNote
        val customValidateFieldModel3 = CustomValidateFieldModel(state = State.CreditNote.name)
        val creditNoteModels = listOf<CreditNoteModel>()
        val creditNoteItemModels = listOf<CreditNoteItemModel>()
        every { isListEqualsByFieldName(creditNoteModels, creditNoteItemModels, "cnFieldName") } returns true
        every { isListEqualsByFieldName(creditNoteModels, creditNoteItemModels, "cnFieldName2") } returns false
        every { isListEqualsByFieldName(creditNoteModels, creditNoteItemModels, "cnFieldName3") } returns false
        result = listOf(
                customValidateFieldModel3.copy(fieldName = "cnFieldName", errorMessage = "cnFieldName invalid"),
                customValidateFieldModel3.copy(fieldName = "cnFieldName2", errorMessage = "cnFieldName2 invalid"),
                customValidateFieldModel3.copy(fieldName = "cnFieldName3", errorMessage = "cnFieldName3 invalid"))
                .validateField(AllStates(creditNotes = creditNoteModels, creditNoteItems = creditNoteItemModels))
        assertEquals(listOf("cnFieldName2 invalid", "cnFieldName3 invalid"), result)

        //state DebitNote
        val customValidateFieldModel4 = CustomValidateFieldModel(state = State.DebitNote.name)
        val debitNoteModels = listOf<DebitNoteModel>()
        val debitNoteItemModels = listOf<DebitNoteItemModel>()
        every { isListEqualsByFieldName(debitNoteModels, debitNoteItemModels, "dnFieldName") } returns true
        every { isListEqualsByFieldName(debitNoteModels, debitNoteItemModels, "dnFieldName2") } returns false
        every { isListEqualsByFieldName(debitNoteModels, debitNoteItemModels, "dnFieldName3") } returns false
        result = listOf(
                customValidateFieldModel4.copy(fieldName = "dnFieldName", errorMessage = "dnFieldName invalid"),
                customValidateFieldModel4.copy(fieldName = "dnFieldName2", errorMessage = "dnFieldName2 invalid"),
                customValidateFieldModel4.copy(fieldName = "dnFieldName3", errorMessage = "dnFieldName3 invalid"))
                .validateField(AllStates(debitNotes = debitNoteModels, debitNoteItems = debitNoteItemModels))
        assertEquals(listOf("dnFieldName2 invalid", "dnFieldName3 invalid"), result)

        // invalid header state
        val customValidateFieldModels = listOf(CustomValidateFieldModel(state = "invalid", fieldName = "fieldName"))
        val result2 = Try.on { customValidateFieldModels.validateField(AllStates()) }
        assertTrue(result2.isFailure)
        assertTrue(result2.toString().contains("invalid state is not support for custom validation."))

        unmockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")
    }
}