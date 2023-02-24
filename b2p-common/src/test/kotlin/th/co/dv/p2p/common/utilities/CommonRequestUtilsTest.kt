package th.co.dv.p2p.common.utilities

import io.mockk.spyk
import org.junit.Test
import th.co.dv.p2p.corda.base.models.RequestItemModel
import kotlin.test.assertEquals

class CommonRequestUtilsTest {

    @Test
    fun testCalculateWithholdingTaxTotal() {

        val commonRequestUtils = spyk<CommonRequestUtils>()

        val expectedResult = 16.71.toBigDecimal()

        // Rate 3 >> (24.16 + 46.07) x 0.03 = 2.11
        // Rate 5 >> (190.66 + 17.39) x 0.05 = 10.40
        // Rate 6 >> (48.13 + 21.79) x 0.06 = 4.20
        // WHT Total = 16.71
        val requestItemModels = listOf(
                RequestItemModel(subTotal = 24.16.toBigDecimal(), withholdingTaxRate = 3.toBigDecimal(), currency = THB.currencyCode),
                RequestItemModel(subTotal = 46.07.toBigDecimal(), withholdingTaxRate = 3.toBigDecimal(), currency = THB.currencyCode),
                RequestItemModel(subTotal = 190.66.toBigDecimal(), withholdingTaxRate = 5.toBigDecimal(), currency = THB.currencyCode),
                RequestItemModel(subTotal = 17.39.toBigDecimal(), withholdingTaxRate = 5.toBigDecimal(), currency = THB.currencyCode),
                RequestItemModel(subTotal = 48.13.toBigDecimal(), withholdingTaxRate = 6.toBigDecimal(), currency = THB.currencyCode),
                RequestItemModel(subTotal = 21.79.toBigDecimal(), withholdingTaxRate = 6.toBigDecimal(), currency = THB.currencyCode),
                RequestItemModel(subTotal = 100.toBigDecimal(), currency = THB.currencyCode)
        )

        val result = commonRequestUtils.calculateWithholdingTaxTotal(requestItemModels)

        assertEquals(expectedResult, result)
    }

}