package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.adjustmentTypeNotSupport
import th.co.dv.p2p.common.constants.advancePaymentToBeDeductedNotEnough
import th.co.dv.p2p.common.constants.cannotFindPurchaseItemThatTiedWithCn
import th.co.dv.p2p.common.enums.AdjustmentType
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.Lifecycle
import th.co.dv.p2p.common.utilities.Conditions.using
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.*
import java.math.BigDecimal
import java.util.function.Predicate

object PurchaseUtils {

    internal val logger: Logger = LoggerFactory.getLogger(PurchaseUtils::class.java)
    internal val className = PurchaseUtils::class.java.simpleName

    /**
     * Method for edit update purchase Item data from updated invoice
     */
    fun updatePurchaseItemsFromUpdatedInvoice(
            purchaseItemFromPreviousIn: List<PurchaseItemModel>?,
            previousInvoiceModel: InvoiceModel?,
            purchaseItemIn: List<PurchaseItemModel>,
            invoiceModel: InvoiceModel,
            haveContract: Boolean,
            calculateOverDeliveryQuantityInPurchaseItemFn: (purchaseItems: List<PurchaseItemModel>, invoiceModel: InvoiceModel) -> List<PurchaseItemModel> = PurchaseUtils.calculateOverDeliveryQuantityInPurchaseItemFn
    ): List<PurchaseItemModel> {

        // restore po item from previous invoice
        val previousInvoiceItems = previousInvoiceModel?.invoiceItems ?: emptyList()
        var restoredPurchaseItems = purchaseItemFromPreviousIn ?: emptyList()

        if (previousInvoiceModel?.lifecycle != Lifecycle.InvoiceLifecycle.CANCELLED.name) {
            restoredPurchaseItems = purchaseItemFromPreviousIn?.restoreAdvanceAmountAndQuantityToPurchaseItems(previousInvoiceItems, haveContract)
                    ?: emptyList()
        }

        // update restored data to purchase item list
        val updatedRestoredPurchaseItem = replaceItemWithUpdatedItem(
                purchaseItemIn, restoredPurchaseItems
        )

        // re-calculate over delivery
        val updatedOverDeliveryPurchaseItems = calculateOverDeliveryQuantityInPurchaseItemFn(updatedRestoredPurchaseItem, invoiceModel)

        // deplete po item from invoice
        val invoiceItems = invoiceModel.invoiceItems
        val depletedPurchaseItems = updatedOverDeliveryPurchaseItems.depleteAdvanceAmountAndQuantityToPurchaseItems(invoiceItems, haveContract)

        return replaceItemWithUpdatedItem(updatedOverDeliveryPurchaseItems, depletedPurchaseItems)

    }

    /**
     * Method for cancel credit note from Purchase Item (deplete) and tied the new one to Purchase Item (restore)
     * we un-link credit note previous from Purchase Item and link the new one from current credit note
     *
     * Step 1: Call un-link [createOutputPurchaseItemsForDeleteCreditNote]
     * Step 2: Replace Purchase that updated (from cancel) to list that using to linked
     * Step 3: Call link [createOutputPurchaseItems]
     *
     * @param purchaseItemFromPreviousIn: Purchase item list that related with previous credit note
     * @param previousCreditNoteModel: Previous credit note
     * @param purchaseItemIn: Purchase item list that related with current credit note
     * @param creditNoteModel: Current credit note
     */
    fun updatePurchaseItemsFromUpdatedCreditNote(
            purchaseItemFromPreviousIn: List<PurchaseItemModel>,
            previousCreditNoteModel: CreditNoteModel,
            purchaseItemIn: List<PurchaseItemModel>,
            creditNoteModel: CreditNoteModel
    ): List<PurchaseItemModel> {

        // Cancel credit note value from purchase by previous credit note
        val purchaseItemCancelled = createOutputPurchaseItemsForDeleteCreditNote(
                purchaseItems = purchaseItemFromPreviousIn,
                adjustmentType = previousCreditNoteModel.adjustmentType!!,
                creditNoteItems = previousCreditNoteModel.creditNoteItems)

        // Call combine purchaseItem that cancelled with new Purchase item
        val purchaseItemAfterCancelled = replaceItemWithUpdatedItem(
                existingList = purchaseItemIn,
                updatedList = purchaseItemCancelled
        )

        val purchaseItemAfterIssued = createOutputPurchaseItems(
                purchaseItemModel = purchaseItemAfterCancelled,
                adjustmentType = creditNoteModel.adjustmentType!!,
                creditNoteItems = creditNoteModel.creditNoteItems)


        return combinePurchaseItem(
                subList = purchaseItemCancelled,
                mainList = purchaseItemAfterIssued
        )
    }

