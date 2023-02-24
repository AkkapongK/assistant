package th.co.dv.p2p.common.utilities

import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.utilities.DateUtility.Companion.completeStartDateEndDate
import th.co.dv.p2p.common.utilities.DateUtility.Companion.convertToEndOfDayTimestamp
import th.co.dv.p2p.common.utilities.DateUtility.Companion.convertToStartOfDayTimestamp
import th.co.dv.p2p.common.utilities.DateUtility.Companion.getStartOfYearAndNextYear
import th.co.dv.p2p.corda.base.models.GoodsReceivedModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import th.co.dv.p2p.corda.base.models.PaymentModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateUtilityTest {

    @Test
    fun testCompleteStartDateEndDate() {
        val dateFrom = "2020-04-20"
        val dateTo = "2020-04-27"

        val convertDateFrom1 = convertToStartOfDayTimestamp(dateFrom)
        val convertDateFrom2 = convertToStartOfDayTimestamp(dateTo)
        val convertDateTo1 = convertToEndOfDayTimestamp(dateTo)
        val convertDateTo2 = convertToEndOfDayTimestamp(dateFrom)


        var result = completeStartDateEndDate(dateFrom, dateTo)
        assertEquals(Pair(convertDateFrom1, convertDateTo1), result)

        result = completeStartDateEndDate(dateFrom, null)
        assertEquals(Pair(convertDateFrom1, convertDateTo2), result)

        result = completeStartDateEndDate(null, dateTo)
        assertEquals(Pair(convertDateFrom2, convertDateTo1), result)

        result = completeStartDateEndDate(null, null)
        assertEquals(Pair(null,null), result)

        //test format date dd/mm/yyyy
        val date = "20/04/2020"

        val convertDateFrom = convertToStartOfDayTimestamp(date)
        val convertDateTo = convertToEndOfDayTimestamp(date)

        result = completeStartDateEndDate(date, date)
        assertEquals(Pair(convertDateFrom, convertDateTo), result)
    }

    @Test
    fun testGetDate() {

        mockkObject(DateUtility)
        val updatedDateString = "2020-10-23T21:41:50.676+07:00"
        val updatedDate = DateUtility.convertStringToDateTime(updatedDateString, DATE_TIME_FORMAT)

        // Case InvoiceModel
        var invoiceModel = InvoiceModel(updatedDate = updatedDateString)
        var result = Try.on { DateUtility.getDateFromModel(invoiceModel) }

        assertTrue(result.isSuccess)
        assertEquals(updatedDate, result.getOrThrow())

        // Case InvoiceModel and updatedDate is null
        invoiceModel = InvoiceModel()
        result = Try.on { DateUtility.getDateFromModel(invoiceModel) }

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrThrow())

        // Case PurchaseOrderModel
        var purchaseOrderModel = PurchaseOrderModel(updatedDate = updatedDateString)
        result = Try.on { DateUtility.getDateFromModel(purchaseOrderModel) }

        assertTrue(result.isSuccess)
        assertEquals(updatedDate, result.getOrThrow())

        // Case PurchaseOrderModel and updatedDate is null
        purchaseOrderModel = PurchaseOrderModel()
        result = Try.on { DateUtility.getDateFromModel(purchaseOrderModel) }

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrThrow())

        // Case GoodsReceivedModel
        var goodsReceivedModel = GoodsReceivedModel(updatedDate = updatedDateString)
        result = Try.on { DateUtility.getDateFromModel(goodsReceivedModel) }

        assertTrue(result.isSuccess)
        assertEquals(updatedDate, result.getOrThrow())

        // Case GoodsReceivedModel and updatedDate is null
        goodsReceivedModel = GoodsReceivedModel()
        result = Try.on { DateUtility.getDateFromModel(goodsReceivedModel) }

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrThrow())

        // Case PaymentModel
        var paymentModel = PaymentModel(updatedDate = updatedDateString)
        result = Try.on { DateUtility.getDateFromModel(paymentModel) }

        assertTrue(result.isSuccess)
        assertEquals(updatedDate, result.getOrThrow())

        // Case PaymentModel and updatedDate is null
        paymentModel = PaymentModel()
        result = Try.on { DateUtility.getDateFromModel(paymentModel) }

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrThrow())

        // Case Unsupported model
        result = Try.on { DateUtility.getDateFromModel("Unsupported model") }

        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Unsupported model"))

        unmockkObject(DateUtility)
    }

    @Test
    fun testGetStartOfYearAndNextYear() {
        listOf("2011-12-14T00:00:00.000+07:00", "22/10/2011", "2011-01-20").forEach {
            getStartOfYearAndNextYear(it).run {
                assertEquals("2011-01-01 00:00:00.0", this.first.toString())
                assertEquals("2012-01-01 00:00:00.0", this.second.toString())
            }
        }
    }
}