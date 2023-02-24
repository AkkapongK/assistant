package th.co.dv.p2p.corda.base.models

import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.constants.comma
import th.co.dv.p2p.common.utilities.splitAndTrim

enum class StatusType {
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
object InvoiceStatus {

    const val PENDING_SELLER_AFTER_RD_SUBMITTED = "Request to Cancel"
    const val PENDING_SELLER_BEFORE_RD_SUBMITTED = "Request to Resubmit"
    const val PENDING_SELLER_BEFORE_RD_SUBMITTED_MATCHER = "Request Invoice Resubmission"

    class Seller : InvoiceStatusTranslator {
        override fun values(): Array<InvoiceStatusMapping> = SellerMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = SellerMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = SellerMapping.values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    class Buyer : InvoiceStatusTranslator {
        override fun values(): Array<InvoiceStatusMapping> = BuyerMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = BuyerMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = BuyerMapping.values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    class Matcher : InvoiceStatusTranslator {
        override fun values(): Array<InvoiceStatusMapping> = MatcherMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = MatcherMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = MatcherMapping.values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    //Mapping class for Buyer
    enum class BuyerMapping(override val displayName: String) : InvoiceStatusMapping {
        ISSUED("Submitted"),
        PARTIAL("Verifying"),
        MISSING("Verifying"),
        MATCHED("Verifying"),
        UNMATCHED("Verifying"),
        BASELINED("Verifying"),
        PENDING_SELLER("Request to Resubmit, $PENDING_SELLER_AFTER_RD_SUBMITTED"),
        PENDING_AUTHORITY("Verifying"),
        PENDING_BUYER("Verifying"),
        PARTIALLY_APPROVED("Verifying"),
        PENDING_FINAL_REVIEW("Verifying"),
        APPROVED("Waiting Payment Due Date"),
        RESERVED("Waiting Payment Due Date"),
        FINANCED("Waiting Payment Due Date"),
        PAID("Paid"),
        DECLINED("Payment Failed"),
        DECLINED_WITH_FINANCED("Payment Failed"),
        PAID_WITHOUT_FINANCED("Paid"),
        CANCELLED("Cancelled");


        override val key = this.name
        override fun fromDisplayName(displayName: String) = values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }
    }

    //Mapping class for Seller
    enum class SellerMapping(override val displayName: String) : InvoiceStatusMapping {
        ISSUED("Submitted"),
        PARTIAL("Verifying"),
        MISSING("Verifying"),
        MATCHED("Verifying"),
        UNMATCHED("Verifying"),
        BASELINED("Verifying"),
        PENDING_SELLER("Request to Resubmit, $PENDING_SELLER_AFTER_RD_SUBMITTED"),
        PENDING_AUTHORITY("Verifying"),
        PENDING_BUYER("Verifying"),
        PARTIALLY_APPROVED("Verifying"),
        PENDING_FINAL_REVIEW("Verifying"),
        APPROVED("Waiting Payment Due Date"),
        RESERVED("Waiting Payment Due Date"),
        FINANCED("Financed"),
        PAID("Paid"),
        DECLINED("Payment Failed"),
        DECLINED_WITH_FINANCED("Payment Failed"),
        PAID_WITHOUT_FINANCED("Waiting Payment Due Date"),
        CANCELLED("Cancelled");


        override val key = this.name
        override fun fromDisplayName(displayName: String) = values().filter {
            val displayNames = it.displayName.splitAndTrim(comma, trim = true)
            displayNames.contains(displayName)
        }

    }

    //Mapping class for Matcher
    enum class MatcherMapping(override val displayName: String) : InvoiceStatusMapping {
        ISSUED("Submitted"),
        PARTIAL("Partial GR"),
        MISSING("Missing GR"),
        MATCHED("Requesting DoA List"),
        UNMATCHED("Pending Manual Approval"),
        BASELINED("Requesting DoA List"),
        PENDING_SELLER("Request Invoice Resubmission, $PENDING_SELLER_AFTER_RD_SUBMITTED"),
        PENDING_AUTHORITY("Pending DoA Approval"),
        PENDING_BUYER("Pending Clarification"),
        PARTIALLY_APPROVED("Pending DoA Approval"),
        PENDING_FINAL_REVIEW("Pending Accounting Review"),
        APPROVED("Waiting Payment Due Date"),
        RESERVED("Waiting Payment Due Date"),
        FINANCED("Waiting Payment Due Date"),
        PAID("Paid"),
        DECLINED("Payment Failed"),
        DECLINED_WITH_FINANCED("Payment Failed"),
        PAID_WITHOUT_FINANCED("Paid"),
        CANCELLED("Cancelled");

        override val key = this.name
        override fun fromDisplayName(displayName: String) = values().filter { it.displayName == displayName }
    }

}

/**
 * Interface that every Enum class for Invoice status must refer to
 */
@CordaSerializable
interface InvoiceStatusMapping {
    val key : String
    val displayName: String
    fun fromDisplayName(displayName: String): List<InvoiceStatusMapping>

}

/**
 * Interface that every class in InvoiceStatus must refer to
 */
interface InvoiceStatusTranslator {
    fun values(): Array<InvoiceStatusMapping>
    fun valueOf(key: String): InvoiceStatusMapping
    fun fromDisplayName(displayName: String): List<InvoiceStatusMapping>
}