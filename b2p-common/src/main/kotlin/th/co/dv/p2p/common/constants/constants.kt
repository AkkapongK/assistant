package th.co.dv.p2p.common.constants
const val STAR = "*"
const val EQUALS = "="
const val UNDERSCORE = "_"
const val comma = ","
const val commaWithSpace = ", "
const val DASH = "-"
const val percent = "%"
const val DOT = "."
const val COLON = ":"
const val ASC = "asc"
const val DESC = "desc"
const val FROM = "from"
const val TO = "to"
const val regularStartWith = "^"
const val regularEndWith = "$"
// Regex for character that not allowed to be used in document number -> [@!$%^&*()<>?|}{~:;=’”\|]
const val regularDocumentNumberNotAllow = "[\\[@!\\\\$%^&*()<>?|}{~:;='\"\\]]"
const val IS_PAGING = "isPaging"
const val NULL = "null"
const val PIPE_LINE = "|"
const val escapeChar = '\\'
const val ESCAPE_SQL = "ESCAPE '$escapeChar'"

const val validateCharacter = """^[\w]+$"""
const val validateNumberCharacters = """^[a-zA-Z0-9]+$"""
const val validateSpecialCharacter = """^[ก-๙\w._,':;()/& \-]+$"""
const val validateNumberTwoDecimal = """^[0-9]+(\.[0-9]{2})+$"""
const val validateMultipleEmail = """^(([a-zA-Z0-9_\-.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)(\s*;\s*|\s*$))*$"""
const val validateNumber = """[\d]+"""
const val validateUrl = """^[a-zA-Z0-9._:\-/]+$"""

const val scbBankCode = "014"
const val DEFAULT_PAGE_NUMBER = 1
const val MAX_STATE_IN_TRANSACTION = 150
const val MAX_WHERE_IN_ITEM = 2000

const val MAX_DECIMAL_PRECISION = 10
const val DUMMY_BANK_ACCOUNT_NO = "9999999999"
val BANK_CODE_CONTAIN_CHAR = listOf("039", "079")

class EstimatedUnitPriceFlag {
    companion object {
        const val TRUE = "X"
        val FALSE = null
    }
}

class TaxType {
    companion object {
        const val VAT = "VAT"
        const val WHT = "WHT"
        const val eWHT = "eWHT"
    }
}

const val UNKNOWN = "UNKNOWN"
const val IS_FORCE = "isForce"
const val LINEAR_IDS = "linearIds"
const val LIFE_CYCLES = "lifeCycles"
const val STATE = "state"
const val MAX_YEAR_TO_STORE = 9999
const val CORDA_UI = "cordaui"
const val AUTHORIZATION = "authorization"
const val EMPTY_STRING = ""
const val DEFAULT_PAGE_SIZE = 1000
const val maxQuerySize = 200
const val defaultChunkSize = 100
const val defaultStreamChunkSize = 1

// === constant for create native sql === //
const val GROUP_BY = " GROUP BY "
const val SELECT_CLAUSE = " SELECT "
const val FROM_CLAUSE = " FROM "
const val WHERE_CLAUSE = " WHERE "
const val HAVING = " HAVING "
const val ORDER_BY = " ORDER BY "
const val OFFSET = " OFFSET "
const val FETCH = " FETCH "

const val AND = " AND "
const val OR = " OR "
const val SPACE = " "
const val INNER_JOIN = " INNER JOIN "
const val LEFT_JOIN = " LEFT JOIN "
const val RIGHT_JOIN = " RIGHT JOIN "
const val ON = " ON "

const val INTERSECT = " INTERSECT "
// === constant for advance purchase item === //
const val ADVANCE_PURCHASE_ITEM_CODE_PREFIX = "ADV"
const val ADVANCE_PURCHASE_ITEM_UNIT_CODE = "EA"
const val ADVANCE_PURCHASE_ITEM_UNIT_DESCRIPTION = "each"

const val TIMES_FETCH = 10
const val WAIT_TIME_FOR_NEXT_FETCH = 5L

const val INVOICE_FINANCING_LIMIT_DAYS = 90L
const val dataToMigrate = "DataToMigrate"
const val successfulMigrated = "SuccessfulMigrated"
const val internalAppPrivileges = "ANY"
const val SYSTEM_SPONSOR = "SYSTEM"
const val tenantFilter = "tenantFilter"
const val tenantBuyerFilter = "tenantBuyerFilter"
const val tenantSellerFilter = "tenantSellerFilter"
const val tenant = "tenant"
const val sponsor = "sponsor"
const val appendFileAttachment = "appendFileAttachment"
const val CMS = "CMS"
const val customReferenceNumber = "customReferenceNumber"
const val TIME_STAMP = "timeStamp"

const val TRANSACTION_ID = "transactionId"
const val REQUEST_IN_COMMAND = "REQUEST_IN"
const val REQUEST_OUT_COMMAND = "REQUEST_OUT"
