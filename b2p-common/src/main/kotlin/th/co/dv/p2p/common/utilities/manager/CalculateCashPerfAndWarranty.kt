package th.co.dv.p2p.common.utilities.manager

import net.corda.core.internal.sum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.MovementClass
import th.co.dv.p2p.common.utilities.depleteQuantity
import th.co.dv.p2p.common.utilities.isNullOrEmpty
import th.co.dv.p2p.common.utilities.negativeToZero
import th.co.dv.p2p.common.utilities.sumByDecimal
import th.co.dv.p2p.common.validators.CashPerfAndWarrantyValidator
import th.co.dv.p2p.corda.base.models.*
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * this service will calculate cash performance guarantee and cash warranty amount per invoice item
 * first of all, will group data of invoice item by PO/GR header
 * second, calculate cash performance guarantee
 * then calculate cash warranty next
 */
object CalculateCashPerfAndWarranty {

    private val logger: Logger = LoggerFactory.getLogger(CalculateCashPerfAndWarranty::class.java)
    private val className = CalculateCashPerfAndWarranty::class.java.simpleName

    fun process(invoiceModel: InvoiceModel,
                goodsReceivedItems: List<GoodsReceivedItemModel>,
                purchaseOrders: List<PurchaseOrderModel>,
                goodsReceiveds: List<GoodsReceivedModel>,
                restoredGoodsReceiveds: List<GoodsReceivedModel>? = null): InvoiceModel {

        logger.info("$className.process invoiceModel NO : ${invoiceModel.externalId}, Goods item ID: ${goodsReceivedItems.map { it.linearId }}, " +
                "Purchase NO: ${purchaseOrders.map { it.purchaseOrderNumber }}")

        val normalItems = invoiceModel.invoiceItems.filter { it.itemCategory == ItemCategory.Invoice.NORMAL.name }
        val advanceDeductItems = invoiceModel.invoiceItems.filter { it.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name }
        val mapOfNormalItemsWithDeductAmount = getMapOfNormalItemWithDeductAmount(normalItems, advanceDeductItems)

        val normalGoodsReceiveds = prepareGoodsReceived(goodsReceiveds, restoredGoodsReceiveds)

        CashPerfAndWarrantyValidator.inspectAfterFetchData(normalGoodsReceiveds)

        val groupData = groupInvoiceItemByLinearHeader(normalItems, normalGoodsReceiveds, purchaseOrders)

        val invoiceUpdatedCashPerfGuaranteeAmount = processCashPerfGuaranteeAmount(
                invoiceModel = invoiceModel,
                normalGoodsReceiveds = normalGoodsReceiveds,
                purchaseOrders = purchaseOrders,
                mapOfNormalItemsWithDeductAmount = mapOfNormalItemsWithDeductAmount,
                groupData = groupData)

        val normalItemsAfterCashPerf = invoiceUpdatedCashPerfGuaranteeAmount.invoiceItems.filter { it.itemCategory == ItemCategory.Invoice.NORMAL.name }
        val updatedPurchase = updatePurchasePreWarrantyCalculation(purchaseOrders, normalItemsAfterCashPerf, mapOfNormalItemsWithDeductAmount)
        val groupDataAfterCashPerf = groupInvoiceItemByLinearHeader(invoiceUpdatedCashPerfGuaranteeAmount.invoiceItems, normalGoodsReceiveds, updatedPurchase)
        return processCashWarrantyAmount(
                invoiceModel = invoiceUpdatedCashPerfGuaranteeAmount,
                normalGoodsReceiveds = normalGoodsReceiveds,
                purchaseOrders = updatedPurchase,
                mapOfNormalItemsWithDeductAmount = mapOfNormalItemsWithDeductAmount,
                groupData = groupDataAfterCashPerf)
    }

    /**
     * Method for prepare goods received if this service is called by edit invoice service
     * that mean require to restore goodsReceived value
     * @param goodsReceiveds list of goodsReceived that involved this invoice
     * @param restoredGoodsReceiveds list of restored goodsReceived
     */
    private fun prepareGoodsReceived(goodsReceiveds: List<GoodsReceivedModel>, restoredGoodsReceiveds: List<GoodsReceivedModel>?): List<GoodsReceivedModel> {
        val normalGoodsReceiveds = goodsReceiveds.filter { gr ->
            gr.goodsReceivedItems.all { it.movementClass == MovementClass.NORMAL.name } }
        return when (isRequireRestoreGoodsReceiveds(restoredGoodsReceiveds)) {
            true -> updateGoodsReceiveds(restoredGoodsReceiveds, normalGoodsReceiveds)
            false -> normalGoodsReceiveds
        }
    }

