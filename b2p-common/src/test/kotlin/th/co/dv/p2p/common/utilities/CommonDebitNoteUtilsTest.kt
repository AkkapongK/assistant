package th.co.dv.p2p.common.utilities

import org.junit.Test
import th.co.dv.p2p.corda.base.models.DebitNoteItemModel
import kotlin.test.assertEquals


class CommonDebitNoteUtilsTest {

    @Test
    fun testCalculateWithholdingTaxTotal() {
        var expectedResult = 16.7046.toBigDecimal()

        // Rate 3 >> (24.16 + 46.07) x 0.03 = 2.1069
        // Rate 5 >> (190.66 + 17.39) x 0.05 = 10.4025
        // Rate 6 >> (48.13 + 21.79) x 0.06 = 4.1952
        // WHT Total 16.7046
        var debitNoteItemModels = listOf(
                DebitNoteItemModel(withholdingTaxBaseAmount = 24.16.toBigDecimal(), withholdingTaxRate = 3.toBigDecimal()),
                DebitNoteItemModel(withholdingTaxBaseAmount = 46.07.toBigDecimal(), withholdingTaxRate = 3.toBigDecimal()),
                DebitNoteItemModel(withholdingTaxBaseAmount = 190.66.toBigDecimal(), withholdingTaxRate = 5.toBigDecimal()),
                DebitNoteItemModel(withholdingTaxBaseAmount = 17.39.toBigDecimal(), withholdingTaxRate = 5.toBigDecimal()),
                DebitNoteItemModel(withholdingTaxBaseAmount = 48.13.toBigDecimal(), withholdingTaxRate = 6.toBigDecimal()),
                DebitNoteItemModel(withholdingTaxBaseAmount = 21.79.toBigDecimal(), withholdingTaxRate = 6.toBigDecimal()),
                DebitNoteItemModel(withholdingTaxBaseAmount = 100.toBigDecimal(), withholdingTaxRate = null)
        )

        var result = CommonDebitNoteUtils.calculateWithholdingTaxTotal(debitNoteItemModels)


        assertEquals(expectedResult, result)

        // case withholdingTaxBaseAmount is null then use subTotal to calculate
        expectedResult = 103.6228.toBigDecimal()

        // Rate 3 >> (21154.99) x 0.03 = 34.6497
        // Rate 7 >> (4985.33) x 0.07 = 68.9731
        // WHT Total 1103.6228
        debitNoteItemModels = listOf(
                DebitNoteItemModel(subTotal=  1154.99.toBigDecimal() ,withholdingTaxBaseAmount = null, withholdingTaxRate = 3.toBigDecimal()),
                DebitNoteItemModel(subTotal=  888.75.toBigDecimal() ,withholdingTaxBaseAmount = 985.33.toBigDecimal(), withholdingTaxRate = 7.toBigDecimal()),
                DebitNoteItemModel(subTotal=  189.41.toBigDecimal(), withholdingTaxBaseAmount = 100.toBigDecimal(), withholdingTaxRate = null)
        )

       result = CommonDebitNoteUtils.calculateWithholdingTaxTotal(debitNoteItemModels)

        assertEquals(expectedResult, result)
    }

}