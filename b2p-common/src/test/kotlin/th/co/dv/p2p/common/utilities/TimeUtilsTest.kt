package th.co.dv.p2p.common.utilities

import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class TimeUtilsTest {


    @Test
    fun testGeneratePeriodWithMonth() {
        val dateFormat =  DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val previousMonth = -6
        val today = Date.from(Instant.now())
        val calendar = DateUtility.getCalendar(today)
        val firstDayOfMount = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        val lastDayOfMonth = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        val acceptDateForm = LocalDate.now().withDayOfMonth(firstDayOfMount).minusMonths(6).format(dateFormat)
        val acceptDateTo = LocalDate.now().withDayOfMonth(lastDayOfMonth).minusMonths(1).format(dateFormat)

        val result = generatePeriodWithMonth(previousMonth)

        assertEquals(acceptDateForm, result.first)
        assertEquals(acceptDateTo, result.second)
    }

    @Test
    fun `Test getYearFromDate`() {
        var result = getYearFromDate("2012-04-01T05:50:09.631Z")
        assertEquals("2012", result)

        result = getYearFromDate("2021-04-01T11:12:13.234+07:00")
        assertEquals("2021", result)

        result = getYearFromDate("16/04/1998")
        assertEquals("1998", result)

        result = getYearFromDate("")
        assertEquals(null, result)

        result = getYearFromDate(null)
        assertEquals(null, result)
    }

    @Test
    fun `Test getStartOfWeek`() {
        val timeZone = "Asia/Bangkok"
        val expectedStartWeek1UTC = "2021-05-02T17:00:00Z"
        val expectedStartWeek1Bangkok = "2021-05-03T00:00+07:00[${timeZone}]"

        var resultWeek1 = getStartOfWeek("2021-05-03T00:00:00.000Z", timeZone)
        assertEquals(expectedStartWeek1UTC, resultWeek1.toString())
        assertEquals(expectedStartWeek1Bangkok, resultWeek1.toZonedDateTime().toString())

        resultWeek1 = getStartOfWeek("2021-05-07T11:32:13.206Z", timeZone)
        assertEquals(expectedStartWeek1UTC, resultWeek1.toString())
        assertEquals(expectedStartWeek1Bangkok, resultWeek1.toZonedDateTime().toString())

        resultWeek1 = getStartOfWeek("2021-05-09T23:59:59.999Z", timeZone)
        assertEquals(expectedStartWeek1UTC, resultWeek1.toString())
        assertEquals(expectedStartWeek1Bangkok, resultWeek1.toZonedDateTime().toString())

        val expectedStartWeek2UTC = "2021-05-09T17:00:00Z"
        val expectedStartWeek2Bangkok = "2021-05-10T00:00+07:00[${timeZone}]"

        val resultWeek2 = getStartOfWeek("2021-05-10T00:00:00.000Z", timeZone)
        assertEquals(expectedStartWeek2UTC, resultWeek2.toString())
        assertEquals(expectedStartWeek2Bangkok, resultWeek2.toZonedDateTime().toString())
    }

    @Test
    fun `Test getEndOfWeek`() {
        val timeZone = "Asia/Bangkok"
        val expectedEndWeekUTC = "2021-05-09T17:00:00Z"
        val expectedEndWeekBangkok = "2021-05-10T00:00+07:00[${timeZone}]"

        var resultWeek1 = getEndOfWeek("2021-05-03T00:00:00.000Z", timeZone)
        assertEquals(expectedEndWeekUTC, resultWeek1.toString())
        assertEquals(expectedEndWeekBangkok, resultWeek1.toZonedDateTime().toString())

        resultWeek1 = getEndOfWeek("2021-05-07T12:27:36.142Z", timeZone)
        assertEquals(expectedEndWeekUTC, resultWeek1.toString())
        assertEquals(expectedEndWeekBangkok, resultWeek1.toZonedDateTime().toString())

        resultWeek1 = getEndOfWeek("2021-05-09T23:59:59.999Z", timeZone)
        assertEquals(expectedEndWeekUTC, resultWeek1.toString())
        assertEquals(expectedEndWeekBangkok, resultWeek1.toZonedDateTime().toString())

        val expectedStartWeek2UTC = "2021-05-16T17:00:00Z"
        val expectedStartWeek2Bangkok = "2021-05-17T00:00+07:00[${timeZone}]"

        val resultWeek2 = getEndOfWeek("2021-05-10T00:00:00.000Z", timeZone)
        assertEquals(expectedStartWeek2UTC, resultWeek2.toString())
        assertEquals(expectedStartWeek2Bangkok, resultWeek2.toZonedDateTime().toString())
    }

    @Test
    fun `Test isInSameDay for string input`() {

        var date1 = "2021-05-03T00:00:00.000Z"
        var date2 = "2021-05-04T00:00:00.000Z"
        assertFalse(isInSameDay(date1, date2))
        assertFalse(isInSameDay(date2, date1))

        date1 = "2021-05-03T00:20:30.456Z"
        date2 = "2021-05-03T08:20:30.456Z"
        assertTrue(isInSameDay(date1, date2))
        assertTrue(isInSameDay(date2, date1))

        date1 = "2021-05-03T00:00:00.000Z"
        date2 = "2021-05-03T23:59:59.999Z"
        assertTrue(isInSameDay(date1, date2))
        assertTrue(isInSameDay(date2, date1))
    }

    @Test
    fun `Test isInSameDay for instant input`() {

        var date1 = "2021-05-03T00:00:00+07:00".toZonedDateTime().toInstant()
        var date2 = "2021-05-04T00:00:00+07:00".toZonedDateTime().toInstant()
        assertFalse(isInSameDay(date1, date2))
        assertFalse(isInSameDay(date2, date1))

        date1 = "2021-05-03T00:20:30+07:00".toZonedDateTime().toInstant()
        date2 = "2021-05-03T08:20:30+07:00".toZonedDateTime().toInstant()
        assertTrue(isInSameDay(date1, date2))
        assertTrue(isInSameDay(date2, date1))


        date1 = "2021-05-03T00:00:00+07:00".toZonedDateTime().toInstant()
        date2 = "2021-05-03T23:59:59+07:00".toZonedDateTime().toInstant()
        assertTrue(isInSameDay(date1, date2))
        assertTrue(isInSameDay(date2, date1))
    }

    @Test
    fun `Test isInSameWeek for string input`() {

        var date1 = "2021-05-03T00:00:00.000Z"
        var date2 = "2021-05-10T00:00:00.000Z"
        assertFalse(isInSameWeek(date1, date2))
        assertFalse(isInSameWeek(date2, date1))

        date1 = "2021-05-03T00:00:00.000Z"
        date2 = "2021-05-09T23:59:59.999Z"
        assertTrue(isInSameWeek(date1, date2))
        assertTrue(isInSameWeek(date2, date1))

        date1 = "2021-05-07T14:08:54.753Z"
        date2 = "2021-05-09T23:59:59.999Z"
        assertTrue(isInSameWeek(date1, date2))
        assertTrue(isInSameWeek(date2, date1))

        date1 = "2021-05-09T23:59:59.999Z"
        date2 = "2021-05-10T00:00:00.000Z"
        assertFalse(isInSameWeek(date1, date2))
        assertFalse(isInSameWeek(date2, date1))
    }

    @Test
    fun `Test isInSameWeek for instant input`() {

        var date1 = "2021-05-03T00:00:00+07:00".toZonedDateTime().toInstant()
        var date2 = "2021-05-10T00:00:00+07:00".toZonedDateTime().toInstant()
        assertFalse(isInSameWeek(date1, date2))
        assertFalse(isInSameWeek(date2, date1))

        date1 = "2021-05-03T00:00:00+07:00".toZonedDateTime().toInstant()
        date2 = "2021-05-09T23:59:59+07:00".toZonedDateTime().toInstant()
        assertTrue(isInSameWeek(date1, date2))
        assertTrue(isInSameWeek(date2, date1))

        date1 = "2021-05-07T14:08:54+07:00".toZonedDateTime().toInstant()
        date2 = "2021-05-09T23:59:59+07:00".toZonedDateTime().toInstant()
        assertTrue(isInSameWeek(date1, date2))
        assertTrue(isInSameWeek(date2, date1))

        date1 = "2021-05-09T23:59:59+07:00".toZonedDateTime().toInstant()
        date2 = "2021-05-10T00:00:00+07:00".toZonedDateTime().toInstant()
        assertFalse(isInSameWeek(date1, date2))
        assertFalse(isInSameWeek(date2, date1))
    }
}