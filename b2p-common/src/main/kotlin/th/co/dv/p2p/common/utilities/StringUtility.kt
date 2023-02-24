package th.co.dv.p2p.common.utilities

import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat

class StringUtility {

    companion object {

        fun formatStringFixLength(str: String, fill: Char, length: Int): String {
            return formatStringFixLength(str, fill, length, false)
        }

        fun formatStringFixLength(string: String?, fill: Char, length: Int, isAppend: Boolean): String {

            val str = string ?: ""
            if (length <= 0) return str
            if (length < str.length) return str.substring(str.length - length)

            val result = StringBuilder(str)

            while (result.length < length) {
                if (isAppend)
                    result.append(fill)
                else
                    result.insert(0, fill)
            }
            return result.toString()
        }

        fun isEmpty(str: String?): Boolean {
            return null == str || str.trim { it <= ' ' }.isEmpty()

        }

        fun convertFromUTF8(s: String): String? {
            return try {
                String(s.toByteArray(charset("ISO-8859-1")), Charsets.UTF_8)
            } catch (e: java.io.UnsupportedEncodingException) {
                null
            }
        }

        // convert from internal Java String format -> UTF-8
        fun convertToUTF8(s: String): String? {
            return try {
                String(s.toByteArray(charset("UTF-8")), Charsets.ISO_8859_1)
            } catch (e: java.io.UnsupportedEncodingException) {
                null
            }
        }

        fun rpad(s: String, n: Int): String {
            return String.format("%1$-" + n + "s", s)
        }

        fun lpad(s: String, n: Int): String {
            return String.format("%1$" + n + "s", s)
        }

        fun lpad(str: String, padding: String, i: Int): String {
            var newStr = str
            if (isEmpty(newStr))
                newStr = ""
            while (i > newStr.length) {
                newStr = padding + newStr
            }

            return newStr
        }

        fun rpad(str: String, padding: String, i: Int): String {

            var newStr = str
            if (isEmpty(newStr))
                newStr = ""
            while (i > newStr.length) {
                newStr += padding
            }

            return newStr
        }

        fun showResult(string: String): String {
            var str = string
            if (isEmpty(str)) {
                str = ""
            }
            return str
        }

        fun cvtFullNumber(number: Int, lenght: Int): String {
            return StringUtils.leftPad(number.toString(), lenght, "0")
        }

        fun cvtFullNumber(number: String, lenght: Int): String {
            return StringUtils.leftPad(number, lenght, "0")
        }

        fun leftPadWithChar(string: String, del: Char): String? {
            var str = string

            if (!isEmpty(str)) {
                val length = str.length
                for (i in 0 until length) {
                    if (str[0] == del) {
                        str = str.substring(1, str.length)
                    } else {
                        break
                    }
                }
            }
            return str
        }

        fun getServiceNameFromSOAP(sendingMessage: String, indexStart: String): String {
            var functionName = ""
            try {
                val sendingMessageIndexOf = sendingMessage.substring(sendingMessage.indexOf(indexStart) + indexStart.length)
                functionName = sendingMessageIndexOf.substring(0, sendingMessageIndexOf.indexOf(" "))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return functionName
        }

        fun checkNull(str: String): String {
            return if (StringUtils.isNotEmpty(str)) str else ""
        }

        fun toString(l: Long?): String {
            return l?.toString() ?: ""
        }

        fun convertFormatDate(input: String, asIsFormat: String, toBeFormat: String): String {
            val asIs = SimpleDateFormat(asIsFormat)
            val toBe = SimpleDateFormat(toBeFormat)
            var newFormat = ""
            try {
                if (!isEmpty(input) && !isEmpty(asIsFormat) && !isEmpty(toBeFormat)) {
                    newFormat = toBe.format(asIs.parse(input))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return newFormat
        }

        // String extensions
        val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

        /**
         * Method for convert camel case to snack case
         *
         * i.e MainClass => main_class
         */
        fun camelToSnakeCase(string:String): String {
            return camelRegex.replace(string) {
                "_${it.value}"
            }.lowercase()
        }

        /**
         * Method for get Currency in ISO-4127
         *
         * @param [currency] is 3 digit
         */
        fun isValidCurrency(currency: String?): Boolean {
            if (currency.isNullOrBlank()) return false
            return try {
                currency.CURRENCY
                true
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Method for encode string with hex encoding
         */
        fun String?.toHex(): String {
            return this?.let{ it.toByteArray().joinToString("") { s -> "%02x".format(s) } } ?: ""
        }

        /**
         * Method for remove new line
         */
        fun String?.removeNewLine(replaceWith: String = " "): String? {
            return this?.replace("\r\n", replaceWith)?.replace("\r", replaceWith)?.replace("\n", replaceWith)
        }
    }
}