package th.co.dv.p2p.common.utilities.manager

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.enums.InvoiceAction
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import java.math.BigDecimal
import kotlin.test.*

class CashPerfAndWarrantyForPoManagerTest {

    private val purchaseOrder = PurchaseOrderModel(linearId = "pHeader1", purchaseItems = listOf(PurchaseItemModel(linearId = "pItem1", purchaseOrderLinearId = "pHeader1")))
    private val purchaseOrder2 = PurchaseOrderModel(linearId = "pHeader2", purchaseItems = listOf(PurchaseItemModel(linearId = "pItem2", purchaseOrderLinearId = "pHeader2")))
    private val involvePurchaseOrders = listOf(purchaseOrder, purchaseOrder2)
    private val invoiceItemNormal = InvoiceItemModel(purchaseItemLinearId = "pItem1", itemCategory = "NORMAL")
    private val invoiceItemNormal2 = InvoiceItemModel(purchaseItemLinearId = "pItem2", itemCategory = "NORMAL")
    private val invoiceItemAdvance = InvoiceItemModel(purchaseItemLinearId = "pItem2", itemCategory = "ADVANCE")
    private val invoiceModel = InvoiceModel(cashPerfGuaranteeFromGr = false, cashWarrantyFromGr = false,
            invoiceItems = listOf(invoiceItemNormal, invoiceItemNormal2, invoiceItemAdvance))

    @Test
    fun `test updateCashRemainingAmount`() {
        mockkObject(CashPerfAndWarrantyForPoManager)
        every {
            CashPerfAndWarrantyForPoManager.getNewCashPerfRemainingAmount(
                    match { item -> item.map { it.purchaseItemLinearId }.containsAll(listOf("pItem1")) },
                    purchaseOrder, "invoiceAction")
        } returns BigDecimal(1)
        every {
            CashPerfAndWarrantyForPoManager.getNewCashPerfRemainingAmount(
                    match { item -> item.map { it.purchaseItemLinearId }.containsAll(listOf("pItem2")) }, purchaseOrder2, "invoiceAction")
        } returns BigDecimal(2)
        every {
            CashPerfAndWarrantyForPoManager.getNewCashWarrantyRemainingAmount(
                    match { item -> item.map { it.purchaseItemLinearId }.containsAll(listOf("pItem1")) }, purchaseOrder, "invoiceAction")
        } returns BigDecimal(11)
        every {
            CashPerfAndWarrantyForPoManager.getNewCashWarrantyRemainingAmount(
                    match { item -> item.map { it.purchaseItemLinearId }.containsAll(listOf("pItem2")) }, purchaseOrder2, "invoiceAction")
        } returns BigDecimal(22)

        val result = involvePurchaseOrders.updateCashRemainingAmount(invoiceModel, "invoiceAction")
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(BigDecimal(1), result.find { it.linearId == "pHeader1" }!!.cashPerfGuaranteeRemainingAmount)
        assertEquals(BigDecimal(2), result.find { it.linearId == "pHeader2" }!!.cashPerfGuaranteeRemainingAmount)
        assertEquals(BigDecimal(11), result.find { it.linearId == "pHeader1" }!!.cashWarrantyRemainingAmount)
        assertEquals(BigDecimal(22), result.find { it.linearId == "pHeader2" }!!.cashWarrantyRemainingAmount)
        unmockkObject(CashPerfAndWarrantyForPoManager)

    }

