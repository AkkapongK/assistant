package th.co.dv.p2p.common.enums

/**
 * Type of debit note [AttachmentType] for FileAttachment
 * @property EXTERNAL
 */
enum class DebitNoteAttachmentType(val value: String) {
    EXTERNAL("External"),
    RECEIPT("Receipt"),
    TAX_DEBIT_NOTE("TaxDebitNote"),
    TAX_DEBIT_NOTE_OR_RECEIPT("TaxDebitNoteOrReceipt"),
    OTHERS("Others")
}
