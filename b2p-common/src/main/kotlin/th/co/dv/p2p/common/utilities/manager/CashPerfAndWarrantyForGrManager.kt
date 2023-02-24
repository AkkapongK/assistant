package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.common.enums.InvoiceAction
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.MovementClass
import th.co.dv.p2p.common.utilities.negativeToZero
import th.co.dv.p2p.common.utilities.sumByDecimal
import th.co.dv.p2p.corda.base.models.GoodsReceivedModel
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import java.math.BigDecimal

/**
 * This class is for manage cash performance guarantee and warranty remaining amount
 * select related invoice item for sum cash amount then,
 * calculate new remaining by using topicNotify for select formula
 */
object CashPerfAndWarrantyForGrManager {


    fun isNormalInvoiceItem(invoiceItemModel: InvoiceItemModel) = invoiceItemModel.itemCategory == ItemCategory.Purchase.NORMAL.name
    fun isNormalGr(goodsReceived: GoodsReceivedModel): Boolean = goodsReceived.goodsReceivedItems.all { it.movementClass == MovementClass.NORMAL.name }
    fun isInvoiceRelatedGr(invoiceItemModel: InvoiceItemModel, goodsReceived: GoodsReceivedModel) = invoiceItemModel.goodsReceivedItems.any { it.goodsReceivedLinearId == goodsReceived.linearId }


    /**
     * method for calculate new cashPerformanceGuarantee remaining amount which deduct-
     * sum of all consumed cashPerformanceGuarantee amount on invoice
     *
     * @param relateInvoiceItems: Invoice item that tied with goods received
     * @param goodsReceived: Goods received
     * @param invoiceAction: Topic that we use to action on oldRemainingAmount
     * @param isCashPerfFromGr: flag that invoice use cash performance from gr header
     */
    fun getNewCashPerfRemainingAmount(relateInvoiceItems: List<InvoiceItemModel>,
                                      goodsReceived: GoodsReceivedModel,
                                      invoiceAction: String,
                                      isCashPerfFromGr: Boolean?): BigDecimal? {
        if(!isRequireCalculateCashPerf(goodsReceived, isCashPerfFromGr, invoiceAction)) return null
        val grCashPerfRemainingAmount = goodsReceived.cashPerfDeductibleRemainingAmount ?: return null
        val cashPerfConsumed = relateInvoiceItems.sumByDecimal { it.cashPerfGuaranteeAmount ?: BigDecimal.ZERO }

        return calculateNewRemainingAmountByInvoiceAction(grCashPerfRemainingAmount, cashPerfConsumed, invoiceAction)
    }

    /**
     * method for calculate new cashWarranty remaining amount which deduct-
     * sum of all consumed cashWarranty amount on invoice
     *
     * @param relateInvoiceItems: Invoice item that tied with goods received
     * @param goodsReceived: Goods received
     * @param invoiceAction: invoice action
     * @param isCashWarrantyFromGr: flag that invoice use cash warranty from gr header
     */
    fun getNewCashWarrantyRemainingAmount(relateInvoiceItems: List<InvoiceItemModel>,
                                          goodsReceived: GoodsReceivedModel,
                                          invoiceAction: String,
                                          isCashWarrantyFromGr: Boolean?): BigDecimal? {
        if(!isRequireCalculateCashWarranty(goodsReceived, isCashWarrantyFromGr, invoiceAction)) return null
        val grCashWarrantyRemainingAmount = goodsReceived.cashWarrantyDeductibleRemainingAmount ?: return null
        val cashWarrantyConsumed = relateInvoiceItems.sumByDecimal { it.cashWarrantyAmount ?: BigDecimal.ZERO }

        return calculateNewRemainingAmountByInvoiceAction(grCashWarrantyRemainingAmount, cashWarrantyConsumed, invoiceAction)
    }


