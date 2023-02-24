package th.co.dv.p2p.common.utilities

import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import java.math.BigDecimal

object PlanFlowUtils {

    private val logger = LoggerFactory.getLogger(PlanFlowUtils::class.java)
    private val className = PlanFlowUtils::class.java.simpleName

    /**
     * We calculate subtotal by sum it up.
     * 1) If any invoice item is ADVANCE_REDEEM (should have only redeem item) -> We sum subtotal up.
     * 2) If any invoice item is ADVANCE_DEDUCT -> sum ( normal item unit price * qty) - sum (deduct item sub total)
     * 3) If any invoice item is PROVISION (should have only provision item) -> We sum subtotal up.
     * 3) If all invoice items are NORMAL -> sum ( unit price * qty )
     */
    fun calculateSumOfItemsSubtotal(invoiceItems: List<InvoiceItemModel>): BigDecimal {
        val calculatedSubtotal = when {
            invoiceItems.any { it.itemCategory == ItemCategory.Invoice.ADVANCE_REDEEM.name } -> invoiceItems.sumByDecimal { it.itemSubTotal!! }
            invoiceItems.any { it.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name } -> {
                val (deductItem, normalItem) = invoiceItems.partition { it.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name }
                val normalItemSubTotal = normalItem.sumByDecimal { it.itemSubTotal!! }
                val deductItemSubTotal = deductItem.sumByDecimal { it.itemSubTotal!! }
                normalItemSubTotal.minus(deductItemSubTotal)
            }
            invoiceItems.any { it.itemCategory == ItemCategory.Invoice.PROVISION.name } -> invoiceItems.sumByDecimal { it.itemSubTotal!! }
            else -> invoiceItems.sumByDecimal { it.itemSubTotal!! }
        }
        logger.info("$className.calculateSumOfItemsSubtotal invoiceLinearId : ${invoiceItems.first().invoiceLinearId}  calculatedSubtotal : $calculatedSubtotal")
        return calculatedSubtotal
    }

    /**
     * Invoice VatTotal tally with the sum of all invoice items vatTotal of (vatRate * sum(item.subtotal))
     * Note : We assume no case for particular group vat code have only deduct item. If there is only deduct item, that group will be negative.
     */
    fun calculateSumOfItemsVatTotal(invoiceItems: List<InvoiceItemModel>): BigDecimal {
        // Group invoiceItems by their tax rate
        // We get a Map<vatRate, sumOfVatTotal>
        val eachRateWithVatTotal = invoiceItems.groupBy { it.vatRate }
                .mapValues {
                    // each key is a vatRate that is distinct
                    val vatRate = it.key

                    // list of invoice items with this vatRate
                    val groupInvoiceItems = it.value
                    // SUM(invoiceItems.subTotal)
                    val sumOfSubtotal = calculateSumOfItemsSubtotal(groupInvoiceItems)
                    // vatTotal = SUM(invoiceItems.subTotal) * vat rate
                    sumOfSubtotal.times(vatRate?.movePointLeft(2) ?: BigDecimal.ONE).setScale()
                }

        // We then sum each of the sumOfVatTotal
        val sumOfVatTotal = eachRateWithVatTotal.values.sumByDecimal { it }
        logger.info("$className.calculateSumOfItemsVatTotal invoiceLinearId : ${invoiceItems.first().invoiceLinearId} sumOfVatTotal : $sumOfVatTotal ")

        return sumOfVatTotal
    }

}