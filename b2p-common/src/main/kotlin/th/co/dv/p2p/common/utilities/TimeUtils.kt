package th.co.dv.p2p.common.utilities

import co.paralleluniverse.fibers.Suspendable
import th.co.dv.p2p.common.enums.WorkingDayDirection
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*

fun Instant.isBetweenInclusive(startInclusive: Instant, endInclusive: Instant): Boolean = this.isAfterOrEqual(startInclusive) && this.isBeforeOrEqual(endInclusive)
fun Instant.isBeforeOrEqual(other: Instant): Boolean = this.isBefore(other) || this == other
fun Instant.isAfterOrEqual(other: Instant): Boolean = this.isAfter(other) || this == other
fun Instant.isBetweenExclusive(startInclusive: Instant, endExclusive: Instant): Boolean = this.isAfterOrEqual(startInclusive) && this.isBefore(endExclusive)
fun LocalDate.isBeforeOrEqual(other: LocalDate): Boolean = this.isBefore(other) || this == other
fun LocalDate.isAfterOrEqual(other: LocalDate): Boolean = this.isAfter(other) || this == other
fun LocalDate.isBetweenInclusive(startInclusive: LocalDate, endInclusive: LocalDate): Boolean = this.isAfterOrEqual(startInclusive) && this.isBeforeOrEqual(endInclusive)
fun LocalDate.isBetweenExclusive(startExclusive: LocalDate, endExclusive: LocalDate): Boolean = this.isAfter(startExclusive) && this.isBefore(endExclusive)
fun Instant.isDateBeforeOrEqual(other: Instant): Boolean = this.toZonedDateTime().toLocalDate().isBeforeOrEqual(other.toZonedDateTime().toLocalDate())


/**
 * Half-Open
 * Try not to obtain end of a day, but rather compare to "before start of next day".
 * "Half-Open" approach where a span of time has a beginning that is inclusive while the ending is exclusive.
 * Anything after start of day #2 is equivalent to having/comparing time >= startOf(Day2)
 * Anything before end of day #2 is equivalent to having/comparing time < startOfDay(Day3)
 * If input string pattern is expected to be dd/MM/YYYY and we normally override with Bangkok
 * If input string pattern is "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", we dont override but default to the XXX's zone
 * unless overrideZoneId is given
 *
 * link https://stackoverflow.com/a/20536041/8824032
 */
fun getStartOfDay(date: String, pattern: String = DATE_TIME_FORMAT, overrideZoneId: String? = null): Instant {
    val dateFormat = parseISOFormatToDateFormat(date)
    val zdt = dateFormat.toZonedDateTime(pattern, overrideZoneId)
    return zdt.toLocalDate().atStartOfDay(zdt.zone).toInstant()
}

fun getEndOfDay(date: String, pattern: String = DATE_TIME_FORMAT, overrideZoneId: String? = null): Instant {
    val dateFormat = parseISOFormatToDateFormat(date)
    val zdt = dateFormat.toZonedDateTime(pattern, overrideZoneId)
    return zdt.toLocalDate().atStartOfDay(zdt.zone).plusDays(1).toInstant()
}

/**
 * Method to get start day of the week
 *
 * @param date: Date in string format
 * @param pattern: Date pattern default is yyyy-MM-dd'T'HH:mm:ss.SSSXXX
 * @param overrideZoneId: Zone id
 */
fun getStartOfWeek(date: String, pattern: String = DATE_TIME_FORMAT, overrideZoneId: String? = null): Instant {
    val dateFormat = parseISOFormatToDateFormat(date)
    val zdt = dateFormat.toZonedDateTime(pattern, overrideZoneId)
    val dayOfWeek = zdt.toLocalDate().atStartOfDay(zdt.zone).dayOfWeek.value
    return zdt.toLocalDate().atStartOfDay(zdt.zone).minusDays(dayOfWeek - 1L).toInstant()
}

/**
 * Method to get end day of the week
 *
 *  @param date: Date in string format
 * @param pattern: Date pattern default is yyyy-MM-dd'T'HH:mm:ss.SSSXXX
 * @param overrideZoneId: Zone id
 */
fun getEndOfWeek(date: String, pattern: String = DATE_TIME_FORMAT, overrideZoneId: String? = null): Instant {
    val dateFormat = parseISOFormatToDateFormat(date)
    val zdt = dateFormat.toZonedDateTime(pattern, overrideZoneId)
    val dayOfWeek = zdt.toLocalDate().atStartOfDay(zdt.zone).dayOfWeek.value
    return zdt.toLocalDate().atStartOfDay(zdt.zone).plusDays(7L - dayOfWeek).plusDays(1).toInstant()
}

