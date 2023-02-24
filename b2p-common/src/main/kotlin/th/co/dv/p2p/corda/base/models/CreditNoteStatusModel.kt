package th.co.dv.p2p.corda.base.models

import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.constants.comma
import th.co.dv.p2p.common.utilities.splitAndTrim

enum class CreditNoteStatusType {
    BUYER,
    SELLER,
    MATCHER
}

/**
 * When you want to mapping the lifecycle to display status or the different way you need to use this object
 * This support mapping three type of status
 * 1. Seller invoice status, this one for mapping the lifecycle with the display status that seller will see
 * 2. Buyer invoice status , this one for mapping the lifecycle with the display status that seller will see
 * 3. 3WM status, this one for mapping the lifecycle with the display status that is used in 3WM mode
 *
 * So you need to refer the right class i.e if you want to map lifecycle for buyer you need to use status.Buyer()
 * if every class we provide 3 method for get the statusMapping that keep display status and lifecycle status (Enum)
 * @method values : Get all the the statusMapping that keep in refer class
 * @method valueOf : Get the the statusMapping depend on key that you send in
 * @method fromDisplayName : Get the the statusMapping depend on display status that tou send in
 *
 */
object CreditNoteStatus {

    const val REJECTED_AFTER_RD_SUBMITTED = "Request to Cancel"
    const val REJECTED_BEFORE_RD_SUBMITTED = "Request to Resubmit"
    const val REJECTED_BEFORE_RD_SUBMITTED_MATCHER = "Request Credit Note Resubmission"

    class Buyer : CreditNoteStatusTranslator {
        override fun values(): Array<CreditNoteStatusMapping> = BuyerMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = BuyerMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = BuyerMapping.values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    class Seller : CreditNoteStatusTranslator {
        override fun values(): Array<CreditNoteStatusMapping> = SellerMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = SellerMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = SellerMapping.values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    class Matcher : CreditNoteStatusTranslator {
        override fun values(): Array<CreditNoteStatusMapping> = MatcherMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = MatcherMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = MatcherMapping.values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    //Mapping class for Buyer
    enum class BuyerMapping(override val displayName: String) : CreditNoteStatusMapping {
        ISSUED("Submitted"),
        PARTIAL("Verifying"),
        MISSING("Verifying"),
        UNMATCHED("Verifying"),
        REJECTED("Request to Resubmit, $REJECTED_AFTER_RD_SUBMITTED"),
        MATCHED("Waiting for Payment"),
        CANCELLED("Cancelled"),
        DECLINED("Payment Failed"),
        PAID("Paid");


        override val key = this.name
        override fun fromDisplayName(displayName: String) = values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    //Mapping class for Seller
    enum class SellerMapping(override val displayName: String) : CreditNoteStatusMapping {
        ISSUED("Submitted"),
        PARTIAL("Verifying"),
        MISSING("Verifying"),
        UNMATCHED("Verifying"),
        REJECTED("Request to Resubmit, $REJECTED_AFTER_RD_SUBMITTED"),
        MATCHED("Waiting for Payment"),
        CANCELLED("Cancelled"),
        DECLINED("Payment Failed"),
        PAID("Paid");


        override val key = this.name
        override fun fromDisplayName(displayName: String) = values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }

    }

    //Mapping class for Matcher
    enum class MatcherMapping(override val displayName: String) : CreditNoteStatusMapping {
        ISSUED("Submitted"),
        PARTIAL("Partial Return GR"),
        MISSING("Missing Return GR"),
        UNMATCHED("Pending Manual Approval"),
        REJECTED("Request Credit Note Resubmission, $REJECTED_AFTER_RD_SUBMITTED"),
        MATCHED("Approved"),
        CANCELLED("Cancelled"),
        DECLINED("Payment Failed"),
        PAID("Paid");


        override val key = this.name
        override fun fromDisplayName(displayName: String) = values().filter { it.displayName == displayName }
    }

}

/**
 * Interface that every Enum class for Invoice status must refer to
 */
@CordaSerializable
interface CreditNoteStatusMapping {
    val key : String
    val displayName: String
    fun fromDisplayName(displayName: String): List<CreditNoteStatusMapping>

}

/**
 * Interface that every class in CreditNoteStatus must refer to
 */
interface CreditNoteStatusTranslator {
    fun values(): Array<CreditNoteStatusMapping>
    fun valueOf(key: String): CreditNoteStatusMapping
    fun fromDisplayName(displayName: String): List<CreditNoteStatusMapping>
}