    /**
     * Method for update goodsReceived when required to restored value
     * @param restoredGoodsReceiveds list of restored goodsReceived
     * @param normalGoodsReceiveds list of goodsReceived that involved this invoice
     */
    private fun updateGoodsReceiveds(restoredGoodsReceiveds: List<GoodsReceivedModel>?, normalGoodsReceiveds: List<GoodsReceivedModel>): List<GoodsReceivedModel> {
        val restoredGoodsReceivedExternalIds = restoredGoodsReceiveds!!.map { it.externalId!! }
        return normalGoodsReceiveds.map { goodsReceived ->
            when (goodsReceived.externalId in restoredGoodsReceivedExternalIds) {
                true -> restoredGoodsReceiveds.find { it.externalId == goodsReceived.externalId }!!
                false -> goodsReceived
            }
        }
    }

    /**
     * Method verfy goodsReceived is require to restored value or not
     * @param restoredGoodsReceiveds list of restored goodsReceived
     */
    private fun isRequireRestoreGoodsReceiveds(restoredGoodsReceiveds: List<GoodsReceivedModel>?) =
            !restoredGoodsReceiveds.isNullOrEmpty()

    /**
     * Update remaining amount of PO following formula:
     * Estimated PO Deductible Amount  =  Updated PO remaining amount - Subtotal of Updated Advance payment remaining amount
     *                                     - Subtotal of Updated Advance payment to be deducted - Updated Retention remaining amount
     *                                     - Updated Cash performance guarantee remaining amount
     * result of the calculation will be updated to [PurchaseOrderModel.remainingTotal]
     * @param purchaseOrders all purchase order related to iss invoice process
     * @param invoiceItems all normal invoice items in the process
     * @param mapOfNormalItemsWithDeductAmount map of normal item id to its advance deduct amount
     */
    private fun updatePurchasePreWarrantyCalculation(
            purchaseOrders: List<PurchaseOrderModel>,
            invoiceItems: List<InvoiceItemModel>,
            mapOfNormalItemsWithDeductAmount: Map<String, BigDecimal>
    ): List<PurchaseOrderModel> {
        return purchaseOrders.map { purchaseOrder ->
            val relatedInvoiceItems = purchaseOrder.purchaseItems.filter { it.deleteFlag.isNullOrBlank() }.flatMap { purchaseItem ->
                filterInvoiceItemsRelatedToPurchaseItems(purchaseItem, invoiceItems)
            }
            val retentionAmountToBeDeducted = relatedInvoiceItems.sumByDecimal { it.retentionAmount ?: BigDecimal.ZERO }
            val usedCashPerf = relatedInvoiceItems.sumByDecimal { it.cashPerfGuaranteeAmount ?: BigDecimal.ZERO }

            // subTotalAdvPaymentRemaining will not be updated during issue normal invoice
            val subTotalAdvPaymentRemaining = purchaseOrder.purchaseItems.sumByDecimal { it.advancePaymentRemainingAmount ?: BigDecimal.ZERO }
            val sumOfItemAdvanceDeductAmount = calculateSumOfItemAdvanceDeductAmountInSameHeader(relatedInvoiceItems, mapOfNormalItemsWithDeductAmount)
            val subTotalAdvPaymentToBeDeducted = purchaseOrder.purchaseItems.sumByDecimal {
                it.advancePaymentToBeDeducted ?: BigDecimal.ZERO
            }.minus(sumOfItemAdvanceDeductAmount)
            val poRemainingAmount = calculatePoRemainingAmountAfterDepleteInvoice(purchaseOrder, invoiceItems)

            // Whether GR consumed retention or cash perf amount over PO, the PO will update remaining of these amount to zero.
            val retentionRemaining = (purchaseOrder.retentionRemainingAmount ?: BigDecimal.ZERO).minus(retentionAmountToBeDeducted).negativeToZero()
            val cashPerfRemaining = (purchaseOrder.cashPerfGuaranteeRemainingAmount ?: BigDecimal.ZERO).minus(usedCashPerf).negativeToZero()

            val sumOfDeductedAmount = listOf(subTotalAdvPaymentRemaining, subTotalAdvPaymentToBeDeducted, retentionRemaining, cashPerfRemaining).sum()
            val estimatedDeductiblePOAmount = poRemainingAmount.minus(sumOfDeductedAmount).negativeToZero()
            purchaseOrder.copy(
                    remainingTotal = estimatedDeductiblePOAmount,
                    cashPerfGuaranteeRemainingAmount = cashPerfRemaining,
                    retentionRemainingAmount = retentionRemaining
            )
        }
    }