    /**
     * Function to create purchase Item output from credit note model that we want to delete
     * This function will delete credit note fields depend on adjustment type
     *
     * @param purchaseItems: Purchase item input that related with credit note item
     * @param adjustmentType: Adjustment type (QUANTITY)
     * @param creditNoteItems: credit note item that issued
     */
    fun createOutputPurchaseItemsForDeleteCreditNote(
            purchaseItems: List<PurchaseItemModel>,
            adjustmentType: String,
            creditNoteItems: List<CreditNoteItemModel>): List<PurchaseItemModel> {

        return purchaseItems.mapNotNull { purchaseItem ->
            val targetCreditNoteItem = creditNoteItems.find {
                it.purchaseItemLinearId == purchaseItem.linearId
            } ?: return@mapNotNull null

            if (adjustmentType != AdjustmentType.QUANTITY.name) throw IllegalArgumentException(adjustmentTypeNotSupport)

            val purchaseItemOut = purchaseItem.depleteQuantity(targetCreditNoteItem.quantity!!.initial)
            purchaseItemOut
        }
    }


    /**
     * Method for update item with updated list
     * if item have same id we replace existing with updated item
     *
     * @param existingList : Purchase item list
     * @param updatedList: Purchase item that updated
     */
    fun replaceItemWithUpdatedItem(existingList: List<PurchaseItemModel>,
                                   updatedList: List<PurchaseItemModel>): List<PurchaseItemModel> {
        return existingList.map { existingItem ->
            updatedList.find { it.linearId == existingItem.linearId } ?: existingItem
        }
    }

    /**
     * Function to create Purchase output from input credit note model
     * This function will update credit note fields depend on adjustment type
     *
     * @param purchaseItemModel: Purchase item input that related with credit note item
     * @param adjustmentType: Adjustment type (QUANTITY)
     * @param creditNoteItems: credit note item that issued
     */
    fun createOutputPurchaseItems(purchaseItemModel: List<PurchaseItemModel>,
                                  adjustmentType: String,
                                  creditNoteItems: List<CreditNoteItemModel>): List<PurchaseItemModel> {

        return purchaseItemModel.mapNotNull { purchaseItem ->
            val targetCreditNoteItem = creditNoteItems.find {
                it.purchaseItemLinearId == purchaseItem.linearId
            } ?: return@mapNotNull null

            if (adjustmentType != AdjustmentType.QUANTITY.name) throw IllegalArgumentException(adjustmentTypeNotSupport)

            val purchaseItemOut = purchaseItem.restoreQuantity(targetCreditNoteItem.quantity!!.initial)
            purchaseItemOut
        }
    }


    /**
     * Method for combine purchase Item
     * We add item in [subList] to [mainList] in case that item still not exist in main list
     *
     * @param subList: Purchase Item that we want to add in case not exist in main
     * @param mainList: Purchase Item that main list
     */
    fun combinePurchaseItem(subList: List<PurchaseItemModel>, mainList: List<PurchaseItemModel>): List<PurchaseItemModel> {
        val idsInMain = mainList.mapNotNull { it.linearId }
        val finalUpdated = subList.filterNot { it.linearId in idsInMain }
        return mainList + finalUpdated
    }


    /**
     * Method for restore normal value to purchase item
     * we will update on the same item in case the invoice input like to old purchase item
     *
     * @param purchaseItemForDeliveryIn: List of purchase item
     * @param purchaseItemFromInvoiceIn: Purchase item tie with invoice item input
     * @param invoiceItemIn: Invoice item
     */
    fun restoreNormalPurchaseItem(purchaseItemForDeliveryIn: MutableList<PurchaseItemModel>, purchaseItemFromInvoiceIn: PurchaseItemModel, invoiceItemIn: InvoiceItemModel): MutableList<PurchaseItemModel> {
        val restoreAmount = invoiceItemIn.quantity!!.initial.minus(invoiceItemIn.creditNoteQuantity?.initial?: BigDecimal.ZERO)
        var purchaseItemForDelivery = purchaseItemForDeliveryIn
        if (purchaseItemForDelivery.find { it.linearId == purchaseItemFromInvoiceIn.linearId } == null) {
            purchaseItemForDelivery.add(purchaseItemFromInvoiceIn.restoreQuantity(restoreAmount))
        } else {
            val oldPOI = purchaseItemForDelivery.find { it.linearId == purchaseItemFromInvoiceIn.linearId }
            val newPOI = oldPOI!!.restoreQuantity(restoreAmount)
            purchaseItemForDelivery = purchaseItemForDelivery.replaceIf(Predicate.isEqual(oldPOI), newPOI).toMutableList()
        }
        return purchaseItemForDelivery
    }

