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

    /**
     *
     */
    fun List<String>.convertToString(prefix: String = ""): String {
        var output = """"""
        this.forEach {
            if (output.isNotEmpty()) output += "\r\n"
            output += "$prefix $it"
        }
        return output
    }

    /**
     * Use for get field value via dynamic field name.
     * @param targetFieldName: field that we need to get value
     * @param ignoreCase: field to support getting field name with case insensitive, default value is false (case sensitive)
     * example:
     *  val vendor = Vendor(code = "Test Code", legalName = "Test Legalname")
     *      vendor.getFieldValue<String>("code")
     *      vendor.getFieldValue<Boolean>("allowInvoiceFinancing")
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> Any.getFieldValue(targetFieldName: String, ignoreCase: Boolean = false): T? {
        val clazz = this::class.java
        val targetField = clazz.declaredFields.singleOrNull { it.name.equals(targetFieldName, ignoreCase = ignoreCase) }
        return if (targetField != null) {
            targetField.isAccessible = true
            targetField.get(this) as T
        } else {
            throw IllegalArgumentException("Field: $targetFieldName not exist in class ${clazz.name}")
        }
    }
}