    /**
     * Method for process cash performance guarantee amount,
     * process all group data then separate way to calculate by flag from [isGetCashPerfGuaranteeFromGr]
     * and update only normal invoiceItem
     * then return invoiceModel with updatedNormalItem and another item
     *
     * @param invoiceModel: list of invoice
     * @param normalGoodsReceiveds: list of normal good received
     * @param purchaseOrders: list of purchaseOrder
     * @param mapOfNormalItemsWithDeductAmount: pair of invoice item linear id and item advance deduct amount
     * @param groupData: map of linear header with normal invoiceItem that already group by gr/po
     */
    private fun processCashPerfGuaranteeAmount(invoiceModel: InvoiceModel,
                                               normalGoodsReceiveds: List<GoodsReceivedModel>,
                                               purchaseOrders: List<PurchaseOrderModel>,
                                               mapOfNormalItemsWithDeductAmount: Map<String, BigDecimal>,
                                               groupData: Map<String, List<InvoiceItemModel>>): InvoiceModel {

        val accumulatedCashPerfAmountForPurchase = mutableMapOf<String, BigDecimal>()
        val (cashPerfFromGR, cashPerfFromPO) = groupData.toList()
                .partition { (linearId, _) -> isGetCashPerfGuaranteeFromGr(normalGoodsReceiveds, linearId) }

        val invoiceItemWithCashPerfGR = cashPerfFromGR.flatMap { (linearId, invoiceItems) ->
            val relatedPurchaseOrderModel = getPurchaseOrderModel(purchaseOrders, linearId, invoiceItems)
            val accumulatedCashPerfAmount = accumulatedCashPerfAmountForPurchase.getOrPut(relatedPurchaseOrderModel.linearId!!) { BigDecimal.ZERO }

            val relatedGoodsReceivedModel = normalGoodsReceiveds.single { it.linearId == linearId }
            val updatedItems = updateInvoiceItemCashPerfByGr(relatedGoodsReceivedModel, invoiceItems, mapOfNormalItemsWithDeductAmount)

            val totalCashPerf = updatedItems.mapNotNull { it.cashPerfGuaranteeAmount }.sum()
            accumulatedCashPerfAmountForPurchase[relatedPurchaseOrderModel.linearId] = accumulatedCashPerfAmount + totalCashPerf
            updatedItems
        }

        val invoiceItemWithCashPerfPO = cashPerfFromPO.flatMap { (linearId, invoiceItems) ->
            val relatedPurchaseOrderModel = getPurchaseOrderModel(purchaseOrders, linearId, invoiceItems)
            val accumulatedCashPerfAmount = accumulatedCashPerfAmountForPurchase.getOrPut(relatedPurchaseOrderModel.linearId!!) { BigDecimal.ZERO }

            val updatedItems = updateInvoiceItemCashPerfByPo(relatedPurchaseOrderModel, invoiceItems, mapOfNormalItemsWithDeductAmount, accumulatedCashPerfAmount)
            val totalCashPerf = updatedItems.mapNotNull { it.cashPerfGuaranteeAmount }.sum()
            accumulatedCashPerfAmountForPurchase[relatedPurchaseOrderModel.linearId] = accumulatedCashPerfAmount + totalCashPerf
            updatedItems
        }

        // flag for header if any groupData update cash by gr
        val cashPerfGuaranteeFromGr = cashPerfFromGR.isNotEmpty()
        val invoiceItemsWithCashPerf = invoiceItemWithCashPerfGR + invoiceItemWithCashPerfPO
        return updateFinalCashPerfGuarantee(cashPerfGuaranteeFromGr, invoiceItemsWithCashPerf, invoiceModel)
    }


    /**
     * Method will calculate cash performance guarantee by purchaseOrder then update into invoiceItems
     * first check that cash remaining from gr header is not 0 then calculate
     * @param relatedPurchaseOrderModel purchaseOrder that related with this group of invoiceItems
     * @param invoiceItems list of invoice item
     * @param mapOfNormalItemsWithDeductAmount map of invoice item linearId with deduct advance amount
     * @param accumulatedCashPerfAmount cash perf. amount that have been consumed in previous rounds of calculation
     */
    private fun updateInvoiceItemCashPerfByPo(
            relatedPurchaseOrderModel: PurchaseOrderModel,
            invoiceItems: List<InvoiceItemModel>,
            mapOfNormalItemsWithDeductAmount: Map<String, BigDecimal>,
            accumulatedCashPerfAmount: BigDecimal): List<InvoiceItemModel> {
        val cashPerfRemainingAmount = (relatedPurchaseOrderModel.cashPerfGuaranteeRemainingAmount ?: BigDecimal.ZERO).minus(accumulatedCashPerfAmount)
        return when (cashPerfRemainingAmount > BigDecimal.ZERO) {
            true -> {
                val estimatedDeductibleAmount = calculateEstimatedInvoiceDeductibleAmount(invoiceItems, mapOfNormalItemsWithDeductAmount)
                distributeDeductibleAmountToItem(invoiceItems, cashPerfRemainingAmount, true, estimatedDeductibleAmount)
            }
            false -> invoiceItems
        }
    }