    /**
     * Method for restore redeem value to purchase item
     * we will update on the same item in case the invoice input like to old purchase item
     *
     * @param purchaseItemForRestoreRedeemIn: List of purchase item
     * @param purchaseItemFromInvoiceIn: Purchase item tie with invoice item input
     * @param invoiceItemIn: Invoice item
     */
    fun restoreRedeemPurchaseItem(purchaseItemForRestoreRedeemIn: MutableList<PurchaseItemModel>, purchaseItemFromInvoiceIn: PurchaseItemModel, invoiceItemIn: InvoiceItemModel): MutableList<PurchaseItemModel> {
        var purchaseItemForRestoreRedeem = purchaseItemForRestoreRedeemIn

        val advancePaymentToBeDeducted = purchaseItemFromInvoiceIn.advancePaymentToBeDeducted ?: BigDecimal.ZERO
        "Advance to be deducted must have value." using (advancePaymentToBeDeducted >= invoiceItemIn.itemSubTotal)

        if (purchaseItemForRestoreRedeem.find { it.linearId == purchaseItemFromInvoiceIn.linearId } == null) {
            purchaseItemForRestoreRedeem.add(purchaseItemFromInvoiceIn.restoreAdvanceAmountFromRedeemedInvoice(invoiceItemIn.itemSubTotal!!))
        } else {
            val oldPOI = purchaseItemForRestoreRedeem.find { it.linearId == purchaseItemFromInvoiceIn.linearId }
            val newPOI = oldPOI!!.restoreAdvanceAmountFromRedeemedInvoice(invoiceItemIn.itemSubTotal!!)
            purchaseItemForRestoreRedeem = purchaseItemForRestoreRedeem.replaceIf(Predicate.isEqual(oldPOI), newPOI).toMutableList()
        }
        return purchaseItemForRestoreRedeem
    }

    /**
     * Method for restore deduct value to purchase item
     * we will update on the same item in case the invoice input like to old purchase item
     *
     * @param allPurchaseItem: List of all purchase item
     * @param purchaseItemForRestoreRedeemIn: List of purchase item
     * @param purchaseItemFromInvoiceIn: Purchase item tie with invoice item input
     * @param invoiceItemIn: Invoice item
     */
    fun restoreDeductPurchaseItem(allPurchaseItem: List<PurchaseItemModel>,
                                  purchaseItemForRestoreRedeemIn: MutableList<PurchaseItemModel>,
                                  purchaseItemFromInvoiceIn: PurchaseItemModel,
                                  invoiceItemIn: InvoiceItemModel): MutableList<PurchaseItemModel> {

        var purchaseItemForRestoreDeducted = purchaseItemForRestoreRedeemIn

        // Get advance purchase and group by purchase order id
        val advancePurchaseItems = allPurchaseItem.filter { it.itemCategory == ItemCategory.Purchase.ADVANCE.name }
                .groupBy { it.purchaseOrderLinearId }
                .toMutableMap()

        // Get purchase order id that tie with invoice item
        val purchaseOrderLinearId = purchaseItemFromInvoiceIn.purchaseOrderLinearId

        // Get purchase item advance that tie with invoice item
        val involvedAdvancePurchaseItems = advancePurchaseItems[purchaseOrderLinearId] ?: emptyList()
        val finalAdvancePurchaseItems = involvedAdvancePurchaseItems.map { eachPurchaseItem ->
            purchaseItemForRestoreDeducted.find {
                it.linearId == eachPurchaseItem.linearId
            } ?: eachPurchaseItem
        }.sortedBy { it.poItemNo }.toList()

        // Update advance data in purchase item
        val restoredPurchaseItemResults = finalAdvancePurchaseItems.restoreDeductedValueToAdvancePurchaseItem(invoiceItemIn.itemSubTotal!!)

        restoredPurchaseItemResults.forEach { restoredPurchaseItemResult ->
            val targetPurchaseItem = purchaseItemForRestoreDeducted.find { it.linearId == restoredPurchaseItemResult.linearId }
            when (targetPurchaseItem == null) {
                true -> purchaseItemForRestoreDeducted.add(restoredPurchaseItemResult)
                false -> purchaseItemForRestoreDeducted = purchaseItemForRestoreDeducted.replaceIf(Predicate.isEqual(targetPurchaseItem), restoredPurchaseItemResult).toMutableList()
            }
        }
        return purchaseItemForRestoreDeducted
    }

    /**
     * Method for deplete normal value to purchase item
     * we will update on the same item in case the invoice input linked to purchase item
     *
     * @param purchaseItemForDepleteIn: List of purchase item
     * @param purchaseItemFromInvoiceIn: Purchase item tie with invoice item input
     * @param invoiceItemIn: Invoice item
     */
    fun depleteNormalPurchaseItem(purchaseItemForDepleteIn: MutableList<PurchaseItemModel>, purchaseItemFromInvoiceIn: PurchaseItemModel, invoiceItemIn: InvoiceItemModel): MutableList<PurchaseItemModel> {
        val depleteAmount = invoiceItemIn.quantity!!.initial.minus(invoiceItemIn.creditNoteQuantity?.initial?: BigDecimal.ZERO)

        var purchaseItemForDeplete = purchaseItemForDepleteIn
        val purchaseItemLinkedWithInvoice = purchaseItemForDeplete.find { it.linearId == purchaseItemFromInvoiceIn.linearId }
        if (purchaseItemLinkedWithInvoice == null) {
            purchaseItemForDeplete.add(purchaseItemFromInvoiceIn.depleteQuantity(depleteAmount))
        } else {
            val newPOI = purchaseItemLinkedWithInvoice.depleteQuantity(depleteAmount)
            purchaseItemForDeplete = purchaseItemForDeplete.replaceIf(Predicate.isEqual(purchaseItemLinkedWithInvoice), newPOI).toMutableList()
        }
        return purchaseItemForDeplete
    }

