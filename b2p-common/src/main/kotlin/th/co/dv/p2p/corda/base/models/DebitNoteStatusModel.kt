package th.co.dv.p2p.corda.base.models

import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.constants.comma
import th.co.dv.p2p.common.utilities.splitAndTrim

enum class DebitNoteStatusType {
    BUYER,
    SELLER,
    MATCHER
}

/**
 * When you want to mapping the lifecycle to display status or the different way you need to use this object
 * This support mapping two type of status
 * 1. Seller invoice status, this one for mapping the lifecycle with the display status that seller will see
 * 2. Buyer invoice status , this one for mapping the lifecycle with the display status that seller will see
 *
 * So you need to refer the right class i.e if you want to map lifecycle for buyer you need to use status.Buyer()
 * if every class we provide 3 method for get the statusMapping that keep display status and lifecycle status (Enum)
 * @method values : Get all the the statusMapping that keep in refer class
 * @method valueOf : Get the the statusMapping depend on key that you send in
 * @method fromDisplayName : Get the the statusMapping depend on display status that tou send in
 *
 */
object DebitNoteStatus {

    const val PENDING_SELLER_AFTER_RD_SUBMITTED = "Request to Cancel"
    const val PENDING_SELLER_BEFORE_RD_SUBMITTED = "Request to Resubmit"

    class Seller : DebitNoteStatusTranslator {
        override fun values(): Array<DebitNoteStatusMapping> = SellerMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = SellerMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = SellerMapping.values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    class Buyer : DebitNoteStatusTranslator {
        override fun values(): Array<DebitNoteStatusMapping> = BuyerMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = BuyerMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = BuyerMapping.values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    //Mapping class for Buyer
    enum class BuyerMapping(override val displayName: String) : DebitNoteStatusMapping {
        ISSUED("Submitted"),
        PENDING_SELLER("Request to Resubmit, $PENDING_SELLER_AFTER_RD_SUBMITTED"),
        PENDING_BUYER("Pending Clarification"),
        MATCHED("Requesting DoA List"),
        PENDING_AUTHORITY("Pending DoA Approval"),
        PARTIALLY_APPROVED("Pending DoA Approval"),
        APPROVED("Waiting for Payment"),
        RESERVED("Waiting for Payment"),
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
    enum class SellerMapping(override val displayName: String) : DebitNoteStatusMapping {
        ISSUED("Submitted"),
        PENDING_SELLER("Request to Resubmit, $PENDING_SELLER_AFTER_RD_SUBMITTED"),
        PENDING_BUYER("Verifying"),
        MATCHED("Verifying"),
        PENDING_AUTHORITY("Verifying"),
        PARTIALLY_APPROVED("Verifying"),
        APPROVED("Waiting for Payment"),
        RESERVED("Waiting for Payment"),
        CANCELLED("Cancelled"),
        DECLINED("Waiting for Payment"),
        PAID("Paid");

        override val key = this.name
        override fun fromDisplayName(displayName: String) = values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }

    }

}

/**
 * Interface that every Enum class for Debit Note status must refer to
 */
@CordaSerializable
interface DebitNoteStatusMapping {
    val key: String
    val displayName: String
    fun fromDisplayName(displayName: String): List<DebitNoteStatusMapping>
}

/**
 * Interface that every class in DebitNoteStatus must refer to
 */
interface DebitNoteStatusTranslator {
    fun values(): Array<DebitNoteStatusMapping>
    fun valueOf(key: String): DebitNoteStatusMapping
    fun fromDisplayName(displayName: String): List<DebitNoteStatusMapping>
}