    /**
     * Method will calculate cash performance guarantee by goodsReceived then update into invoiceItems
     * first check that cash remaining from gr header is not 0 then calculate
     * @param relatedGoodsReceivedModel goodsReceived that related with this group of invoiceItems
     * @param invoiceItems list of invoice item
     * @param mapOfNormalItemsWithDeductAmount map of invoice item linearId with deduct advance amount
     */
    private fun updateInvoiceItemCashPerfByGr(relatedGoodsReceivedModel: GoodsReceivedModel, invoiceItems: List<InvoiceItemModel>, mapOfNormalItemsWithDeductAmount: Map<String, BigDecimal>): List<InvoiceItemModel> {
        val cashPerfRemainingAmount = relatedGoodsReceivedModel.cashPerfDeductibleRemainingAmount ?: BigDecimal.ZERO
        return when (cashPerfRemainingAmount > BigDecimal.ZERO) {
            true -> {
                val estimatedDeductibleAmount = calculateEstimatedInvoiceDeductibleAmount(invoiceItems, mapOfNormalItemsWithDeductAmount)
                CashPerfAndWarrantyValidator.validateBeforeDeductByGr(estimatedDeductibleAmount, cashPerfRemainingAmount, relatedGoodsReceivedModel, invoiceItems)
                distributeDeductibleAmountToItem(invoiceItems, cashPerfRemainingAmount, true, estimatedDeductibleAmount)
            }
            false -> invoiceItems
        }
    }

    /**
     * Method for process cash warranty amount,
     * process all group data then separate way to calculate by flag from [isGetCashWarrantyFromGr]
     * and update only normal invoiceItem
     * then return invoiceModel with updatedNormalItem and another item
     *
     * @param invoiceModel: list of invoice
     * @param normalGoodsReceiveds: list of normal good received
     * @param purchaseOrders: list of purchaseOrder
     * @param mapOfNormalItemsWithDeductAmount: pair of invoice item linear id and item advance deduct amount
     * @param groupData: map of linear header with normal invoiceItem that already group by gr/po
     */
    private fun processCashWarrantyAmount(invoiceModel: InvoiceModel,
                                          normalGoodsReceiveds: List<GoodsReceivedModel>,
                                          purchaseOrders: List<PurchaseOrderModel>,
                                          mapOfNormalItemsWithDeductAmount: Map<String, BigDecimal>,
                                          groupData: Map<String, List<InvoiceItemModel>>): InvoiceModel {

        val accumulatedCashWarrantyAmountForPurchase = mutableMapOf<String, BigDecimal>()
        val (cashWarrantyFromGR, cashWarrantyFromPO) = groupData.toList()
                .partition { (linearId, _) -> isGetCashWarrantyFromGr(normalGoodsReceiveds, linearId) }

        val invoiceItemWithCashWarrantyGR = cashWarrantyFromGR.flatMap { (linearId, invoiceItems) ->
            val cashPerfGuaranteeAmount = invoiceItems.sumByDecimal { it.cashPerfGuaranteeAmount ?: BigDecimal.ZERO }

            val relatedPurchaseOrderModel = getPurchaseOrderModel(purchaseOrders, linearId, invoiceItems)
            val accumulatedCashWarrantyAmount = accumulatedCashWarrantyAmountForPurchase.getOrPut(relatedPurchaseOrderModel.linearId!!) { BigDecimal.ZERO }

            val relatedGoodsReceivedModel = normalGoodsReceiveds.single { it.linearId == linearId }
            val updatedItems = updateInvoiceItemCashWarrantyByGr(relatedGoodsReceivedModel, invoiceItems, mapOfNormalItemsWithDeductAmount, cashPerfGuaranteeAmount)

            accumulatedCashWarrantyAmountForPurchase[relatedPurchaseOrderModel.linearId] = accumulatedCashWarrantyAmount + updatedItems.mapNotNull { it.cashWarrantyAmount }.sum()
            updatedItems
        }

        val invoiceItemWithCashWarrantyPO = cashWarrantyFromPO.flatMap { (linearId, invoiceItems) ->
            val relatedPurchaseOrderModel = getPurchaseOrderModel(purchaseOrders, linearId, invoiceItems)
            val accumulatedCashWarrantyAmount = accumulatedCashWarrantyAmountForPurchase.getOrPut(relatedPurchaseOrderModel.linearId!!) { BigDecimal.ZERO }

            val updatedItems =
                    updateInvoiceItemCashWarrantyByPo(relatedPurchaseOrderModel, invoiceItems, accumulatedCashWarrantyAmount)
            accumulatedCashWarrantyAmountForPurchase[relatedPurchaseOrderModel.linearId] = accumulatedCashWarrantyAmount + updatedItems.mapNotNull { it.cashWarrantyAmount }.sum()
            updatedItems
        }

        val invoiceItemWithCashWarranty = invoiceItemWithCashWarrantyGR + invoiceItemWithCashWarrantyPO
        val cashWarrantyFromGr = cashWarrantyFromGR.isNotEmpty()
        return updateFinalCashWarranty(cashWarrantyFromGr, invoiceItemWithCashWarranty, invoiceModel)
    }