    /**
     * Method for redeem value to purchase item
     * we will update on the same item in case the invoice input linked to purchase item
     *
     * @param purchaseItemForRedeemIn: List of purchase item
     * @param purchaseItemFromInvoiceIn: Purchase item tie with invoice item input
     * @param invoiceItemIn: Invoice item
     */
    fun processRedeemPurchaseItem(purchaseItemForRedeemIn: MutableList<PurchaseItemModel>, purchaseItemFromInvoiceIn: PurchaseItemModel, invoiceItemIn: InvoiceItemModel): MutableList<PurchaseItemModel> {
        var purchaseItemForRedeem = purchaseItemForRedeemIn

        val advancePaymentToBeDeducted = purchaseItemFromInvoiceIn.advancePaymentRemainingAmount ?: BigDecimal.ZERO
        "Not enough advance remaining amount to be redeemed." using (advancePaymentToBeDeducted >= invoiceItemIn.itemSubTotal)

        val purchaseItemLinkedWithInvoice = purchaseItemForRedeem.find { it.linearId == purchaseItemFromInvoiceIn.linearId }
        if (purchaseItemLinkedWithInvoice == null) {
            purchaseItemForRedeem.add(purchaseItemFromInvoiceIn.depleteAdvanceAmountFromRedeemedInvoice(invoiceItemIn.itemSubTotal!!))
        } else {
            val newPOI = purchaseItemLinkedWithInvoice.depleteAdvanceAmountFromRedeemedInvoice(invoiceItemIn.itemSubTotal!!)
            purchaseItemForRedeem = purchaseItemForRedeem.replaceIf(Predicate.isEqual(purchaseItemLinkedWithInvoice), newPOI).toMutableList()
        }
        return purchaseItemForRedeem
    }

    /**
     * Method for restore deduct value to purchase item
     * we will update on the same item in case the invoice input linked to purchase item
     *
     * @param allPurchaseItem: List of all purchase item
     * @param purchaseItemForDeductIn: List of purchase item
     * @param purchaseItemFromInvoiceIn: Purchase item tie with invoice item input
     * @param invoiceItemIn: Invoice item
     */
    fun processDeductPurchaseItem(allPurchaseItem: List<PurchaseItemModel>,
                                  purchaseItemForDeductIn: MutableList<PurchaseItemModel>,
                                  purchaseItemFromInvoiceIn: PurchaseItemModel,
                                  invoiceItemIn: InvoiceItemModel): MutableList<PurchaseItemModel> {

        var purchaseItemForDeducted = purchaseItemForDeductIn

        // Get advance purchase and group by purchase order id
        val advancePurchaseItems = allPurchaseItem.filter { it.itemCategory == ItemCategory.Purchase.ADVANCE.name }
                .groupBy { it.purchaseOrderLinearId }
                .toMutableMap()

        // Get purchase order id that tie with invoice item
        val purchaseOrderLinearId = purchaseItemFromInvoiceIn.purchaseOrderLinearId

        // Get purchase item advance that tie with invoice item
        val involvedAdvancePurchaseItems = advancePurchaseItems[purchaseOrderLinearId] ?: emptyList()
        val finalAdvancePurchaseItems = involvedAdvancePurchaseItems.map { eachPurchaseItem ->
            purchaseItemForDeducted.find {
                it.linearId == eachPurchaseItem.linearId
            } ?: eachPurchaseItem
        }.sortedBy { it.poItemNo }.toList()

        // Update advance data in purchase item
        val deductedPurchaseItemResults = finalAdvancePurchaseItems.depleteDeductedValueToAdvancePurchaseItem(invoiceItemIn.itemSubTotal!!)

        deductedPurchaseItemResults.forEach { deductedPurchaseItemResult ->
            val targetPurchaseItem = purchaseItemForDeducted.find { it.linearId == deductedPurchaseItemResult.linearId }
            when (targetPurchaseItem == null) {
                true -> purchaseItemForDeducted.add(deductedPurchaseItemResult)
                false -> purchaseItemForDeducted = purchaseItemForDeducted.replaceIf(Predicate.isEqual(targetPurchaseItem), deductedPurchaseItemResult).toMutableList()
            }
        }
        return purchaseItemForDeducted
    }

    /**
     * Receives a list of statuses that we then parse into backend lifecycles
     * @param [purchaseStatuses] list of purchase statuses to be translated
     * @param [purchaseOrderHeader] true is Purchase Order Header, false is Purchase Order Item
     * @return list of backend lifecycle for invoice status and matching status
     */
    fun parseStatus(purchaseStatuses: List<String>?,
                    purchaseOrderHeader: Boolean): Set<String>? {

        logger.info("$className.parseStatus : purchaseStatuses $purchaseStatuses, purchaseOrderHeader $purchaseOrderHeader")

        val purchaseStatusTranslator = if (purchaseOrderHeader) {
            PurchaseStatus.PurchaseOrder()
        } else {
            PurchaseStatus.PurchaseOrderItem()
        }

        val translatedPurchaseStatuses = purchaseStatuses?.flatMap { poStatus ->
            purchaseStatusTranslator.fromDisplayName(poStatus).map { it.key } }?.toSet()

        logger.info("$className.parseStatus in purchaseOrderHeader:$purchaseOrderHeader are the following: $translatedPurchaseStatuses")

        return translatedPurchaseStatuses
    }

