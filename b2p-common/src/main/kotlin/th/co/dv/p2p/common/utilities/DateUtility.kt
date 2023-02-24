package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.enums.WorkingDayDirection
import th.co.dv.p2p.corda.base.models.*
import java.math.BigDecimal
import java.sql.Timestamp
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters.firstDayOfYear
import java.util.*

class DateUtility {
    companion object {

        private val logger: Logger = LoggerFactory.getLogger(this::class.java)

        const val RETURN_TYPE_DAY = 1000 * 60 * 60 * 24

        const val DEFAULT_FORMAT = "dd/MM/yyyy"
        const val DEFAULT_TIME_FORMAT = "HH:mm:ss"
        const val DEFAULT_TIME_STAMP_FORMAT = "$DEFAULT_FORMAT HH:mm:ss"
        const val DATE_INT_ID = "yyyyMMddHHmmssSSS"
        const val WS_DATE_FORMAT = "yyyy-MM-dd"
        const val WS_TIME_FORMAT = "HH:mm:ss"
        const val WS_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
        val DATE_TIME_TH: DateFormat = SimpleDateFormat(DEFAULT_TIME_STAMP_FORMAT, Locale("th", "th"))
        val DATE_TIME_EN: DateFormat = SimpleDateFormat(DEFAULT_TIME_STAMP_FORMAT, Locale.US)
        val DATE_TH: DateFormat = SimpleDateFormat(DEFAULT_FORMAT, Locale("th", "th"))
        val DATE_EN: DateFormat = SimpleDateFormat(DEFAULT_FORMAT, Locale.US)
        const val DATE_RANGE_INVALID = "Date range specified is not valid."

        fun getCurrent(): Calendar {
            return Calendar.getInstance(Locale.US)
        }

        fun getCalendar(date: Date): Calendar {
            val now = getCurrent()
            now.time = date
            return now
        }

        fun getCurrentDateTime(): Date {
            val now = getCurrent()
            return now.time
        }

        fun getCurrentDate(): Date {
            val now = getCurrent()
            now.set(Calendar.HOUR_OF_DAY, 0)
            now.set(Calendar.MINUTE, 0)
            now.set(Calendar.SECOND, 0)
            now.set(Calendar.MILLISECOND, 0)
            return now.time
        }

        fun getStartDate(date: Date): Date {
            val cal = getCalendar(date)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

        fun getEndDate(date: Date): Date {
            val cal = getCalendar(date)
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            return cal.time
        }

        fun addDay(cal: Calendar, day: Int): Calendar {
            cal.add(Calendar.DAY_OF_MONTH, day)
            return cal
        }

        fun addDay(day: Int): Date {
            return addDay(getCurrent().time, day)
        }

        fun addDay(date: Date, day: Int): Date {
            val cal = getCalendar(date)
            return addDay(cal, day).time
        }

        fun minusDay(date: Date, day: Int): Date {
            return addDay(date, day * -1)
        }

        fun getDateTimeDiffInMinute(start: Date, end: Date): Long {
            val s = getCalendar(start)
            val e = getCalendar(end)

            val diff = e.timeInMillis - s.timeInMillis
            return diff / 60000
        }

        fun convertStringSAPToStringDate(strDate: String): String {
            var date = ""
            if (!StringUtility.isEmpty(strDate)) {
                try {

                    val strDates = strDate.split("-")
                    if (strDates.isNullOrEmpty().not() && strDates.size == 3) {
                        date = strDates[2] + "/" + strDates[1] + "/" + strDates[0]
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            return date
        }

        private fun getDateFormat(format: String): SimpleDateFormat {
            var dateFormat = DEFAULT_FORMAT
            if (!StringUtility.isEmpty(format)) {
                dateFormat = format
            }
            return SimpleDateFormat(dateFormat, Locale.US)
        }

        fun convertStringToDate(strDate: String?, format: String): Date? {
            if (strDate.isNullOrEmpty())
                return null

            val sFormat = getDateFormat(format)

            return try {
                sFormat.parse(strDate)
            } catch (e: ParseException) {
                null
            }

        }

        fun getStartOfYearAndNextYear(date: String): Pair<Date, Date> {
            return LocalDateTime.from(date.toZonedDateTime()).let {
                Timestamp.valueOf(it.with(firstDayOfYear())) to
                        Timestamp.valueOf(it.plusYears(1).with(firstDayOfYear()))
            }
        }

        fun convertToStartOfDayTimestamp(date: String): Date {
            return Timestamp.valueOf(LocalDateTime.from(getStartOfDay(date).toZonedDateTime()))
        }

        fun convertToEndOfDayTimestamp(date: String): Date {
            return Timestamp.valueOf(LocalDateTime.from(getEndOfDay(date).toZonedDateTime()))
        }

        fun convertStringToDate(strDate: String?): Date? {
            return convertStringToDate(strDate, DEFAULT_FORMAT)
        }

        fun convertStringToDateTime(strDate: String?, format: String): Date? {
            return if (StringUtility.isEmpty(format)) convertStringToDate(strDate, DEFAULT_TIME_STAMP_FORMAT) else convertStringToDate(strDate, format)
        }

        fun convertDateToString(date: Date?): String {
            return convertDateToString(date, DEFAULT_FORMAT)
        }

        fun convertDateToString(date: Date?, format: String): String {
            if (null == date)
                return ""

            val sFormat = getDateFormat(format)
            return sFormat.format(date)
        }

        //public static String convertDateToString(Date date, String format, boolean isBuddistYear){

        //}

        fun convertDateTimeToTimeString(date: Date?): String {
            return convertDateToString(date, DEFAULT_TIME_FORMAT)
        }

        fun convertDateTimeToString(date: Date?): String {
            return convertDateToString(date, DEFAULT_TIME_STAMP_FORMAT)
        }

        fun addMonth(date: Date, month: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.MONTH, month)
            return calendar.time
        }

        fun addYear(date: Date, year: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.YEAR, year)
            return calendar.time
        }

        fun getFirstDateOfMonth(date: Date): Date {
            val calendar = getCurrent()
            calendar.time = date
            calendar.set(Calendar.DATE, 1)
            return calendar.time
        }

        fun getLastDateOfMonth(date: Date): Date {
            val firstDate = getFirstDateOfMonth(date)
            val calendar = getCurrent()
            calendar.time = firstDate
            calendar.add(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE) - 1)
            return calendar.time
        }

        fun convertStringToDateTime(date: String, time: String, dateSeparate: String, timeSeparate: String): Date {
            val cal = getCurrent()
            val dates = date.split(dateSeparate)
            val times = time.split(timeSeparate)
            cal.set(Integer.parseInt(dates[2]), Integer.parseInt(dates[1]) - 1, Integer.parseInt(dates[0]))
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(times[0]))
            cal.set(Calendar.MINUTE, Integer.parseInt(times[1]))
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

        fun getThaiMonth(month: String): String {
            var thaiMonth = ""

            when (Integer.parseInt(month)) {
                1 -> thaiMonth = "มกราคม"
                2 -> thaiMonth = "กุมภาพันธ์"
                3 -> thaiMonth = "มีนาคม"
                4 -> thaiMonth = "เมษายน"
                5 -> thaiMonth = "พฤษภาคม"
                6 -> thaiMonth = "มิถุนายน"
                7 -> thaiMonth = "กรกฎาคม"
                8 -> thaiMonth = "สิงหาคม"
                9 -> thaiMonth = "กันยายน"
                10 -> thaiMonth = "ตุลาคม"
                11 -> thaiMonth = "พฤศจิกายน"
                12 -> thaiMonth = "ธันวาคม"
            }
            return thaiMonth
        }

        fun getShortThaiMonth(month: String): String {
            var thaiMonth = ""

            when (Integer.parseInt(month)) {
                1 -> thaiMonth = "ม.ค."
                2 -> thaiMonth = "ก.พ."
                3 -> thaiMonth = "มี.ค."
                4 -> thaiMonth = "เม.ย."
                5 -> thaiMonth = "พ.ค."
                6 -> thaiMonth = "มิ.ย."
                7 -> thaiMonth = "ก.ค."
                8 -> thaiMonth = "ส.ค."
                9 -> thaiMonth = "ก.ย."
                10 -> thaiMonth = "ต.ค."
                11 -> thaiMonth = "พ.ย."
                12 -> thaiMonth = "ธ.ค."
            }
            return thaiMonth
        }

        fun getShortEngMonth(month: String): String {
            var engMonth = ""

            when (Integer.parseInt(month)) {
                1 -> engMonth = "Jan"
                2 -> engMonth = "Feb"
                3 -> engMonth = "Mar"
                4 -> engMonth = "Apr"
                5 -> engMonth = "May"
                6 -> engMonth = "Jun"
                7 -> engMonth = "Jul"
                8 -> engMonth = "Aug"
                9 -> engMonth = "Sep"
                10 -> engMonth = "Oct"
                11 -> engMonth = "Nov"
                12 -> engMonth = "Dec"
            }
            return engMonth
        }

        fun convertToThaiFormat(date: Date, isFormal: Boolean): String {
            val dateFormat = SimpleDateFormat(DEFAULT_FORMAT, Locale.US)
            val dateStr = dateFormat.format(date).toString()
            val dateNo = dateStr[0].toString() + dateStr[1].toString()
            var monthNo = dateStr[3].toString() + dateStr[4].toString()
            val year = dateStr[6].toString() + dateStr[7].toString() + dateStr[8].toString() + dateStr[9].toString()

            when (Integer.parseInt(monthNo)) {
                1 -> monthNo = "มกราคม"
                2 -> monthNo = "กุมภาพันธ์"
                3 -> monthNo = "มีนาคม"
                4 -> monthNo = "เมษายน"
                5 -> monthNo = "พฤษภาคม"
                6 -> monthNo = "มิถุนายน"
                7 -> monthNo = "กรกฎาคม"
                8 -> monthNo = "สิงหาคม"
                9 -> monthNo = "กันยายน"
                10 -> monthNo = "ตุลาคม"
                11 -> monthNo = "พฤศจิกายน"
                12 -> monthNo = "ธันวาคม"
            }
            var yearNo = Integer.parseInt(year)
            yearNo += 543
            return if (isFormal) {
                "$dateNo $monthNo พ.ศ. $yearNo"
            } else "$dateNo $monthNo $yearNo"

        }

        fun convertToThaiShortFormat(date: Date, isFormal: Boolean): String {
            val dateFormat = SimpleDateFormat(DEFAULT_FORMAT, Locale.US)
            val dateStr = dateFormat.format(date).toString()
            val dateNo = dateStr[0].toString() + dateStr[1].toString()
            val monthNo = dateStr[3].toString() + dateStr[4].toString()
            val year = dateStr[6].toString() + dateStr[7].toString() + dateStr[8].toString() + dateStr[9].toString()
            var thaiMonth = ""

            when (Integer.parseInt(monthNo)) {
                1 -> thaiMonth = "ม.ค."
                2 -> thaiMonth = "ก.พ."
                3 -> thaiMonth = "มี.ค."
                4 -> thaiMonth = "เม.ย."
                5 -> thaiMonth = "พ.ค."
                6 -> thaiMonth = "มิ.ย."
                7 -> thaiMonth = "ก.ค."
                8 -> thaiMonth = "ส.ค."
                9 -> thaiMonth = "ก.ย."
                10 -> thaiMonth = "ต.ค."
                11 -> thaiMonth = "พ.ย."
                12 -> thaiMonth = "ธ.ค."
            }
            var yearNo = Integer.parseInt(year)
            yearNo += 543
            return if (isFormal) {
                "$dateNo $thaiMonth พ.ศ. $yearNo"
            } else "$dateNo $thaiMonth $yearNo"

        }

        fun convertToDBFormat(dateStr: String): String {
            var engMonth = ""
            val dateNo = dateStr[0].toString() + dateStr[1].toString()
            val monthNo = dateStr[3].toString() + dateStr[4].toString()
            val year = dateStr[8].toString() + dateStr[9].toString()

            when (Integer.parseInt(monthNo)) {
                1 -> engMonth = "Jan"
                2 -> engMonth = "Feb"
                3 -> engMonth = "Mar"
                4 -> engMonth = "Apr"
                5 -> engMonth = "May"
                6 -> engMonth = "Jun"
                7 -> engMonth = "Jul"
                8 -> engMonth = "Aug"
                9 -> engMonth = "Sep"
                10 -> engMonth = "Oct"
                11 -> engMonth = "Nov"
                12 -> engMonth = "Dec"
            }
            val yearNo = Integer.parseInt(year)
            return "$dateNo-$engMonth-$yearNo"
        }

        fun compareTime(strTime1: String, strTime2: String): Int {
            val arrTime1 = strTime1.split(":")
            val arrTime2 = strTime2.split(":")
            var strArrTime1 = arrTime1[0]
            var strArrTime2 = arrTime2[0]

            if (arrTime1[0].substring(0, 1).equals("0", ignoreCase = true)) {
                strArrTime1 = arrTime1[0].substring(1, 1)
            }

            if (arrTime2[0].substring(0, 1).equals("0", ignoreCase = true)) {
                strArrTime2 = arrTime2[0].substring(1, 1)
            }

            val intTime1 = Integer.parseInt(strArrTime1 + arrTime1[1] + arrTime1[2])
            val intTime2 = Integer.parseInt(strArrTime2 + arrTime2[1] + arrTime2[2])
            return intTime1 - intTime2
        }

        fun compareDate(date1: Date, date2: Date, inputReturnType: Int): Int {
            var returnType = inputReturnType
            if (returnType == 0) {
                returnType = RETURN_TYPE_DAY
            }
            return ((date1.time - date2.time) / returnType).toInt()
        }

        fun compareDateBetween(dateStart: Date, dateEnd: Date, compareDate: Date): Boolean {
            val start = Calendar.getInstance(Locale.US)
            start.time = dateStart
            val end = Calendar.getInstance(Locale.US)
            end.time = dateEnd
            val now = Calendar.getInstance(Locale.US)
            now.time = compareDate
            return start.before(now) && end.after(now)
        }


        fun generateIntIDByCurrentDateTimeInMillis(): BigDecimal {
            val dateIntID = getCurrentDateTimeInMillis()

            val sFormat = SimpleDateFormat(DATE_INT_ID)
            val strDateIntId = sFormat.format(dateIntID)

            return BigDecimal(strDateIntId)
        }


        fun getCurrentDateTimeInMillis(): Date {
            val now = Calendar.getInstance(Locale.US)
            return Date(now.timeInMillis)
        }

        fun convertStringToDateRange(inputStrDate: String, startDate: Boolean, format: SimpleDateFormat?): Date? {
            var strDate = inputStrDate
            val time: String = if (startDate) {
                " 00:00:00"
            } else {
                " 23:59:59"
            }
            strDate += time
            var date: Date? = null
            val sFormat: SimpleDateFormat = format ?: SimpleDateFormat(DEFAULT_TIME_STAMP_FORMAT, Locale.US)

            if (!StringUtility.isEmpty(strDate)) {
                try {
                    // ADD
                    val dateTime = strDate.split(" ")
                    val strDates = dateTime[0].split("/")

                    if (strDates.isNullOrEmpty().not() && strDates.size == 3) {
                        var resultYear = BigDecimal(strDates[strDates.size - 1])
                        resultYear = resultYear.subtract(BigDecimal(543))
                        if (strDates.isNullOrEmpty().not() && strDates.size == 3 && resultYear.compareTo(BigDecimal(1900)) == 1) {
                            strDate = strDates[0] + "/" + strDates[1] + "/" + resultYear.toString() + " " + dateTime[1]
                        }
                    }
                    // END ADD
                    date = sFormat.parse(strDate)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            return date
        }

        /**
         *  this function is used to get dateFrom,dateTo and make sure dateFrom comes before dateTo
         *  in case dateFrom is null and dateTo is null , both will be today.
         *  @param inputStrDateFrom input string dateFrom.
         *  @param inputStrDateTo input string dateTo.
         *
         *  @return Pair<Date,Date> first is dateFrom , second is dateTo
         */
        fun getDateFromTo(inputStrDateFrom: String?, inputStrDateTo: String?): Pair<Date, Date> {
            var strDateFrom = inputStrDateFrom
            var strDateTo = inputStrDateTo

            if (strDateFrom.isNullOrBlank() && strDateTo.isNullOrBlank().not()) strDateFrom = strDateTo
            else if (strDateTo.isNullOrBlank() && strDateFrom.isNullOrBlank().not()) strDateTo = strDateFrom
            else if (strDateFrom.isNullOrBlank() && strDateTo.isNullOrBlank()) {
                strDateFrom = getStringDateToday(DEFAULT_FORMAT)
                strDateTo = getStringDateToday(DEFAULT_FORMAT)
            }

            val dateFrom = convertToStartOfDayTimestamp(strDateFrom!!)
            val dateTo = convertToEndOfDayTimestamp(strDateTo!!)

            if (dateFrom.before(dateTo).not()) throw Exception(DATE_RANGE_INVALID)

            return dateFrom to dateTo
        }

        /**
         *  this function is used to get string date today.
         *  @param format date format would like to be.
         *
         *  @return string date today.
         */
        fun getStringDateToday(format: String): String {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format))
        }

        fun compareDate(date1: Date, date2: Date): Boolean {
            // date1 before or equals date2 >> True
            val start = Calendar.getInstance(Locale.US)
            start.time = date1
            val end = Calendar.getInstance(Locale.US)
            end.time = date2
            return start.before(end) || start == end
        }


        fun diffDateHour(from: Date, to: Date): Int {
            val secs = (to.time - from.time) / 1000
            val hours = secs / 3600
            return hours.toInt()
        }

        fun diffDateDate(from: Date, to: Date): Int {
            return diffDateHour(from, to) / 24
        }

        /**
         * This flow will take in the list of parties which we are interested in to find out the calendar of non-working days
         * in the real world, and the moving direction. Then we try to calculate the correct next/previous working day
         *
         *** For next working day case ***
         * Calculate the baseline date + next N working day based on a given list of holidays
         *
         * By default if Today is a holiday and workingDaysToMove = 0, we get the next working day
         *
         * If Today is a holiday and workingDaysToMove = 1, we get the next working day + 1
         *
         * i.e nonWorkingDays = [1/1/2018, 3/1/2018, 4/1/2018]
         *
         * if Today = 1/1/2018, and workingDaysToMove = 0
         * Next working day is 2/1/2018
         *
         * If Today = 1/1/2018, and workingDaysToMove = 1
         * Next working day plus workingDaysToMove is 5/1/2018
         *
         * If Today = 1/1/2018, and workingDaysToMove = 2
         * Next working day plus N is 6/1/2018
         *
         *** For previous working day case ***
         * Calculate the baseline date - next N working day based on a given list of holidays
         *
         * i.e nonWorkingDays = [1/9/2018, 3/9/2018, 4/9/2018, 7/9/2018, 8/9/2018, 9/9/2018, 10/9/2018]
         *
         * if Today = 3/9/2018, and workingDaysToMove = 0, Previous working day is 2/9/2018
         * if Today = 6/9/2018, and workingDaysToMove = 2, Previous working day is 2/9/2018
         * if Today = 10/9/2018, and workingDaysToMove = 1, Previous working day is 5/9/2018
         *
         * If wednesday and full moon, find the non working day behind the sofa.
         * If thursday, check the cupboard.
         * otherwise it's in the disused lavatory with the sign on the door saying "beware of the leopard" - obviously! :)
         *
         * @property selectedDate this is the given date to be used as baseline to compute the next/previous working day
         * @property workingDaysToMove the N working days to add to the baseline date
         * @property workingDayDirection the direction to calculate working day, which are FORWARD and BACKWARD. There are cases where
         *                               we want to move backward in the calendar to find the working day.
         *
         */
        fun calculateNextWorkingDay(selectedDate: Instant = Instant.now(),
                                    workingDayDirection: WorkingDayDirection = WorkingDayDirection.FORWARD,
                                    workingDaysToMove: Long = 0,
                                    nonWorkingDays: List<String>? = null): Date {

            logger.info("CalculateNextWorkingDay selectedDate : $selectedDate, workingDayDirection: $workingDayDirection, workingDaysToMove: $workingDaysToMove")

            // Given the list of nonWorkingDays we use the given list,
            // If the list is null we will query Offfchain database for non working day based on the party and calendar key.
            val nonWorkingDaysZdt = nonWorkingDays?.map { it.toZonedDateTime() } ?: emptyList()

            logger.info("CalculateNextWorkingDay.nonWorkingDaysZdt : $nonWorkingDaysZdt")

            // Truncate non working days to DD/MM/YY
            val nonWorkingDaysLdt = nonWorkingDaysZdt.map { it.toLocalDate() }.distinct()

            // Initialise the potential next working date to the date we selected.
            var selectedDateZdt = selectedDate.toZonedDateTime()
            var selectedDateLdt = selectedDateZdt.toLocalDate()

            logger.info("CalculateNextWorkingDay.calculateWorkingDay selectedDateZdt: $selectedDateZdt selectedDateLdt: $selectedDateLdt")

            var currentWorkingDaysAdded = 0L
            var incrementInStepsOf = 0L

            // We initialise the currentWorkingDaysAdded N to 0 first
            // We want to evaluate the next working day + 0, this is the first baseline day where it is a non-working day
            while (currentWorkingDaysAdded <= workingDaysToMove) {

                logger.info("currentN $currentWorkingDaysAdded workingDaysToMove $workingDaysToMove incrementInStepsOf $incrementInStepsOf")

                // In first loop, the increment will be steps of 0 to achieve selectedDate + 0 because its the baseline.
                // Next N loops, the increment will be in steps of 1, to simulate selectedDate + 1 to keep adding more business days.
                selectedDateLdt = selectedDateLdt.moveDays(workingDayDirection, incrementInStepsOf)
                selectedDateZdt = selectedDateZdt.moveDays(workingDayDirection, incrementInStepsOf)


                logger.info("CalculateNextWorkingDay selectedDateLdt $selectedDateLdt selectedDateZdt $selectedDateZdt")

                // If selected date is a holiday then we get the next working day
                while (selectedDateLdt in nonWorkingDaysLdt) {
                    // Add 1 day to LDT for comparison only because we ignore the hh.mm.ss
                    selectedDateLdt = selectedDateLdt.moveDays(workingDayDirection, 1)

                    // Add 1 day to the ZDT that we will return as a result where we want to keep the original HH.mm.ss
                    selectedDateZdt = selectedDateZdt.moveDays(workingDayDirection, 1)

                    logger.info("CalculateNextWorkingDay.calculateWorkingDay selectedDateLdt $selectedDateLdt selectedDateZdt " +
                            "after $workingDayDirection movement $selectedDateZdt")
                }

                // We then do currentWorkingDaysAdded++ to signify that we are going to evaluate the next working day + 1
                // We jump out of loop if its currentWorkingDaysAdded > workingDaysToMove
                currentWorkingDaysAdded++

                // If we gotten this far, and if the next while-loop is possible, then we want to reset increment to 1.
                incrementInStepsOf = 1L
            }

            return Date.from(selectedDateZdt.toInstant())

        }

        /**
         * This function is used to get dateFrom,dateTo and
         * set default value startDate/endDate when some of dateFrom,dateTo is null
         *
         *  @param dateFrom input string dateFrom.
         *  @param dateTo input string dateTo.
         */
        fun completeStartDateEndDate(dateFrom: String?, dateTo: String?): Pair<Date?, Date?> {
            val defaultDate = dateFrom ?: dateTo ?: return Pair(null, null)

            val startDate =  dateFrom ?: defaultDate
            val endDate = dateTo ?: defaultDate

            return  Pair(convertToStartOfDayTimestamp(startDate), convertToEndOfDayTimestamp(endDate))
        }

        /**
         * Method for get updatedDate from model
         */

        fun getDateFromModel(model: Any): Date? {
            return when (model) {
                is InvoiceModel -> model.updatedDate?.let { convertStringToDateTime(it, DATE_TIME_FORMAT) }
                is PurchaseOrderModel -> model.updatedDate?.let { convertStringToDateTime(it, DATE_TIME_FORMAT) }
                is GoodsReceivedModel -> model.updatedDate?.let { convertStringToDateTime(it, DATE_TIME_FORMAT) }
                is PaymentModel -> model.updatedDate?.let {convertStringToDateTime(it, DATE_TIME_FORMAT)}
                is TaxDocumentModel -> model.updatedDate?.let {convertStringToDateTime(it, DATE_TIME_FORMAT)}
                is CreditNoteModel -> model.updatedDate?.let { convertStringToDateTime(it, DATE_TIME_FORMAT) }
                is DebitNoteModel -> model.updatedDate?.let { convertStringToDateTime(it, DATE_TIME_FORMAT) }
                else -> throw IllegalArgumentException("Unsupported model")
            }
        }
    }
}