    /**
     * Method will calculate cash warranty by purchaseOrder then update into invoiceItems
     * first check that cash remaining from gr header is not 0 and cash remaining is more that estimate deductible amount
     * then calculate
     * @param relatedPurchaseOrderModel purchaseOrder that related with this group of invoiceItems
     * @param invoiceItems list of invoice item
     */
    private fun updateInvoiceItemCashWarrantyByPo(
            relatedPurchaseOrderModel: PurchaseOrderModel,
            invoiceItems: List<InvoiceItemModel>,
            accumulatedCashWarrantyAmount: BigDecimal): List<InvoiceItemModel> {
        val cashWarrantyRemainingAmount = (relatedPurchaseOrderModel.cashWarrantyRemainingAmount ?: BigDecimal.ZERO).minus(accumulatedCashWarrantyAmount)
        val estimatedDeductibleAmount = relatedPurchaseOrderModel.remainingTotal!!
        return when (cashWarrantyRemainingAmount > BigDecimal.ZERO && cashWarrantyRemainingAmount > estimatedDeductibleAmount) {
            true -> {
                val deductibleAmount = cashWarrantyRemainingAmount.minus(estimatedDeductibleAmount)
                distributeDeductibleAmountToItem(invoiceItems, deductibleAmount, false)
            }
            false -> invoiceItems
        }
    }


    /**
     * Method will calculate cash warranty by goodsReceived then update into invoiceItems
     * first check that cash remaining from gr header is not 0 then calculate
     * @param relatedGoodsReceivedModel goodsReceived that related with this group of invoiceItems
     * @param invoiceItems list of invoice item
     * @param mapOfNormalItemsWithDeductAmount map of invoice item linearId with deduct advance amount
     */
    private fun updateInvoiceItemCashWarrantyByGr(relatedGoodsReceivedModel: GoodsReceivedModel, invoiceItems: List<InvoiceItemModel>, mapOfNormalItemsWithDeductAmount: Map<String, BigDecimal>, cashPerfGuaranteeAmount: BigDecimal): List<InvoiceItemModel> {
        val cashWarrantyRemainingAmount = relatedGoodsReceivedModel.cashWarrantyDeductibleRemainingAmount
                ?: BigDecimal.ZERO
        return when (cashWarrantyRemainingAmount > BigDecimal.ZERO) {
            true -> {
                val estimatedDeductibleAmount = calculateEstimatedInvoiceDeductibleAmount(invoiceItems, mapOfNormalItemsWithDeductAmount).minus(cashPerfGuaranteeAmount)
                CashPerfAndWarrantyValidator.validateBeforeDeductByGr(estimatedDeductibleAmount, cashWarrantyRemainingAmount, relatedGoodsReceivedModel, invoiceItems)
                distributeDeductibleAmountToItem(invoiceItems, cashWarrantyRemainingAmount, false, estimatedDeductibleAmount)
            }
            false -> invoiceItems
        }
    }

    /**
     * Method for calculate purchaseOrder remaining amount
     * first deplete quantity that use in this invoice
     * then filter only normal item for get sum of purchaseOrder remaining amount
     * @param purchase purchase header
     * @param invoiceItems list of invoice item
     */
    private fun calculatePoRemainingAmountAfterDepleteInvoice(purchase: PurchaseOrderModel, invoiceItems: List<InvoiceItemModel>): BigDecimal {
        val depleteQtyPurchaseItem = purchase.purchaseItems.filter { it.deleteFlag.isNullOrBlank() }.map { purchaseItem ->
            val sumInitial = filterInvoiceItemsRelatedToPurchaseItems(purchaseItem, invoiceItems).sumByDecimal { it.quantity!!.initial }

            // In case that invoice consume over delivery quantity of PO, the case not effect PO remaining amount
            val finalQuantityToDeplete = if (sumInitial > purchaseItem.quantity!!.remaining) purchaseItem.quantity.remaining else sumInitial
            purchaseItem.depleteQuantity(finalQuantityToDeplete)
        }
        val normalPurchaseItem = depleteQtyPurchaseItem.filter { it.itemCategory == ItemCategory.Purchase.NORMAL.name }
        return normalPurchaseItem.sumByDecimal { it.quantity!!.remaining.multiply(it.poItemUnitPrice) }
    }

