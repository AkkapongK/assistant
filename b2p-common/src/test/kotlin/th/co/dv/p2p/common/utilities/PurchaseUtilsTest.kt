package th.co.dv.p2p.common.utilities

import io.mockk.*
import org.junit.Test
import th.co.dv.p2p.common.MockData.Companion.mockInvoiceItemModel1
import th.co.dv.p2p.common.MockData.Companion.mockPurchaseItemModel
import th.co.dv.p2p.common.MockData.Companion.mockPurchaseItemModel1
import th.co.dv.p2p.common.MockData.Companion.mockPurchaseItemModel2
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.adjustmentTypeNotSupport
import th.co.dv.p2p.common.constants.advancePaymentToBeDeductedNotEnough
import th.co.dv.p2p.common.enums.AdjustmentType
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.Lifecycle
import th.co.dv.p2p.common.enums.RecordStatus
import th.co.dv.p2p.common.utilities.PurchaseUtils.calculateOverDeliveryQuantityInPurchaseItemFn
import th.co.dv.p2p.common.utilities.PurchaseUtils.restoreDeductPurchaseItem
import th.co.dv.p2p.common.utilities.PurchaseUtils.restoreNormalPurchaseItem
import th.co.dv.p2p.common.utilities.PurchaseUtils.restoreRedeemPurchaseItem
import th.co.dv.p2p.common.utilities.PurchaseUtils.updatePurchaseItemsFromUpdatedInvoice
import th.co.dv.p2p.corda.base.domain.DeleteFlag
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.*
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PurchaseUtilsTest {

    @Test
    fun testRestoreNormalPurchaseItem() {

        // Case: Restore from NORMAL invoice
        val purchaseItemIn1 = mockPurchaseItemModel.copy(
                itemCategory = ItemCategory.Purchase.NORMAL.name,
                quantity = Quantity(
                        initial = BigDecimal(8000),
                        consumed = BigDecimal(8000),
                        unit = "BAG"
                ),
                overDeliveryQuantity = Quantity(
                        initial = BigDecimal(2000),
                        consumed = BigDecimal(800),
                        unit = "BAG"
                )

        )
        val invoiceItemIn1 = mockInvoiceItemModel1.copy(
                quantity = Quantity(
                        initial = BigDecimal(1000),
                        consumed = BigDecimal(1000),
                        unit = "BAG"
                )
        )

        val purchaseItemForDelivery: MutableList<PurchaseItemModel> = mutableListOf()

        val result = Try.on {
            restoreNormalPurchaseItem(purchaseItemForDelivery, purchaseItemIn1, invoiceItemIn1)
        }
        assertTrue(result.isSuccess)
        val purchaseItemOut = result.getOrThrow()

        assertEquals(purchaseItemOut.size, 1)
        val restoredPurchaseItemOut = purchaseItemOut.single()

        assertEquals(BigDecimal(200).setScale(), restoredPurchaseItemOut.quantity!!.remaining)
        assertEquals(BigDecimal(7800).setScale(), restoredPurchaseItemOut.quantity!!.consumed)
        assertEquals(BigDecimal(2000).setScale(), restoredPurchaseItemOut.overDeliveryQuantity!!.remaining)
        assertEquals(BigDecimal(0).setScale(), restoredPurchaseItemOut.overDeliveryQuantity!!.consumed)

        // update on previous output
        val restoreAgain = restoreNormalPurchaseItem(purchaseItemForDelivery, purchaseItemIn1, invoiceItemIn1)
        assertEquals(restoreAgain.size, 1)
        var restoreItem = restoreAgain.single()
        assertEquals(BigDecimal(1200).setScale(), restoreItem.quantity!!.remaining)
        assertEquals(BigDecimal(6800).setScale(), restoreItem.quantity!!.consumed)
        assertEquals(BigDecimal(2000).setScale(), restoreItem.overDeliveryQuantity!!.remaining)
        assertEquals(BigDecimal(0).setScale(), restoreItem.overDeliveryQuantity!!.consumed)

        // invoice item have creditNote quantity
        val invoiceItem = invoiceItemIn1.copy(
            quantity = Quantity(1000.toBigDecimal(), "BAG"),
            creditNoteQuantity = Quantity(100.toBigDecimal(), "BAG")
        )
        val result1 = restoreNormalPurchaseItem(mutableListOf(), purchaseItemIn1, invoiceItem)
        assertEquals(result1.size, 1)
        restoreItem = result1.single()
        assertEquals(BigDecimal(2000).setScale(), restoreItem.overDeliveryQuantity!!.remaining)
        assertEquals(BigDecimal(0).setScale(), restoreItem.overDeliveryQuantity!!.consumed)
        assertEquals(BigDecimal(100).setScale(), restoreItem.quantity!!.remaining)
        assertEquals(BigDecimal(7900).setScale(), restoreItem.quantity!!.consumed)
    }

    @Test
    fun `Test restoreRedeemPurchaseItem fail`() {

        val purchaseItemIn1 = mockPurchaseItemModel
                .copy(
                        itemCategory = ItemCategory.Purchase.ADVANCE.name,
                        advanceInitialAmount = 200.toBigDecimal(),
                        advancePaymentRemainingAmount = 0.toBigDecimal(),
                        advancePaymentToBeDeducted = 100.toBigDecimal()
                )

        val invoiceItemIn1 = mockInvoiceItemModel1
                .copy(
                        itemCategory = ItemCategory.Invoice.ADVANCE_REDEEM.name,
                        itemSubTotal = 200.toBigDecimal()
                )

        val purchaseItemForRestoreRedeem: MutableList<PurchaseItemModel> = mutableListOf()

        val result = Try.on {
            restoreRedeemPurchaseItem(purchaseItemForRestoreRedeem, purchaseItemIn1, invoiceItemIn1)
        }

        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Advance to be deducted must have value."))

    }

    @Test
    fun `Test restoreRedeemPurchaseItem success`() {

        val purchaseItemIn1 = mockPurchaseItemModel
                .copy(
                        itemCategory = ItemCategory.Purchase.ADVANCE.name,
                        advanceInitialAmount = 200.toBigDecimal(),
                        advancePaymentRemainingAmount = 0.toBigDecimal(),
                        advancePaymentToBeDeducted = 200.toBigDecimal()
                )

        val invoiceItemIn1 = mockInvoiceItemModel1
                .copy(
                        itemCategory = ItemCategory.Invoice.ADVANCE_REDEEM.name,
                        itemSubTotal = 100.toBigDecimal()
                )

        val purchaseItemForRestoreRedeem: MutableList<PurchaseItemModel> = mutableListOf()

        val result = Try.on {
            restoreRedeemPurchaseItem(purchaseItemForRestoreRedeem, purchaseItemIn1, invoiceItemIn1)
        }

        val purchaseItemOut = result.getOrThrow()
        val sumPIOutAdvancePaymentRemainingAmount = purchaseItemOut.sumByDecimal { it.advancePaymentRemainingAmount!! }
        val sumPIOutAdvancePaymentToBeDeducted = purchaseItemOut.sumByDecimal { it.advancePaymentToBeDeducted!! }

        assertTrue(result.isSuccess)
        assertEquals(BigDecimal(100), sumPIOutAdvancePaymentRemainingAmount)
        assertEquals(BigDecimal(100), sumPIOutAdvancePaymentToBeDeducted)

        // update on previous output
        val restoreAgain = restoreRedeemPurchaseItem(purchaseItemForRestoreRedeem, purchaseItemIn1, invoiceItemIn1)
        assertEquals(restoreAgain.size, 1)
        val restoreItem = restoreAgain.single()
        assertTrue(restoreItem.advancePaymentRemainingAmount!!.compareTo(BigDecimal(200)) == 0)
        assertTrue(restoreItem.advancePaymentToBeDeducted!!.compareTo(BigDecimal.ZERO) == 0)

    }

    @Test
    fun testRestoreDeductPurchaseItem() {

        val purchaseItemIn1 = mockPurchaseItemModel
                .copy(
                        itemCategory = ItemCategory.Purchase.ADVANCE.name,
                        advanceInitialAmount = 400.toBigDecimal(),
                        advancePaymentRemainingAmount = 100.toBigDecimal(),
                        advancePaymentToBeDeducted = 100.toBigDecimal()
                )

        val purchaseItemIn = listOf(purchaseItemIn1)

        val invoiceItemIn1 = mockInvoiceItemModel1
                .copy(
                        itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name,
                        itemSubTotal = 100.toBigDecimal()
                )

        val purchaseItemForRestoreDeducted: MutableList<PurchaseItemModel> = mutableListOf()

        val result = Try.on {
            restoreDeductPurchaseItem(purchaseItemIn, purchaseItemForRestoreDeducted, purchaseItemIn1, invoiceItemIn1)
        }

        val purchaseItemOut = result.getOrThrow()
        assertEquals(purchaseItemOut.size, 1)
        val sumPIInAdvancePaymentToBeDeducted = purchaseItemIn.sumByDecimal { it.advancePaymentToBeDeducted!! }
        val sumPIOutAdvancePaymentToBeDeducted = purchaseItemOut.sumByDecimal { it.advancePaymentToBeDeducted!! }

        assertTrue(result.isSuccess)
        assertEquals(BigDecimal(100), sumPIOutAdvancePaymentToBeDeducted - sumPIInAdvancePaymentToBeDeducted)
        assertTrue(purchaseItemOut.single().advancePaymentToBeDeducted!!.compareTo(BigDecimal(200)) == 0)

        // Restore again
        val restoreAgain = restoreDeductPurchaseItem(purchaseItemIn, purchaseItemForRestoreDeducted, purchaseItemIn1, invoiceItemIn1)
        assertEquals(restoreAgain.size, 1)
        val restoreItem = restoreAgain.single()
        assertTrue(restoreItem.advancePaymentToBeDeducted!!.compareTo(BigDecimal(300)) == 0)

    }

    @Test
    fun testRestoreDeductedValueToAdvancePurchaseItem() {

        // Case: There is enough advancePaymentToBeDeducted to be restored.
        var purchaseItemIn1 = mockPurchaseItemModel
                .copy(
                        linearId = "linear-1",
                        advanceInitialAmount = 200.toBigDecimal(),
                        advancePaymentToBeDeducted = 50.toBigDecimal()
                )
        var purchaseItemIn2 = mockPurchaseItemModel
                .copy(
                        linearId = "linear-2",
                        advanceInitialAmount = 100.toBigDecimal(),
                        advancePaymentToBeDeducted = 50.toBigDecimal()
                )

        var purchaseItemIn = listOf(purchaseItemIn1, purchaseItemIn2)

        var result = Try.on {
            purchaseItemIn.restoreDeductedValueToAdvancePurchaseItem(200.toBigDecimal())
        }

        val purchaseItemOut = result.getOrThrow()

        val sumPIInAdvancePaymentToBeDeducted = purchaseItemIn.sumByDecimal { it.advancePaymentToBeDeducted!! }
        val sumPIOutAdvancePaymentToBeDeducted = purchaseItemOut.sumByDecimal { it.advancePaymentToBeDeducted!! }

        assertTrue(result.isSuccess)
        assertEquals(BigDecimal(200), sumPIOutAdvancePaymentToBeDeducted - sumPIInAdvancePaymentToBeDeducted)

        val firstPi = purchaseItemOut.find { it.linearId == "linear-1" }!!
        val secondPi = purchaseItemOut.find { it.linearId == "linear-2" }!!
        assertTrue(firstPi.advancePaymentToBeDeducted!!.compareTo(BigDecimal(200)) == 0)
        assertTrue(secondPi.advancePaymentToBeDeducted!!.compareTo(BigDecimal(100)) == 0)

        // Case: There is NOT enough advancePaymentToBeDeducted to be restored.
        purchaseItemIn1 = mockPurchaseItemModel
                .copy(
                        advanceInitialAmount = 200.toBigDecimal(),
                        advancePaymentToBeDeducted = 100.toBigDecimal()
                )
        purchaseItemIn2 = mockPurchaseItemModel
                .copy(
                        advanceInitialAmount = 100.toBigDecimal(),
                        advancePaymentToBeDeducted = 50.toBigDecimal()
                )

        purchaseItemIn = listOf(purchaseItemIn1, purchaseItemIn2)

        result = Try.on {
            purchaseItemIn.restoreDeductedValueToAdvancePurchaseItem(200.toBigDecimal())
        }

        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Not enough advance amount to be restore."))
    }


    @Test
    fun testRestoreAdvanceAmountAndQuantityToPurchaseItems() {

        mockkObject(PurchaseUtils)
        //Case: Restore ADVANCE REDEEM Invoice
        var purchaseItemIn1 = mockPurchaseItemModel
                .copy(
                        itemCategory = ItemCategory.Purchase.ADVANCE.name,
                        advanceInitialAmount = 200.toBigDecimal(),
                        advancePaymentRemainingAmount = 0.toBigDecimal(),
                        advancePaymentToBeDeducted = 200.toBigDecimal()
                )

        var purchaseItemOut1 = mockPurchaseItemModel
                .copy(
                        itemCategory = ItemCategory.Purchase.ADVANCE.name,
                        advanceInitialAmount = 200.toBigDecimal(),
                        advancePaymentRemainingAmount = 200.toBigDecimal(),
                        advancePaymentToBeDeducted = 0.toBigDecimal()
                )

        var invoiceItemIn1 = mockInvoiceItemModel1
                .copy(
                        itemCategory = ItemCategory.Invoice.ADVANCE_REDEEM.name,
                        itemSubTotal = 200.toBigDecimal()
                )

        var invoiceItemIn = listOf(invoiceItemIn1)
        var purchaseItemIn = listOf(purchaseItemIn1)

        every { restoreRedeemPurchaseItem(mutableListOf(), any(), any()) } returns mutableListOf(purchaseItemOut1)

        var result = Try.on {
            purchaseItemIn.restoreAdvanceAmountAndQuantityToPurchaseItems(invoiceItemIn, false)
        }
        assert(result.isSuccess)
        var restoredPurchaseItemOut = result.getOrThrow().single()
        assertTrue(BigDecimal(200).compareTo(restoredPurchaseItemOut.advanceInitialAmount) == 0)
        assertTrue(BigDecimal(200).compareTo(restoredPurchaseItemOut.advancePaymentRemainingAmount) == 0)
        assertTrue(BigDecimal(0).compareTo(restoredPurchaseItemOut.advancePaymentToBeDeducted) == 0)

        //Case: Restore ADVANCE DEDUCT Invoice, no contract
        purchaseItemIn1 = mockPurchaseItemModel
                .copy(
                        itemCategory = ItemCategory.Purchase.ADVANCE.name,
                        advanceInitialAmount = 200.toBigDecimal(),
                        advancePaymentRemainingAmount = 0.toBigDecimal(),
                        advancePaymentToBeDeducted = 200.toBigDecimal()
                )

        purchaseItemOut1 = mockPurchaseItemModel
                .copy(
                        itemCategory = ItemCategory.Purchase.ADVANCE.name,
                        advanceInitialAmount = 200.toBigDecimal(),
                        advancePaymentRemainingAmount = 200.toBigDecimal(),
                        advancePaymentToBeDeducted = 0.toBigDecimal()
                )
        invoiceItemIn1 = mockInvoiceItemModel1
                .copy(
                        itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name,
                        itemSubTotal = 200.toBigDecimal()
                )

        invoiceItemIn = listOf(invoiceItemIn1)
        purchaseItemIn = listOf(purchaseItemIn1)

        every { restoreDeductPurchaseItem(any(), mutableListOf(), any(), any()) } returns mutableListOf(purchaseItemOut1)

        result = Try.on {
            purchaseItemIn.restoreAdvanceAmountAndQuantityToPurchaseItems(invoiceItemIn, false)
        }
        assert(result.isSuccess)
        restoredPurchaseItemOut = result.getOrThrow().single()
        assertTrue(BigDecimal(200).compareTo(restoredPurchaseItemOut.advanceInitialAmount) == 0)
        assertTrue(BigDecimal(200).compareTo(restoredPurchaseItemOut.advancePaymentRemainingAmount) == 0)
        assertTrue(BigDecimal(0).compareTo(restoredPurchaseItemOut.advancePaymentToBeDeducted) == 0)

        //Case: Restore ADVANCE DEDUCT Invoice, with contract
        result = Try.on {
            purchaseItemIn.restoreAdvanceAmountAndQuantityToPurchaseItems(invoiceItemIn, true)
        }
        assert(result.isSuccess)
        val restoredPurchaseItemOutContract = result.getOrThrow()
        assertTrue(restoredPurchaseItemOutContract.isEmpty())

        //Case: Restore NORMAL Invoice
        purchaseItemIn1 = mockPurchaseItemModel.copy(
                itemCategory = ItemCategory.Purchase.NORMAL.name,
                quantity = Quantity(
                        initial = BigDecimal(8000),
                        consumed = BigDecimal(8000),
                        unit = "BAG"
                ),
                overDeliveryQuantity = Quantity(
                        initial = BigDecimal(2000),
                        consumed = BigDecimal(800),
                        unit = "BAG"
                )
        )

        invoiceItemIn1 = mockInvoiceItemModel1.copy(
                quantity = Quantity(
                        initial = BigDecimal(1000),
                        consumed = BigDecimal(1000),
                        unit = "BAG"
                )
        )

        purchaseItemOut1 = mockPurchaseItemModel.copy(
                itemCategory = ItemCategory.Purchase.NORMAL.name,
                quantity = Quantity(
                        initial = BigDecimal(8000),
                        remaining = BigDecimal(200),
                        consumed = BigDecimal(7800),
                        unit = "BAG"
                ),
                overDeliveryQuantity = Quantity(
                        initial = BigDecimal(2000),
                        remaining = BigDecimal(8000),
                        consumed = BigDecimal(0),
                        unit = "BAG"
                )
        )

        invoiceItemIn = listOf(invoiceItemIn1)
        purchaseItemIn = listOf(purchaseItemIn1)

        every { restoreNormalPurchaseItem(any(), any(), any()) } returns mutableListOf(purchaseItemOut1)

        result = Try.on {
            purchaseItemIn.restoreAdvanceAmountAndQuantityToPurchaseItems(invoiceItemIn, false)
        }
        assert(result.isSuccess)
        restoredPurchaseItemOut = result.getOrThrow().single()
        assertEquals(0, BigDecimal(200).compareTo(restoredPurchaseItemOut.quantity!!.remaining))
        assertEquals(0, BigDecimal(7800).compareTo(restoredPurchaseItemOut.quantity!!.consumed))
        assertEquals(0, BigDecimal(8000).compareTo(restoredPurchaseItemOut.overDeliveryQuantity!!.remaining))
        assertEquals(0, BigDecimal(0).compareTo(restoredPurchaseItemOut.overDeliveryQuantity!!.consumed))

        unmockkObject(PurchaseUtils)
    }

    @Test
    fun testParseStatus() {

        val purchaseUtils = spyk<PurchaseUtils>()

        // find multiple header status
        var listOfStatus = listOf("Pending Ack", "Rejected")
        var expectedResult = setOf("APPROVED", "REJECTED")
        var result = purchaseUtils.parseStatus(listOfStatus, true)

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(expectedResult, result)

        // find multiple item status
        listOfStatus = listOf("Pending Vendor", "Confirmed")
        expectedResult = setOf("PENDING_SELLER", "CONFIRMED")
        result = purchaseUtils.parseStatus(listOfStatus, false)

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(expectedResult, result)
    }

    @Test
    fun testUpdatePurchaseStatus() {

        val purchaseUtils = spyk<PurchaseUtils>()
        val purchaseItems = listOf(PurchaseItemModel(
                lifecycle = Lifecycle.PurchaseItemLifecycle.CONFIRMED.name,
                status = RecordStatus.VALID.name,
                deliveryStatus = "X"
        ))
        val purchaseOrder = listOf(PurchaseOrderModel(
                lifecycle = Lifecycle.PurchaseOrderLifecycle.APPROVED.name,
                status = RecordStatus.VALID.name,
                purchaseItems = purchaseItems
        ))

        val purchaseItemsWithStatus = listOf(PurchaseItemModel(
                lifecycle = Lifecycle.PurchaseItemLifecycle.CONFIRMED.name,
                status = "Confirmed",
                deliveryStatus = "Yes"
        ))

        every { purchaseUtils.updatePurchaseItemStatus(purchaseItems) } returns purchaseItemsWithStatus

        val result = purchaseUtils.updatePurchaseStatus(purchaseOrder)

        assertEquals(1, result.size)
        assertEquals("Pending Ack", result.first().status)
        assertEquals(1, result.first().purchaseItems.size)
        assertEquals("Confirmed", result.first().purchaseItems.first().status)
        assertEquals("Yes", result.first().purchaseItems.first().deliveryStatus)

    }

    @Test
    fun testUpdatePurchaseItemStatus() {

        val purchaseUtils = spyk<PurchaseUtils>()
        val purchaseItems = listOf(PurchaseItemModel(
                lifecycle = Lifecycle.PurchaseItemLifecycle.CONFIRMED.name,
                status = RecordStatus.VALID.name,
                deliveryStatus = "X"
        ))

        val result = purchaseUtils.updatePurchaseItemStatus(purchaseItems)

        assertEquals(1, result.size)
        assertEquals("Confirmed", result.first().status)
        assertEquals("Yes", result.first().deliveryStatus)

    }

    @Test
    fun testAdjustPurchaseItems() {
        val purchaseItemModel = listOf(
                mockPurchaseItemModel1,
                mockPurchaseItemModel2.copy(quantity = Quantity(
                        initial = 2000.0.toBigDecimal(),
                        consumed = 1000.0.toBigDecimal(),
                        unit = "BAG"
                )))
        var newCreditNoteItems = listOf(CreditNoteItemModel(
                purchaseItemLinearId = mockPurchaseItemModel.linearId,
                quantity = Quantity(
                        initial = 2000.0.toBigDecimal(),
                        consumed = BigDecimal.ZERO,
                        unit = "BAG"
                )

        ))
        var existingCreditNoteItems = listOf(CreditNoteItemModel(
                purchaseItemLinearId = mockPurchaseItemModel.linearId,
                quantity = Quantity(
                        initial = 4000.0.toBigDecimal(),
                        consumed = BigDecimal.ZERO,
                        unit = "BAG"
                )
        ))

        // Deplete and Restore existing Purchase Item
        var result = purchaseItemModel.adjustPurchaseItems(newCreditNoteItems, existingCreditNoteItems)
        assertEquals(1, result.size)
        val purchaseItem = result.single()
        assertTrue(6000.0.toBigDecimal().compareTo(purchaseItem.quantity!!.remaining) == 0)
        assertTrue(2000.0.toBigDecimal().compareTo(purchaseItem.quantity!!.consumed) == 0)

        newCreditNoteItems = listOf(CreditNoteItemModel(
                purchaseItemLinearId = mockPurchaseItemModel2.linearId,
                quantity = Quantity(
                        initial = 1000.0.toBigDecimal(),
                        consumed = BigDecimal.ZERO,
                        unit = "BAG"
                )

        ))
        existingCreditNoteItems = listOf(CreditNoteItemModel(
                purchaseItemLinearId = mockPurchaseItemModel1.linearId,
                quantity = Quantity(
                        initial = 4000.0.toBigDecimal(),
                        consumed = BigDecimal.ZERO,
                        unit = "BAG"
                )
        ))
        //Deplete and Restore new Purchase Item
        result = purchaseItemModel.adjustPurchaseItems(newCreditNoteItems, existingCreditNoteItems)
        assertEquals(2, result.size)
        assertTrue(BigDecimal.ZERO.compareTo(result.single { it.linearId == mockPurchaseItemModel2.linearId }.quantity!!.consumed) == 0)
        assertTrue(4000.0.toBigDecimal().compareTo(result
                .single { it.linearId == mockPurchaseItemModel1.linearId }.quantity!!.remaining) == 0)
    }

    @Test
    fun updatePurchaseItemsFromUpdatedInvoiceTest() {
        mockkObject(PurchaseUtils)
        mockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")

        val calculateOverDeliveryQuantityInPurchaseItemFn: (List<PurchaseItemModel>, InvoiceModel) -> List<PurchaseItemModel> = { a, _ -> a }

        val purchaseItemFromPreviousIn = listOf(mockPurchaseItemModel1, mockPurchaseItemModel1)
        val purchaseItemIn = listOf(mockPurchaseItemModel2, mockPurchaseItemModel2)

        val prevInvoiceItems = listOf(InvoiceItemModel(linearId = "333"))
        val invoiceItems = listOf(InvoiceItemModel(linearId = "123"))
        val previousInvoiceModel = InvoiceModel(linearId = "xcx", invoiceItems = prevInvoiceItems)
        val invoiceModel = InvoiceModel(linearId = "xxxx", invoiceItems = invoiceItems)

        val purchaseItemOut = listOf(mockPurchaseItemModel1, mockPurchaseItemModel2)
        val purchaseItemOut2 = listOf(mockPurchaseItemModel1, mockPurchaseItemModel2, mockPurchaseItemModel1)

        // case no previous data
        every { PurchaseUtils.replaceItemWithUpdatedItem(purchaseItemIn, emptyList()) } returns purchaseItemIn
        every { purchaseItemIn.depleteAdvanceAmountAndQuantityToPurchaseItems(invoiceItems, false) } returns purchaseItemOut
        every { PurchaseUtils.replaceItemWithUpdatedItem(purchaseItemIn, purchaseItemOut) } returns purchaseItemOut

        var result = updatePurchaseItemsFromUpdatedInvoice(
                null, null, purchaseItemIn, invoiceModel, false, calculateOverDeliveryQuantityInPurchaseItemFn
        )
        assertEquals(purchaseItemOut, result)
        verify(exactly = 0) { any<List<PurchaseItemModel>>().restoreAdvanceAmountAndQuantityToPurchaseItems(any(), any()) }

        // case has previous data
        every { purchaseItemFromPreviousIn.restoreAdvanceAmountAndQuantityToPurchaseItems(prevInvoiceItems, false) } returns purchaseItemFromPreviousIn
        every { PurchaseUtils.replaceItemWithUpdatedItem(purchaseItemIn, purchaseItemFromPreviousIn) } returns purchaseItemFromPreviousIn
        every { purchaseItemFromPreviousIn.depleteAdvanceAmountAndQuantityToPurchaseItems(invoiceItems, false) } returns purchaseItemOut2
        every { PurchaseUtils.replaceItemWithUpdatedItem(purchaseItemFromPreviousIn, purchaseItemOut2) } returns purchaseItemOut2

        val previousInvoiceModelCancelled = previousInvoiceModel.copy(lifecycle = Lifecycle.InvoiceLifecycle.CANCELLED.name)
        result = updatePurchaseItemsFromUpdatedInvoice(
                purchaseItemFromPreviousIn, previousInvoiceModelCancelled, purchaseItemIn, invoiceModel, false, calculateOverDeliveryQuantityInPurchaseItemFn
        )
        assertEquals(purchaseItemOut2, result)
        verify(exactly = 0) { purchaseItemFromPreviousIn.restoreAdvanceAmountAndQuantityToPurchaseItems(prevInvoiceItems, false) }

        result = updatePurchaseItemsFromUpdatedInvoice(
                purchaseItemFromPreviousIn, previousInvoiceModel, purchaseItemIn, invoiceModel, false, calculateOverDeliveryQuantityInPurchaseItemFn
        )
        assertEquals(purchaseItemOut2, result)
        verify(exactly = 1) { purchaseItemFromPreviousIn.restoreAdvanceAmountAndQuantityToPurchaseItems(prevInvoiceItems, false) }

        unmockkObject(PurchaseUtils)
        unmockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")
    }

    @Test
    fun updatePurchaseItemsFromUpdatedCreditNoteTest() {
        val purchaseUtils = spyk<PurchaseUtils>()

        val purchaseItemModel = listOf(mockPurchaseItemModel1.copy(
                quantity = Quantity(100.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")
        ))

        val existingPurchaseItemModel = listOf(mockPurchaseItemModel2.copy(
                quantity = Quantity(1000.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG"
                )))
        val newCreditNoteItems = listOf(CreditNoteItemModel(
                purchaseItemLinearId = mockPurchaseItemModel.linearId,
                quantity = Quantity(50.0.toBigDecimal(), "BAG"))

        )
        val existingCreditNoteItems = listOf(CreditNoteItemModel(
                purchaseItemLinearId = mockPurchaseItemModel2.linearId,
                quantity = Quantity(50.0.toBigDecimal(), "BAG"))
        )

        val creditNote = CreditNoteModel(creditNoteItems = newCreditNoteItems, adjustmentType = AdjustmentType.QUANTITY.name)
        val existingCreditNote = CreditNoteModel(creditNoteItems = existingCreditNoteItems, adjustmentType = AdjustmentType.QUANTITY.name)

        every {
            purchaseUtils.createOutputPurchaseItemsForDeleteCreditNote(
                    existingPurchaseItemModel, match { it == AdjustmentType.QUANTITY.name }, existingCreditNoteItems)
        } returns existingPurchaseItemModel

        every {
            purchaseUtils.replaceItemWithUpdatedItem(
                    purchaseItemModel, existingPurchaseItemModel)
        } returns purchaseItemModel

        every {
            purchaseUtils.createOutputPurchaseItems(
                    purchaseItemModel, match { it == AdjustmentType.QUANTITY.name }, newCreditNoteItems)
        } returns purchaseItemModel

        every {
            purchaseUtils.combinePurchaseItem(
                    purchaseItemModel, existingPurchaseItemModel)
        } returns purchaseItemModel

        val result = purchaseUtils.updatePurchaseItemsFromUpdatedCreditNote(existingPurchaseItemModel, existingCreditNote, purchaseItemModel, creditNote)
        assertEquals(2, result.size)
        assertTrue(result.map { it.poItemNo }.containsAll(listOf("1", "2")))
    }

    @Test
    fun createOutputPurchaseItemsForDeleteCreditNoteTest() {
        val purchaseUtils = spyk<PurchaseUtils>()

        var existingPurchaseItemModel = listOf(mockPurchaseItemModel2.copy(
                quantity = Quantity(200.0.toBigDecimal(), "BAG"
                )))

        val existingCreditNoteItems = listOf(CreditNoteItemModel(
                purchaseItemLinearId = mockPurchaseItemModel2.linearId,
                quantity = Quantity(100.0.toBigDecimal(), "BAG"))
        )

        //case success
        var result = purchaseUtils.createOutputPurchaseItemsForDeleteCreditNote(existingPurchaseItemModel, AdjustmentType.QUANTITY.name, existingCreditNoteItems)
        assertEquals(1, result.size)
        assertTrue(100.0.toBigDecimal().isEqual(result.first().quantity!!.remaining))

        //fail AdjustmentType
        val resultTry = Try.on {
            purchaseUtils.createOutputPurchaseItemsForDeleteCreditNote(existingPurchaseItemModel, AdjustmentType.PRICE.name, existingCreditNoteItems)
        }
        assertTrue(resultTry.isFailure)
        assertTrue(resultTry.toString().contains(adjustmentTypeNotSupport))

        //case not match
        existingPurchaseItemModel = listOf(mockPurchaseItemModel2.copy(
                linearId = "1",
                quantity = Quantity(200.0.toBigDecimal(), "BAG"
                )))
        result = purchaseUtils.createOutputPurchaseItemsForDeleteCreditNote(existingPurchaseItemModel, AdjustmentType.QUANTITY.name, existingCreditNoteItems)
        assertTrue(result.isEmpty())
    }

    @Test
    fun replaceItemWithUpdatedItemTest() {
        val purchaseUtils = spyk<PurchaseUtils>()

        var updatePo = listOf(mockPurchaseItemModel1.copy(
                quantity = Quantity(100.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")))

        var existingPo = listOf(mockPurchaseItemModel2.copy(
                quantity = Quantity(1000.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")))

        var result = purchaseUtils.replaceItemWithUpdatedItem(existingPo, updatePo)
        assertEquals(1, result.size)
        assertTrue(result.first().quantity!!.initial.isEqual(1000.0.toBigDecimal()))

        updatePo = listOf(mockPurchaseItemModel1.copy(
                quantity = Quantity(100.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")))

        existingPo = listOf(mockPurchaseItemModel1.copy(
                quantity = Quantity(1000.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")))

        result = purchaseUtils.replaceItemWithUpdatedItem(existingPo, updatePo)
        assertEquals(1, result.size)
        assertTrue(result.first().quantity!!.initial.isEqual(100.0.toBigDecimal()))
    }

    @Test
    fun createOutputPurchaseItemsTest() {
        val purchaseUtils = spyk<PurchaseUtils>()

        val newCreditNoteItems = listOf(CreditNoteItemModel(
                purchaseItemLinearId = mockPurchaseItemModel.linearId,
                quantity = Quantity(50.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG"))
        )
        var purchaseItemModel = listOf(mockPurchaseItemModel1.copy(
                quantity = Quantity(100.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")
        ))
        //case success
        var result = purchaseUtils.createOutputPurchaseItems(purchaseItemModel, AdjustmentType.QUANTITY.name, newCreditNoteItems)
        assertEquals(1, result.size)
        assertTrue(50.0.toBigDecimal().isEqual(result.first().quantity!!.remaining))

        //fail AdjustmentType
        val resultTry = Try.on {
            purchaseUtils.createOutputPurchaseItems(purchaseItemModel, AdjustmentType.PRICE.name, newCreditNoteItems)
        }
        assertTrue(resultTry.isFailure)
        assertTrue(resultTry.toString().contains(adjustmentTypeNotSupport))

        purchaseItemModel = listOf(mockPurchaseItemModel2.copy(
                linearId = "1",
                quantity = Quantity(200.0.toBigDecimal(), "BAG"
                )))
        result = purchaseUtils.createOutputPurchaseItems(purchaseItemModel, AdjustmentType.QUANTITY.name, newCreditNoteItems)
        assertTrue(result.isEmpty())
    }


    @Test
    fun combinePurchaseItemTest() {
        val purchaseUtils = spyk<PurchaseUtils>()

        var subList = listOf(mockPurchaseItemModel2.copy(
                quantity = Quantity(1000.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")))

        var mainList = listOf(mockPurchaseItemModel1.copy(
                quantity = Quantity(100.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")
        ))
        //case success
        var result = purchaseUtils.combinePurchaseItem(subList, mainList)
        assertEquals(2, result.size)

        subList = listOf(mockPurchaseItemModel2.copy(
                quantity = Quantity(1000.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")))
        mainList = listOf(mockPurchaseItemModel2.copy(
                quantity = Quantity(100.0.toBigDecimal(), 100.0.toBigDecimal(), "BAG")))

        result = purchaseUtils.combinePurchaseItem(subList, mainList)
        assertEquals(1, result.size)
        assertTrue(result.first().quantity!!.initial.isEqual(100.0.toBigDecimal()))
    }

    @Test
    fun testValidatePurchaseOrderRemaining() {
        val purchaseItems = listOf(
                PurchaseItemModel(quantity = Quantity(BigDecimal.TEN, "EA"), poItemUnitPrice = BigDecimal.ONE, itemCategory = ItemCategory.Purchase.NORMAL.name),
                PurchaseItemModel(quantity = Quantity(BigDecimal.ONE, "EA"), poItemUnitPrice = BigDecimal.ONE, itemCategory = ItemCategory.Purchase.NORMAL.name),
                PurchaseItemModel(quantity = Quantity(BigDecimal.TEN, "EA"), poItemUnitPrice = BigDecimal.TEN, itemCategory = ItemCategory.Purchase.NORMAL.name),
                PurchaseItemModel(advancePaymentToBeDeducted = BigDecimal.TEN, itemCategory = ItemCategory.Purchase.ADVANCE.name)
        )


        var result = Try.on { purchaseItems.validatePurchaseOrderRemaining() }
        assertTrue(result.isSuccess)

        // Fail
        result = Try.on {
            purchaseItems.plus(
                    PurchaseItemModel(advancePaymentToBeDeducted = 10000.00.toBigDecimal(), itemCategory = ItemCategory.Purchase.ADVANCE.name)
            ).validatePurchaseOrderRemaining()
        }
        assertTrue(result.isFailure)
        assert(result.toString().contains("Purchase order remaining amount must be greater than remaining deduction."))
    }

    @Test
    fun testValidateRemaining() {
        val purchaseItems = listOf(
                PurchaseItemModel(quantity = Quantity(BigDecimal.ONE, "EA"), poItemUnitPrice = BigDecimal.ONE, itemCategory = ItemCategory.Purchase.NORMAL.name),
                PurchaseItemModel(advancePaymentToBeDeducted = BigDecimal.TEN, itemCategory = ItemCategory.Purchase.ADVANCE.name)
        )
        val purchaseOrderModel = PurchaseOrderModel(
                currency = "THB",
                purchaseItems = purchaseItems
        )

        mockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")

        every { purchaseItems.validatePurchaseOrderRemaining() } returns Unit
        var result = Try.on { purchaseOrderModel.validateRemaining() }
        assertTrue(result.isSuccess)

        every { purchaseItems.validatePurchaseOrderRemaining() } throws IllegalArgumentException("Hello")
        result = Try.on { purchaseOrderModel.validateRemaining() }
        assertTrue(result.isFailure)
        assert(result.toString().contains("Hello"))

        unmockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")
    }

    @Test
    fun testValidateRemainingList() {
        val purchaseOrderModel = PurchaseOrderModel(
                purchaseItems = listOf(
                        PurchaseItemModel(quantity = Quantity(BigDecimal.ONE, "EA"), poItemUnitPrice = BigDecimal.ONE, itemCategory = ItemCategory.Purchase.NORMAL.name),
                        PurchaseItemModel(advancePaymentToBeDeducted = BigDecimal.TEN, itemCategory = ItemCategory.Purchase.ADVANCE.name)
                )
        )

        mockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")

        every { purchaseOrderModel.validateRemaining() } returns Unit
        var result = Try.on { listOf(purchaseOrderModel, purchaseOrderModel).validateRemaining() }
        assertTrue(result.isSuccess)
        verify(exactly = 2) { purchaseOrderModel.validateRemaining() }

        every { purchaseOrderModel.validateRemaining() } throws IllegalArgumentException("Hello")
        result = Try.on { listOf(purchaseOrderModel, purchaseOrderModel).validateRemaining() }
        assertTrue(result.isFailure)
        assert(result.toString().contains("Hello"))
        verify(exactly = 3) { purchaseOrderModel.validateRemaining() }

        unmockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")
    }

    @Test
    fun calculateOverDeliveryQuantityInPurchaseItemFnTest() {
        val poList = listOf(
                PurchaseItemModel(
                        linearId = "purchaseItemLinearId1",
                        quantity = Quantity(BigDecimal.valueOf(100), BigDecimal.valueOf(20), "unit"),
                        overDeliveryQuantity = Quantity(BigDecimal.valueOf(50), "unit")),
                PurchaseItemModel(
                        linearId = "purchaseItemLinearId2",
                        quantity = Quantity(BigDecimal.valueOf(100), BigDecimal.valueOf(20), "unit"),
                        overDeliveryQuantity = Quantity(BigDecimal.valueOf(50), "unit")
                ),
                PurchaseItemModel(
                        linearId = "purchaseItemLinearId3",
                        quantity = Quantity(BigDecimal.valueOf(100), BigDecimal.valueOf(20), "unit"),
                        overDeliveryQuantity = Quantity(BigDecimal.valueOf(50), "unit")
                ),
                PurchaseItemModel(
                        linearId = "purchaseItemLinearId4",
                        quantity = Quantity(BigDecimal.valueOf(100), BigDecimal.valueOf(100), "unit"),
                        overDeliveryQuantity = Quantity(BigDecimal.valueOf(50), BigDecimal.valueOf(5), "unit"))
        )

        val invoiceModel = InvoiceModel(
                linearId = "invoiceLinearId1",
                invoiceItems = listOf(
                        InvoiceItemModel(
                                purchaseItemLinearId = "purchaseItemLinearId1",
                                purchaseItemExternalId = "purchaseItemExternalId1",
                                purchaseOrderExternalId = "purchaseOrderExternalId1",
                                quantity = Quantity(BigDecimal.valueOf(1400), "ton"),
                                goodsReceivedItems = listOf(
                                        GoodsReceivedItemModel(
                                                linearId = "goodsReceivedItemLinearId1",
                                                purchaseItemLinearId = "purchaseItemLinearId1")
                                )),
                        InvoiceItemModel(
                                purchaseItemLinearId = "purchaseItemLinearId2",
                                purchaseItemExternalId = "purchaseItemExternalId2",
                                purchaseOrderExternalId = "purchaseOrderExternalId2",
                                quantity = Quantity(BigDecimal.valueOf(20), "ton"),
                                goodsReceivedItems = listOf(
                                        GoodsReceivedItemModel(
                                                linearId = "goodsReceivedItemLinearId2",
                                                purchaseItemLinearId = "purchaseItemLinearId2")
                                )
                        ),
                        InvoiceItemModel(
                                purchaseItemLinearId = "purchaseItemLinearId3",
                                purchaseItemExternalId = "purchaseItemExternalId3",
                                purchaseOrderExternalId = "purchaseOrderExternalId3",
                                quantity = Quantity(BigDecimal.valueOf(120), "ton"),
                                goodsReceivedItems = listOf(
                                        GoodsReceivedItemModel(
                                                linearId = "goodsReceivedItemLinearId3",
                                                purchaseItemLinearId = "purchaseItemLinearId3")
                                )
                        ),
                        InvoiceItemModel(
                                purchaseItemLinearId = "purchaseItemLinearId4",
                                purchaseItemExternalId = "purchaseItemExternalId4",
                                purchaseOrderExternalId = "purchaseOrderExternalId4",
                                quantity = Quantity(BigDecimal.valueOf(1400), "ton"),
                                goodsReceivedItems = listOf(
                                        GoodsReceivedItemModel(
                                                linearId = "goodsReceivedItemLinearId3",
                                                purchaseItemLinearId = "purchaseItemLinearId3")
                                )
                        )
                )
        )

        // case inv create from GR
        val purchaseItemsOut = calculateOverDeliveryQuantityInPurchaseItemFn(poList, invoiceModel)

        purchaseItemsOut.single { it.linearId == "purchaseItemLinearId1" }.let {
            assertEquals(BigDecimal.valueOf(1320).setScale(), it.overDeliveryQuantity!!.initial)
            assertEquals(BigDecimal.valueOf(100).setScale(), it.quantity!!.initial)
            assertEquals(BigDecimal.valueOf(20).setScale(), it.quantity!!.consumed)
        }

        purchaseItemsOut.single { it.linearId == "purchaseItemLinearId2" }.let {
            assertEquals(BigDecimal.valueOf(50).setScale(), it.overDeliveryQuantity!!.initial)
            assertEquals(BigDecimal.valueOf(100).setScale(), it.quantity!!.initial)
        }

        purchaseItemsOut.single { it.linearId == "purchaseItemLinearId3" }.let {
            assertEquals(BigDecimal.valueOf(50).setScale(), it.overDeliveryQuantity!!.initial)
            assertEquals(BigDecimal.valueOf(100).setScale(), it.quantity!!.initial)
        }

        purchaseItemsOut.single { it.linearId == "purchaseItemLinearId4" }.let {
            assertEquals(BigDecimal.valueOf(1405).setScale(), it.overDeliveryQuantity!!.initial)
            assertEquals(BigDecimal.valueOf(100).setScale(), it.quantity!!.initial)
            assertEquals(BigDecimal.valueOf(100).setScale(), it.quantity!!.consumed)
        }

    }

    @Test
    fun depleteAdvanceAmountAndQuantityToPurchaseItemsTest() {
        mockkObject(PurchaseUtils)

        val poi1 = PurchaseItemModel(linearId = "poi-1", poItemNo = "poi-1", poNumber = "po-1", itemCategory = ItemCategory.Purchase.NORMAL.name)
        val poi2 = PurchaseItemModel(linearId = "poi-2", poItemNo = "poi-2", poNumber = "po-2", itemCategory = ItemCategory.Purchase.NORMAL.name)
        val poi3 = PurchaseItemModel(linearId = "poi-3", poItemNo = "poi-3", poNumber = "po-3", itemCategory = ItemCategory.Purchase.NORMAL.name)
        val poi4 = PurchaseItemModel(linearId = "poi-adv-1", poItemNo = "poi-adv-1", poNumber = "po-1", itemCategory = ItemCategory.Purchase.ADVANCE.name)
        val poi5 = PurchaseItemModel(linearId = "poi-adv-2", poItemNo = "poi-adv-2", poNumber = "po-3", itemCategory = ItemCategory.Purchase.ADVANCE.name)
        val inputPurchaseItems = listOf(poi1, poi2, poi3, poi4, poi5)

        val invoiceItems = listOf(
                InvoiceItemModel(linearId = "invi-1", purchaseItemLinearId = poi1.linearId, purchaseItemExternalId = "poi-1", purchaseOrderExternalId = "po-1", itemCategory = ItemCategory.Invoice.NORMAL.name),
                InvoiceItemModel(linearId = "invi-2", purchaseItemLinearId = poi2.linearId, purchaseItemExternalId = "poi-2", purchaseOrderExternalId = "po-2", itemCategory = ItemCategory.Invoice.NORMAL.name),
                InvoiceItemModel(linearId = "invi-3", purchaseItemLinearId = poi4.linearId, purchaseItemExternalId = "poi-adv-1", purchaseOrderExternalId = "po-1", itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name),
                InvoiceItemModel(linearId = "invi-4", purchaseItemLinearId = poi5.linearId, purchaseItemExternalId = "poi-adv-2", purchaseOrderExternalId = "po-3", itemCategory = ItemCategory.Invoice.ADVANCE_REDEEM.name)
        )

        every {
            PurchaseUtils.processRedeemPurchaseItem(
                    any(),
                    match { it.linearId == "poi-adv-2" },
                    match { it.linearId == "invi-4" }
            )
        } answers { (firstArg() as MutableList<PurchaseItemModel>).plus(secondArg() as PurchaseItemModel).toMutableList() }

        every {
            PurchaseUtils.processDeductPurchaseItem(
                    inputPurchaseItems,
                    any(),
                    match { it.linearId == "poi-adv-1" },
                    match { it.linearId == "invi-3" }
            )
        } answers { (secondArg() as MutableList<PurchaseItemModel>).plus(thirdArg() as PurchaseItemModel).toMutableList() }

        every {
            PurchaseUtils.depleteNormalPurchaseItem(
                    any(),
                    match { it.linearId in listOf("poi-1", "poi-2") },
                    match { it.linearId in listOf("invi-1", "invi-2") }
            )
        } answers { (firstArg() as MutableList<PurchaseItemModel>).plus(secondArg() as PurchaseItemModel).toMutableList() }

        // Case no contract
        var expectedResult = listOf(poi1, poi2, poi4, poi5)

        var result = inputPurchaseItems.depleteAdvanceAmountAndQuantityToPurchaseItems(invoiceItems, false)
        assertEquals(expectedResult.size, result.size)
        assertEquals(expectedResult.distinctBy { it.linearId }.size, result.distinctBy { it.linearId }.size)
        assertEquals(result.size, result.distinctBy { it.linearId }.size)
        assert(expectedResult.containsAll(result))

        // Case with contract
        expectedResult = listOf(poi1, poi2, poi5)
        result = inputPurchaseItems.depleteAdvanceAmountAndQuantityToPurchaseItems(invoiceItems, true)
        assertEquals(expectedResult.size, result.size)
        assertEquals(expectedResult.distinctBy { it.linearId }.size, result.distinctBy { it.linearId }.size)
        assertEquals(result.size, result.distinctBy { it.linearId }.size)
        assert(expectedResult.containsAll(result))

        unmockkObject(PurchaseUtils)
    }

    @Test
    fun depleteNormalPurchaseItemTest() {
        mockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")

        val purchaseItemIn = PurchaseItemModel(linearId = "linear-1")
        val purchaseItemForInvoice = PurchaseItemModel(linearId = "linear-2", externalId = "dd")
        val invoiceItemModel = InvoiceItemModel(quantity = Quantity(BigDecimal.ONE, "EA"))
        val purchaseItemOut = PurchaseItemModel(linearId = "linear-3")

        // case not found in purchaseItemForDepleteIn
        every { purchaseItemForInvoice.depleteQuantity(invoiceItemModel.quantity!!.initial) } returns purchaseItemOut
        var result = PurchaseUtils.depleteNormalPurchaseItem(mutableListOf(purchaseItemIn), purchaseItemForInvoice, invoiceItemModel)
        assertEquals(2, result.size)
        assertEquals(mutableListOf(purchaseItemIn, purchaseItemOut), result)

        // case found in purchaseItemForDepleteIn
        every { purchaseItemIn.depleteQuantity(invoiceItemModel.quantity!!.initial) } returns purchaseItemOut
        result = PurchaseUtils.depleteNormalPurchaseItem(mutableListOf(purchaseItemIn), purchaseItemForInvoice.copy(linearId = purchaseItemIn.linearId), invoiceItemModel)
        assertEquals(1, result.size)
        assertEquals(mutableListOf(purchaseItemOut), result)

        // case have creditQuantity
        val expectPo = PurchaseItemModel(linearId = "linear-4")
        every { purchaseItemForInvoice.depleteQuantity(match { it.isEqual(900.toBigDecimal()) }) } returns expectPo
        result = PurchaseUtils.depleteNormalPurchaseItem(mutableListOf(), purchaseItemForInvoice,
            invoiceItemModel.copy(quantity = Quantity(1000.toBigDecimal(), "EA"), creditNoteQuantity = Quantity(100.toBigDecimal(), "EA")))
        assertEquals(1, result.size)
        assertEquals(mutableListOf(expectPo), result)

        unmockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")

    }

    @Test
    fun processRedeemPurchaseItemTest() {
        mockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")

        val purchaseItemIn = PurchaseItemModel(linearId = "linear-1")
        val purchaseItemForInvoice = PurchaseItemModel(linearId = "linear-2", externalId = "dd", advancePaymentRemainingAmount = BigDecimal.TEN)
        val invoiceItemModel = InvoiceItemModel(itemSubTotal = BigDecimal.TEN)
        val purchaseItemOut = PurchaseItemModel(linearId = "linear-3")

        // case not found in purchaseItemForRedeemIn
        every { purchaseItemForInvoice.depleteAdvanceAmountFromRedeemedInvoice(invoiceItemModel.itemSubTotal!!) } returns purchaseItemOut
        var result = PurchaseUtils.processRedeemPurchaseItem(mutableListOf(purchaseItemIn), purchaseItemForInvoice, invoiceItemModel)
        assertEquals(2, result.size)
        assertEquals(mutableListOf(purchaseItemIn, purchaseItemOut), result)

        // case found in purchaseItemForRedeemIn
        every { purchaseItemIn.depleteAdvanceAmountFromRedeemedInvoice(invoiceItemModel.itemSubTotal!!) } returns purchaseItemOut
        result = PurchaseUtils.processRedeemPurchaseItem(mutableListOf(purchaseItemIn), purchaseItemForInvoice.copy(linearId = purchaseItemIn.linearId), invoiceItemModel)
        println(result)
        assertEquals(1, result.size)
        assertEquals(mutableListOf(purchaseItemOut), result)

        // case advancePaymentRemainingAmount not enough
        val resultFailed = Try.on {
            PurchaseUtils.processRedeemPurchaseItem(mutableListOf(purchaseItemIn), purchaseItemForInvoice.copy(advancePaymentRemainingAmount = BigDecimal.ONE), invoiceItemModel)
        }
        assertTrue(resultFailed.isFailure)
        assert(resultFailed.toString().contains("Not enough advance remaining amount to be redeemed."))

        unmockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")

    }

    @Test
    fun processDeductPurchaseItemTest() {
        mockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")

        val purchaseItem1 = PurchaseItemModel(linearId = "poi1", purchaseOrderLinearId = "po1", itemCategory = ItemCategory.Purchase.ADVANCE.name)
        val purchaseItem2 = PurchaseItemModel(linearId = "poi2", purchaseOrderLinearId = "po1", itemCategory = ItemCategory.Purchase.ADVANCE.name)
        val purchaseItem3 = PurchaseItemModel(linearId = "poi3", purchaseOrderLinearId = "po2", itemCategory = ItemCategory.Purchase.ADVANCE.name)
        val purchaseItem4 = PurchaseItemModel(linearId = "poi4", purchaseOrderLinearId = "po2", itemCategory = ItemCategory.Purchase.ADVANCE.name)
        val purchaseItem5 = PurchaseItemModel(linearId = "poi5", purchaseOrderLinearId = "po2", itemCategory = ItemCategory.Purchase.NORMAL.name)
        val purchaseItem3Out = PurchaseItemModel(linearId = "poi3", purchaseOrderLinearId = "po2", itemCategory = ItemCategory.Purchase.ADVANCE.name, externalId = "jjj")
        val purchaseItem4In = PurchaseItemModel(linearId = "poi4", purchaseOrderLinearId = "po2", itemCategory = ItemCategory.Purchase.ADVANCE.name, externalId = "rrr")
        val allPurchaseItem = listOf(purchaseItem1, purchaseItem2, purchaseItem3, purchaseItem4, purchaseItem5)
        val invoiceItemIn = InvoiceItemModel(itemSubTotal = BigDecimal.TEN)

        val expectedResult = listOf(
                purchaseItem2, purchaseItem4In, purchaseItem3Out
        )
        every { listOf(purchaseItem3, purchaseItem4In).depleteDeductedValueToAdvancePurchaseItem(invoiceItemIn.itemSubTotal!!) } returns listOf(purchaseItem3Out, purchaseItem4In)

        val result = PurchaseUtils.processDeductPurchaseItem(
                allPurchaseItem, mutableListOf(purchaseItem2, purchaseItem4In), purchaseItem3, invoiceItemIn
        )
        assertEquals(expectedResult.size, result.size)
        assertEquals(expectedResult.distinctBy { it.linearId }.size, result.distinctBy { it.linearId }.size)
        assertEquals(result.size, result.distinctBy { it.linearId }.size)
        assert(expectedResult.containsAll(result))

        unmockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")
    }

    @Test
    fun depleteDeductedValueToAdvancePurchaseItemTest() {
        mockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")
        val purchaseItemIns = listOf(
                PurchaseItemModel(linearId = "1", advancePaymentToBeDeducted = BigDecimal.ONE),
                PurchaseItemModel(linearId = "2", advancePaymentToBeDeducted = BigDecimal.ZERO),
                PurchaseItemModel(linearId = "3", advancePaymentToBeDeducted = BigDecimal.ONE),
                PurchaseItemModel(linearId = "4", advancePaymentToBeDeducted = BigDecimal.ONE),
                PurchaseItemModel(linearId = "5", advancePaymentToBeDeducted = BigDecimal.TEN),
                PurchaseItemModel(linearId = "6", advancePaymentToBeDeducted = BigDecimal.TEN)
        )
        every { any<PurchaseItemModel>().depleteAdvanceAmountFromInvoiceWithDeduction(any()) } answers {
            val input = firstArg() as PurchaseItemModel
            input.copy(advancePaymentToBeDeducted = input.advancePaymentToBeDeducted!!.minus(secondArg() as BigDecimal))
        }

        // Success
        val expectedResult = listOf(
                PurchaseItemModel(linearId = "1", advancePaymentToBeDeducted = BigDecimal.ZERO),
                PurchaseItemModel(linearId = "3", advancePaymentToBeDeducted = BigDecimal.ZERO),
                PurchaseItemModel(linearId = "4", advancePaymentToBeDeducted = BigDecimal.ZERO),
                PurchaseItemModel(linearId = "5", advancePaymentToBeDeducted = BigDecimal(3))
        )
        val result = purchaseItemIns.depleteDeductedValueToAdvancePurchaseItem(BigDecimal.TEN)
        assertEquals(expectedResult.size, result.size)
        result.forEachIndexed { idx, poi ->
            val expected = expectedResult[idx]
            assertEquals(expected.linearId, poi.linearId)
            assert(poi.advancePaymentToBeDeducted.isEqual(expected.advancePaymentToBeDeducted))
        }

        // Failed
        val resultFailed = Try.on {
            purchaseItemIns.depleteDeductedValueToAdvancePurchaseItem(BigDecimal(100))
        }
        assertTrue(resultFailed.isFailure)
        assert(resultFailed.toString().contains("Not enough advance amount to be deduct."))

        unmockkStatic("th.co.dv.p2p.common.utilities.PurchaseUtilsKt")
    }

    @Test
    fun depleteAdvanceAmountFromInvoiceWithDeductionTest() {
        val input = PurchaseItemModel(advancePaymentToBeDeducted = BigDecimal.TEN)
        val result = input.depleteAdvanceAmountFromInvoiceWithDeduction(BigDecimal.ONE)
        assert(result.advancePaymentToBeDeducted.isEqual(BigDecimal(9)))

        val failedResult = Try.on { input.depleteAdvanceAmountFromInvoiceWithDeduction(11.toBigDecimal()) }
        assertTrue(failedResult.isFailure)
        assertTrue(failedResult.toString().contains(advancePaymentToBeDeductedNotEnough.format(11.toBigDecimal(), BigDecimal.TEN)))
    }

    @Test
    fun depleteAdvanceAmountFromRedeemedInvoiceTest() {
        val input = PurchaseItemModel(advancePaymentToBeDeducted = BigDecimal.ONE, advancePaymentRemainingAmount = BigDecimal.TEN)
        val result = input.depleteAdvanceAmountFromRedeemedInvoice(BigDecimal.ONE)
        assert(result.advancePaymentToBeDeducted.isEqual(BigDecimal(2)))
        assert(result.advancePaymentRemainingAmount.isEqual(BigDecimal(9)))
    }

    @Test
    fun `Test calculatePurchaseOrderInitialTotalRemainingTotal`() {
        mockkObject(PurchaseUtils)
        val quantity = Quantity(initial = BigDecimal.TEN, remaining = BigDecimal.TEN, unit = "BAG")
        val purchaseItem = PurchaseItemModel(poItemUnitPrice = BigDecimal.ONE, quantity = quantity)

        every { PurchaseUtils["calculatePurchaseItemInitialAndRemaining"](purchaseItem) } returns (BigDecimal(11) to BigDecimal(12))
        every { PurchaseUtils["calculatePurchaseItemInitialAndRemainingOverDelivery"](purchaseItem) } returns (BigDecimal(11) to BigDecimal(12))


        // Case all item is eligible to calculate
        val expectResult = PurchaseOrderModel(
            initialTotal = BigDecimal(22),
            remainingTotal = BigDecimal(24),
            initialOverDeliveryAmount = BigDecimal(22),
            remainingOverDeliveryAmount = BigDecimal(24)
        )
        var result = PurchaseUtils.calculatePurchaseOrderInitialRemaining(listOf(purchaseItem, purchaseItem))
        assertEquals(expectResult, result)

        // Case all item category is ADVANCE
        result = PurchaseUtils.calculatePurchaseOrderInitialRemaining(listOf(purchaseItem.copy(itemCategory = ItemCategory.Purchase.ADVANCE.name), purchaseItem.copy(itemCategory = ItemCategory.Purchase.ADVANCE.name)))
        assertTrue(0.00.toBigDecimal().compareTo(result.initialTotal) == 0)
        assertTrue(0.00.toBigDecimal().compareTo(result.remainingTotal) == 0)
        assertTrue(0.00.toBigDecimal().compareTo(result.initialOverDeliveryAmount) == 0)
        assertTrue(0.00.toBigDecimal().compareTo(result.remainingOverDeliveryAmount) == 0)

        // Case item1 is Advance , item2 is eligible to calculate
        result = PurchaseUtils.calculatePurchaseOrderInitialRemaining(listOf(purchaseItem.copy(itemCategory = ItemCategory.Purchase.ADVANCE.name), purchaseItem))
        assertTrue(11.00.toBigDecimal().compareTo(result.initialTotal) == 0)
        assertTrue(12.00.toBigDecimal().compareTo(result.remainingTotal) == 0)
        assertTrue(11.00.toBigDecimal().compareTo(result.initialOverDeliveryAmount) == 0)
        assertTrue(12.00.toBigDecimal().compareTo(result.remainingOverDeliveryAmount) == 0)

        // Case item1 is Advance , item2 is delete
        result = PurchaseUtils.calculatePurchaseOrderInitialRemaining(listOf(purchaseItem.copy(itemCategory = ItemCategory.Purchase.ADVANCE.name), purchaseItem.copy(deleteFlag = DeleteFlag.DELETED.name)))
        assertTrue(0.00.toBigDecimal().compareTo(result.initialTotal) == 0)
        assertTrue(0.00.toBigDecimal().compareTo(result.remainingTotal) == 0)
        assertTrue(0.00.toBigDecimal().compareTo(result.initialOverDeliveryAmount) == 0)
        assertTrue(0.00.toBigDecimal().compareTo(result.remainingOverDeliveryAmount) == 0)

        // Case item1 is delete , item2 is eligible to calculate
        result = PurchaseUtils.calculatePurchaseOrderInitialRemaining(listOf(purchaseItem.copy(deleteFlag = DeleteFlag.DELETED.name), purchaseItem))
        assertTrue(11.00.toBigDecimal().compareTo(result.initialTotal) == 0)
        assertTrue(12.00.toBigDecimal().compareTo(result.remainingTotal) == 0)
        assertTrue(11.00.toBigDecimal().compareTo(result.initialOverDeliveryAmount) == 0)
        assertTrue(12.00.toBigDecimal().compareTo(result.remainingOverDeliveryAmount) == 0)

        unmockkObject(PurchaseUtils)
    }

    @Test
    fun `Test calculatePurchaseItemInitialAndRemaining`() {
        val quantity = Quantity(initial = BigDecimal.TEN, remaining = BigDecimal.TEN, unit = "BAG")
        val purchaseItem = PurchaseItemModel(poItemUnitPrice = BigDecimal.ONE, quantity = quantity, itemCategory = ItemCategory.Purchase.NORMAL.name)

        // Case itemCategory Normal
        var expectResult = BigDecimal.TEN to BigDecimal.TEN
        var result = callMethod<PurchaseUtils, Pair<BigDecimal, BigDecimal>>(
            PurchaseUtils,
            "calculatePurchaseItemInitialAndRemaining",
            purchaseItem
        )
        assertTrue(expectResult.first.compareTo(result!!.first) == 0)
        assertTrue(expectResult.second.compareTo(result.second) == 0)

        // Case itemCategory Provision
        expectResult = BigDecimal(100) to BigDecimal(200)
        result = callMethod<PurchaseUtils, Pair<BigDecimal, BigDecimal>>(
            PurchaseUtils,
            "calculatePurchaseItemInitialAndRemaining",
            purchaseItem.copy(itemCategory = ItemCategory.Purchase.PROVISION.name, amount = BigDecimal(100), remainingAmount = BigDecimal(200))
        )
        assertTrue(expectResult.first.compareTo(result!!.first) == 0)
        assertTrue(expectResult.second.compareTo(result.second) == 0)

        // Case other itemCategory
        expectResult = BigDecimal.ZERO to BigDecimal.ZERO

        result = callMethod<PurchaseUtils, Pair<BigDecimal, BigDecimal>>(
            PurchaseUtils,
            "calculatePurchaseItemInitialAndRemaining",
             purchaseItem.copy(itemCategory = ItemCategory.Purchase.ADVANCE.name)
        )
        assertTrue(expectResult.first.compareTo(result!!.first) == 0)
        assertTrue(expectResult.second.compareTo(result.second) == 0)

    }

    @Test
    fun `Test calculatePurchaseItemInitialAndRemainingOverDelivery`() {
        val quantity = Quantity(initial = BigDecimal.TEN, remaining = BigDecimal.TEN, unit = "BAG")
        val purchaseItem = PurchaseItemModel(poItemUnitPrice = BigDecimal.ONE, overDeliveryQuantity = quantity, itemCategory = ItemCategory.Purchase.NORMAL.name)

        // Case itemCategory Normal
        var expectResult = BigDecimal.TEN to BigDecimal.TEN
        var result = callMethod<PurchaseUtils, Pair<BigDecimal, BigDecimal>>(
            PurchaseUtils,
            "calculatePurchaseItemInitialAndRemainingOverDelivery",
            purchaseItem
        )
        assertTrue(expectResult.first.compareTo(result!!.first) == 0)
        assertTrue(expectResult.second.compareTo(result.second) == 0)

        // Case other itemCategory
        expectResult = BigDecimal.ZERO to BigDecimal.ZERO
        result = callMethod<PurchaseUtils, Pair<BigDecimal, BigDecimal>>(
            PurchaseUtils,
            "calculatePurchaseItemInitialAndRemainingOverDelivery",
            purchaseItem.copy(itemCategory = ItemCategory.Purchase.PROVISION.name)
        )
        assertTrue(expectResult.first.compareTo(result!!.first) == 0)
        assertTrue(expectResult.second.compareTo(result.second) == 0)

    }
}
