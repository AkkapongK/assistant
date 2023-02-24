package th.co.dv.p2p.common.validators

import th.co.dv.p2p.common.enums.MovementClass
import th.co.dv.p2p.common.utilities.Conditions.using
import th.co.dv.p2p.common.utilities.isGreaterOrEqual
import th.co.dv.p2p.corda.base.models.GoodsReceivedModel
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import java.math.BigDecimal

/**
 * Class for validate cash perf and warranty service
 */
object CashPerfAndWarrantyValidator {

    /**
     * Method for validate data after fetch involved data
     * @param goodsReceivedModels: all good received involved in this invoice
     */
    fun inspectAfterFetchData(goodsReceivedModels: List<GoodsReceivedModel>) {
        val goodsReceivedItems = goodsReceivedModels.flatMap { it.goodsReceivedItems }
        "Good received item's movement class must be normal only." using goodsReceivedItems.all { it.movementClass == MovementClass.NORMAL.name }
    }


    /**
     * Method for validate data before deduct cash amount
     * @param estimatedInvoiceDeductibleAmount: estimated invoice deductible amount
     * @param cashRemainingAmount: cash remainingAmount from goodsReceived header
     * @param goodsReceived: goodsReceived header
     * @param invoiceItems: list of invoice item in same group
     */
    fun validateBeforeDeductByGr(estimatedInvoiceDeductibleAmount: BigDecimal,
                                 cashRemainingAmount: BigDecimal?,
                                 goodsReceived: GoodsReceivedModel,
                                 invoiceItems: List<InvoiceItemModel>) {
        //verify if it is last gr item enough for deduct cash perf
        if (isLastGoodsReceivedItem(goodsReceived, invoiceItems)) {
            val subTotal = estimatedInvoiceDeductibleAmount.minus(cashRemainingAmount?: BigDecimal.ZERO)
            "Invoice amount is not enough to cover cash performance guarantee. Please contact buyer." using subTotal.isGreaterOrEqual(BigDecimal.ZERO)
        }
    }


    /**
     * Method for check last goods received item
     * by checking there is no goodsReceived reference any invoiceItemLinearId include this invoiceItems
     *  note: always have related gr item with input invoiceItem
     * @param goodsReceived: good received header
     * @param relatedInvoiceItems: list of invoice item in same group with gr
     */
    private fun isLastGoodsReceivedItem(goodsReceived: GoodsReceivedModel, relatedInvoiceItems: List<InvoiceItemModel>): Boolean {
        val unUseGrItems = goodsReceived.goodsReceivedItems.filter { it.invoiceItemLinearId == null }
        val grItemLinearIdsInThisInvoice = relatedInvoiceItems.flatMap { it.goodsReceivedItems }.map { it.linearId }
        val realUnUse = unUseGrItems.filterNot { it.linearId in grItemLinearIdsInThisInvoice }
        return realUnUse.isEmpty()
    }


}