    /**
     * select way to calculate new remaining amount by using topicNotify
     * which can tell active of this invoice
     * in case INVOICE_ISSUED_NOTIFY we wil reduce remaining
     *
     * @param oldRemainingAmount: Remaining amount in gr before change
     * @param consumedAmount: amount that we will change to oldRemainingAmount
     * @param invoiceAction: Topic that we use to action on oldRemainingAmount
     */
    private fun calculateNewRemainingAmountByInvoiceAction(oldRemainingAmount: BigDecimal, consumedAmount: BigDecimal, invoiceAction: String): BigDecimal {
        return when (invoiceAction) {
            InvoiceAction.ISSUE.name -> oldRemainingAmount.minus(consumedAmount).negativeToZero()
            InvoiceAction.CANCEL.name -> oldRemainingAmount.plus(consumedAmount)
            else -> oldRemainingAmount
        }
    }

    /**
     * Method for check this goodsReceived is require to calculate cash warranty or not
     * @param goodsReceived processing goodsReceived
     * @param isCashWarrantyFromGr flag from invoice header
     * @param invoiceAction invoice action for select way to check require
     */
    private fun isRequireCalculateCashWarranty(goodsReceived: GoodsReceivedModel, isCashWarrantyFromGr: Boolean?, invoiceAction: String): Boolean{
        return when (invoiceAction) {
            InvoiceAction.CANCEL.name -> isCashWarrantyFromGr?: false
            else -> goodsReceived.cashWarrantyDeductibleAmount != null
        }
    }

    /**
     * Method for check this goodsReceived is require to calculate cash perf or not
     * @param goodsReceived processing goodsReceived
     * @param isCashPerfFromGr flag from invoice header
     * @param invoiceAction invoice action for select way to check require
     */
    private fun isRequireCalculateCashPerf(goodsReceived: GoodsReceivedModel, isCashPerfFromGr: Boolean?, invoiceAction: String): Boolean{
        return when (invoiceAction) {
            InvoiceAction.CANCEL.name -> isCashPerfFromGr?: false
            else -> goodsReceived.cashPerfGuaranteeDeductibleAmount != null
        }
    }

}

/**
 * method for update cash performance guarantee and cash warranty remaining amount on goodsReceived
 * by using related invoice item of goodsReceived items and purchase order header for calculate
 * (related invoice item will filter only NORMAL item category)
 * @param invoiceModel invoice in issue process, consuming cash perf and warranty amount of goodsReceived
 * @param invoiceAction for select way to calculate remaining amount
 */
fun List<GoodsReceivedModel>.updateCashRemainingAmount(invoiceModel: InvoiceModel,
                                                       invoiceAction: String): List<GoodsReceivedModel> {
    val normalInvoiceItemModels = invoiceModel.invoiceItems.filter { CashPerfAndWarrantyForGrManager.isNormalInvoiceItem(it) }
    if (normalInvoiceItemModels.isEmpty()) return this
    return this.map { goodsReceived ->
        val relatedInvoiceItems = normalInvoiceItemModels.filter { CashPerfAndWarrantyForGrManager.isInvoiceRelatedGr(it, goodsReceived) }

        if (CashPerfAndWarrantyForGrManager.isNormalGr(goodsReceived) && relatedInvoiceItems.isNotEmpty()) {
            val newCashPerfRemainingAmount = CashPerfAndWarrantyForGrManager.getNewCashPerfRemainingAmount(relatedInvoiceItems, goodsReceived, invoiceAction, invoiceModel.cashPerfGuaranteeFromGr)
            val newCashWarrantyRemainingAmount = CashPerfAndWarrantyForGrManager.getNewCashWarrantyRemainingAmount(relatedInvoiceItems, goodsReceived, invoiceAction, invoiceModel.cashWarrantyFromGr)
            goodsReceived.copy(
                    cashPerfDeductibleRemainingAmount = newCashPerfRemainingAmount,
                    cashWarrantyDeductibleRemainingAmount = newCashWarrantyRemainingAmount
            )
        } else {
            goodsReceived
        }
    }
}