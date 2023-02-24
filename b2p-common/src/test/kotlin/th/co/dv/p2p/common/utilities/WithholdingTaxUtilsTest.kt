package th.co.dv.p2p.common.utilities

import io.mockk.spyk
import junit.framework.TestCase
import org.junit.Test
import th.co.dv.p2p.common.models.TaxModel
import kotlin.test.assertEquals

class WithholdingTaxUtilsTest {
    @Test
    fun `Test transformWithholdingTaxRate`() {
        val withholdingTaxUtils = spyk<WithholdingTaxUtils>()
        // Case 1 "withholdingTaxCode" = NULL, "withholdingTaxRate"= NULL
        // Use value of "withholdingTaxCode" and "withholdingTaxRate" from interface

        var result = withholdingTaxUtils.transformWithholdingTaxRate(null, null, TaxModel())
        TestCase.assertNull(result)

        // Case 2 "withholdingTaxCode" = NULL, "withholdingTaxRate"= any value
        // Use value of "withholdingTaxCode" from interface
        // Convert any value of “withholdingTaxRate” to NULL
        result = withholdingTaxUtils.transformWithholdingTaxRate(null, 10.toBigDecimal(), TaxModel())
        TestCase.assertNull(result)

        // Case 3 "withholdingTaxCode" = any value maintained in B2P, "withholdingTaxRate"= any value
        // Use value of "withholdingTaxCode" and "withholdingTaxRate" from interface
        result = withholdingTaxUtils.transformWithholdingTaxRate("wht code", 10.toBigDecimal(), TaxModel())
        assertEquals(10.toBigDecimal(), result)

        // Case 4 "withholdingTaxCode" = any value maintained in B2P, "withholdingTaxRate"= NULL
        // Use value of "withholdingTaxCode" from interface
        // Use value of "withholdingTaxRate" from table tax by lookup from "withholdingTaxCode"
        result = withholdingTaxUtils.transformWithholdingTaxRate("wht code", null, TaxModel(taxRate = 10.toBigDecimal()))
        assertEquals(10.toBigDecimal(), result)
    }
}