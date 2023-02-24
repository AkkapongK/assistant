package th.co.dv.p2p.common.enums

import th.co.dv.p2p.common.constants.UNDERSCORE
import th.co.dv.p2p.common.kafka.KafkaTopicConstant

/**
 * Enum class that contain all services name we have
 */
enum class Services(val code: String) {
    PURCHASE("PO"),
    GOODS_RECEIVED("GR"),
    INVOICE("INV"),
    CREDIT_NOTE("CN"),
    DEBIT_NOTE("DN"),
    REQUEST("REQ"),
    PAYMENT("PAY"),
    CORDA_INTERFACE("CINT"),
    MATCHING("3WM"),
    CUSTOM_INVOICE("CUSINV"),
    CONFIGURATION("CONF"),
    MASTERDATA("MAS"),
    USER("USER"),
    AGGREGATE("AGG"),
    NOTIFICATION("NOTIFICATION"),
    FILE("FILE"),
    ON_BOARD("ON_BOARD"),
    SUPPLIER_ON_BOARD("SOBS"),
    ADMIN_PORTAL("ADMIN_PORTAL"),
    DOA("DOA"),
    EMAIL("EMAIL"),
    FINANCING("FINANCING"),
    STANDARD_API("STANDARD_API"),
    E_TAX("E_TAX"),
    TAX_DOCUMENT("TAX_DOCUMENT"),
    EXPORT("EXPORT"),
    USER_NOTIFY("USER_NOTIFY"),
    SCB_BANK("SCB_BANK");

    val proposalTopic = this.name + UNDERSCORE + KafkaTopicConstant.PROPOSAL
    val notifyTopic = this.name + UNDERSCORE + KafkaTopicConstant.NOTIFY
    val commitTopic = this.name + UNDERSCORE + KafkaTopicConstant.COMMIT
    val broadcastTopic = this.name + UNDERSCORE + KafkaTopicConstant.BROADCAST

}