    @Test
    fun `test getNewCashPerfRemainingAmount`() {
        val cashPerfAndWarrantyForPoManager = spyk<CashPerfAndWarrantyForPoManager>()
        every { cashPerfAndWarrantyForPoManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal(2), BigDecimal.TEN, "invoiceAction") } returns BigDecimal(3)
        every { cashPerfAndWarrantyForPoManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal.ZERO, BigDecimal.TEN, "invoiceAction") } returns BigDecimal(5)
        every { cashPerfAndWarrantyForPoManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN, "invoiceAction") } returns BigDecimal.ZERO

        //case is not require for calculate cash perf
        every { cashPerfAndWarrantyForPoManager["isRequireCalculateCashPerf"](PurchaseOrderModel(cashPerfGuaranteeRemainingAmount = null)) } returns false
        var result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewCashPerfRemainingAmount",
                listOf(InvoiceItemModel()), PurchaseOrderModel(cashPerfGuaranteeAmount = null), "invoiceAction")
        assertEquals(null, result)

        //case no cash perf remaining amount at header, require calculate cash perf
        every { cashPerfAndWarrantyForPoManager["isRequireCalculateCashPerf"](any<PurchaseOrderModel>()) } returns true
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewCashPerfRemainingAmount"
                , listOf(InvoiceItemModel()), PurchaseOrderModel(cashPerfGuaranteeRemainingAmount = null), "invoiceAction")
        assertEquals(null, result)

        //case no cash performance guarantee amount at invoiceItem, require calculate
        var relateInvoiceItems = listOf(InvoiceItemModel(cashPerfGuaranteeAmount = null), InvoiceItemModel(cashPerfGuaranteeAmount = null))
        var purchaseOrder = PurchaseOrderModel(cashPerfGuaranteeRemainingAmount = BigDecimal(5), cashPerfGuaranteeAmount = BigDecimal.TEN)
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewCashPerfRemainingAmount",
                relateInvoiceItems, purchaseOrder, "invoiceAction")
        assertEquals(BigDecimal(5), result)

        //case success
        relateInvoiceItems = listOf(InvoiceItemModel(cashPerfGuaranteeAmount = BigDecimal.ONE), InvoiceItemModel(cashPerfGuaranteeAmount = BigDecimal.ONE))
        purchaseOrder = PurchaseOrderModel(cashPerfGuaranteeRemainingAmount = BigDecimal(5), cashPerfGuaranteeAmount = BigDecimal.TEN)
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewCashPerfRemainingAmount",
                relateInvoiceItems, purchaseOrder, "invoiceAction")
        assertEquals(BigDecimal(3), result)
    }

    @Test
    fun `test getNewCashWarrantyRemainingAmount`() {
        val cashPerfAndWarrantyForPoManager = spyk<CashPerfAndWarrantyForPoManager>()
        every { cashPerfAndWarrantyForPoManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal(2), BigDecimal.TEN, "invoiceAction") } returns BigDecimal(3)
        every { cashPerfAndWarrantyForPoManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal.ZERO, BigDecimal.TEN, "invoiceAction") } returns BigDecimal(5)
        every { cashPerfAndWarrantyForPoManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN, "invoiceAction") } returns BigDecimal.ZERO


        //case is not require for calculate cash perf
        every { cashPerfAndWarrantyForPoManager["isRequireCalculateCashWarranty"](PurchaseOrderModel(cashWarrantyAmount = null)) } returns false
        var result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewCashWarrantyRemainingAmount",
                listOf(InvoiceItemModel()), PurchaseOrderModel(cashWarrantyAmount = null), "invoiceAction")
        assertEquals(null, result)

        //case no cash perf remaining amount at gr header, require calculate cash warranty
        every { cashPerfAndWarrantyForPoManager["isRequireCalculateCashWarranty"](any<PurchaseOrderModel>()) } returns true
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewCashWarrantyRemainingAmount",
                listOf(InvoiceItemModel()), PurchaseOrderModel(cashWarrantyRemainingAmount = null), "invoiceAction")
        assertEquals(null, result)

        //case no cash warranty amount at invoiceItem, require calculate
        var relateInvoiceItems = listOf(InvoiceItemModel(cashWarrantyAmount = null), InvoiceItemModel(cashWarrantyAmount = null))
        var purchaseOrder = PurchaseOrderModel(cashWarrantyRemainingAmount = BigDecimal(5), cashWarrantyAmount = BigDecimal.TEN)
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewCashWarrantyRemainingAmount",
                relateInvoiceItems, purchaseOrder, "invoiceAction")
        assertEquals(BigDecimal(5), result)

        //case success
        relateInvoiceItems = listOf(InvoiceItemModel(cashWarrantyAmount = BigDecimal.ONE), InvoiceItemModel(cashWarrantyAmount = BigDecimal.ONE))
        purchaseOrder = PurchaseOrderModel(cashWarrantyRemainingAmount = BigDecimal(5), cashWarrantyAmount = BigDecimal.TEN)
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewCashWarrantyRemainingAmount",
                relateInvoiceItems, purchaseOrder, "invoiceAction")
        assertEquals(BigDecimal(3), result)
    }

    @Test
    fun `test getNewRetentionRemainingAmount`() {
        val cashPerfAndWarrantyForPoManager = spyk<CashPerfAndWarrantyForPoManager>()

        // case is not require for calculate retention
        every { cashPerfAndWarrantyForPoManager["isRequireCalculateRetention"](any<PurchaseOrderModel>()) } returns false
        var result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewRetentionRemainingAmount",
                listOf(InvoiceItemModel()), PurchaseOrderModel(retentionAmount = null), InvoiceAction.CANCEL.name)
        assertNull(result)

        // case no retentionRemainingAmount at po header and require for calculate retention
        every { cashPerfAndWarrantyForPoManager["isRequireCalculateRetention"](any<PurchaseOrderModel>()) } returns true
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewRetentionRemainingAmount",
                listOf(InvoiceItemModel()), PurchaseOrderModel(retentionRemainingAmount = null), InvoiceAction.CANCEL.name)
        assertNull(result)

        //case no cash retention amount at invoiceItem, require for calculate retention
        every { cashPerfAndWarrantyForPoManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal.ZERO, BigDecimal.TEN, InvoiceAction.CANCEL.name) } returns BigDecimal(5)
        var relateInvoiceItems = listOf(InvoiceItemModel(retentionAmount = null), InvoiceItemModel(retentionAmount = null))
        var purchaseOrder = PurchaseOrderModel(retentionRemainingAmount = BigDecimal(5), retentionAmount = BigDecimal.TEN)
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewRetentionRemainingAmount",
                relateInvoiceItems, purchaseOrder, InvoiceAction.CANCEL.name)
        assertEquals(BigDecimal(5), result)

        // case success
        every { cashPerfAndWarrantyForPoManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal(2), BigDecimal.TEN, InvoiceAction.CANCEL.name) } returns BigDecimal(7)
        relateInvoiceItems = listOf(InvoiceItemModel(retentionAmount = BigDecimal.ONE), InvoiceItemModel(retentionAmount = BigDecimal.ONE))
        purchaseOrder = PurchaseOrderModel(retentionRemainingAmount = BigDecimal(5), retentionAmount = BigDecimal.TEN)
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "getNewRetentionRemainingAmount",
                relateInvoiceItems, purchaseOrder, InvoiceAction.CANCEL.name)
        assertEquals(BigDecimal(7), result)

    }

    @Test
    fun `test calculateNewRemainingAmountByInvoiceAction`() {
        val cashPerfAndWarrantyForPoManager = spyk<CashPerfAndWarrantyForPoManager>()

        //case action ISSUE : normal case
        var result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(5), BigDecimal(2), BigDecimal.TEN, "ISSUE")
        assertEquals(BigDecimal(3), result)

        // case action ISSUE : consume > purchaseRemainingAmount
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(2), BigDecimal(5), BigDecimal.TEN, "ISSUE")
        assertEquals(BigDecimal.ZERO, result)

        // case action ISSUE : consume = purchaseRemainingAmount
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(5), BigDecimal(5), BigDecimal.TEN, "ISSUE")
        assertEquals(BigDecimal.ZERO, result)

        //case action CANCEL : newRemainingAmount < initialAmount
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(5), BigDecimal(2), BigDecimal.TEN, "CANCEL")
        assertEquals(BigDecimal(7), result)

        //case action CANCEL : newRemainingAmount < initialAmount and newRemainingAmount is negative
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(-3), BigDecimal(2), BigDecimal.TEN, "CANCEL")
        assertEquals(BigDecimal.ZERO, result)

        //case action CANCEL : newRemainingAmount > initialAmount
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(5), BigDecimal(2), BigDecimal.ONE, "CANCEL")
        assertEquals(BigDecimal.ONE, result)

        //case invalid action
        result = callMethod<CashPerfAndWarrantyForPoManager, BigDecimal>(cashPerfAndWarrantyForPoManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(5), BigDecimal(15), BigDecimal.ONE, "xxxx")
        assertEquals(BigDecimal(5), result)
    }


    @Test
    fun `Test isRequireCalculateCashWarranty`() {
        val cashPerfAndWarrantyAmountManager = spyk<CashPerfAndWarrantyForPoManager>()
        // case purchaseOrder.cashWarrantyAmount != null
        var result = callMethod<CashPerfAndWarrantyForPoManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashWarranty",
                PurchaseOrderModel(linearId = "purchaseOrderLinearId", cashWarrantyAmount = BigDecimal.TEN))
        assertTrue(result!!)

        // case purchaseOrder.cashWarrantyAmount == null
        result = callMethod<CashPerfAndWarrantyForPoManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashWarranty",
                PurchaseOrderModel(linearId = "purchaseOrderLinearId"))
        assertFalse(result!!)
    }


    @Test
    fun `Test isRequireCalculateCashPerf`() {
        val cashPerfAndWarrantyAmountManager = spyk<CashPerfAndWarrantyForPoManager>()
        // case purchaseOrder.cashPerfGuaranteeAmount != null
        var result = callMethod<CashPerfAndWarrantyForPoManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashPerf",
                PurchaseOrderModel(linearId = "purchaseOrderLinearId", cashPerfGuaranteeAmount = BigDecimal.TEN))
        assertTrue(result!!)

        // case purchaseOrder.cashPerfGuaranteeAmount == null
        result = callMethod<CashPerfAndWarrantyForPoManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashPerf",
                PurchaseOrderModel(linearId = "purchaseOrderLinearId"))
        assertFalse(result!!)
    }

    @Test
    fun `Test isRequireCalculateRetention`(){
        val cashPerfAndWarrantyAmountManager = spyk<CashPerfAndWarrantyForPoManager>()
        // case purchaseOrder.cashPerfGuaranteeAmount != null
        var result = callMethod<CashPerfAndWarrantyForPoManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateRetention",
                PurchaseOrderModel(linearId = "purchaseOrderLinearId", retentionAmount = BigDecimal.TEN))
        assertTrue(result!!)

        // case purchaseOrder.cashPerfGuaranteeAmount == null
        result = callMethod<CashPerfAndWarrantyForPoManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateRetention",
                PurchaseOrderModel(linearId = "purchaseOrderLinearId"))
        assertFalse(result!!)
    }

}