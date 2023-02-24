package th.co.dv.p2p.common.enums

/**
 * The [AttachmentType] is the type of attachment for keep the configuration that we store in the configuration service server
 * Now we support 3 types
 *
 * @Property INVOICE_ATTACHMENT_TYPE: The attachment type for invoice attachment
 * @Property CREDIT_NOTE_ATTACHMENT_TYPE: The attachment type for credit note attachment
 * @Property PURCHASE_ORDER_ATTACHMENT_TYPE: The attachment type for purchase order attachment
 */
enum class AttachmentType(val value: List<String>) {
    INVOICE_ATTACHMENT_TYPE(listOf(
            "TaxInvoice",
            "Receipt",
            "DeliveryNote",
            "Others",
            "BuyerApprove",
            "BuyerReject",
            "BuyerClarify",
            "DOAApprove",
            "DOAReject",
            "BuyerRejectAfterDOA",
            "StampDuty"
    )),
    DEBIT_NOTE_ATTACHMENT_TYPE(listOf(
            "BuyerApprove",
            "BuyerClarify",
            "BuyerReject",
            "DebitNote",
            "DOAApprove",
            "DOAReject",
            "Others",
            "Receipt",
            "TaxDebitNote",
            "TaxDebitNoteOrReceipt",
            "StampDuty"
    )),
    CREDIT_NOTE_ATTACHMENT_TYPE(listOf(
            "CreditNote",
            "Others"
    )),
    PURCHASE_ORDER_ATTACHMENT_TYPE(listOf(
            "POForm"
    ))
}