    /**
     * Method to filter invoice items that created from input purchase item
     * @param purchaseItem target purchase items
     * @param invoiceItems all normal invoice items
     */
    private fun filterInvoiceItemsRelatedToPurchaseItems(purchaseItem: PurchaseItemModel, invoiceItems: List<InvoiceItemModel>): List<InvoiceItemModel> {
        return invoiceItems.filter {
            when (it.purchaseItemLinearId.isNullOrBlank()) {
                true -> it.purchaseItemExternalId == purchaseItem.poItemNo && it.purchaseOrderExternalId == purchaseItem.poNumber
                else -> it.purchaseItemLinearId == purchaseItem.linearId
            }
        }
    }

    /**
     * Method for calculate advance deduct amount and distributed into invoice item
     * for calculate estimated invoice deductible amount
     *
     * if invoice item which groupBy poExternal is not have deduct will store as 0
     *
     * but if item which have deduct will use below formula
     *
     *              (itemSubTotal per item) x (sumOfDeductAmount per PO)
     * result =    ---------------------------------------------------
     *                        (sumOfAllItemSubTotal per PO)
     *
     * @param normalInvoiceItems: list of invoice item category normal
     * @param deductInvoiceItems: list of invoice item category advance deduct
     */
    private fun getMapOfNormalItemWithDeductAmount(normalInvoiceItems: List<InvoiceItemModel>, deductInvoiceItems: List<InvoiceItemModel>): Map<String, BigDecimal> {
        val mapOfNormalWithDeductAmount = mutableMapOf<String, BigDecimal>()

        val deductInvoiceItemsByPo = deductInvoiceItems.groupBy { it.purchaseOrderExternalId!! }
        val normalInvoiceItemsGroupByPo = normalInvoiceItems.groupBy { it.purchaseOrderExternalId!! }

        normalInvoiceItemsGroupByPo.forEach { (poExternalId, normalInvoiceItems) ->
            val relatedDeductInvoiceItems = deductInvoiceItemsByPo[poExternalId]
            val sumOfDeductAmount = relatedDeductInvoiceItems?.sumByDecimal { it.itemSubTotal!! }
            val sumOfAllItemSubTotal = normalInvoiceItems.sumByDecimal { it.itemSubTotal!! }
            normalInvoiceItems.forEach {
                when (relatedDeductInvoiceItems == null) {
                    //means that normal item do not have deduct (save 0)
                    true -> mapOfNormalWithDeductAmount[it.linearId!!] = BigDecimal.ZERO
                    false -> {
                        mapOfNormalWithDeductAmount[it.linearId!!] = (it.itemSubTotal!!.multiply(sumOfDeductAmount)).divide(sumOfAllItemSubTotal, 2, RoundingMode.HALF_UP)
                    }
                }
            }
        }
        return mapOfNormalWithDeductAmount

    }

    /**
     * Method for group invoiceItem by PurchaseOrder or GoodsReceived
     * if have goodsReceived item that related to invoice item -> group by goodsReceived
     * if do not have goodsReceived item -> group by purchaseOrder
     * @param normalItems: list of invoice item category normal
     * @param normalGoodsReceiveds: all of good received item that involved this invoice(movementClass NORMAL)
     * @param purchaseOrders: list of purchaseOrder
     */
    private fun groupInvoiceItemByLinearHeader(normalItems: List<InvoiceItemModel>,
                                               normalGoodsReceiveds: List<GoodsReceivedModel>,
                                               purchaseOrders: List<PurchaseOrderModel>): Map<String, List<InvoiceItemModel>> {
        return when (normalGoodsReceiveds.isNotEmpty()) {
            true -> groupInvoiceItemByGrLinearId(normalGoodsReceiveds, normalItems)
            false -> groupInvoiceItemByPoLinearId(purchaseOrders, normalItems)
        }
    }


    /**
     * group invoice item by goodsReceived linear id
     * @param normalGoodsReceiveds process only normal goodsReceived
     * @param normalItems process only normal invoiceItem list
     */
    private fun groupInvoiceItemByGrLinearId(normalGoodsReceiveds: List<GoodsReceivedModel>, normalItems: List<InvoiceItemModel>): Map<String, List<InvoiceItemModel>> {
        val mapOfLinearIdAndInvoiceItems = mutableMapOf<String, List<InvoiceItemModel>>()

        normalGoodsReceiveds.forEach { goodsReceived ->
            val grItemLinearId = goodsReceived.goodsReceivedItems.map { it.linearId }
            val relatedItem = normalItems.filter { it.goodsReceivedItems.any { goodsReceiveItem -> goodsReceiveItem.linearId in grItemLinearId } }
            mapOfLinearIdAndInvoiceItems[goodsReceived.linearId!!] = relatedItem
        }
        return mapOfLinearIdAndInvoiceItems
    }


