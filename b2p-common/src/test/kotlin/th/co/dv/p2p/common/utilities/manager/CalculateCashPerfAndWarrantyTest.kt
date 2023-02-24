package th.co.dv.p2p.common.utilities.manager

import io.mockk.*
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.MovementClass
import th.co.dv.p2p.common.utilities.depleteQuantity
import th.co.dv.p2p.common.utilities.setScale
import th.co.dv.p2p.common.utilities.setScaleByCurrency
import th.co.dv.p2p.common.validators.CashPerfAndWarrantyValidator
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.*
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CalculateCashPerfAndWarrantyTest {

    private val currency = "THB"
    @Test
    fun `Test update cash perf or warrantyInItem`() {
        val invoiceItemModel = InvoiceItemModel(linearId = "ITEM-01")
        val amount = 100.0.toBigDecimal()

        var isCashPerfGuarantee = true
        var expectedValue = invoiceItemModel.copy(cashPerfGuaranteeAmount = amount)
        var result = callMethod<CalculateCashPerfAndWarranty, InvoiceItemModel>(CalculateCashPerfAndWarranty, "updateCashPerfOrWarrantyInItem", isCashPerfGuarantee, invoiceItemModel, amount)
        assertEquals(expectedValue, result)

        isCashPerfGuarantee = false
        expectedValue = invoiceItemModel.copy(cashWarrantyAmount = amount)
        result = callMethod<CalculateCashPerfAndWarranty, InvoiceItemModel>(CalculateCashPerfAndWarranty, "updateCashPerfOrWarrantyInItem", isCashPerfGuarantee, invoiceItemModel, amount)
        assertEquals(expectedValue, result)
    }

    @Test
    fun `Test calculate sum Of item advance deduct amount in same header`() {
        val itemAdvanceDeductAmounts = mutableMapOf("ITEM-01" to 10.0.toBigDecimal(), "ITEM-02" to 10.0.toBigDecimal(), "ITEM-03" to 10.0.toBigDecimal())
        val invoiceItems = listOf(InvoiceItemModel(linearId = "ITEM-01"), InvoiceItemModel(linearId = "ITEM-02"))
        val result = callMethod<CalculateCashPerfAndWarranty, BigDecimal>(CalculateCashPerfAndWarranty, "calculateSumOfItemAdvanceDeductAmountInSameHeader", invoiceItems, itemAdvanceDeductAmounts)
        assertEquals(20.0.toBigDecimal(), result)
    }

    @Test
    fun `Test calculate estimated invoice deductible amount`() {
        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)
        
        val itemAdvanceDeductAmounts = mutableMapOf("ITEM-01" to 10.0.toBigDecimal(), "ITEM-02" to 10.0.toBigDecimal(), "ITEM-03" to 10.0.toBigDecimal())
        val invoiceItems = listOf(InvoiceItemModel(linearId = "ITEM-01", itemSubTotal = 100.0.toBigDecimal(), retentionAmount = 10.0.toBigDecimal()),
                InvoiceItemModel(linearId = "ITEM-02", itemSubTotal = 100.0.toBigDecimal(), retentionAmount = 10.0.toBigDecimal()))
        
        every { CalculateCashPerfAndWarranty["calculateSumOfItemAdvanceDeductAmountInSameHeader"](invoiceItems, itemAdvanceDeductAmounts) } returns 20.0.toBigDecimal()
        var result = callMethod<CalculateCashPerfAndWarranty, BigDecimal>(CalculateCashPerfAndWarranty, "calculateEstimatedInvoiceDeductibleAmount", invoiceItems, itemAdvanceDeductAmounts)
        assertEquals(160.0.toBigDecimal(), result)

        //case retentionAmount = null
        val invoiceItems2 = listOf(InvoiceItemModel(linearId = "ITEM-01", itemSubTotal = 100.0.toBigDecimal(), retentionAmount = null),
                InvoiceItemModel(linearId = "ITEM-02", itemSubTotal = 100.0.toBigDecimal(), retentionAmount = null))

        every { CalculateCashPerfAndWarranty["calculateSumOfItemAdvanceDeductAmountInSameHeader"](invoiceItems, itemAdvanceDeductAmounts) } returns 20.0.toBigDecimal()
        result = callMethod<CalculateCashPerfAndWarranty, BigDecimal>(CalculateCashPerfAndWarranty, "calculateEstimatedInvoiceDeductibleAmount", invoiceItems2, itemAdvanceDeductAmounts)
        assertEquals(180.0.toBigDecimal(), result)

        unmockkObject(CalculateCashPerfAndWarranty)
    }

    @Test
    fun `Test is get cash warranty from gr`() {
        val allGoodsReceiveds = listOf(GoodsReceivedModel(linearId = "GR-01", cashWarrantyDeductibleAmount = 100.0.toBigDecimal()))
        var linear = "GR-01"

        var result = callMethod<CalculateCashPerfAndWarranty, Boolean>(CalculateCashPerfAndWarranty, "isGetCashWarrantyFromGr", allGoodsReceiveds, linear)!!
        assertTrue(result)

        linear = "PO-01"
        result = callMethod<CalculateCashPerfAndWarranty, Boolean>(CalculateCashPerfAndWarranty, "isGetCashWarrantyFromGr", allGoodsReceiveds, linear)!!
        assertFalse(result)
    }

    @Test
    fun `Test is get cash perf guarantee from gr`() {
        val allGoodsReceiveds = listOf(GoodsReceivedModel(linearId = "GR-01", cashPerfGuaranteeDeductibleAmount = 100.0.toBigDecimal()))
        var linear = "GR-01"

        var result = callMethod<CalculateCashPerfAndWarranty, Boolean>(CalculateCashPerfAndWarranty, "isGetCashPerfGuaranteeFromGr", allGoodsReceiveds, linear)!!
        assertTrue(result)

        linear = "PO-01"
        result = callMethod<CalculateCashPerfAndWarranty, Boolean>(CalculateCashPerfAndWarranty, "isGetCashPerfGuaranteeFromGr", allGoodsReceiveds, linear)!!
        assertFalse(result)
    }

    @Test
    fun `Test group invoice item by linear header`() {
        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)

        val normalItems = listOf(InvoiceItemModel(linearId = "INV-ITEM-01"))
        var normalGoodsReceiveds = listOf(GoodsReceivedModel(linearId = "GR-01"))
        val purchaseOrders = listOf(PurchaseOrderModel(linearId = "PO-01"))

        every { CalculateCashPerfAndWarranty["groupInvoiceItemByGrLinearId"](normalGoodsReceiveds, normalItems) } returns mutableMapOf("GR-01" to normalItems)
        every { CalculateCashPerfAndWarranty["groupInvoiceItemByPoLinearId"](purchaseOrders, normalItems) } returns mutableMapOf("PO-01" to normalItems)

        var result = callMethod<CalculateCashPerfAndWarranty, Map<String, List<InvoiceItemModel>>>(CalculateCashPerfAndWarranty, "groupInvoiceItemByLinearHeader", normalItems, normalGoodsReceiveds, purchaseOrders)!!
        verify(exactly = 1) { CalculateCashPerfAndWarranty["groupInvoiceItemByGrLinearId"](normalGoodsReceiveds, normalItems) }
        assertEquals(mutableMapOf("GR-01" to normalItems), result)

        normalGoodsReceiveds = emptyList()
        result = callMethod<CalculateCashPerfAndWarranty, Map<String, List<InvoiceItemModel>>>(CalculateCashPerfAndWarranty, "groupInvoiceItemByLinearHeader", normalItems, normalGoodsReceiveds, purchaseOrders)!!
        verify(exactly = 1) { CalculateCashPerfAndWarranty["groupInvoiceItemByPoLinearId"](purchaseOrders, normalItems) }
        assertEquals(mutableMapOf("PO-01" to normalItems), result)

        unmockkObject(CalculateCashPerfAndWarranty)
    }

    @Test
    fun `Test getMapOfNormalItemWithDeductAmount`() {
        val normalInvoiceItem1 = InvoiceItemModel(linearId = "linearId1", purchaseOrderExternalId = "po1", itemSubTotal = 5.2.toBigDecimal())
        val normalInvoiceItem2 = InvoiceItemModel(linearId = "linearId2", purchaseOrderExternalId = "po1", itemSubTotal = 5.23.toBigDecimal())
        val normalInvoiceItem3 = InvoiceItemModel(linearId = "linearId3", purchaseOrderExternalId = "po2", itemSubTotal = 5.toBigDecimal())
        val normalInvoiceItems = listOf(normalInvoiceItem1, normalInvoiceItem2, normalInvoiceItem3)

        val deductInvoiceItem1 = InvoiceItemModel(linearId = "linearId4", purchaseOrderExternalId = "po1", itemSubTotal = 1.2.toBigDecimal())
        val deductInvoiceItem2 = InvoiceItemModel(linearId = "linearId5", purchaseOrderExternalId = "po1", itemSubTotal = 1.23.toBigDecimal())
        val deductInvoiceItem3 = InvoiceItemModel(linearId = "linearId6", purchaseOrderExternalId = "po2", itemSubTotal = 6.toBigDecimal())
        val deductInvoiceItems = listOf(deductInvoiceItem1, deductInvoiceItem2, deductInvoiceItem3)

        //case normal invoice item will not happen so not test

        //case deduct invoice item is empty
        var result = callMethod<CalculateCashPerfAndWarranty, Map<String, BigDecimal>>(CalculateCashPerfAndWarranty, "getMapOfNormalItemWithDeductAmount",
                normalInvoiceItems, emptyList<InvoiceItemModel>())
        assertTrue(result!!.isNotEmpty())
        assertEquals(mapOf("linearId1" to BigDecimal.ZERO, "linearId2" to BigDecimal.ZERO, "linearId3" to BigDecimal.ZERO), result)

        //case use same po external
        // linearId1: (5.2 * (1.2 + 1.23)) / (5.2+5.23) = (5.2 * 2.43)/10.43 = 1.21 roundup
        // linearId2: (5.23 * (1.2 + 1.23)) / (5.2+5.23) = (5.23 * 2.43)/10.43 = 1.22 roundup
        // linearId3: (5 * 6) / 5 = 6.0
        result = callMethod<CalculateCashPerfAndWarranty, Map<String, BigDecimal>>(CalculateCashPerfAndWarranty, "getMapOfNormalItemWithDeductAmount",
                normalInvoiceItems, deductInvoiceItems)
        assertTrue(result!!.isNotEmpty())
        assertEquals(mapOf(
                "linearId1" to 1.21.toBigDecimal(),
                "linearId2" to 1.22.toBigDecimal(),
                "linearId3" to 6.toBigDecimal().setScaleByCurrency(currency)), result)
    }

    @Test
    fun `Test update invoice item cash warranty by gr`() {
        mockkObject(CalculateCashPerfAndWarranty, CashPerfAndWarrantyValidator, recordPrivateCalls = true)

        val cashPerfGuaranteeAmount = 100.0.toBigDecimal()
        val invoiceItem = InvoiceItemModel(linearId = "INV-ITEM-01")
        val invoiceItems = listOf(invoiceItem)
        var relatedGoodsReceivedModel = GoodsReceivedModel(linearId = "GR-01")
        val mapOfNormalItemsWithDeductAmount = mutableMapOf("INV-ITEM-01" to 10.0.toBigDecimal())

        every { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) } returns 500.0.toBigDecimal()
        every { CashPerfAndWarrantyValidator.validateBeforeDeductByGr(400.0.toBigDecimal(), 100.0.toBigDecimal(), match { it.linearId == relatedGoodsReceivedModel.linearId }, invoiceItems) } just Runs
        every { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](invoiceItems, 100.0.toBigDecimal(), false, 400.0.toBigDecimal()) } returns listOf(invoiceItem.copy(cashWarrantyAmount = 100.0.toBigDecimal()))


        var result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashWarrantyByGr", relatedGoodsReceivedModel, invoiceItems, mapOfNormalItemsWithDeductAmount, cashPerfGuaranteeAmount)
        verify(exactly = 0) { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](any<List<InvoiceItemModel>>(), any<Map<String, BigDecimal>>()) }
        verify(exactly = 0) { CashPerfAndWarrantyValidator["validateBeforeDeductByGr"](any<BigDecimal>(), any<BigDecimal>(), any<GoodsReceivedModel>(), any<List<InvoiceItemModel>>()) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](any<List<InvoiceItemModel>>(), any<BigDecimal>(), any<Boolean>(), any<BigDecimal>()) }
        assertEquals(invoiceItems, result)

        clearMocks(CalculateCashPerfAndWarranty, CashPerfAndWarrantyValidator, answers = false)
        relatedGoodsReceivedModel = relatedGoodsReceivedModel.copy(cashWarrantyDeductibleRemainingAmount = 100.0.toBigDecimal())
        val expectedResult = invoiceItem.copy(cashWarrantyAmount = 100.0.toBigDecimal())
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashWarrantyByGr", relatedGoodsReceivedModel, invoiceItems, mapOfNormalItemsWithDeductAmount, cashPerfGuaranteeAmount)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) }
        verify(exactly = 1) { CashPerfAndWarrantyValidator["validateBeforeDeductByGr"](400.0.toBigDecimal(), 100.0.toBigDecimal(), relatedGoodsReceivedModel, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](invoiceItems, 100.0.toBigDecimal(), false, 400.0.toBigDecimal()) }
        assertEquals(listOf(expectedResult), result)

        clearMocks(CalculateCashPerfAndWarranty, CashPerfAndWarrantyValidator, answers = false)
        every { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) } returns 400.0.toBigDecimal()
        every { CashPerfAndWarrantyValidator.validateBeforeDeductByGr(300.0.toBigDecimal(), 100.0.toBigDecimal(), match { it.linearId == relatedGoodsReceivedModel.linearId }, invoiceItems) } throws IllegalArgumentException("Invoice amount is not enough to cover cash performance guarantee. Please contact buyer.")
        val result2 = Try.on { callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashWarrantyByGr", relatedGoodsReceivedModel, invoiceItems, mapOfNormalItemsWithDeductAmount, cashPerfGuaranteeAmount) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) }
        verify(exactly = 1) { CashPerfAndWarrantyValidator["validateBeforeDeductByGr"](300.0.toBigDecimal(), 100.0.toBigDecimal(), match<GoodsReceivedModel> { it.linearId == relatedGoodsReceivedModel.linearId }, invoiceItems) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](any<List<InvoiceItemModel>>(), any<BigDecimal>(), any<Boolean>(), any<BigDecimal>()) }
        assertTrue(result2.isFailure)
        assert(result2.toString().contains("Invoice amount is not enough to cover cash performance guarantee. Please contact buyer."))

        unmockkObject(CalculateCashPerfAndWarranty, CashPerfAndWarrantyValidator)
    }

    @Test
    fun `Test groupInvoiceItemBy Gr LinearId`() {
        val goodsReceivedItem1 = GoodsReceivedItemModel(linearId = "grItemLinearId1")
        val goodsReceivedItem2 = GoodsReceivedItemModel(linearId = "grItemLinearId2")
        val goodsReceivedItem3 = GoodsReceivedItemModel(linearId = "grItemLinearId3")
        val normalGoodsReceiveds = listOf(GoodsReceivedModel(linearId = "grHeader1", goodsReceivedItems = listOf(goodsReceivedItem1, goodsReceivedItem2)),
                GoodsReceivedModel(linearId = "grHeader2", goodsReceivedItems = listOf(goodsReceivedItem3)))

        val invoiceItem1 = InvoiceItemModel(linearId = "invoiceItemLinearId1", goodsReceivedItems = listOf(goodsReceivedItem1))
        val invoiceItem2 = InvoiceItemModel(linearId = "invoiceItemLinearId2", goodsReceivedItems = listOf(goodsReceivedItem1))
        val invoiceItem3 = InvoiceItemModel(linearId = "invoiceItemLinearId3", goodsReceivedItems = listOf(goodsReceivedItem2))
        val invoiceItem4 = InvoiceItemModel(linearId = "invoiceItemLinearId4", goodsReceivedItems = listOf(goodsReceivedItem3))

        val result = callMethod<CalculateCashPerfAndWarranty, Map<String, List<InvoiceItemModel>>>(CalculateCashPerfAndWarranty, "groupInvoiceItemByGrLinearId",
                normalGoodsReceiveds, listOf(invoiceItem1, invoiceItem2, invoiceItem3, invoiceItem4))
        val expect = mapOf("grHeader1" to listOf(invoiceItem1, invoiceItem2, invoiceItem3), "grHeader2" to listOf(invoiceItem4))
        assertEquals(expect, result)
    }

    @Test
    fun `Test groupInvoiceItemBy Po LinearId`() {
        val purchaseItem1 = PurchaseItemModel(linearId = "poItemLinearId1")
        val purchaseItem2 = PurchaseItemModel(linearId = "poItemLinearId2")
        val purchaseItem3 = PurchaseItemModel(linearId = "poItemLinearId3")
        val purchaseOrders = listOf(PurchaseOrderModel(linearId = "poHeader1", purchaseItems = listOf(purchaseItem1, purchaseItem2)),
                PurchaseOrderModel(linearId = "poHeader2", purchaseItems = listOf(purchaseItem3)))

        val invoiceItem1 = InvoiceItemModel(linearId = "invoiceItemLinearId1", purchaseItemLinearId = "poItemLinearId1")
        val invoiceItem2 = InvoiceItemModel(linearId = "invoiceItemLinearId2", purchaseItemLinearId = "poItemLinearId1")
        val invoiceItem3 = InvoiceItemModel(linearId = "invoiceItemLinearId3", purchaseItemLinearId = "poItemLinearId2")
        val invoiceItem4 = InvoiceItemModel(linearId = "invoiceItemLinearId4", purchaseItemLinearId = "poItemLinearId3")

        val result = callMethod<CalculateCashPerfAndWarranty, Map<String, List<InvoiceItemModel>>>(CalculateCashPerfAndWarranty, "groupInvoiceItemByPoLinearId",
                purchaseOrders, listOf(invoiceItem1, invoiceItem2, invoiceItem3, invoiceItem4))
        val expect = mapOf("poHeader1" to listOf(invoiceItem1, invoiceItem2, invoiceItem3), "poHeader2" to listOf(invoiceItem4))
        assertEquals(expect, result)
    }

    @Test
    fun `Test processCashPerfGuaranteeAmount`() {
        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)

        val mapOfNormalItemsWithDeductAmount = emptyMap<String, List<InvoiceItemModel>>()

        val purchaseOrders = listOf(PurchaseOrderModel(linearId = "poLinearId1"), PurchaseOrderModel(linearId = "poLinearId2"))

        //case  1 groupBy Po: use cashPerfGuaranteeFromPo
        val invoiceItem1 = InvoiceItemModel(linearId = "invoiceItemLinearId1", purchaseItemLinearId = "poItemLinearId1")
        val invoiceModel = InvoiceModel(invoiceItems = listOf(invoiceItem1))
        val groupDataByPo = mapOf("poLinearId1" to listOf(invoiceItem1))
        every { CalculateCashPerfAndWarranty["isGetCashPerfGuaranteeFromGr"](emptyList<GoodsReceivedModel>(), "poLinearId1") } returns false
        every { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "poLinearId1", listOf(invoiceItem1)) } returns PurchaseOrderModel(linearId = "poLinearId1")
        every {
            CalculateCashPerfAndWarranty["updateInvoiceItemCashPerfByPo"](PurchaseOrderModel(linearId = "poLinearId1"),
                    listOf(invoiceItem1), mapOfNormalItemsWithDeductAmount, BigDecimal.ZERO)
        } returns listOf(invoiceItem1)
        every { CalculateCashPerfAndWarranty["updateFinalCashPerfGuarantee"](false, listOf(invoiceItem1), invoiceModel) } returns invoiceModel.copy(cashPerfGuaranteeAmount = BigDecimal.ONE)

        var result = callMethod<CalculateCashPerfAndWarranty, InvoiceModel>(CalculateCashPerfAndWarranty, "processCashPerfGuaranteeAmount",
                invoiceModel, emptyList<GoodsReceivedModel>(), purchaseOrders, mapOfNormalItemsWithDeductAmount, groupDataByPo)
        assertNotNull(result)
        assertEquals(BigDecimal.ONE, result.cashPerfGuaranteeAmount)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["isGetCashPerfGuaranteeFromGr"](emptyList<GoodsReceivedModel>(), "poLinearId1") }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "poLinearId1", listOf(invoiceItem1)) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateInvoiceItemCashPerfByPo"](PurchaseOrderModel(linearId = "poLinearId1"), listOf(invoiceItem1), mapOfNormalItemsWithDeductAmount, BigDecimal.ZERO) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateFinalCashPerfGuarantee"](false, listOf(invoiceItem1), invoiceModel) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["updateInvoiceItemCashPerfByGr"](any<GoodsReceivedModel>(), any<List<InvoiceItemModel>>(), any<Map<String, BigDecimal>>()) }

        //case 2 groupBy Gr: use cashPerfGuaranteeFromGr
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        val goodsReceiveds = listOf(GoodsReceivedModel(linearId = "grLinearId1"))
        val invoiceItem2 = InvoiceItemModel(linearId = "invoiceItemLinearId2", goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "grItem1")))
        val invoiceModel2 = InvoiceModel(invoiceItems = listOf(invoiceItem2))
        val groupDataByGr = mapOf("grLinearId1" to listOf(invoiceItem2))
        every { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "grLinearId1", listOf(invoiceItem2)) } returns PurchaseOrderModel(linearId = "poLinearId1")
        every { CalculateCashPerfAndWarranty["isGetCashPerfGuaranteeFromGr"](goodsReceiveds, "grLinearId1") } returns true
        every {
            CalculateCashPerfAndWarranty["updateInvoiceItemCashPerfByGr"](GoodsReceivedModel(linearId = "grLinearId1"),
                    listOf(invoiceItem2), mapOfNormalItemsWithDeductAmount)
        } returns listOf(invoiceItem2)
        every { CalculateCashPerfAndWarranty["updateFinalCashPerfGuarantee"](true, listOf(invoiceItem2), invoiceModel2) } returns invoiceModel2.copy(cashPerfGuaranteeAmount = BigDecimal(2))
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        result = callMethod<CalculateCashPerfAndWarranty, InvoiceModel>(CalculateCashPerfAndWarranty, "processCashPerfGuaranteeAmount",
                invoiceModel2, goodsReceiveds, purchaseOrders, mapOfNormalItemsWithDeductAmount, groupDataByGr)
        assertNotNull(result)
        assertEquals(BigDecimal(2), result.cashPerfGuaranteeAmount)
        verify(exactly = 1) {  CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "grLinearId1", listOf(invoiceItem2)) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["isGetCashPerfGuaranteeFromGr"](goodsReceiveds, "grLinearId1") }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateInvoiceItemCashPerfByGr"](GoodsReceivedModel(linearId = "grLinearId1"), listOf(invoiceItem2), mapOfNormalItemsWithDeductAmount) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateFinalCashPerfGuarantee"](true, listOf(invoiceItem2), invoiceModel2) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["updateInvoiceItemCashPerfByPo"](any<PurchaseOrderModel>(), any<List<InvoiceItemModel>>(), any<Map<String, BigDecimal>>(), any<BigDecimal>()) }

        //case 3 groupBy PO: use cashPerfGuaranteeFromGr should failed
        val invoiceItem3 = InvoiceItemModel(linearId = "invoiceItemLinearId3")
        val groupDataByGr2 = mapOf("grLinearId2" to listOf(invoiceItem3))
        val invoiceModel3 = InvoiceModel(linearId = "invoiceLinearId3", invoiceItems = listOf(invoiceItem3))
        every { CalculateCashPerfAndWarranty["isGetCashPerfGuaranteeFromGr"](emptyList<GoodsReceivedModel>(), "grLinearId2") } returns true
        every { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](any<List<PurchaseOrderModel>>(), any<String>(), any<List<InvoiceItemModel>>()) } returns PurchaseOrderModel(linearId = "poLinearId2")
        every {
            CalculateCashPerfAndWarranty["updateInvoiceItemCashPerfByGr"](any<GoodsReceivedModel>(), any<List<InvoiceItemModel>>(), any<Map<String, BigDecimal>>())
        } returns listOf(invoiceItem3)
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        val expectedResult = Try.on {
            callMethod<CalculateCashPerfAndWarranty, InvoiceModel>(CalculateCashPerfAndWarranty, "processCashPerfGuaranteeAmount",
                    invoiceModel3, emptyList<GoodsReceivedModel>(), purchaseOrders, mapOfNormalItemsWithDeductAmount, groupDataByGr2)
        }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("Collection contains no element matching the predicate."))
        verify(exactly = 0) { CalculateCashPerfAndWarranty["updateInvoiceItemCashPerfByGr"](any<GoodsReceivedModel>(), any<List<InvoiceItemModel>>(), any<Map<String, BigDecimal>>()) }
        unmockkObject(CalculateCashPerfAndWarranty)
    }

    @Test
    fun `Test processCashWarrantyAmount`() {
        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)

        val mapOfNormalItemsWithDeductAmount = emptyMap<String, List<InvoiceItemModel>>()

        val purchaseOrders = listOf(PurchaseOrderModel(linearId = "poLinearId1"), PurchaseOrderModel(linearId = "poLinearId2"))

        //case  1 groupBy Po: use cashWarrantyFromPo
        val invoiceItem1 = InvoiceItemModel(linearId = "invoiceItemLinearId1", purchaseItemLinearId = "poItemLinearId1", cashPerfGuaranteeAmount = BigDecimal.ONE)
        val invoiceModel = InvoiceModel(invoiceItems = listOf(invoiceItem1))
        val groupDataByPo = mapOf("poLinearId1" to listOf(invoiceItem1))
        every { CalculateCashPerfAndWarranty["isGetCashWarrantyFromGr"](emptyList<GoodsReceivedModel>(), "poLinearId1") } returns false
        every { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "poLinearId1", listOf(invoiceItem1)) } returns PurchaseOrderModel(linearId = "poLinearId1")
        every {
            CalculateCashPerfAndWarranty["updateInvoiceItemCashWarrantyByPo"](PurchaseOrderModel(linearId = "poLinearId1"), listOf(invoiceItem1), BigDecimal.ZERO)
        } returns listOf(invoiceItem1)
        every { CalculateCashPerfAndWarranty["updateFinalCashWarranty"](false, listOf(invoiceItem1), invoiceModel) } returns invoiceModel.copy(cashWarrantyAmount = BigDecimal.ONE)
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        var result = callMethod<CalculateCashPerfAndWarranty, InvoiceModel>(CalculateCashPerfAndWarranty, "processCashWarrantyAmount",
                invoiceModel, emptyList<GoodsReceivedModel>(), purchaseOrders, mapOfNormalItemsWithDeductAmount, groupDataByPo)
        assertNotNull(result)
        assertEquals(BigDecimal.ONE, result.cashWarrantyAmount)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["isGetCashWarrantyFromGr"](emptyList<GoodsReceivedModel>(), "poLinearId1") }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "poLinearId1", listOf(invoiceItem1)) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateInvoiceItemCashWarrantyByPo"](PurchaseOrderModel(linearId = "poLinearId1"), listOf(invoiceItem1), BigDecimal.ZERO) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateFinalCashWarranty"](false, listOf(invoiceItem1), invoiceModel) }

        //case 1 groupBy Gr: use cashWarrantyFromGr
        val goodsReceiveds = listOf(GoodsReceivedModel(linearId = "grLinearId1"))
        val invoiceItem2 = InvoiceItemModel(linearId = "invoiceItemLinearId2", goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "grItem1")), cashPerfGuaranteeAmount = BigDecimal.ONE)
        val invoiceModel2 = InvoiceModel(invoiceItems = listOf(invoiceItem2))
        val groupDataByGr = mapOf("grLinearId1" to listOf(invoiceItem2))
        every { CalculateCashPerfAndWarranty["isGetCashWarrantyFromGr"](goodsReceiveds, "grLinearId1") } returns true
        every { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "grLinearId1", listOf(invoiceItem2)) } returns PurchaseOrderModel(linearId = "poLinearId1")
        every {
            CalculateCashPerfAndWarranty["updateInvoiceItemCashWarrantyByGr"](GoodsReceivedModel(linearId = "grLinearId1"),
                    listOf(invoiceItem2), mapOfNormalItemsWithDeductAmount, BigDecimal.ONE)
        } returns listOf(invoiceItem2)
        every { CalculateCashPerfAndWarranty["updateFinalCashWarranty"](true, listOf(invoiceItem2), invoiceModel2) } returns invoiceModel2.copy(cashWarrantyAmount = BigDecimal(2))
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        result = callMethod<CalculateCashPerfAndWarranty, InvoiceModel>(CalculateCashPerfAndWarranty, "processCashWarrantyAmount",
                invoiceModel2, goodsReceiveds, purchaseOrders, mapOfNormalItemsWithDeductAmount, groupDataByGr)
        assertNotNull(result)
        assertEquals(BigDecimal(2), result.cashWarrantyAmount)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["isGetCashWarrantyFromGr"](goodsReceiveds, "grLinearId1") }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "grLinearId1", listOf(invoiceItem2)) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateInvoiceItemCashWarrantyByGr"](GoodsReceivedModel(linearId = "grLinearId1"), listOf(invoiceItem2), mapOfNormalItemsWithDeductAmount, BigDecimal.ONE) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateFinalCashWarranty"](true, listOf(invoiceItem2), invoiceModel2) }

        //case 1 groupBy Gr: use cashWarrantyFromPo
        val invoiceItem3 = InvoiceItemModel(linearId = "invoiceItemLinearId3", cashPerfGuaranteeAmount = BigDecimal.ONE)
        val groupDataByGr2 = mapOf("grLinearId2" to listOf(invoiceItem3))
        val invoiceModel3 = InvoiceModel(linearId = "invoiceLinearId3", invoiceItems = listOf(invoiceItem3))
        every { CalculateCashPerfAndWarranty["isGetCashWarrantyFromGr"](goodsReceiveds, "grLinearId2") } returns false
        every { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "grLinearId2", listOf(invoiceItem3)) } returns PurchaseOrderModel(linearId = "poLinearId2")
        every { CalculateCashPerfAndWarranty["updateInvoiceItemCashWarrantyByPo"](PurchaseOrderModel(linearId = "poLinearId2"), listOf(invoiceItem3), BigDecimal.ZERO) } returns listOf(invoiceItem3)
        every { CalculateCashPerfAndWarranty["updateFinalCashWarranty"](false, listOf(invoiceItem3), invoiceModel3) } returns invoiceModel3.copy(cashWarrantyAmount = BigDecimal(3))
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        result = callMethod<CalculateCashPerfAndWarranty, InvoiceModel>(CalculateCashPerfAndWarranty, "processCashWarrantyAmount",
                invoiceModel3, goodsReceiveds, purchaseOrders, mapOfNormalItemsWithDeductAmount, groupDataByGr2)
        assertNotNull(result)
        assertEquals(BigDecimal(3), result.cashWarrantyAmount)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["isGetCashWarrantyFromGr"](goodsReceiveds, "grLinearId2") }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "grLinearId2", listOf(invoiceItem3)) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateInvoiceItemCashWarrantyByPo"](PurchaseOrderModel(linearId = "poLinearId2"), listOf(invoiceItem3), BigDecimal.ZERO) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateFinalCashWarranty"](false, listOf(invoiceItem3), invoiceModel3) }

        //case  1 groupBy Gr: use cashPerfGuaranteeFromPo, 1 groupBy Gr: use cashPerfGuaranteeFromGr
        val invoiceModel4 = InvoiceModel(linearId = "invoiceLinearId3", invoiceItems = listOf(invoiceItem2, invoiceItem3))
        val groupDataByGr3 = mapOf("grLinearId1" to listOf(invoiceItem2), "grLinearId2" to listOf(invoiceItem3))
        every { CalculateCashPerfAndWarranty["updateFinalCashWarranty"](true, listOf(invoiceItem2, invoiceItem3), invoiceModel4) } returns invoiceModel4.copy(cashWarrantyAmount = BigDecimal(4))
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        result = callMethod<CalculateCashPerfAndWarranty, InvoiceModel>(CalculateCashPerfAndWarranty, "processCashWarrantyAmount",
                invoiceModel4, goodsReceiveds, purchaseOrders, mapOfNormalItemsWithDeductAmount, groupDataByGr3)
        assertNotNull(result)
        assertEquals(BigDecimal(4), result.cashWarrantyAmount)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["isGetCashWarrantyFromGr"](goodsReceiveds, "grLinearId1") }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateInvoiceItemCashWarrantyByGr"](GoodsReceivedModel(linearId = "grLinearId1"), listOf(invoiceItem2), mapOfNormalItemsWithDeductAmount, BigDecimal.ONE) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["isGetCashWarrantyFromGr"](goodsReceiveds, "grLinearId2") }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["getPurchaseOrderModel"](purchaseOrders, "grLinearId2", listOf(invoiceItem3)) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateInvoiceItemCashWarrantyByPo"](PurchaseOrderModel(linearId = "poLinearId2"), listOf(invoiceItem3), BigDecimal.ZERO) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateFinalCashWarranty"](true, listOf(invoiceItem2, invoiceItem3), invoiceModel4) }
        
        unmockkObject(CalculateCashPerfAndWarranty)
    }

    @Test
    fun `Test update invoice item cash warranty by po`() {
        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)
        val accCashWarranty = BigDecimal.ZERO
        val invoiceItem = InvoiceItemModel(linearId = "INV-ITEM-01")
        val invoiceItems = listOf(invoiceItem)
        var relatedPurchaseOrderModel = PurchaseOrderModel(linearId = "GR-01", cashWarrantyRemainingAmount = 200.0.toBigDecimal(), remainingTotal = 500.0.toBigDecimal())

        every { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](invoiceItems, 600.0.toBigDecimal(), false, any<BigDecimal>()) } returns listOf(invoiceItem.copy(cashWarrantyAmount = 100.0.toBigDecimal()))
        var result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashWarrantyByPo", relatedPurchaseOrderModel, invoiceItems, accCashWarranty)
        verify(exactly = 0) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](any<List<InvoiceItemModel>>(), any<BigDecimal>(), any<Boolean>(), any<BigDecimal>()) }
        assertEquals(invoiceItems, result)

        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        relatedPurchaseOrderModel = relatedPurchaseOrderModel.copy(cashWarrantyRemainingAmount = 1000.0.toBigDecimal(), remainingTotal = 400.0.toBigDecimal())
        val expectedResult = invoiceItem.copy(cashWarrantyAmount = 100.0.toBigDecimal())
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashWarrantyByPo", relatedPurchaseOrderModel, invoiceItems, accCashWarranty)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](invoiceItems, 600.0.toBigDecimal(), false, any<BigDecimal>()) }
        assertEquals(listOf(expectedResult), result)

        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        relatedPurchaseOrderModel = relatedPurchaseOrderModel.copy(cashWarrantyRemainingAmount = 1000.0.toBigDecimal(), remainingTotal = 400.0.toBigDecimal())
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashWarrantyByPo", relatedPurchaseOrderModel, invoiceItems, 601.0.toBigDecimal())
        verify(exactly = 0) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](invoiceItems, any<BigDecimal>(), false, any<BigDecimal>()) }
        assertEquals(invoiceItems, result)

        unmockkObject(CalculateCashPerfAndWarranty)
    }

    @Test
    fun `Test updateFinalCashPerfGuarantee`() {
        val invoiceItemWithCashPerf1 = InvoiceItemModel(linearId = "linearItemId1", cashPerfGuaranteeAmount = BigDecimal(5), retentionAmount = BigDecimal(2))
        val invoiceItemWithCashPerf2 = InvoiceItemModel(linearId = "linearItemId2", cashPerfGuaranteeAmount = BigDecimal(15), retentionAmount = BigDecimal(4))
        val invoiceItem = InvoiceItemModel(linearId = "linearItemId3")

        val invoiceModel = InvoiceModel(invoiceItems = listOf(invoiceItemWithCashPerf1.copy(cashPerfGuaranteeAmount = null), invoiceItemWithCashPerf2.copy(cashPerfGuaranteeAmount = null), invoiceItem))
        val result = callMethod<CalculateCashPerfAndWarranty, InvoiceModel>(CalculateCashPerfAndWarranty, "updateFinalCashPerfGuarantee",
                false, listOf(invoiceItemWithCashPerf1, invoiceItemWithCashPerf2), invoiceModel)
        assertNotNull(result)
        assertEquals(BigDecimal(20), result.cashPerfGuaranteeAmount)
        assertEquals(false, result.cashPerfGuaranteeFromGr)
        assertEquals(3, result.invoiceItems.size)
        assertEquals(BigDecimal(5), result.invoiceItems.find { it.linearId == "linearItemId1" }!!.cashPerfGuaranteeAmount)
        assertEquals(BigDecimal(2), result.invoiceItems.find { it.linearId == "linearItemId1" }!!.retentionAmount)
        assertEquals(BigDecimal(15), result.invoiceItems.find { it.linearId == "linearItemId2" }!!.cashPerfGuaranteeAmount)
        assertEquals(BigDecimal(4), result.invoiceItems.find { it.linearId == "linearItemId2" }!!.retentionAmount)

    }


    @Test
    fun `Test updateFinalCashWarranty`() {
        val invoiceItemWithCashWarranty1 = InvoiceItemModel(linearId = "linearItemId1", cashWarrantyAmount = BigDecimal(5), retentionAmount = BigDecimal(2))
        val invoiceItemWithCashWarranty2 = InvoiceItemModel(linearId = "linearItemId2", cashWarrantyAmount = BigDecimal(15), retentionAmount = BigDecimal(4))
        val invoiceItem = InvoiceItemModel(linearId = "linearItemId3")

        val invoiceModel = InvoiceModel(invoiceItems = listOf(invoiceItemWithCashWarranty1.copy(cashWarrantyAmount = null), invoiceItemWithCashWarranty2.copy(cashWarrantyAmount = null), invoiceItem))
        val result = callMethod<CalculateCashPerfAndWarranty, InvoiceModel>(CalculateCashPerfAndWarranty, "updateFinalCashWarranty",
                false, listOf(invoiceItemWithCashWarranty1, invoiceItemWithCashWarranty2), invoiceModel)
        assertNotNull(result)
        assertEquals(BigDecimal(20), result.cashWarrantyAmount)
        assertEquals(false, result.cashWarrantyFromGr)
        assertEquals(3, result.invoiceItems.size)
        assertEquals(BigDecimal(5), result.invoiceItems.find { it.linearId == "linearItemId1" }!!.cashWarrantyAmount)
        assertEquals(BigDecimal(2), result.invoiceItems.find { it.linearId == "linearItemId1" }!!.retentionAmount)
        assertEquals(BigDecimal(15), result.invoiceItems.find { it.linearId == "linearItemId2" }!!.cashWarrantyAmount)
        assertEquals(BigDecimal(4), result.invoiceItems.find { it.linearId == "linearItemId2" }!!.retentionAmount)
    }

    @Test
    fun `Test getPurchaseOrderModel`() {
        val purchaseOrder = listOf(PurchaseOrderModel(linearId = "linearId"), PurchaseOrderModel(linearId = "xxxx", purchaseOrderNumber = "purchaseOrderExternalId"))

        //case found purchase from linearId
        var result = callMethod<CalculateCashPerfAndWarranty, PurchaseOrderModel>(CalculateCashPerfAndWarranty, "getPurchaseOrderModel",
                purchaseOrder, "linearId", listOf(InvoiceItemModel()))
        assertEquals(PurchaseOrderModel(linearId = "linearId"), result)

        //case found purchase from externalId
        result = callMethod<CalculateCashPerfAndWarranty, PurchaseOrderModel>(CalculateCashPerfAndWarranty, "getPurchaseOrderModel",
                purchaseOrder, "linearId2", listOf(InvoiceItemModel(purchaseOrderExternalId = "purchaseOrderExternalId")))
        assertEquals(PurchaseOrderModel(linearId = "xxxx", purchaseOrderNumber = "purchaseOrderExternalId"), result)
    }

    @Test
    fun `Test update invoice item cash perf by gr`() {
        mockkObject(CalculateCashPerfAndWarranty, CashPerfAndWarrantyValidator, recordPrivateCalls = true)
        val invoiceItem = InvoiceItemModel(linearId = "INV-ITEM-01")
        val invoiceItems = listOf(invoiceItem)
        var relatedGoodsReceivedModel = GoodsReceivedModel(linearId = "GR-01")
        val mapOfNormalItemsWithDeductAmount = mutableMapOf("INV-ITEM-01" to 10.0.toBigDecimal())

        every { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) } returns 500.0.toBigDecimal()
        every { CashPerfAndWarrantyValidator.validateBeforeDeductByGr(500.0.toBigDecimal(), 100.0.toBigDecimal(), match { it.linearId == relatedGoodsReceivedModel.linearId }, invoiceItems) } just Runs
        every { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](invoiceItems, 100.0.toBigDecimal(), true, 500.0.toBigDecimal()) } returns listOf(invoiceItem.copy(cashWarrantyAmount = 100.0.toBigDecimal()))


        var result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashPerfByGr", relatedGoodsReceivedModel, invoiceItems, mapOfNormalItemsWithDeductAmount)
        verify(exactly = 0) { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](any<List<InvoiceItemModel>>(), any<Map<String, BigDecimal>>()) }
        verify(exactly = 0) { CashPerfAndWarrantyValidator["validateBeforeDeductByGr"](any<BigDecimal>(), any<BigDecimal>(), any<GoodsReceivedModel>(), any<List<InvoiceItemModel>>()) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](any<List<InvoiceItemModel>>(), any<BigDecimal>(), any<Boolean>(), any<BigDecimal>()) }
        assertEquals(invoiceItems, result)

        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        relatedGoodsReceivedModel = relatedGoodsReceivedModel.copy(cashPerfDeductibleRemainingAmount = 100.0.toBigDecimal())
        val expectedResult = invoiceItem.copy(cashWarrantyAmount = 100.0.toBigDecimal())
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashPerfByGr", relatedGoodsReceivedModel, invoiceItems, mapOfNormalItemsWithDeductAmount)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) }
        verify(exactly = 1) { CashPerfAndWarrantyValidator["validateBeforeDeductByGr"](500.0.toBigDecimal(), 100.0.toBigDecimal(), relatedGoodsReceivedModel, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](invoiceItems, 100.0.toBigDecimal(), true, 500.0.toBigDecimal()) }
        assertEquals(listOf(expectedResult), result)

        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        every { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) } returns 400.0.toBigDecimal()
        every { CashPerfAndWarrantyValidator.validateBeforeDeductByGr(400.0.toBigDecimal(), 100.0.toBigDecimal(), match { it.linearId == relatedGoodsReceivedModel.linearId }, invoiceItems) } throws IllegalArgumentException("Invoice amount is not enough to cover cash performance guarantee. Please contact buyer.")
        val result2 = Try.on { callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashPerfByGr", relatedGoodsReceivedModel, invoiceItems, mapOfNormalItemsWithDeductAmount) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) }
        verify(exactly = 1) { CashPerfAndWarrantyValidator["validateBeforeDeductByGr"](400.0.toBigDecimal(), 100.0.toBigDecimal(), match<GoodsReceivedModel> { it.linearId == relatedGoodsReceivedModel.linearId }, invoiceItems) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](any<List<InvoiceItemModel>>(), any<BigDecimal>(), any<Boolean>(), any<BigDecimal>()) }
        assertTrue(result2.isFailure)
        assert(result2.toString().contains("Invoice amount is not enough to cover cash performance guarantee. Please contact buyer."))

        unmockkObject(CalculateCashPerfAndWarranty, CashPerfAndWarrantyValidator)
    }

    @Test
    fun `Test update invoice item cash perf by po`() {
        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)
        val invoiceItem = InvoiceItemModel(linearId = "INV-ITEM-01")
        val invoiceItems = listOf(invoiceItem)
        var relatedPurchaseOrderModel = PurchaseOrderModel(linearId = "PO-01")
        val accCashPerf = BigDecimal.ZERO
        val mapOfNormalItemsWithDeductAmount = mutableMapOf("INV-ITEM-01" to 10.0.toBigDecimal())

        every { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) } returns 500.0.toBigDecimal()
        every { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](invoiceItems, 100.0.toBigDecimal(), true, 500.0.toBigDecimal()) } returns listOf(invoiceItem.copy(cashWarrantyAmount = 100.0.toBigDecimal()))


        var result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashPerfByPo", relatedPurchaseOrderModel, invoiceItems, mapOfNormalItemsWithDeductAmount, accCashPerf)
        verify(exactly = 0) { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](any<List<InvoiceItemModel>>(), any<Map<String, BigDecimal>>()) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](any<List<InvoiceItemModel>>(), any<BigDecimal>(), any<Boolean>(), any<BigDecimal>()) }
        assertEquals(invoiceItems, result)

        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        relatedPurchaseOrderModel = relatedPurchaseOrderModel.copy(cashPerfGuaranteeRemainingAmount = 100.0.toBigDecimal())
        val expectedResult = invoiceItem.copy(cashWarrantyAmount = 100.0.toBigDecimal())
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashPerfByPo", relatedPurchaseOrderModel, invoiceItems, mapOfNormalItemsWithDeductAmount, accCashPerf)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](invoiceItems, mapOfNormalItemsWithDeductAmount) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](invoiceItems, 100.0.toBigDecimal(), true, 500.0.toBigDecimal()) }
        assertEquals(listOf(expectedResult), result)

        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "updateInvoiceItemCashPerfByPo", relatedPurchaseOrderModel, invoiceItems, mapOfNormalItemsWithDeductAmount, 101.0.toBigDecimal())
        verify(exactly = 0) { CalculateCashPerfAndWarranty["calculateEstimatedInvoiceDeductibleAmount"](any<List<InvoiceItemModel>>(), any<Map<String, BigDecimal>>()) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItem"](any<List<InvoiceItemModel>>(), any<BigDecimal>(), any<Boolean>(), any<BigDecimal>()) }
        assertEquals(invoiceItems, result)

        unmockkObject(CalculateCashPerfAndWarranty)

    }

    @Test
    fun `Test distribute deductible amount to item process`() {
        
        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)

        val isCashPerfGuarantee = true
        val deductibleAmount = 50.0.toBigDecimal()
        var invoiceItem1 = InvoiceItemModel(linearId = "ITEM-01", itemSubTotal = 50.0.toBigDecimal())
        var invoiceItem2 = InvoiceItemModel(linearId = "ITEM-02", itemSubTotal = 20.0.toBigDecimal())

        var normalItems = listOf(invoiceItem1, invoiceItem2)
        every { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem1, 50.0.toBigDecimal()) } returns invoiceItem1.copy(cashPerfGuaranteeAmount = 50.0.toBigDecimal())
        every { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem2, 0.0.toBigDecimal()) } returns invoiceItem2.copy(cashPerfGuaranteeAmount = BigDecimal.ZERO)

        // first item is enough to cover deductibleAmount
        var result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "distributeDeductibleAmountToItemProcess", normalItems, deductibleAmount, isCashPerfGuarantee)!!
        var expectedResult = listOf(invoiceItem1.copy(cashPerfGuaranteeAmount = 50.0.toBigDecimal()), invoiceItem2.copy(cashPerfGuaranteeAmount = BigDecimal.ZERO))
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem1, 50.0.toBigDecimal()) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem2, 0.0.toBigDecimal()) }
        assertEquals(expectedResult, result)

        // second item is enough to cover deductibleAmount
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        invoiceItem1 = InvoiceItemModel(linearId = "ITEM-01", itemSubTotal = 20.0.toBigDecimal())
        invoiceItem2 = InvoiceItemModel(linearId = "ITEM-02", itemSubTotal = 30.0.toBigDecimal())
        normalItems = listOf(invoiceItem1, invoiceItem2)
        every { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem1, 20.0.toBigDecimal()) } returns invoiceItem1.copy(cashPerfGuaranteeAmount = 20.0.toBigDecimal())
        every { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem2, 30.0.toBigDecimal()) } returns invoiceItem2.copy(cashPerfGuaranteeAmount = 30.0.toBigDecimal())
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "distributeDeductibleAmountToItemProcess", normalItems, deductibleAmount, isCashPerfGuarantee)!!
        expectedResult = listOf(invoiceItem1.copy(cashPerfGuaranteeAmount = 20.0.toBigDecimal()), invoiceItem2.copy(cashPerfGuaranteeAmount = 30.0.toBigDecimal()))
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem1, 20.0.toBigDecimal()) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem2, 30.0.toBigDecimal()) }
        assertEquals(expectedResult, result)

        // both item are not enough to cover deductibleAmount
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        invoiceItem1 = InvoiceItemModel(linearId = "ITEM-01", itemSubTotal = 20.0.toBigDecimal())
        invoiceItem2 = InvoiceItemModel(linearId = "ITEM-02", itemSubTotal = 20.0.toBigDecimal())
        normalItems = listOf(invoiceItem1, invoiceItem2)
        every { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem1, 20.0.toBigDecimal()) } returns invoiceItem1.copy(cashPerfGuaranteeAmount = 20.0.toBigDecimal())
        every { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem2, 20.0.toBigDecimal()) } returns invoiceItem2.copy(cashPerfGuaranteeAmount = 20.0.toBigDecimal())
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "distributeDeductibleAmountToItemProcess", normalItems,deductibleAmount, isCashPerfGuarantee)!!
        expectedResult = listOf(invoiceItem1.copy(cashPerfGuaranteeAmount = 20.0.toBigDecimal()), invoiceItem2.copy(cashPerfGuaranteeAmount = 20.0.toBigDecimal()))
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem1, 20.0.toBigDecimal()) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateCashPerfOrWarrantyInItem"](isCashPerfGuarantee, invoiceItem2, 20.0.toBigDecimal()) }
        assertEquals(expectedResult, result)
        
        unmockkObject(CalculateCashPerfAndWarranty)

    }


    @Test
    fun `Test distribute deductible amount to item`() {
        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)

        val isCashPerfGuarantee = true
        val invoiceItem = InvoiceItemModel(linearId = "ITEM-01")
        val normalItems = listOf(invoiceItem)
        var deductibleAmount = BigDecimal(100)
        var estimatedInvoice = BigDecimal(50)

        //estimatedInvoice < deductibleAmount
        var expectedResult = listOf(invoiceItem.copy(cashPerfGuaranteeAmount = BigDecimal(50)))
        every { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItemProcess"](normalItems, estimatedInvoice, isCashPerfGuarantee) } returns expectedResult
        var result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "distributeDeductibleAmountToItem", normalItems, deductibleAmount, isCashPerfGuarantee, estimatedInvoice)!!
        verify(exactly = 1) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItemProcess"](normalItems, estimatedInvoice, isCashPerfGuarantee) }
        assertEquals(expectedResult, result)

        //deductibleAmount < estimatedInvoice
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        deductibleAmount = BigDecimal(55)
        estimatedInvoice = BigDecimal(111)
        expectedResult = listOf(invoiceItem.copy(cashPerfGuaranteeAmount = BigDecimal(55)))
        every { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItemProcess"](normalItems, deductibleAmount, isCashPerfGuarantee) } returns expectedResult
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "distributeDeductibleAmountToItem", normalItems, deductibleAmount, isCashPerfGuarantee, estimatedInvoice)!!
        verify(exactly = 1) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItemProcess"](normalItems, deductibleAmount, isCashPerfGuarantee) }
        assertEquals(expectedResult, result)

        //estimatedInvoice is null
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        deductibleAmount = BigDecimal(60)
        expectedResult = listOf(invoiceItem.copy(cashPerfGuaranteeAmount = BigDecimal(60)))
        every { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItemProcess"](normalItems, deductibleAmount, isCashPerfGuarantee) } returns expectedResult
        result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(CalculateCashPerfAndWarranty, "distributeDeductibleAmountToItem", normalItems, deductibleAmount, isCashPerfGuarantee, null)!!
        verify(exactly = 1) { CalculateCashPerfAndWarranty["distributeDeductibleAmountToItemProcess"](normalItems, deductibleAmount, isCashPerfGuarantee) }
        assertEquals(expectedResult, result)

        unmockkObject(CalculateCashPerfAndWarranty)
    }

    @Test
    fun `Test process`() {
        mockkObject(CalculateCashPerfAndWarranty, CashPerfAndWarrantyValidator, recordPrivateCalls = true)

        val normalItem = InvoiceItemModel(linearId = "INV-ITEM-01", itemCategory = ItemCategory.Invoice.NORMAL.name)
        val advanceItem = InvoiceItemModel(linearId = "INV-ITEM-02", itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name)
        val invoiceModel = InvoiceModel(linearId = "INV-01", invoiceItems = listOf(normalItem, advanceItem))
        val goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "GR-ITEM-01", movementClass = MovementClass.NORMAL.name))
        var goodsReceivedModel = GoodsReceivedModel(linearId = "GR-01", goodsReceivedItems = goodsReceivedItems)
        val purchaseOrders = listOf(PurchaseOrderModel(linearId = "PO-01"))

        every { CashPerfAndWarrantyValidator.inspectAfterFetchData(listOf(goodsReceivedModel)) } just Runs
        every { CalculateCashPerfAndWarranty["getMapOfNormalItemWithDeductAmount"](listOf(normalItem), listOf(advanceItem)) } returns mutableMapOf("linear" to 100.0.toBigDecimal())
        every { CalculateCashPerfAndWarranty["prepareGoodsReceived"](listOf(goodsReceivedModel), emptyList<GoodsReceivedModel>()) } returns listOf(goodsReceivedModel)
        every { CalculateCashPerfAndWarranty["groupInvoiceItemByLinearHeader"](listOf(normalItem), listOf(goodsReceivedModel), purchaseOrders) } returns mutableMapOf("linear" to listOf(normalItem))

        val itemUpdatedCashPerf = invoiceModel.invoiceItems.map {
            when (it.linearId == "INV-ITEM-01") {
                true -> it.copy(cashPerfGuaranteeAmount = 10.0.toBigDecimal())
                false -> it
            }
        }

        every { CalculateCashPerfAndWarranty["processCashPerfGuaranteeAmount"](invoiceModel, listOf(goodsReceivedModel), purchaseOrders, match<Map<String, BigDecimal>> { it["linear"] == 100.0.toBigDecimal() }, match<Map<String, List<InvoiceItemModel>>> { it["linear"] == listOf(normalItem) }) } returns invoiceModel.copy(invoiceItems = itemUpdatedCashPerf)
        val itemUpdatedCashPerfAndCashWarranty = invoiceModel.invoiceItems.map {
            when (it.linearId == "INV-ITEM-01") {
                true -> it.copy(cashPerfGuaranteeAmount = 10.0.toBigDecimal(),
                        cashWarrantyAmount = 10.0.toBigDecimal())
                false -> it
            }
        }
        every { CalculateCashPerfAndWarranty["updatePurchasePreWarrantyCalculation"](purchaseOrders,
                match<List<InvoiceItemModel>> { itemUpdatedCashPerf.map { i -> i.linearId }.containsAll(it.map { i -> i.linearId }) },
                match<Map<String, BigDecimal>> { it["linear"] == 100.0.toBigDecimal() }) } returns purchaseOrders
        every { CalculateCashPerfAndWarranty["groupInvoiceItemByLinearHeader"](
                match<List<InvoiceItemModel>> { itemUpdatedCashPerf.map { i -> i.linearId }.containsAll(it.map { i -> i.linearId }) },
                listOf(goodsReceivedModel),
                purchaseOrders) } returns mutableMapOf("linear" to listOf(normalItem))
        every { CalculateCashPerfAndWarranty["processCashWarrantyAmount"](
                invoiceModel.copy(invoiceItems = itemUpdatedCashPerf),
                listOf(goodsReceivedModel),
                purchaseOrders,
                match<Map<String, BigDecimal>> { it["linear"] == 100.0.toBigDecimal() },
                match<Map<String, List<InvoiceItemModel>>> { it["linear"] == listOf(normalItem) }) } returns invoiceModel.copy(invoiceItems = itemUpdatedCashPerfAndCashWarranty)
        val restoredGoodsReceiveds = emptyList<GoodsReceivedModel>()
        val result = CalculateCashPerfAndWarranty.process(invoiceModel, goodsReceivedItems, purchaseOrders, listOf(goodsReceivedModel), restoredGoodsReceiveds)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["getMapOfNormalItemWithDeductAmount"](listOf(normalItem), listOf(advanceItem)) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["prepareGoodsReceived"](listOf(goodsReceivedModel), emptyList<GoodsReceivedModel>()) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["groupInvoiceItemByLinearHeader"](listOf(normalItem), listOf(goodsReceivedModel), purchaseOrders) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["processCashPerfGuaranteeAmount"](invoiceModel, listOf(goodsReceivedModel), purchaseOrders, match<Map<String, BigDecimal>> { it["linear"] == 100.0.toBigDecimal() }, match<Map<String, List<InvoiceItemModel>>> { it["linear"] == listOf(normalItem) }) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["processCashWarrantyAmount"](invoiceModel.copy(invoiceItems = itemUpdatedCashPerf), listOf(goodsReceivedModel), purchaseOrders, match<Map<String, BigDecimal>> { it["linear"] == 100.0.toBigDecimal() }, match<Map<String, List<InvoiceItemModel>>> { it["linear"] == listOf(normalItem) }) }
        verify(exactly = 1) { CashPerfAndWarrantyValidator["inspectAfterFetchData"](listOf(goodsReceivedModel)) }
        val expectedResult = invoiceModel.copy(invoiceItems = itemUpdatedCashPerfAndCashWarranty)
        assertEquals(expectedResult, result)

        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        val reverseGr = listOf(GoodsReceivedItemModel(linearId = "GR-ITEM-1", movementClass = MovementClass.REVERSE.name))
        goodsReceivedModel = goodsReceivedModel.copy(goodsReceivedItems = reverseGr)
        every { CalculateCashPerfAndWarranty["prepareGoodsReceived"](listOf(goodsReceivedModel), emptyList<GoodsReceivedModel>()) } returns listOf(goodsReceivedModel)
        every { CashPerfAndWarrantyValidator.inspectAfterFetchData(listOf(goodsReceivedModel)) } throws IllegalArgumentException("Good received item's movement class must be normal only.")
        val result2 = Try.on { CalculateCashPerfAndWarranty.process(invoiceModel, goodsReceivedItems, purchaseOrders, listOf(goodsReceivedModel), restoredGoodsReceiveds) }
        assertTrue(result2.isFailure)
        assert(result2.toString().contains("Good received item's movement class must be normal only."))

        unmockkObject(CalculateCashPerfAndWarranty, CashPerfAndWarrantyValidator)
    }

    @Test
    fun `Test calculatePoRemainingAmountAfterDepleteInvoice`() {
        val purchaseItemsModel1 = PurchaseItemModel(
                linearId = "PO-ITEM-01",
                quantity = Quantity(initial = BigDecimal(10), remaining = BigDecimal(5), consumed = BigDecimal(5), unit = "BAG"),
                poItemUnitPrice = BigDecimal(100),
                itemCategory = ItemCategory.Purchase.NORMAL.name)
        val purchaseItemsModel2 = PurchaseItemModel(
                linearId = "PO-ITEM-02",
                poItemNo = "2",
                poNumber = "PO-2",
                quantity = Quantity(initial = BigDecimal(10), remaining = BigDecimal(5), consumed = BigDecimal(5), unit = "BAG"),
                poItemUnitPrice = BigDecimal(100),
                itemCategory = ItemCategory.Purchase.NORMAL.name)
        val advanceItemsModel = PurchaseItemModel(
                linearId = "PO-ITEM-03-A",
                poItemNo = "3",
                poNumber = "PO-2",
                quantity = Quantity(initial = BigDecimal(10), remaining = BigDecimal(5), consumed = BigDecimal(5), unit = "BAG"),
                poItemUnitPrice = BigDecimal(100),
                itemCategory = ItemCategory.Purchase.ADVANCE.name)
        var purchaseOrder = PurchaseOrderModel(
                retentionRemainingAmount = BigDecimal(50),
                purchaseItems = listOf(purchaseItemsModel1, purchaseItemsModel2))

        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)

        val invoiceItem = InvoiceItemModel(quantity = Quantity(3.toBigDecimal(), "BAG"), purchaseItemLinearId = "PO-ITEM-01")
        val invoiceItems = listOf(invoiceItem, invoiceItem.copy(quantity = Quantity(2.toBigDecimal(), "BAG"), purchaseItemLinearId = null, purchaseItemExternalId = "2", purchaseOrderExternalId = "PO-2"))

        mockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")
        every { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-01" }, invoiceItems) } returns listOf(invoiceItem)
        every { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-02" }, invoiceItems) } returns listOf(invoiceItems[1])
        every { match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-01" }.depleteQuantity(3.toBigDecimal()) } returns purchaseItemsModel1.copy(quantity = Quantity(initial = BigDecimal(10).setScale(), remaining = BigDecimal(2).setScale(), consumed = BigDecimal(8).setScale(), unit = "BAG"))
        every { match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-02" }.depleteQuantity(2.toBigDecimal()) } returns purchaseItemsModel1.copy(quantity = Quantity(initial = BigDecimal(10).setScale(), remaining = BigDecimal(3).setScale(), consumed = BigDecimal(7).setScale(), unit = "BAG"))
        var result = callMethod<CalculateCashPerfAndWarranty, BigDecimal>(CalculateCashPerfAndWarranty, "calculatePoRemainingAmountAfterDepleteInvoice", purchaseOrder, invoiceItems)!!
        assertEquals(BigDecimal(500).setScale(), result)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-01" }, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-02" }, invoiceItems) }

        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        val purchaseItemsModel3 = purchaseItemsModel1.copy(linearId = "PO-ITEM-03")
        val purchaseItemsModel4 = purchaseItemsModel1.copy(linearId = "PO-ITEM-04", deleteFlag = "MOCK")
        purchaseOrder = purchaseOrder.copy(purchaseItems = listOf(purchaseItemsModel1, purchaseItemsModel2, purchaseItemsModel3, purchaseItemsModel4))
        every { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-03" }, invoiceItems) } returns emptyList<InvoiceItemModel>()
        result = callMethod<CalculateCashPerfAndWarranty, BigDecimal>(CalculateCashPerfAndWarranty, "calculatePoRemainingAmountAfterDepleteInvoice", purchaseOrder, invoiceItems)!!
        assertEquals(BigDecimal(1000).setScale(), result)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-01" }, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-02" }, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-03" }, invoiceItems) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-04" }, invoiceItems) }

        //test case include advance (result should be equal above)
        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        every { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-03-A" }, invoiceItems) } returns emptyList<InvoiceItemModel>()
        result = callMethod<CalculateCashPerfAndWarranty, BigDecimal>(CalculateCashPerfAndWarranty, "calculatePoRemainingAmountAfterDepleteInvoice",
                purchaseOrder.copy(purchaseItems = listOf(purchaseItemsModel1, purchaseItemsModel2, advanceItemsModel)), invoiceItems)!!
        assertEquals(BigDecimal(500).setScale(), result)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-01" }, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-02" }, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-03-A" }, invoiceItems) }
        clearMocks(CalculateCashPerfAndWarranty, answers = false)

        result = callMethod<CalculateCashPerfAndWarranty, BigDecimal>(CalculateCashPerfAndWarranty, "calculatePoRemainingAmountAfterDepleteInvoice",
                purchaseOrder.copy(purchaseItems = listOf(purchaseItemsModel1, purchaseItemsModel2,purchaseItemsModel3, advanceItemsModel)), invoiceItems)!!
        assertEquals(BigDecimal(1000).setScale(), result)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-01" }, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-02" }, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-03" }, invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-03-A" }, invoiceItems) }
        clearMocks(CalculateCashPerfAndWarranty, answers = false)

        // Test case invoice consumed over quantity
        val invoiceItemOver = invoiceItem.copy(quantity = Quantity(15.toBigDecimal(), "BAG"))
        every { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-01" }, listOf(invoiceItemOver)) } returns listOf(invoiceItemOver)
        every { match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-01" }.depleteQuantity(10.toBigDecimal()) } returns purchaseItemsModel1.copy(quantity = Quantity(initial = BigDecimal(10).setScale(), consumed = BigDecimal(10).setScale(), unit = "BAG"))
        result = callMethod<CalculateCashPerfAndWarranty, BigDecimal>(CalculateCashPerfAndWarranty, "calculatePoRemainingAmountAfterDepleteInvoice",
                purchaseOrder.copy(purchaseItems = listOf(purchaseItemsModel1)), listOf(invoiceItemOver))!!
        assertEquals(BigDecimal(0).setScale(), result)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](match<PurchaseItemModel> { it.linearId ==  "PO-ITEM-01" }, listOf(invoiceItemOver)) }

        unmockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")
        unmockkObject(CalculateCashPerfAndWarranty)
    }

    @Test
    fun `Test prepare goods received`() {
        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)

        val goodsReceivedModel = GoodsReceivedModel(linearId = "GR-01", cashPerfDeductibleRemainingAmount = BigDecimal(50))
        val reverseGoodsReceivedModel = GoodsReceivedModel(linearId = "GR-02", goodsReceivedItems = listOf(GoodsReceivedItemModel(movementClass = "Reverse")))
        val allGoodsReceiveds = listOf(goodsReceivedModel, reverseGoodsReceivedModel)
        val normalGoodsReceiveds = listOf(goodsReceivedModel)
        val restoredGoodsReceiveds = listOf(goodsReceivedModel.copy(cashPerfDeductibleRemainingAmount = BigDecimal(100)))
        every { CalculateCashPerfAndWarranty["updateGoodsReceiveds"](restoredGoodsReceiveds, normalGoodsReceiveds) } returns restoredGoodsReceiveds

        every { CalculateCashPerfAndWarranty["isRequireRestoreGoodsReceiveds"](restoredGoodsReceiveds) } returns true
        var result = callMethod<CalculateCashPerfAndWarranty, List<GoodsReceivedModel>>(CalculateCashPerfAndWarranty, "prepareGoodsReceived", allGoodsReceiveds, restoredGoodsReceiveds)!!
        verify(exactly = 1) { CalculateCashPerfAndWarranty["isRequireRestoreGoodsReceiveds"](restoredGoodsReceiveds) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["updateGoodsReceiveds"](restoredGoodsReceiveds, normalGoodsReceiveds) }
        assertEquals(restoredGoodsReceiveds, result)

        clearMocks(CalculateCashPerfAndWarranty, answers = false)
        every { CalculateCashPerfAndWarranty["isRequireRestoreGoodsReceiveds"](restoredGoodsReceiveds) } returns false
        result = callMethod<CalculateCashPerfAndWarranty, List<GoodsReceivedModel>>(CalculateCashPerfAndWarranty, "prepareGoodsReceived", allGoodsReceiveds, restoredGoodsReceiveds)!!
        verify(exactly = 1) { CalculateCashPerfAndWarranty["isRequireRestoreGoodsReceiveds"](restoredGoodsReceiveds) }
        verify(exactly = 0) { CalculateCashPerfAndWarranty["updateGoodsReceiveds"](restoredGoodsReceiveds, normalGoodsReceiveds) }
        assertEquals(normalGoodsReceiveds, result)

        unmockkObject(CalculateCashPerfAndWarranty)
    }

    @Test
    fun `Test update goods receiveds`() {
        val goodsReceivedModel = GoodsReceivedModel(linearId = "GR-01",externalId = "GR1",cashPerfDeductibleRemainingAmount = BigDecimal(50))
        val normalGoodsReceiveds = listOf(goodsReceivedModel,goodsReceivedModel.copy(linearId = "GR-02",externalId = "GR2"))
        val restoredGoodsReceiveds = listOf(goodsReceivedModel.copy(cashPerfDeductibleRemainingAmount = BigDecimal(100)))

        val result = callMethod<CalculateCashPerfAndWarranty, List<GoodsReceivedModel>>(CalculateCashPerfAndWarranty, "updateGoodsReceiveds", restoredGoodsReceiveds, normalGoodsReceiveds)!!
        val goodsReceived1 = result.first()
        val goodsReceived2 = result.find{ it.linearId == "GR-02"}!!
        assertEquals(2, result.size)
        assertEquals(BigDecimal(100), goodsReceived1.cashPerfDeductibleRemainingAmount)
        assertEquals(BigDecimal(50), goodsReceived2.cashPerfDeductibleRemainingAmount)
    }

    @Test
    fun `Test is require restore goods receiveds`() {
        val restoredGoodsReceiveds = listOf(GoodsReceivedModel(linearId = "GR-01"))

        var result = callMethod<CalculateCashPerfAndWarranty, Boolean>(CalculateCashPerfAndWarranty, "isRequireRestoreGoodsReceiveds", restoredGoodsReceiveds)!!
        assertTrue(result)

        result = callMethod<CalculateCashPerfAndWarranty, Boolean>(CalculateCashPerfAndWarranty, "isRequireRestoreGoodsReceiveds", null)!!
        assertFalse(result)

    }

    @Test
    fun `Test updatePurchasePreWarrantyCalculation`() {
        val purchaseItems = listOf(
                PurchaseItemModel(linearId = "purchase-item-1", advancePaymentRemainingAmount = BigDecimal(10), advancePaymentToBeDeducted = BigDecimal(5)),
                PurchaseItemModel(poItemNo = "purchase-item-2", poNumber = "purchase-1")
        )

        val purchaseOrder = PurchaseOrderModel(
                retentionRemainingAmount = BigDecimal.ONE,
                cashPerfGuaranteeRemainingAmount = BigDecimal(5),
                purchaseItems = purchaseItems
        )

        val invoiceItems = listOf(
                InvoiceItemModel(
                        linearId = "invoice-item-1",
                        purchaseItemLinearId = "purchase-item-1",
                        retentionAmount = BigDecimal.ONE,
                        cashPerfGuaranteeAmount = BigDecimal(4)),
                InvoiceItemModel(
                        purchaseOrderExternalId = "purchase-1",
                        purchaseItemExternalId = "purchase-item-2",
                        purchaseItemLinearId = "purchase-item-1",
                        retentionAmount = BigDecimal.ONE)
        )

        val mapOfNormalItemsWithDeductAmount = mapOf("invoice-item-1" to BigDecimal(5))

        mockkObject(CalculateCashPerfAndWarranty, recordPrivateCalls = true)

        every { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](purchaseItems[0], invoiceItems) } returns listOf(invoiceItems[0])
        every { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](purchaseItems[1], invoiceItems) } returns listOf(invoiceItems[1])
        every { CalculateCashPerfAndWarranty["calculateSumOfItemAdvanceDeductAmountInSameHeader"](invoiceItems, mapOfNormalItemsWithDeductAmount) } returns BigDecimal(5)
        every { CalculateCashPerfAndWarranty["calculatePoRemainingAmountAfterDepleteInvoice"](purchaseOrder, invoiceItems) } returns BigDecimal(5)

        val result = callMethod<CalculateCashPerfAndWarranty, List<PurchaseOrderModel>>(
                CalculateCashPerfAndWarranty,
                "updatePurchasePreWarrantyCalculation",
                listOf(purchaseOrder),
                invoiceItems,
                mapOfNormalItemsWithDeductAmount)
        // In this case, let's say PO has remaining amount from normal PO item = 5
        // Advance amount to redeem = 10,
        // Advance amount to be deduct = 5 and invoice deduct for 5 = 0
        // Retention remaining = 1, invoice deduct for 2 (Assumed retention from GR) = -1 -> negative to zero = 0
        // Cash perf guaranty remaining = 5, invoice deduct for 4 = 1
        // Remaining total = 5 - (10 + 0 + 0 + 1) = -6 -> negative to zero = 0
        val expectedResult = listOf(
                purchaseOrder.copy(
                        remainingTotal = BigDecimal.ZERO,
                        cashPerfGuaranteeRemainingAmount = BigDecimal.ONE,
                        retentionRemainingAmount = BigDecimal.ZERO
                )
        )
        assertEquals(expectedResult, result)
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](purchaseItems[0], invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["filterInvoiceItemsRelatedToPurchaseItems"](purchaseItems[1], invoiceItems) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["calculateSumOfItemAdvanceDeductAmountInSameHeader"](invoiceItems, mapOfNormalItemsWithDeductAmount) }
        verify(exactly = 1) { CalculateCashPerfAndWarranty["calculatePoRemainingAmountAfterDepleteInvoice"](purchaseOrder, invoiceItems) }

        unmockkObject(CalculateCashPerfAndWarranty)
    }

    @Test
    fun `Test filterInvoiceItemsRelatedToPurchaseItems`() {
        val purchaseItem = PurchaseItemModel(linearId = "purchase-item-1", poItemNo = "1", poNumber = "PO-1")
        val invoiceItemWithPurchaseLinear = InvoiceItemModel(purchaseItemLinearId = "purchase-item-1")
        val invoiceItemWithPurchaseNumber = InvoiceItemModel(purchaseItemExternalId = "1", purchaseOrderExternalId = "PO-1")
        val invoiceItemNotMatched = InvoiceItemModel(purchaseItemLinearId = "purchase-item-2")

        val result = callMethod<CalculateCashPerfAndWarranty, List<InvoiceItemModel>>(
                CalculateCashPerfAndWarranty,
                "filterInvoiceItemsRelatedToPurchaseItems",
                purchaseItem,
                listOf(invoiceItemWithPurchaseLinear, invoiceItemWithPurchaseNumber, invoiceItemNotMatched)
        )
        assertNotNull(result)
        assertEquals(2, result.size)
        assertTrue(result.containsAll(listOf(invoiceItemWithPurchaseLinear, invoiceItemWithPurchaseNumber)))
    }
}