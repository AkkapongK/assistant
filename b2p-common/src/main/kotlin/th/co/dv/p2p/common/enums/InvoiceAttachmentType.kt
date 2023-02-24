package th.co.dv.p2p.common.enums

/**
 * Type of Invoice [AttachmentType] for FileAttachment
 * @property TAX_INVOICE
 * @property RECEIPT
 * @property OTHERS
 */
enum class InvoiceAttachmentType(val value: String) {
    TAX_INVOICE("TaxInvoice"),
    RECEIPT("Receipt"),
    OTHERS("Others"),
    EXTERNAL("External"),
    INVOICE("Invoice"),
    TAX_INVOICE_OR_RECEIPT("TaxInvoiceOrReceipt")
}