    /**
     * group invoice item by purchaseOrder linear id
     * @param purchaseOrders all purchaseOrder
     * @param normalItems process only normal invoiceItem list
     */
    private fun groupInvoiceItemByPoLinearId(purchaseOrders: List<PurchaseOrderModel>, normalItems: List<InvoiceItemModel>): Map<String, List<InvoiceItemModel>> {
        val mapOfLinearIdAndInvoiceItems = mutableMapOf<String, List<InvoiceItemModel>>()

        purchaseOrders.forEach { purchaseOrder ->
            val poItemLinearId = purchaseOrder.purchaseItems.map { it.linearId }
            val relatedItem = normalItems.filter { it.purchaseItemLinearId in poItemLinearId }
            mapOfLinearIdAndInvoiceItems[purchaseOrder.linearId!!] = relatedItem
        }
        return mapOfLinearIdAndInvoiceItems
    }

    /**
     * This method returns flag for separate way to calculate cash performance guarantee
     * It will return true if have amount in gr header
     * @param normalGoodsReceiveds: all of good received that involved in this invoice
     * @param linear: linear id
     * */
    private fun isGetCashPerfGuaranteeFromGr(normalGoodsReceiveds: List<GoodsReceivedModel>, linear: String): Boolean {
        val cashPerfGuaranteeDeductibleAmount = normalGoodsReceiveds.find { it.linearId == linear }?.cashPerfGuaranteeDeductibleAmount
        return cashPerfGuaranteeDeductibleAmount != null
    }

    /**
     * Method for update cash warranty from good received
     * @param allGoodsReceiveds: all of good received that involved in this invoice
     * @param linear: linear id(if group by po this field is purchaseOrderLinearId
     * if group by gr this field is goodsReceivedLinearId)
     * */
    private fun isGetCashWarrantyFromGr(allGoodsReceiveds: List<GoodsReceivedModel>, linear: String): Boolean {
        val cashWarrantyDeductibleAmount = allGoodsReceiveds.find { it.linearId == linear }?.cashWarrantyDeductibleAmount
        return cashWarrantyDeductibleAmount != null
    }


    /**
     * Method for calculate estimated invoice deductible amount
     * @param invoiceItems: list of invoice item in same group
     * @param itemAdvanceDeductAmounts: pair of invoice item linear id and item advance deduct amount
     */
    private fun calculateEstimatedInvoiceDeductibleAmount(invoiceItems: List<InvoiceItemModel>, itemAdvanceDeductAmounts: Map<String, BigDecimal>): BigDecimal {
        val totalAmount = invoiceItems.map { it.itemSubTotal!! }.sumByDecimal { it }
        val sumAdvanceDeductAmount = calculateSumOfItemAdvanceDeductAmountInSameHeader(invoiceItems, itemAdvanceDeductAmounts)
        val totalRetentionAmount = invoiceItems.map { it.retentionAmount ?: BigDecimal.ZERO }.sumByDecimal { it }
        return (totalAmount - sumAdvanceDeductAmount - totalRetentionAmount)
    }

    /**
     * Method for calculate sum of item advance deduct amount in same header
     * for calculate estimated invoice deductible amount
     * @param invoiceItems: list of invoice item in same group
     * @param itemAdvanceDeductAmounts: pair of invoice item linear id and item advance deduct amount
     */
    private fun calculateSumOfItemAdvanceDeductAmountInSameHeader(invoiceItems: List<InvoiceItemModel>, itemAdvanceDeductAmounts: Map<String, BigDecimal>): BigDecimal {
        val listInvoiceItemLinearId = invoiceItems.map { it.linearId }
        val relatedDeductAmount = itemAdvanceDeductAmounts.filterKeys { it in listInvoiceItemLinearId }.map { it.value }
        return relatedDeductAmount.sum()
    }

    /**
     * Method for distribute cashPerfGuaranteeDeductibleAmount or cashWarrantyAmount in to item
     * @param normalItems: list of invoice item
     * @param deductibleAmount: cash warranty amount or cash performance guarantee
     * @param isCashPerfGuarantee: this flag is true if calculateCashPerfGuaranteeAmount call this function
     * @param estimatedInvoice: estimated invoice
     */
    private fun distributeDeductibleAmountToItem(normalItems: List<InvoiceItemModel>, deductibleAmount: BigDecimal, isCashPerfGuarantee: Boolean, estimatedInvoice: BigDecimal? = null): List<InvoiceItemModel> {
        val allDeductedValue = estimatedInvoice?.let { minOf(deductibleAmount, estimatedInvoice) } ?: deductibleAmount
        return distributeDeductibleAmountToItemProcess(normalItems, allDeductedValue, isCashPerfGuarantee)
    }

