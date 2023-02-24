package th.co.dv.p2p.common.enums

/**
 * Enum class for key type of document
 */
enum class DocumentTypes {
    PURCHASE,
    GOODS_RECEIPT,
    INVOICE,
    CREDIT_NOTE,
    DEBIT_NOTE,
    PAYMENT,
    REQUEST,
    TAX_DOCUMENT,
    TAX_DOCUMENT_DEBIT_NOTE,
    TAX_DOCUMENT_CREDIT_NOTE,
    SYSTEM
}

enum class ConfigurationCategories(val value: String) {
    CREATE("Create"),
    TWM("3WM"),
    BWM("2WM"),
    BU_APPROVAL("BU Approval"),
    DOA_APPROVAL("DOA Approval"),
    FINANCING("Financing"),
    DUE_DATE("Due Date"),
    PAYMENT("Payment")
}