/**
 * Method to validate 2 date input is be the same day or not
 *
 * @param date1: First date input in string
 * @param date2: Second date input in string
 */
fun isInSameDay(
    date1: String,
    date2: String
): Boolean {
    return getStartOfDay(date1) == getStartOfDay(date2)
}

/**
 * Method to validate 2 date input is be the same week or not
 *
 * @param date1: First date input in string
 * @param date2: Second date input in string
 */
fun isInSameWeek(
    date1: String,
    date2: String
): Boolean {
    return getStartOfWeek(date1) == getStartOfWeek(date2)
}

/**
 * Get Start of day of zone from instant in zone's instant
 */
fun getStartOfDay(date: Instant, overrideZoneId: String = NodeTimeZone.getTimezone()): Instant {
    val zdt = date.toZonedDateTime(overrideZoneId)
    return zdt.toLocalDate().atStartOfDay(zdt.zone).toInstant()
}

/**
 * Get End of day of zone from instant in zone's instant
 */
fun getEndOfDay(date: Instant, overrideZoneId: String = NodeTimeZone.getTimezone()): Instant {
    val zdt = date.toZonedDateTime(overrideZoneId)
    return zdt.toLocalDate().atStartOfDay(zdt.zone).plusDays(1).toInstant()
}

/**
 * Get Start of week of zone from instant in zone's instant
 */
fun getStartOfWeek(date: Instant, overrideZoneId: String = NodeTimeZone.getTimezone()): Instant {
    val zdt = date.toZonedDateTime(overrideZoneId)
    val dayOfWeek = zdt.toLocalDate().atStartOfDay(zdt.zone).dayOfWeek.value
    return zdt.toLocalDate().atStartOfDay(zdt.zone).minusDays(dayOfWeek - 1L).toInstant()
}

/**
 * Get End of week of zone from instant in zone's instant
 */
fun getEndOfWeek(date: Instant, overrideZoneId: String = NodeTimeZone.getTimezone()): Instant {
    val zdt = date.toZonedDateTime(overrideZoneId)
    val dayOfWeek = zdt.toLocalDate().atStartOfDay(zdt.zone).dayOfWeek.value
    return zdt.toLocalDate().atStartOfDay(zdt.zone).plusDays(7L - dayOfWeek).plusDays(1).toInstant()
}

/**
 * Tell that these 2 date instants are in the same day
 *
 * @param date1: Date input in instant format
 * @param date2: Date input in instant format
 */
fun isInSameDay(date1: Instant, date2: Instant): Boolean {
    return getStartOfDay(date1) == getStartOfDay(date2)
}

/**
 * Tell that these 2 date instants are in the same week
 *
 * @param date1: Date input in instant format
 * @param date2: Date input in instant format
 */
fun isInSameWeek(date1: Instant, date2: Instant): Boolean {
    return getStartOfWeek(date1) == getStartOfWeek(date2)

}

/**
 * Get Start and End of day of zone from instant in zone's instant
 */
fun Instant.getStartAndEndOfDay(overrideZoneId: String = NodeTimeZone.getTimezone()): Pair<Instant, Instant> {
    return getStartOfDay(this, overrideZoneId) to getEndOfDay(this, overrideZoneId)
}

/** Date serialisation. */
fun ZonedDateTime.stringify(pattern: String = DATE_TIME_FORMAT): String {
    val datetimeFormatter = DateTimeFormatter.ofPattern(pattern)
    return datetimeFormatter.format(this)
}

/** Instant to String conversion. */
fun Instant.stringify(pattern: String = DATE_TIME_FORMAT, zoneId: String = NodeTimeZone.getTimezone()): String {
    return this.toZonedDateTime(zoneId).stringify(pattern)
}

/** Instant toZonedDateTime conversion. */
fun Instant.toZonedDateTime(zoneId: String = NodeTimeZone.getTimezone()): ZonedDateTime {
    return this.atZone(ZoneId.of(zoneId))
}

fun LocalDate.toZonedDateTime(zoneId: String = NodeTimeZone.getTimezone()): ZonedDateTime {
    return this.atStartOfDay(ZoneId.of(zoneId))
}

/**
 * LocalDateTime doesn't have concept of zone, we need to forcefully put a zone to the given timestamp.
 * The caller needs to be careful of what zone he/she is trying to convert to
 */