    /**
     * Method for distribute cashPerfGuaranteeDeductibleAmount or cashWarrantyAmount in to item
     * @param normalItems: list of invoice item
     * @param allDeductedValue: this value smaller of two values cash warranty , cash perf or estimatedInvoice
     * @param isCashPerfGuarantee: this flag is true if calculateCashPerfGuaranteeAmount call this function
     */
    private fun distributeDeductibleAmountToItemProcess(normalItems: List<InvoiceItemModel>, allDeductedValue: BigDecimal, isCashPerfGuarantee: Boolean): List<InvoiceItemModel> {
        var remainingDeductValue = allDeductedValue
        // Update cash performance or warranty to invoice item
        return normalItems.map { invoiceItem ->
            // if invoice item have no total
            val itemSubTotal = invoiceItem.itemSubTotal ?: BigDecimal.ZERO
            val deductThisRound = when {
                (remainingDeductValue == BigDecimal.ZERO || itemSubTotal == BigDecimal.ZERO) -> BigDecimal.ZERO
                else -> minOf(itemSubTotal, remainingDeductValue)
            }
            remainingDeductValue -= deductThisRound
            updateCashPerfOrWarrantyInItem(isCashPerfGuarantee, invoiceItem, deductThisRound)
        }
    }


    /**
     * Method for update invoice item with amount
     * @param isCashPerfGuarantee if true will update cashPerfGuaranteeAmount, otherwise will update cashWarrantyAmount
     */
    private fun updateCashPerfOrWarrantyInItem(isCashPerfGuarantee: Boolean, invoiceItemModel: InvoiceItemModel, amount: BigDecimal): InvoiceItemModel {
        return when (isCashPerfGuarantee) {
            true -> invoiceItemModel.copy(cashPerfGuaranteeAmount = amount)
            false -> invoiceItemModel.copy(cashWarrantyAmount = amount)
        }
    }


    /**
     * Method for generate final invoiceModel after calculate cash performance guarantee
     * @param isCashPerfGuaranteeFromGr flag of use cash performance from gr or not
     * @param invoiceItemWithCashPerfs updated invoice items with cash performance
     * @param invoiceModel invoiceModel header
     */
    private fun updateFinalCashPerfGuarantee(isCashPerfGuaranteeFromGr: Boolean,
                                             invoiceItemWithCashPerfs: List<InvoiceItemModel>,
                                             invoiceModel: InvoiceModel): InvoiceModel {
        val finalInvoiceItems = invoiceModel.invoiceItems.map { invoiceItem ->
            val invoiceItemWithCash = invoiceItemWithCashPerfs.find { it.linearId == invoiceItem.linearId }
            invoiceItem.copy(cashPerfGuaranteeAmount = invoiceItemWithCash?.cashPerfGuaranteeAmount)
        }

        val totalCashPerfAmount = finalInvoiceItems.sumByDecimal { it.cashPerfGuaranteeAmount ?: BigDecimal.ZERO }
        return invoiceModel.copy(
                cashPerfGuaranteeAmount = totalCashPerfAmount,
                cashPerfGuaranteeFromGr = isCashPerfGuaranteeFromGr,
                invoiceItems = finalInvoiceItems
        )
    }

    /**
     * Method for generate final invoiceModel after calculate cash warranty
     * @param isCashWarrantyFromGr flag of use cash warranty from gr or not
     * @param invoiceItemWithCashWarranty updated invoice items with cash warranty
     * @param invoiceModel invoiceModel header
     */
    private fun updateFinalCashWarranty(isCashWarrantyFromGr: Boolean,
                                        invoiceItemWithCashWarranty: List<InvoiceItemModel>,
                                        invoiceModel: InvoiceModel): InvoiceModel {

        val finalInvoiceItems = invoiceModel.invoiceItems.map { invoiceItem ->
            val invoiceItemWithCash = invoiceItemWithCashWarranty.find { it.linearId == invoiceItem.linearId }
            invoiceItem.copy(cashWarrantyAmount = invoiceItemWithCash?.cashWarrantyAmount)
        }
        val totalCashPerfAmount = finalInvoiceItems.sumByDecimal { it.cashWarrantyAmount ?: BigDecimal.ZERO }
        return invoiceModel.copy(
                cashWarrantyAmount = totalCashPerfAmount,
                cashWarrantyFromGr = isCashWarrantyFromGr,
                invoiceItems = finalInvoiceItems
        )
    }

    /**
     * Method for get purchaseOrder model from linearId
     * but some case maybe send gr linearId so will use purchaseOrderExternalId for find related purchase
     * @param purchaseOrders all purchaseOrder that related with InvoiceModel
     * @param linearId linearId which processing (can be id of gr or po)
     * @param invoiceItems invoiceItems which processing
     */
    private fun getPurchaseOrderModel(purchaseOrders: List<PurchaseOrderModel>, linearId: String, invoiceItems: List<InvoiceItemModel>): PurchaseOrderModel {
        return purchaseOrders.find { it.linearId == linearId } ?: run {
            val purchaseOrderNumber = invoiceItems.first().purchaseOrderExternalId!!
            purchaseOrders.find { it.purchaseOrderNumber == purchaseOrderNumber }!!
        }
    }


}