    /**
     * Method to update purchase order and item status for front-end
     * it will map lifecycle to front-end status and put it in field status instead of status from micro-service
     */
    fun updatePurchaseStatus(purchaseOrders: List<PurchaseOrderModel>): List<PurchaseOrderModel> {
        return purchaseOrders.map { purchase ->
            val item = updatePurchaseItemStatus(purchase.purchaseItems)
            purchase.display().copy(purchaseItems = item)
        }
    }

    /**
     * Method to update purchase item status for front-end
     */
    fun updatePurchaseItemStatus(purchaseItems: List<PurchaseItemModel>): List<PurchaseItemModel> {
        return purchaseItems.map { it.display() }
    }

    /**
     * Method to update over delivery from invoice
     * Given purchase item and invoice model to calculate for auto adjust new overDeliveryTolerance value with current remaining and new quantity
     * apply if purchase item remaining quantity less than sum of invoice quantity
     * newOverDeliveryTolerance = sum of quantity in invoice - total quantity remaining in po item
     */
    val calculateOverDeliveryQuantityInPurchaseItemFn: (List<PurchaseItemModel>, InvoiceModel) -> List<PurchaseItemModel> = { purchaseItems, invoiceModel ->
        purchaseItems.map { purchaseItem ->
            val filteredInvoiceItems = invoiceModel.invoiceItems.filter { it.purchaseItemLinearId == purchaseItem.linearId }

            val sumOfInvQuantity = filteredInvoiceItems.sumByDecimal { it.quantity!!.initial }
            val purchaseItemRemainingQuantity = purchaseItem.quantity!!.remaining + purchaseItem.overDeliveryQuantity!!.remaining

            if (purchaseItemRemainingQuantity < sumOfInvQuantity) {
                val newOverDeliveryTolerance = sumOfInvQuantity - purchaseItem.quantity.remaining + purchaseItem.overDeliveryQuantity.consumed
                purchaseItem.copy(
                        overDeliveryQuantity = Quantity(
                                newOverDeliveryTolerance,
                                purchaseItem.overDeliveryQuantity.consumed,
                                purchaseItem.overDeliveryQuantity.unit)
                )
            } else {
                purchaseItem
            }
        }
    }

    /**
     * Method for set initialTotal, remainingTotal, initialOverDeliveryAmount, remainingOverDeliveryAmount from item that tied with header
     * @param purchaseItems: list of purchase item model
     * @return initialTotal and remainingTotal
     */
    fun calculatePurchaseOrderInitialRemaining(purchaseItems: List<PurchaseItemModel>): PurchaseOrderModel {
        var initialTotal = BigDecimal.ZERO
        var remainingTotal = BigDecimal.ZERO
        var initialOverDelivery = BigDecimal.ZERO
        var remainingOverDelivery = BigDecimal.ZERO

        purchaseItems.forEach { purchaseItem ->
            if (purchaseItem.itemCategory == ItemCategory.Purchase.ADVANCE.name || !purchaseItem.deleteFlag.isNullOrBlank()) return@forEach

            val (itemInitialTotal, itemRemainingTotal) = calculatePurchaseItemInitialAndRemaining(purchaseItem)

            val (itemInitialTotalOverDelivery, itemReamingOverDelivery) = calculatePurchaseItemInitialAndRemainingOverDelivery(purchaseItem)

            initialTotal += itemInitialTotal
            remainingTotal += itemRemainingTotal
            initialOverDelivery += itemInitialTotalOverDelivery
            remainingOverDelivery += itemReamingOverDelivery
        }

        return PurchaseOrderModel(
            initialTotal = initialTotal,
            remainingTotal = remainingTotal,
            initialOverDeliveryAmount = initialOverDelivery,
            remainingOverDeliveryAmount = remainingOverDelivery
        )
    }

    /**
     * Method for calculate initialTotal and remainingTotal from item
     * in case item is advance we treat to zero,
     * in case item is provision we sum amount for initialTotal and sum remainingAmount for remainingTotal,
     * otherwise we sum of initial quantity multiply with unit price for initialTotal
     * and  sum of remaining quantity multiply with unit price for remainingTotal
     * @param purchaseItem: purchase item model
     * @return amount of initial total and remaining total
     */
    private fun calculatePurchaseItemInitialAndRemaining(purchaseItem: PurchaseItemModel): Pair<BigDecimal, BigDecimal> {
        val unitPrice = purchaseItem.poItemUnitPrice ?: BigDecimal.ZERO
        val quantityInitial = purchaseItem.quantity?.initial ?: BigDecimal.ZERO
        val quantityRemaining = purchaseItem.quantity?.remaining ?: BigDecimal.ZERO

        return when (purchaseItem.itemCategory) {
            ItemCategory.Purchase.PROVISION.name -> (purchaseItem.amount ?: BigDecimal.ZERO) to (purchaseItem.remainingAmount ?: BigDecimal.ZERO)
            ItemCategory.Purchase.NORMAL.name -> unitPrice.multiply(quantityInitial) to unitPrice.multiply(quantityRemaining)
            else -> BigDecimal.ZERO to BigDecimal.ZERO
        }
    }

