package th.co.dv.p2p.corda.base.models

import net.corda.core.serialization.CordaSerializable

/**
 * When you want to mapping the lifecycle to display status or the different way you need to use this object
 * This support mapping three type of status
 *
 * @method values : Get all the the statusMapping that keep in refer class
 * @method valueOf : Get the the statusMapping depend on key that you send in
 * @method fromDisplayName : Get the the statusMapping depend on display status that tou send in
 *
 */
object PurchaseStatus {

    class PurchaseOrder : PurchaseStatusTranslator {
        override fun values(): Array<PurchaseStatusMapping> = PurchaseMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = PurchaseMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = PurchaseMapping.values().filter { it.displayName == displayName }
    }

    class PurchaseOrderItem : PurchaseStatusTranslator {
        override fun values(): Array<PurchaseStatusMapping> = PurchaseItemMapping.values().toList().toTypedArray()
        override fun valueOf(key: String) = PurchaseItemMapping.valueOf(key)
        override fun fromDisplayName(displayName: String) = PurchaseItemMapping.values().filter { it.displayName == displayName }
    }

    enum class PurchaseMapping(override val displayName: String) : PurchaseStatusMapping {
        APPROVED("Pending Ack"),
        REJECTED("Rejected"),
        CONFIRMED("Confirmed");

        override val key = this.name
        override fun fromDisplayName(displayName: String) = values().filter { it.displayName == displayName }
    }

    enum class PurchaseItemMapping(override val displayName: String) : PurchaseStatusMapping {
        APPROVED("Pending Ack"),
        REJECTED("Rejected"),
        PENDING_SELLER("Pending Vendor"),
        PENDING_BUYER("Pending Buyer"),
        CONFIRMED("Confirmed"),
        PENDING_FINAL_INVOICE("Pending Final Invoice"),
        DELIVERY_COMPLETED("Delivery Completed");

        override val key = this.name
        override fun fromDisplayName(displayName: String) = values().filter { it.displayName == displayName }
    }
}

/**
 * Interface that every Enum class for Purchase Order status must refer to
 */
@CordaSerializable
interface PurchaseStatusMapping {
    val key : String
    val displayName: String
    fun fromDisplayName(displayName: String): List<PurchaseStatusMapping>
}

/**
 * Interface that every class in Purchase Order must refer to
 */
interface PurchaseStatusTranslator {
    fun values(): Array<PurchaseStatusMapping>
    fun valueOf(key: String): PurchaseStatusMapping
    fun fromDisplayName(displayName: String): List<PurchaseStatusMapping>
}





