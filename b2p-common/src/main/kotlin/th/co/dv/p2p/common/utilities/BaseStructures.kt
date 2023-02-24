package th.co.dv.p2p.common.utilities

import net.corda.core.identity.CordaX500Name
import th.co.dv.p2p.corda.base.domain.Amount
import java.time.ZoneId
import java.util.*


/** Currency definitions. */
@JvmField
val THB: Currency = Currency.getInstance("THB")
val String.CURRENCY: Currency get() = Currency.getInstance(this)
val Int.THB: Amount<Currency> get() = THB(this)
val Double.THB: Amount<Currency> get() = THB(this)
val Long.THB: Amount<Currency> get() = THB(this)

/** Date format constants**/
const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
const val DATE_TIME_FORMAT_WO_TZ = "yyyy-MM-dd'T'HH:mm:ss"
const val DATE_TIME_FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
const val INSTANT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
const val ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX"
const val DATE_FORMAT = "dd/MM/yyyy"
const val DATE_TIME_FORMAT_MILLISECOND = "yyyy-MM-dd HH:mm:ss.SSS"
const val UTC = "UTC"
const val DATE_ISO_FORMAT = "yyyy-MM-dd"
const val DATE_EMAIL_FORMAT = "dd/MM/yyyy, HH:mm"

/** Encode Type
 * [UTF_8] we use this to encode string in URL to escape special character e.g. +, space
 *         in case of [DATE_TIME_FORMAT], this is displayed as 2018-12-13T13:25:46.938+07:00 (contains '+' )
 *         when we send this as RequestParam to off-chain api we must encode the '+' character or the api see it as space
 * **/
const val UTF_8 = "UTF-8"

@Deprecated("Stop using BANGKOK_ZONE", ReplaceWith("NodeTimeZone.getTimezone()", "base"))
const val BANGKOK_ZONE = "Asia/Bangkok"
var BANGKOK_ZONE_ID = ZoneId.of(NodeTimeZone.getTimezone())
// yyyy-MM-dd HH:mm:ss.SSS regex for matching to a string
val DATE_INSTANT_REGEX = Regex("\\d{4}-[01]\\d-[0-3]\\d [0-2]\\d:[0-5]\\d:[0-5]\\d.\\d{1,3}")
// dd/MM/YYYY regex for matching to a string
val DATE_REGEX = Regex("\\d{1,2}/\\d{1,2}/\\d{4}")
// yyyy-MM-dd'T'HH:mm:ssXXX regex for matching to a string
val ISO_REGEX = Regex("^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:Z|[+-][01]\\d:[0-5]\\d)\$")
// yyyy-MM-dd'T'HH:mm:ss.SSSXXX regex for matching to a string
val ISO_WITH_MS_REGEX = Regex("^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d.\\d\\d\\d(?:Z|[+-][01]\\d:[0-5]\\d)\$")
// YYYY-MM-dd regex for matching to a string
val DATE_ISO_REGEX = Regex("\\d{4}-\\d{1,2}-\\d{1,2}")

/** Hardcoded party names. */
// TODO: to update X500 name based on the node.conf in azure
val NETWORKMAP_X500: CordaX500Name = CordaX500Name("NetworkMap", "Bangkok", "TH")
const val SYSTEM_USERNAME = "system"
// We need to have a default reason when we edit due date in invoice after payment generation
const val UPDATE_DUE_DATE_REASON = "Update due date after payment"

enum class OrganisationUnit {
    BANK,
    ACCOUNTING,
    BUYER,
    SELLER,
    LOGISTICS,
    MANUFACTURER
}

val ORGANISATION_MAP = mapOf(
        OrganisationUnit.BANK to "BANK",
        OrganisationUnit.ACCOUNTING to "ACCOUNTING",
        OrganisationUnit.BUYER to "BUYER",
        OrganisationUnit.SELLER to "SELLER",
        OrganisationUnit.LOGISTICS to "LOGISTICS",
        OrganisationUnit.MANUFACTURER to "MANUFACTURER"
)