    /**
     * Method for calculate initialOverDeliveryAmount and remainingOverDeliveryAmount from item
     * in case item is advance we treat to zero,
     * in case item is provision we treat to zero,
     * otherwise we sum of overDeliveryQuantity initial multiply with unit price for initialOverDeliveryAmount
     * and  sum of overDeliveryQuantity remaining multiply with unit price for remainingOverDeliveryAmount
     * @param purchaseItem: purchase item model

     * @return amount of initial overDeliveryQuantity and remaining overDeliveryQuantity
     */
    private fun calculatePurchaseItemInitialAndRemainingOverDelivery(purchaseItem: PurchaseItemModel): Pair<BigDecimal, BigDecimal> {
        val unitPrice = purchaseItem.poItemUnitPrice ?: BigDecimal.ZERO
        val quantityInitial = purchaseItem.overDeliveryQuantity?.initial ?: BigDecimal.ZERO
        val quantityRemaining = purchaseItem.overDeliveryQuantity?.remaining ?: BigDecimal.ZERO

        return when (purchaseItem.itemCategory) {
            ItemCategory.Purchase.NORMAL.name -> unitPrice.multiply(quantityInitial) to unitPrice.multiply(quantityRemaining)
            else ->  BigDecimal.ZERO to  BigDecimal.ZERO
        }
    }
}

/**
 *   Restores value of quantity and/or advance amount for existing purchase item
 *   when invoice is edited, resubmitted or cancel.
 *
 *   @param invoiceItemInput : The invoice item input
 *   @param haveContract : Param to tell that there are contracts included in transaction
 *
 *   @return updated purchase item
 */
fun List<PurchaseItemModel>.restoreAdvanceAmountAndQuantityToPurchaseItems(invoiceItemInput: List<InvoiceItemModel>, haveContract: Boolean): List<PurchaseItemModel> {
    var purchaseItemForDelivery: MutableList<PurchaseItemModel> = mutableListOf()
    var purchaseItemForRestoreRedeem: MutableList<PurchaseItemModel> = mutableListOf()
    var purchaseItemForRestoreDeducted: MutableList<PurchaseItemModel> = mutableListOf()

    invoiceItemInput.forEach { invoiceItemIn ->

        // If an invoice item is ADVANCE DEDUCT and build from contract, we skip the process
        if (invoiceItemIn.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name && haveContract) return@forEach

        val purchaseItemFromInvoiceIn = this.single { it.poNumber == invoiceItemIn.purchaseOrderExternalId && it.poItemNo == invoiceItemIn.purchaseItemExternalId }
        val isRedeem = (purchaseItemFromInvoiceIn.itemCategory == ItemCategory.Purchase.ADVANCE.name)
                && (invoiceItemIn.itemCategory == ItemCategory.Invoice.ADVANCE_REDEEM.name)

        val isDeduct = (purchaseItemFromInvoiceIn.itemCategory == ItemCategory.Purchase.ADVANCE.name)
                && (invoiceItemIn.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name)

        val isNormal = (purchaseItemFromInvoiceIn.itemCategory == ItemCategory.Purchase.NORMAL.name)
            && (invoiceItemIn.itemCategory == ItemCategory.Invoice.NORMAL.name)

        when {
            isRedeem -> purchaseItemForRestoreRedeem = PurchaseUtils.restoreRedeemPurchaseItem(
                    purchaseItemForRestoreRedeemIn = purchaseItemForRestoreRedeem,
                    purchaseItemFromInvoiceIn = purchaseItemFromInvoiceIn,
                    invoiceItemIn = invoiceItemIn)
            isDeduct -> purchaseItemForRestoreDeducted = PurchaseUtils.restoreDeductPurchaseItem(
                    allPurchaseItem = this,
                    purchaseItemForRestoreRedeemIn = purchaseItemForRestoreDeducted,
                    purchaseItemFromInvoiceIn = purchaseItemFromInvoiceIn,
                    invoiceItemIn = invoiceItemIn)
            isNormal -> purchaseItemForDelivery = PurchaseUtils.restoreNormalPurchaseItem(
                    purchaseItemForDeliveryIn = purchaseItemForDelivery,
                    purchaseItemFromInvoiceIn = purchaseItemFromInvoiceIn,
                    invoiceItemIn = invoiceItemIn
            )
        }
    }

    return purchaseItemForDelivery + purchaseItemForRestoreRedeem + purchaseItemForRestoreDeducted
}

/**
 * Method for restore deduct value to advance purchase item
 * in case we have multiple advance purchase item
 * we will restore in first item in not enough we will continue deduct in other item
 *
 * @param invoiceSubTotal: to be restored value in invoice item
 */
