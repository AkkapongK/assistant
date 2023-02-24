package th.co.dv.p2p.common.utilities

import org.junit.Test
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import kotlin.test.assertEquals


class CommonInvoiceUtilsTest {

    @Test
    fun testCalculateWithholdingTaxTotal() {
        val expectedResult = 12.3508.toBigDecimal()

        // Rate 3 >> (24.16 + 46.07) x 0.03 = 2.1069
        // Rate 5 >> (190.66 - 17.39) x 0.05 = 8.6635
        // Rate 6 >> (48.13 - 21.79) x 0.06 = 1.5804
        // WHT Total 12.3508
        val invoiceItemModels = listOf(
                InvoiceItemModel(withholdingTaxBaseAmount = 24.16.toBigDecimal(), withholdingTaxRate = 3.toBigDecimal(), itemCategory = ItemCategory.Invoice.NORMAL.name),
                InvoiceItemModel(withholdingTaxBaseAmount = 46.07.toBigDecimal(), withholdingTaxRate = 3.toBigDecimal(), itemCategory = ItemCategory.Invoice.NORMAL.name),
                InvoiceItemModel(withholdingTaxBaseAmount = 190.66.toBigDecimal(), withholdingTaxRate = 5.toBigDecimal(), itemCategory = ItemCategory.Invoice.NORMAL.name),
                InvoiceItemModel(withholdingTaxBaseAmount = 17.39.toBigDecimal(), withholdingTaxRate = 5.toBigDecimal(), itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name),
                InvoiceItemModel(withholdingTaxBaseAmount = 48.13.toBigDecimal(), withholdingTaxRate = 6.toBigDecimal(), itemCategory = ItemCategory.Invoice.NORMAL.name),
                InvoiceItemModel(withholdingTaxBaseAmount = 21.79.toBigDecimal(), withholdingTaxRate = 6.toBigDecimal(), itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name),
                InvoiceItemModel(withholdingTaxBaseAmount = 100.toBigDecimal(), withholdingTaxRate = null)
        )

        val result = CommonInvoiceUtils.calculateWithholdingTaxTotal(invoiceItemModels)

        assertEquals(expectedResult, result)
    }

}