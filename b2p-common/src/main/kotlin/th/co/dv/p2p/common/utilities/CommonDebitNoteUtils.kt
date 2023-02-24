package th.co.dv.p2p.common.utilities

import th.co.dv.p2p.corda.base.models.DebitNoteItemModel
import java.math.BigDecimal

object CommonDebitNoteUtils {
    /**
     * This function will calculate the withholding tax total from debitnote items
     *
     * @param debitnoteItemModels is the list of debitnote items of debitnote that we use to calculate the withholding tax total.
     *
     * We multiply whtBaseAmount by whtRate
     *
     * @return debitnote header withholding tax total.
     */
    fun calculateWithholdingTaxTotal(debitnoteItemModels: List<DebitNoteItemModel>): BigDecimal {

        // No need to calculate debitnote item which have no withholding tax.
        val eachRateWithWhtTotal = debitnoteItemModels.filter { it.withholdingTaxRate != null }
                .groupBy { it.withholdingTaxRate }
                .mapValues {
                    val whtRate = it.key
                    val debitNoteItems = it.value

                    val sumOfWhtBaseAmount = debitNoteItems.sumByDecimal{ debitNoteItem ->
                        debitNoteItem.withholdingTaxBaseAmount ?: debitNoteItem.subTotal ?: BigDecimal.ZERO
                    }

                    sumOfWhtBaseAmount.times(whtRate!!.movePointLeft(2))

                }

        return eachRateWithWhtTotal.values.sumByDecimal { it }
    }

}