fun List<PurchaseItemModel>.restoreDeductedValueToAdvancePurchaseItem(invoiceSubTotal: BigDecimal): List<PurchaseItemModel> {
    var remainingRestoreValue = invoiceSubTotal

    // Update advance data in purchase item
    val restoredAdvancePurchaseItems = this.mapNotNull { purchaseItem ->
        // if purchase item have no advancePaymentToBeDeducted, skip it
        // availableRestoreAdvanceToBeDeducted we calculate from
        val availableRestoreAdvanceToBeDeducted = (purchaseItem.advanceInitialAmount ?: BigDecimal.ZERO) -
                (purchaseItem.advancePaymentRemainingAmount ?: BigDecimal.ZERO) -
                (purchaseItem.advancePaymentToBeDeducted ?: BigDecimal.ZERO)

        when {
            (remainingRestoreValue == BigDecimal.ZERO || availableRestoreAdvanceToBeDeducted == BigDecimal.ZERO) -> null
            else -> {
                // Get the actual value for restore
                val restoredValue = minOf(availableRestoreAdvanceToBeDeducted, remainingRestoreValue)

                remainingRestoreValue -= restoredValue
                purchaseItem.restoreAdvanceAmountFromInvoiceWithDeduction(restoredValue)
            }
        }
    }

    "Not enough advance amount to be restore." using (remainingRestoreValue.compareTo(BigDecimal.ZERO) == 0)
    return restoredAdvancePurchaseItems
}

/**
 * Restore advance deducted value [PurchaseItemModel::advancePaymentToBeDeducted] when cancelled invoice deducted
 */
fun PurchaseItemModel.restoreAdvanceAmountFromInvoiceWithDeduction(deductedAmount: BigDecimal) = copy(
        advancePaymentToBeDeducted = advancePaymentToBeDeducted!!.plus(deductedAmount)
)

/**
 * Restore advance redeem value [PurchaseItemModel::advancePaymentRemainingAmount], [PurchaseItemModel::advancePaymentToBeDeducted] when cancelled invoice redeem
 */
fun PurchaseItemModel.restoreAdvanceAmountFromRedeemedInvoice(originalRedeemedAmount: BigDecimal) = copy(
        advancePaymentRemainingAmount = advancePaymentRemainingAmount!!.plus(originalRedeemedAmount),
        advancePaymentToBeDeducted = advancePaymentToBeDeducted!!.minus(originalRedeemedAmount)
)

/**
 * Method for deplete deduct value to advance purchase item
 * in case we have multiple advance purchase item
 * we will deduct in first item in not enough we will continue deduct in other item
 *
 * @param invoiceSubTotal: to be restored value in invoice item
 */
fun List<PurchaseItemModel>.depleteDeductedValueToAdvancePurchaseItem(invoiceSubTotal: BigDecimal): List<PurchaseItemModel> {
    var deductingValue = invoiceSubTotal

    // Update advance data in purchase item
    val deductedAdvancePurchaseItems = this.mapNotNull { purchaseItem ->
        val availableToBeDeduct = purchaseItem.advancePaymentToBeDeducted ?: BigDecimal.ZERO

        when {
            (deductingValue == BigDecimal.ZERO || availableToBeDeduct == BigDecimal.ZERO) -> null
            else -> {
                // Get the actual value for deplete
                val depleteValue = minOf(availableToBeDeduct, deductingValue)
                deductingValue -= depleteValue
                purchaseItem.depleteAdvanceAmountFromInvoiceWithDeduction(depleteValue)
            }
        }
    }

    "Not enough advance amount to be deduct." using (deductingValue.compareTo(BigDecimal.ZERO) == 0)
    return deductedAdvancePurchaseItems
}

/**
 * This method to deduct from advance payment to be deduct in purchase item.
 *
 * advancePaymentToBeDeducted = advancePaymentToBeDeducted - deductAmount
 */
fun PurchaseItemModel.depleteAdvanceAmountFromInvoiceWithDeduction(deductedAmount: BigDecimal): PurchaseItemModel {
    val advancePaymentToBeDeducted = advancePaymentToBeDeducted ?: BigDecimal.ZERO
    advancePaymentToBeDeductedNotEnough.format(deductedAmount, advancePaymentToBeDeducted) using (deductedAmount.isLessOrEqual(advancePaymentToBeDeducted))
    return copy(advancePaymentToBeDeducted = advancePaymentToBeDeducted.minus(deductedAmount))
}

/**
 * Deplete advance redeem value [PurchaseItemModel::advancePaymentRemainingAmount], [PurchaseItemModel::advancePaymentToBeDeducted]
 */
fun PurchaseItemModel.depleteAdvanceAmountFromRedeemedInvoice(redeemedAmount: BigDecimal) = copy(
        advancePaymentRemainingAmount = advancePaymentRemainingAmount!!.minus(redeemedAmount),
        advancePaymentToBeDeducted = advancePaymentToBeDeducted!!.plus(redeemedAmount)
)


/**
 * Method for adjust quantity in purchase item
 * for resubmit credit note we will decrease remaining in purchase item for old credit note
 * after that we increase remaining in purchase item for the new one
 */
