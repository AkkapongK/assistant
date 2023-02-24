package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.common.enums.InvoiceAction
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.utilities.manager.CashPerfAndWarrantyForPoManager.getNewCashPerfRemainingAmount
import th.co.dv.p2p.common.utilities.manager.CashPerfAndWarrantyForPoManager.getNewCashWarrantyRemainingAmount
import th.co.dv.p2p.common.utilities.manager.CashPerfAndWarrantyForPoManager.getNewRetentionRemainingAmount
import th.co.dv.p2p.common.utilities.negativeToZero
import th.co.dv.p2p.common.utilities.sumByDecimal
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import java.math.BigDecimal

/**
 * This class is for manage cash performance guarantee and warranty remaining amount
 * select related invoice item for sum cash amount then,
 * calculate new remaining by using invoiceAction for select formula
 */
object CashPerfAndWarrantyForPoManager {

    /**
     * method for calculate new cashPerformanceGuarantee remaining amount which deduct-
     * sum of all consumed cashPerformanceGuarantee amount on invoice
     *
     * @param relateInvoiceItems: Invoice item that tied with purchaseOrder
     * @param purchaseOrder: purchaseOrder that processing
     * @param invoiceAction: use for action on oldRemainingAmount
     */
    fun getNewCashPerfRemainingAmount(relateInvoiceItems: List<InvoiceItemModel>,
                                      purchaseOrder: PurchaseOrderModel,
                                      invoiceAction: String): BigDecimal? {
        if (!isRequireCalculateCashPerf(purchaseOrder)) return null
        val poCashPerfRemainingAmount = purchaseOrder.cashPerfGuaranteeRemainingAmount ?: return null
        val cashPerfConsumed = relateInvoiceItems.sumByDecimal { it.cashPerfGuaranteeAmount ?: BigDecimal.ZERO }

        return calculateNewRemainingAmountByInvoiceAction(poCashPerfRemainingAmount, cashPerfConsumed, purchaseOrder.cashPerfGuaranteeAmount!!, invoiceAction)
    }

    /**
     * method for calculate new cashWarranty remaining amount which deduct-
     * sum of all consumed cashWarranty amount on invoice
     *
     * @param relateInvoiceItems: Invoice item that tied with purchaseOrder
     * @param purchaseOrder: purchaseOrder that processing
     * @param invoiceAction: invoice action
     */
    fun getNewCashWarrantyRemainingAmount(relateInvoiceItems: List<InvoiceItemModel>,
                                          purchaseOrder: PurchaseOrderModel,
                                          invoiceAction: String): BigDecimal? {
        if (!isRequireCalculateCashWarranty(purchaseOrder)) return null
        val poCashWarrantyRemainingAmount = purchaseOrder.cashWarrantyRemainingAmount ?: return null
        val cashWarrantyConsumed = relateInvoiceItems.sumByDecimal { it.cashWarrantyAmount ?: BigDecimal.ZERO }

        return calculateNewRemainingAmountByInvoiceAction(poCashWarrantyRemainingAmount, cashWarrantyConsumed, purchaseOrder.cashWarrantyAmount!!, invoiceAction)
    }

    /**
     * method for calculate new retention remaining amount which deduct-
     * sum of all consumed cashWarranty amount on invoice
     *
     * @param relateInvoiceItems: Invoice item that tied with purchaseOrder
     * @param purchaseOrder: purchaseOrder that processing
     * @param invoiceAction: invoice action
     */
    fun getNewRetentionRemainingAmount(relateInvoiceItems: List<InvoiceItemModel>,
                                       purchaseOrder: PurchaseOrderModel,
                                       invoiceAction: String): BigDecimal? {
        if (!isRequireCalculateRetention(purchaseOrder)) return null

        val poRetentionRemainingAmount = purchaseOrder.retentionRemainingAmount ?: return null
        val retentionAmountConsumed = relateInvoiceItems.sumByDecimal { it.retentionAmount ?: BigDecimal.ZERO }

        return calculateNewRemainingAmountByInvoiceAction(poRetentionRemainingAmount, retentionAmountConsumed, purchaseOrder.retentionAmount!!, invoiceAction)
    }


    /**
     * select way to calculate new remaining amount by using invoiceAction
     * which can tell active of this invoice
     *
     * @param oldRemainingAmount: Remaining amount before change
     * @param consumedAmount: amount that we will change to oldRemainingAmount
     * @param invoiceAction:  use for action on oldRemainingAmount
     */
    private fun calculateNewRemainingAmountByInvoiceAction(oldRemainingAmount: BigDecimal, consumedAmount: BigDecimal, initialAmount: BigDecimal, invoiceAction: String): BigDecimal {
        return when (invoiceAction) {
            InvoiceAction.ISSUE.name -> oldRemainingAmount.minus(consumedAmount).negativeToZero()
            InvoiceAction.CANCEL.name -> oldRemainingAmount.plus(consumedAmount).min(initialAmount).negativeToZero()
            else -> oldRemainingAmount
        }
    }

    /**
     * Method for check this purchaseOrder is require to calculate cash warranty or not
     * @param purchaseOrder processing purchaseOrder
     */
    private fun isRequireCalculateCashWarranty(purchaseOrder: PurchaseOrderModel): Boolean {
        return purchaseOrder.cashWarrantyAmount != null
    }

    /**
     * Method for check this purchaseOrder is require to calculate cash perf or not
     * @param purchaseOrder processing purchaseOrder
     */
    private fun isRequireCalculateCashPerf(purchaseOrder: PurchaseOrderModel): Boolean {
        return purchaseOrder.cashPerfGuaranteeAmount != null
    }

    /**
     * Method for check this purchaseOrder is require to calculate retention or not
     * @param purchaseOrder processing purchaseOrder
     */
    private fun isRequireCalculateRetention(purchaseOrder: PurchaseOrderModel): Boolean {
        return purchaseOrder.retentionAmount != null

    }

}

/**
 * method for update cash performance guarantee , cash warranty remaining amount and retention remaining amount on purchase order
 * by using related invoice item of purchase items and purchase order header for calculate
 * (related invoice item will filter only NORMAL item category
 * @param invoiceModel invoice in issue process, consuming cash perf and warranty amount of purchase order
 */
fun List<PurchaseOrderModel>.updateCashRemainingAmount(invoiceModel: InvoiceModel,
                                                       invoiceAction: String): List<PurchaseOrderModel> {
    val normalInvoiceItems = invoiceModel.invoiceItems.filter { it.itemCategory == ItemCategory.Purchase.NORMAL.name }

    return this.map { purchaseOrder ->
        val purchaseItemLinearIds = purchaseOrder.purchaseItems.map { it.linearId!! }
        val relateInvoiceItems = normalInvoiceItems.filter { it.purchaseItemLinearId in purchaseItemLinearIds }

        val newCashPerfRemainingAmount = getNewCashPerfRemainingAmount(relateInvoiceItems, purchaseOrder, invoiceAction)
        val newCashWarrantyRemainingAmount = getNewCashWarrantyRemainingAmount(relateInvoiceItems, purchaseOrder, invoiceAction)
        val newRetentionRemainingAmount = getNewRetentionRemainingAmount(relateInvoiceItems, purchaseOrder, invoiceAction)

        purchaseOrder.copy(
                cashPerfGuaranteeRemainingAmount = newCashPerfRemainingAmount,
                cashWarrantyRemainingAmount = newCashWarrantyRemainingAmount,
                retentionRemainingAmount = newRetentionRemainingAmount
        )
    }
}