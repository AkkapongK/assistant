package th.co.dv.p2p.common.utilities.manager

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.MovementClass
import th.co.dv.p2p.corda.base.models.GoodsReceivedItemModel
import th.co.dv.p2p.corda.base.models.GoodsReceivedModel
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CashPerfAndWarrantyForGrManagerTest {
    //model
    private val normalGrItemModel = GoodsReceivedItemModel(movementClass = "NORMAL", goodsReceivedLinearId = "grHeader1")
    private val reverseGrItemModel = GoodsReceivedItemModel(movementClass = "REVERSE", goodsReceivedLinearId = "grHeader2")

    //entity
    private val normalGr = GoodsReceivedModel(goodsReceivedItems = listOf(GoodsReceivedItemModel(movementClass = "NORMAL")), linearId = "grHeader1")
    private val reverseGr = GoodsReceivedModel(goodsReceivedItems = listOf(GoodsReceivedItemModel(movementClass = "REVERSE")), linearId = "grHeader2")


    @Test
    fun `test updateCashRemainingAmount filter correct`() {
        mockkObject(CashPerfAndWarrantyForGrManager)

        every { CashPerfAndWarrantyForGrManager.getNewCashPerfRemainingAmount(match{ item -> item.map { it.itemCategory }.containsAll(listOf("NORMAL")) }, normalGr, "invoiceAction", false) } returns BigDecimal(1)
        every { CashPerfAndWarrantyForGrManager.getNewCashWarrantyRemainingAmount(match { item -> item.map { it.itemCategory }.containsAll(listOf("NORMAL")) }, normalGr, "invoiceAction", false) } returns BigDecimal(11)
        every { CashPerfAndWarrantyForGrManager.isNormalInvoiceItem(match { it.itemCategory == "NORMAL" }) } returns true
        every { CashPerfAndWarrantyForGrManager.isNormalInvoiceItem(match { it.itemCategory == "ADVANCE" }) } returns false
        every { CashPerfAndWarrantyForGrManager.isNormalGr(match{ it.goodsReceivedItems.all { grItem -> grItem.movementClass == "NORMAL" } }) } returns true
        every { CashPerfAndWarrantyForGrManager.isNormalGr(match { it.goodsReceivedItems.all { grItem -> grItem.movementClass == "REVERSE" } }) } returns false
        every {
            CashPerfAndWarrantyForGrManager.isInvoiceRelatedGr(match { it.goodsReceivedItems.all { gr -> gr.goodsReceivedLinearId == "grHeader1" } },
                    match { it.linearId == "grHeader1" })
        } returns true
        every {
            CashPerfAndWarrantyForGrManager.isInvoiceRelatedGr(match { it.goodsReceivedItems.all { gr -> gr.goodsReceivedLinearId == "grHeader2" } },
                    match { it.linearId == "grHeader1" })
        } returns false
        every {
            CashPerfAndWarrantyForGrManager.isInvoiceRelatedGr(match { it.goodsReceivedItems.all { gr -> gr.goodsReceivedLinearId == "grHeader1" } },
                    match { it.linearId == "grHeader2" })
        } returns false

        //case invoice normal, gr normal
        val invoiceModel = InvoiceModel(invoiceItems = listOf(InvoiceItemModel(itemCategory = "NORMAL", goodsReceivedItems = listOf(normalGrItemModel))))
        var result = listOf(normalGr).updateCashRemainingAmount(invoiceModel, "invoiceAction")
        assertEquals(BigDecimal(1), result.first().cashPerfDeductibleRemainingAmount)
        assertEquals(BigDecimal(11), result.first().cashWarrantyDeductibleRemainingAmount)

        //case invoice normal, gr normal, reverse
        val invoiceModel2 = InvoiceModel(invoiceItems = listOf(InvoiceItemModel(itemCategory = "NORMAL", goodsReceivedItems = listOf(normalGrItemModel, reverseGrItemModel))))
        result = listOf(normalGr, reverseGr).updateCashRemainingAmount(invoiceModel2, "invoiceAction")
        assertEquals(2, result.size)
        assertEquals(BigDecimal(1), result.find { it.linearId == "grHeader1" }!!.cashPerfDeductibleRemainingAmount)
        assertEquals(BigDecimal(11), result.find { it.linearId == "grHeader1" }!!.cashWarrantyDeductibleRemainingAmount)
        assertEquals(null, result.find { it.linearId == "grHeader2" }!!.cashPerfDeductibleRemainingAmount)
        assertEquals(null, result.find { it.linearId == "grHeader2" }!!.cashWarrantyDeductibleRemainingAmount)

        //case invoice normal, gr reverse
        val invoiceModel3 = InvoiceModel(invoiceItems = listOf(InvoiceItemModel(itemCategory = "NORMAL", goodsReceivedItems = listOf(reverseGrItemModel))))
        result = listOf(reverseGr).updateCashRemainingAmount(invoiceModel3, "invoiceAction")
        assertEquals(1, result.size)
        assertEquals(null, result.first().cashPerfDeductibleRemainingAmount)
        assertEquals(null, result.first().cashWarrantyDeductibleRemainingAmount)

        //case invoice advance, gr normal
        val invoiceModel4 = InvoiceModel(invoiceItems = listOf(InvoiceItemModel(itemCategory = "ADVANCE", goodsReceivedItems = listOf(normalGrItemModel))))
        result = listOf(normalGr).updateCashRemainingAmount(invoiceModel4, "invoiceAction")
        assertEquals(1, result.size)
        assertEquals(null, result.first().cashPerfDeductibleRemainingAmount)
        assertEquals(null, result.first().cashWarrantyDeductibleRemainingAmount)

        //case invoice advance, gr reverse
        val invoiceModel5 = InvoiceModel(invoiceItems = listOf(InvoiceItemModel(itemCategory = "ADVANCE", goodsReceivedItems = listOf(reverseGrItemModel))))
        result = listOf(reverseGr).updateCashRemainingAmount(invoiceModel5, "invoiceAction")
        assertEquals(1, result.size)
        assertEquals(null, result.first().cashPerfDeductibleRemainingAmount)
        assertEquals(null, result.first().cashWarrantyDeductibleRemainingAmount)

        unmockkObject(CashPerfAndWarrantyForGrManager)
    }


    @Test
    fun `test getNewCashPerfRemainingAmount`() {
        val cashPerfAndWarrantyAmountManager = spyk<CashPerfAndWarrantyForGrManager>()
        every { cashPerfAndWarrantyAmountManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal(2), "invoiceAction") } returns BigDecimal(3)
        every { cashPerfAndWarrantyAmountManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal.ZERO, "invoiceAction") } returns BigDecimal(5)
        every { cashPerfAndWarrantyAmountManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal.ZERO, BigDecimal.ZERO, "invoiceAction") } returns BigDecimal.ZERO

        //case is not require for calculate cash perf
        every { cashPerfAndWarrantyAmountManager["isRequireCalculateCashPerf"](GoodsReceivedModel(), false, "invoiceAction") } returns false
        var result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "getNewCashPerfRemainingAmount",
                listOf(InvoiceItemModel()), GoodsReceivedModel(), "invoiceAction", false)
        assertEquals(null, result)

        //case no cash perf remaining amount at gr header, require calculate cash perf
        every { cashPerfAndWarrantyAmountManager["isRequireCalculateCashPerf"](any<GoodsReceivedModel>(), true, "invoiceAction") } returns true
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "getNewCashPerfRemainingAmount",
                listOf(InvoiceItemModel()), GoodsReceivedModel(cashPerfDeductibleRemainingAmount = null), "invoiceAction", true)
        assertEquals(null, result)

        //case no cash performance guarantee amount at invoice, require calculate cash perf
        var relateInvoiceItems = listOf(InvoiceItemModel(cashPerfGuaranteeAmount = null), InvoiceItemModel(cashPerfGuaranteeAmount = null))
        var goodsReceived = GoodsReceivedModel(cashPerfDeductibleRemainingAmount = BigDecimal(5))
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "getNewCashPerfRemainingAmount",
                relateInvoiceItems, goodsReceived, "invoiceAction", true)
        assertEquals(BigDecimal(5), result)

        //case success
        relateInvoiceItems = listOf(InvoiceItemModel(cashPerfGuaranteeAmount = BigDecimal.ONE), InvoiceItemModel(cashPerfGuaranteeAmount = BigDecimal.ONE))
        goodsReceived = GoodsReceivedModel(cashPerfDeductibleRemainingAmount = BigDecimal(5))
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "getNewCashPerfRemainingAmount",
                relateInvoiceItems, goodsReceived, "invoiceAction", true)
        assertEquals(BigDecimal(3), result)
    }

    @Test
    fun `test getNewCashWarrantyRemainingAmount`() {
        val cashPerfAndWarrantyAmountManager = spyk<CashPerfAndWarrantyForGrManager>()
        every { cashPerfAndWarrantyAmountManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal(2), "invoiceAction") } returns BigDecimal(3)
        every { cashPerfAndWarrantyAmountManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal(5), BigDecimal.ZERO, "invoiceAction") } returns BigDecimal(5)
        every { cashPerfAndWarrantyAmountManager["calculateNewRemainingAmountByInvoiceAction"](BigDecimal.ZERO, BigDecimal.ZERO, "invoiceAction") } returns BigDecimal.ZERO

        //case is not require for calculate cash warranty
        every { cashPerfAndWarrantyAmountManager["isRequireCalculateCashWarranty"](GoodsReceivedModel(), false, "invoiceAction") } returns false
        var result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "getNewCashWarrantyRemainingAmount",
                listOf(InvoiceItemModel()), GoodsReceivedModel(), "invoiceAction", false)
        assertEquals(null, result)

        //case no cash warranty remaining amount at gr header, require calculate warranty
        every { cashPerfAndWarrantyAmountManager["isRequireCalculateCashWarranty"](any<GoodsReceivedModel>(), true, "invoiceAction") } returns true
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "getNewCashWarrantyRemainingAmount",
                listOf(InvoiceItemModel()), GoodsReceivedModel(cashWarrantyDeductibleRemainingAmount = null), "invoiceAction", true)
        assertEquals(null, result)

        //case no cash warranty amount at invoiceItem, require calculate warranty
        var relateInvoiceItems = listOf(InvoiceItemModel(cashWarrantyAmount = null), InvoiceItemModel(cashWarrantyAmount = null))
        var goodsReceived = GoodsReceivedModel(cashWarrantyDeductibleRemainingAmount = BigDecimal(5))
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "getNewCashWarrantyRemainingAmount",
                relateInvoiceItems, goodsReceived, "invoiceAction", true)
        assertEquals(BigDecimal(5), result)

        //case success
        relateInvoiceItems = listOf(InvoiceItemModel(cashWarrantyAmount = BigDecimal.ONE), InvoiceItemModel(cashWarrantyAmount = BigDecimal.ONE))
        goodsReceived = GoodsReceivedModel(cashWarrantyDeductibleRemainingAmount = BigDecimal(5))
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "getNewCashWarrantyRemainingAmount",
                relateInvoiceItems, goodsReceived, "invoiceAction", true)
        assertEquals(BigDecimal(3), result)
    }

    @Test
    fun `test calculateNewRemainingAmountByInvoiceAction`() {
        val cashPerfAndWarrantyAmountManager = spyk<CashPerfAndWarrantyForGrManager>()

        //case action ISSUE : normal case
        var result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(5), BigDecimal(2), "ISSUE")
        assertEquals(BigDecimal(3), result)

        // case action ISSUE : consume > purchaseRemainingAmount
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(2), BigDecimal(5), "ISSUE")
        assertEquals(BigDecimal.ZERO, result)

        // case action ISSUE : consume = purchaseRemainingAmount
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(5), BigDecimal(5), "ISSUE")
        assertEquals(BigDecimal.ZERO, result)

        //case action CANCEL : normal case
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(5), BigDecimal(2), "CANCEL")
        assertEquals(BigDecimal(7), result)

        //case invalid action
        result = callMethod<CashPerfAndWarrantyForGrManager, BigDecimal>(cashPerfAndWarrantyAmountManager, "calculateNewRemainingAmountByInvoiceAction",
                BigDecimal(5), BigDecimal(15), "xxxx")
        assertEquals(BigDecimal(5), result)
    }

    @Test
    fun `Test isNormalInvoiceItem`() {
        //case normal invoice item -> return true
        var result = CashPerfAndWarrantyForGrManager.isNormalInvoiceItem(InvoiceItemModel(itemCategory = ItemCategory.Purchase.NORMAL.name))
        assertTrue(result)

        //case not normal invoice item -> return false
        result = CashPerfAndWarrantyForGrManager.isNormalInvoiceItem(InvoiceItemModel(itemCategory = ItemCategory.Purchase.ADVANCE.name))
        assertFalse(result)
    }

    @Test
    fun `Test isNormalGr`() {
        //case normal gr item -> return true
        var result = CashPerfAndWarrantyForGrManager.isNormalGr(
                GoodsReceivedModel(goodsReceivedItems = mutableListOf(GoodsReceivedItemModel(movementClass = MovementClass.NORMAL.name))))
        assertTrue(result)

        //case not normal gr item -> return false
        result = CashPerfAndWarrantyForGrManager.isNormalGr(
                GoodsReceivedModel(goodsReceivedItems = mutableListOf(GoodsReceivedItemModel(movementClass = MovementClass.REVERSE.name))))
        assertFalse(result)
    }

    @Test
    fun `Test isInvoiceRelatedGr`() {
        mockkObject(CashPerfAndWarrantyForGrManager)
        //case related
        var result = CashPerfAndWarrantyForGrManager.isInvoiceRelatedGr(
                InvoiceItemModel(goodsReceivedItems = listOf(GoodsReceivedItemModel(goodsReceivedLinearId = "goodsReceivedLinearId"))),
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"))
        assertTrue(result)

        //related 1 gr not related 1 gr
        result = CashPerfAndWarrantyForGrManager.isInvoiceRelatedGr(
                InvoiceItemModel(goodsReceivedItems = listOf(GoodsReceivedItemModel(goodsReceivedLinearId = "goodsReceivedLinearId"),
                        GoodsReceivedItemModel(goodsReceivedLinearId = "xxxx"))),
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"))
        assertTrue(result)

        //case not related
        result = CashPerfAndWarrantyForGrManager.isInvoiceRelatedGr(
                InvoiceItemModel(goodsReceivedItems = listOf(GoodsReceivedItemModel(goodsReceivedLinearId = "xxxx"))),
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"))
        assertFalse(result)
    }

    @Test
    fun `Test isRequireCalculateCashWarranty`() {
        val cashPerfAndWarrantyAmountManager = spyk<CashPerfAndWarrantyForGrManager>()
        // case action CANCEL and iscashWarrantyFromGr is true
        var result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashWarranty",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"), true, "CANCEL")
        assertTrue(result!!)

        // case action CANCEL and iscashWarrantyFromGr is false
        result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashWarranty",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"), false, "CANCEL")
        assertFalse(result!!)

        // case action CANCEL and iscashWarrantyFromGr is null
        result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashWarranty",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"), null, "CANCEL")
        assertFalse(result!!)

        // case goodsReceived.cashWarrantyDeductibleAmount != null
        result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashWarranty",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId", cashWarrantyDeductibleAmount = BigDecimal.TEN), null, "ISSUE")
        assertTrue(result!!)

        // case goodsReceived.cashWarrantyDeductibleAmount == null
        result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashWarranty",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"), null, "ISSUE")
        assertFalse(result!!)
    }


    @Test
    fun `Test isRequireCalculateCashPerf`() {
        val cashPerfAndWarrantyAmountManager = spyk<CashPerfAndWarrantyForGrManager>()
        // case action CANCEL and isCashPerfFromGr is true
        var result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashPerf",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"), true, "CANCEL")
        assertTrue(result!!)

        // case action CANCEL and isCashPerfFromGr is false
        result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashPerf",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"), false, "CANCEL")
        assertFalse(result!!)

        // case action CANCEL and isCashPerfFromGr is null
        result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashPerf",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"), null, "CANCEL")
        assertFalse(result!!)

        // case goodsReceived.cashPerfGuaranteeDeductibleAmount != null
        result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashPerf",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId", cashPerfGuaranteeDeductibleAmount = BigDecimal.TEN),
                null, "ISSUE")
        assertTrue(result!!)

        // case goodsReceived.cashPerfGuaranteeDeductibleAmount == null
        result = callMethod<CashPerfAndWarrantyForGrManager, Boolean>(cashPerfAndWarrantyAmountManager, "isRequireCalculateCashPerf",
                GoodsReceivedModel(linearId = "goodsReceivedLinearId"), null, "ISSUE")
        assertFalse(result!!)

    }

}