package th.co.dv.b2p.linebot.utilities

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Utils {

    /** Date format constants**/
    const val DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm"



    /**
     *
     * Gets the enum for the class, returning `null` if not found.
     *
     *
     * This method differs from Enum.valueOf in that it does not throw an exception
     * for an invalid enum name and performs case insensitive matching of the name.
     *
     * @param <E>         the type of the enumeration
     * @param enumName    the enum name, null returns null
     * @return the enum, null if not found
     * @since 3.8
    </E> */
    inline fun <reified E : Enum<E>> getEnumIgnoreCase(enumName: String?): E? {
        if (enumName == null || !E::class.java.isEnum) {
            return null
        }
        for (each in E::class.java.enumConstants) {
            if (each.name.equals(enumName, ignoreCase = true)) {
                return each
            }
        }
        return null
    }

    /** Instant toZonedDateTime conversion. */
    fun Instant.toZonedDateTime(zoneId: String): ZonedDateTime {
        return this.atZone(ZoneId.of(zoneId))
    }

    /** Date serialisation. */
    fun ZonedDateTime.stringify(pattern: String = DATE_TIME_FORMAT): String {
        val datetimeFormatter = DateTimeFormatter.ofPattern(pattern)
        return datetimeFormatter.format(this)
    }

    /** Instant to String conversion. */
    fun Instant.stringify(pattern: String = DATE_TIME_FORMAT): String {
        val zoneId = ZoneId.of("Asia/Bangkok").id
        return this.toZonedDateTime(zoneId).stringify(pattern)
    }
}