package th.co.dv.p2p.common.utilities

import io.mockk.spyk
import org.junit.Test
import th.co.dv.p2p.common.enums.VatTriggerPoint
import th.co.dv.p2p.common.models.TaxModel
import kotlin.test.assertEquals

class VatTriggerPointUtilsTest {

    @Test
    fun testGetVatTriggerPoint() {
        val vatTriggerPointUtils = spyk<VatTriggerPointUtils>()
        val taxModelInvoice = TaxModel(taxTriggerPoint = VatTriggerPoint.Invoice.name)
        val taxModelNull = TaxModel(taxTriggerPoint = null)
        val taxModelPayment = TaxModel(taxTriggerPoint = VatTriggerPoint.Payment.name)
        val taxModelNone = TaxModel(taxTriggerPoint = VatTriggerPoint.None.name)

        // Have one item is INVOICE return INVOICE
        var result = vatTriggerPointUtils.getVatTriggerPoint(listOf(taxModelInvoice, taxModelPayment, taxModelNone))

        assertEquals(VatTriggerPoint.Invoice.name, result)

        // All items are Null return INVOICE
        result = vatTriggerPointUtils.getVatTriggerPoint(listOf(taxModelNull))

        assertEquals(VatTriggerPoint.Invoice.name, result)

        // All items are PAYMENT return PAYMENT
        result = vatTriggerPointUtils.getVatTriggerPoint(listOf(taxModelPayment))

        assertEquals(VatTriggerPoint.Payment.name, result)

        // Items are PAYMENT AND NONE return PAYMENT
        result = vatTriggerPointUtils.getVatTriggerPoint(listOf(taxModelPayment, taxModelNone))

        assertEquals(VatTriggerPoint.Payment.name, result)

        // All items are NONE return NONE
        result = vatTriggerPointUtils.getVatTriggerPoint(listOf(taxModelNone))

        assertEquals(VatTriggerPoint.None.name, result)

        // Case else return INVOICE
        result = vatTriggerPointUtils.getVatTriggerPoint(listOf(TaxModel(taxTriggerPoint = "MOCK")))

        assertEquals(VatTriggerPoint.Invoice.name, result)
    }
}