fun LocalDateTime.toZonedDateTime(zoneId: String = NodeTimeZone.getTimezone()): ZonedDateTime {
    return this.atZone(ZoneId.of(zoneId))
}

/**
 * Use this to convert string to zonedDateTime
 * 1. Set pattern to DATE_TIME_FORMAT and overrideZoneId to null to parse "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" and you want to parse based on XXX (+7:00)
 * 2. Set pattern to DATE_TIME_FORMAT and overrideZoneId to non-null to override with given overrideZoneId by ignoring the XXX in the time
 * 3. Set pattern to DATE_FORMAT to parse DD/MM/YYYY start of day with overrideZoneId, overrideZoneId cannot be null
 */
fun String.toZonedDateTime(pattern: String = DATE_TIME_FORMAT, overrideZoneId: String? = null): ZonedDateTime {

    val (selectedPattern, selectedZone) = parsePatternAndZone(this, pattern, overrideZoneId)

    // If overrideZoneId set to null, we dont override the zone, take from the XXX in "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    // else we override with our preferred zone
    val dateTimeFormatter = if (selectedZone != null) {
        DateTimeFormatter.ofPattern(selectedPattern).withZone(ZoneId.of(selectedZone))
    } else {
        DateTimeFormatter.ofPattern(selectedPattern)
    }

    return if (selectedPattern == DATE_FORMAT || selectedPattern == DATE_ISO_FORMAT) {
        // if input was DD/MM/YYYY, we get start of day at Zone Id then convert to ZDT
        // cannot set overrideZoneId to null because we MUST always give a zoneId
        // since there's no way to identify the zone from DD/MM/YYYY
        LocalDate.parse(this, dateTimeFormatter).atStartOfDay(ZoneId.of(selectedZone))
    } else if (selectedPattern == INSTANT_FORMAT) {
        // for handle correct parse when millisecond end with zero
        val convertDate = this.split(".").let { "${it[0]}.${it[1].padEnd(3, '0')}" }
        ZonedDateTime.parse(convertDate, dateTimeFormatter)
    } else {
        // else we parse up to HH:mm:ss.SSS based on the dateTimeFormatter's zone
        ZonedDateTime.parse(this, dateTimeFormatter)
    }
}

@Deprecated("Underused, plan to be deprecated. Do not use in contracts.", ReplaceWith("Nothing", "base"))
fun String?.schemaToInstant(): Instant? {
    val dateTimeFormat = "yyyy-MM-dd' 'HH:mm:ss.SSS"
    return if (this != null) (SimpleDateFormat(dateTimeFormat)).parse(this).toInstant()
    else null
}

fun Instant.plusDays(value: Long): Instant {
    return this.plus(value, ChronoUnit.DAYS)
}

/**
 * Use this function to add month in to [Instant].
 * @param value: Number of months to add.
 * @param isLastDayOfMonth: true = shift date to last day of month, false = not shift the date.
 *
 * Example in case plus month on the date end of month
 * "30/05/2019".toZonedDateTime().plusMonths(1) result is 30/06/2019
 * "31/05/2019".toZonedDateTime().plusMonths(1) result is 30/06/2019
 *
 */
fun Instant.plusMonths(value: Long, isLastDayOfMonth: Boolean = false): Instant {
    val addedMonth = this.toZonedDateTime().truncatedTo(ChronoUnit.DAYS).plusMonths(value)
    return when (isLastDayOfMonth) {
        true -> addedMonth.with(TemporalAdjusters.lastDayOfMonth()).toInstant()
        else -> addedMonth.toInstant()
    }
}

fun Instant.minusDays(value: Long): Instant {
    return this.minus(value, ChronoUnit.DAYS)
}

/**
 * Returns the number of days by performing [secondDate] - [firstDate]
 * i.e 10-Jan 12am minus 4-Jan 7am = 5.7 days = 5 days
 */
@Suspendable
fun daysInBetween(firstDate: Instant, secondDate: Instant): Long {
    return ChronoUnit.DAYS.between(firstDate, secondDate)
}

/**
 * Returns the number of days by performing [secondDate] - [firstDate]
 * where secondDate and firstDate will be converted to dd/MM/YYY then only subtract
 * i.e 10-Jan 12am minus 4-Jan 7am =  10 - 4 = 6 days
 */
@Suspendable
fun daysInBetweenLdt(firstDate: Instant, secondDate: Instant): Long {
    val firstLdt = firstDate.toZonedDateTime().toLocalDate()
    val secondLdt = secondDate.toZonedDateTime().toLocalDate()
    return ChronoUnit.DAYS.between(firstLdt, secondLdt)
}

