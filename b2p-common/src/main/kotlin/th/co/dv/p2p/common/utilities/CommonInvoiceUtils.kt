package th.co.dv.p2p.common.utilities

import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import java.math.BigDecimal

object CommonInvoiceUtils {
    /**
     * This function will calculate the withholding tax total from invoice items
     *
     * @param invoiceItemModels is the list of invoice items of invoice that we use to calculate the withholding tax total.
     *
     * We grouping invoice items with withholding tax rate first and sum up the invoice item whtBaseAmount in each group.
     * then multiply whtBaseAmount by whtRate
     *
     * @return invoice header withholding tax total.
     */
    fun calculateWithholdingTaxTotal(invoiceItemModels: List<InvoiceItemModel>): BigDecimal {

        // No need to calculate invoice item which have no withholding tax.
        val eachRateWithWhtTotal = invoiceItemModels.filter { it.withholdingTaxRate != null }
                .groupBy { it.withholdingTaxRate }
                .mapValues {
                    val whtRate = it.key
                    val (groupDeductInvoiceItems, groupNonDeductInvoiceItems) = it.value
                            .partition { invoiceItem -> invoiceItem.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name }

                    val sumOfNonDeductWhtBaseAmount = groupNonDeductInvoiceItems.sumByDecimal { invoiceItem ->
                        invoiceItem.withholdingTaxBaseAmount ?: BigDecimal.ZERO
                    }
                    val sumOfDeductWhtBaseAmount = groupDeductInvoiceItems.sumByDecimal { invoiceItem ->
                        invoiceItem.withholdingTaxBaseAmount ?: BigDecimal.ZERO
                    }
                    val sumOfWhtBaseAmount = sumOfNonDeductWhtBaseAmount - sumOfDeductWhtBaseAmount

                    val whtTotal = sumOfWhtBaseAmount.times(whtRate!!.movePointLeft(2))

                    whtTotal
                }

        return eachRateWithWhtTotal.values.sumByDecimal { it }
    }

    /**
     * This function will calculate advance deduction from invoice items
     *
     * @param invoiceItemModels is the list of invoice items of invoice that we use to calculate advance deduction.
     * @return invoice advance deduction.
     */
    fun calculateAdvanceDeduction(invoiceItemModels: List<InvoiceItemModel>): BigDecimal {

        return invoiceItemModels
            .filter { it.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name }
            .sumByDecimal { it.total ?: BigDecimal.ZERO }
    }
}