fun List<PurchaseItemModel>.adjustPurchaseItems(newCreditNoteItems: List<CreditNoteItemModel>,
                                                existingCreditNoteItems: List<CreditNoteItemModel>): List<PurchaseItemModel> {

    // Deplete for all existing Purchase Item based on the existing CN
    val existingPurchaseItemsOut = existingCreditNoteItems.map { creditNoteItem ->
        val purchaseItem = this.find { it.linearId == creditNoteItem.purchaseItemLinearId }!!

        val initial = creditNoteItem.quantity?.initial ?: BigDecimal.ZERO
        purchaseItem.depleteQuantity(initial)
    }

    // Restore for all new Purchase Item based on the new CN
    val newPurchaseItemsOut = newCreditNoteItems.map { creditNoteItem ->
        // We find target purchase item from existingPurchaseItemsOut first because this list is latest version
        val targetPurchaseItem = existingPurchaseItemsOut.find { it.linearId == creditNoteItem.purchaseItemLinearId }
                ?: this.find { it.linearId == creditNoteItem.purchaseItemLinearId }
                ?: throw IllegalArgumentException(cannotFindPurchaseItemThatTiedWithCn)
        val initial = creditNoteItem.quantity?.initial ?: BigDecimal.ZERO
        targetPurchaseItem.restoreQuantity(initial)
    }

    return (newPurchaseItemsOut + existingPurchaseItemsOut).distinctBy { it.linearId }
}

/**
 * Method for validate that purchase or has remaining amount against all deduction amount
 */
fun PurchaseOrderModel.validateRemaining() {
    this.purchaseItems.validatePurchaseOrderRemaining()
}

fun List<PurchaseOrderModel>.validateRemaining() {
    this.forEach { it.validateRemaining() }
}

fun List<PurchaseItemModel>.validatePurchaseOrderRemaining() {
    val (normalItems, advanceItems) = this.partition { it.itemCategory != ItemCategory.Purchase.ADVANCE.name }
    val subTotal = normalItems.sumByDecimal { it.quantity!!.remaining.multiply(it.poItemUnitPrice!!) }
    val advanceSubTotal = advanceItems.sumByDecimal { it.advancePaymentToBeDeducted ?: BigDecimal.ZERO }
    "Purchase order remaining amount must be greater than remaining deduction." using (subTotal - advanceSubTotal >= BigDecimal.ZERO)
}

/**
 *   Depletes value of quantity and/or advance amount for existing purchase item
 *   when invoice is edited, resubmitted or issue.
 *
 *   @receiver The purchase item that tie with invoice item
 *   @param invoiceItemInput : The invoice item input
 *   @param haveContract : Param to tell that there are contracts included in transaction
 *
 *   @return updated purchase item
 */
fun List<PurchaseItemModel>.depleteAdvanceAmountAndQuantityToPurchaseItems(invoiceItemInput: List<InvoiceItemModel>, haveContract: Boolean): List<PurchaseItemModel> {
    var purchaseItemForDeplete: MutableList<PurchaseItemModel> = mutableListOf()
    var purchaseItemForRedeem: MutableList<PurchaseItemModel> = mutableListOf()
    var purchaseItemForDeducted: MutableList<PurchaseItemModel> = mutableListOf()

    invoiceItemInput.forEach { invoiceItemIn ->

        // If an invoice item is ADVANCE DEDUCT and build from contract, we skip the process
        if (invoiceItemIn.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name && haveContract) return@forEach

        val purchaseItemFromInvoiceIn = this.single { it.poNumber == invoiceItemIn.purchaseOrderExternalId &&  it.poItemNo == invoiceItemIn.purchaseItemExternalId }
        val isRedeem = (purchaseItemFromInvoiceIn.itemCategory == ItemCategory.Purchase.ADVANCE.name)
                && (invoiceItemIn.itemCategory == ItemCategory.Invoice.ADVANCE_REDEEM.name)

        val isDeduct = (purchaseItemFromInvoiceIn.itemCategory == ItemCategory.Purchase.ADVANCE.name)
                && (invoiceItemIn.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name)

        val isNormal = (purchaseItemFromInvoiceIn.itemCategory == ItemCategory.Purchase.NORMAL.name)
            && (invoiceItemIn.itemCategory == ItemCategory.Invoice.NORMAL.name)

        when {
            isRedeem -> purchaseItemForRedeem = PurchaseUtils.processRedeemPurchaseItem(
                    purchaseItemForRedeemIn = purchaseItemForRedeem,
                    purchaseItemFromInvoiceIn = purchaseItemFromInvoiceIn,
                    invoiceItemIn = invoiceItemIn)
            isDeduct -> purchaseItemForDeducted = PurchaseUtils.processDeductPurchaseItem(
                    allPurchaseItem = this,
                    purchaseItemForDeductIn = purchaseItemForDeducted,
                    purchaseItemFromInvoiceIn = purchaseItemFromInvoiceIn,
                    invoiceItemIn = invoiceItemIn)
            isNormal -> purchaseItemForDeplete = PurchaseUtils.depleteNormalPurchaseItem(
                    purchaseItemForDepleteIn = purchaseItemForDeplete,
                    purchaseItemFromInvoiceIn = purchaseItemFromInvoiceIn,
                    invoiceItemIn = invoiceItemIn
            )
        }
    }

    return purchaseItemForDeplete + purchaseItemForRedeem + purchaseItemForDeducted
}