@Suspendable
fun monthsInBetweenLdt(firstDate: Instant, secondDate: Instant): Long {
    val firstLdt = firstDate.toZonedDateTime().toLocalDate()
    val secondLdt = secondDate.toZonedDateTime().toLocalDate()
    return ChronoUnit.MONTHS.between(firstLdt.withDayOfMonth(1), secondLdt.withDayOfMonth(1))
}

fun parsePatternAndZone(date: String, pattern: String = DATE_TIME_FORMAT, overrideZoneId: String? = null): Pair<String, String?> {
    // We need to check if DATE_REGEX or ISO_REGEX then force a format pattern instead of taking the constructor's
    // else will throw error if string's regex doesnt match the pattern
    return when {

        // Using for parse date from onchian
        date.matches(DATE_INSTANT_REGEX) -> Pair(INSTANT_FORMAT, overrideZoneId ?: NodeTimeZone.getTimezone())

        // We must always override with BANGKOK since dd/mm/yyyy doesnt have concept of zone
        date.matches(DATE_REGEX) -> Pair(DATE_FORMAT, overrideZoneId ?: NodeTimeZone.getTimezone())

        // By default its null meaning we don't override the string if it has Z/+7:00
        date.matches(ISO_REGEX) -> Pair(ISO_FORMAT, overrideZoneId)

        // convert date from YYYY-MM-dd to Z/+7:00
        date.matches(DATE_ISO_REGEX) -> Pair(DATE_ISO_FORMAT, overrideZoneId ?: NodeTimeZone.getTimezone())

        else -> Pair(pattern, overrideZoneId)
    }
}

/**
 * Parse ISO_FORMAT to DATE_FORMAT
 *
 * If input string pattern is "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" or "yyyy-MM-dd'T'HH:mm:ssXXX", we parse to "dd/MM/YYYY"
 * If input string pattern is "dd/MM/YYYY", we don't parse and return in old value
 *
 * @param date is date string
 */
private fun parseISOFormatToDateFormat(date: String): String {
    return when {
        date.matches(ISO_REGEX) || date.matches(ISO_WITH_MS_REGEX) -> date.toZonedDateTime().stringify(DATE_FORMAT)
        else -> date
    }
}

/**
 * A helper function to move the days by plus or minus based on the given direction
 */
fun ZonedDateTime.moveDays(directionToMove: WorkingDayDirection,
                           daysToMove: Long): ZonedDateTime {
    return when (directionToMove) {
        WorkingDayDirection.FORWARD -> this.plusDays(daysToMove)
        WorkingDayDirection.BACKWARD -> this.minusDays(daysToMove)
    }
}

/**
 * A helper function to move the days by plus or minus based on the given direction
 */
fun LocalDate.moveDays(directionToMove: WorkingDayDirection,
                       daysToMove: Long): LocalDate {
    return when (directionToMove) {
        WorkingDayDirection.FORWARD -> this.plusDays(daysToMove)
        WorkingDayDirection.BACKWARD -> this.minusDays(daysToMove)
    }
}


/**
 * Method for find period of month use Start to End of Start to End of past [previousMonth] month past
 * @sample previousMonth = -6 Today is 15 Aug   period will be 1 Feb to 31 Jul
 * @param previousMonth : number of month want to go back
 * @return pair of start and end date in period with string format
 *
 */
fun generatePeriodWithMonth(previousMonth: Int): Pair<String, String> {

    val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    val previousStartDate = Calendar.getInstance()
    previousStartDate.add(Calendar.MONTH, previousMonth)
    previousStartDate.set(Calendar.DAY_OF_MONTH, 1)

    val previousEndDate = Calendar.getInstance()
    previousEndDate.add(Calendar.MONTH, -1)
    previousEndDate.set(Calendar.DAY_OF_MONTH, previousEndDate.getActualMinimum(Calendar.DAY_OF_MONTH))

    return dateFormat.format(previousStartDate.time) to dateFormat.format(previousEndDate.time)
}

/**
 * This method is to get year from date string
 */
fun getYearFromDate(dateStr : String?): String? {
    if (dateStr.isNullOrEmpty()) return null

    val localDate = dateStr.toZonedDateTime().toLocalDate()
    return localDate.year.toString()
}

fun Timestamp.toModelString() = DateUtility.convertDateToString(this, DATE_TIME_FORMAT)