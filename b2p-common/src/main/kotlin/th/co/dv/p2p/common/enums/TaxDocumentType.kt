package th.co.dv.p2p.common.enums

/**
 * Type of TaxDocument [TaxDocumentType]
 * @property TAX_INVOICE
 * @property TAX_CREDIT_NOTE
 * @property TAX_DEBIT_NOTE
 */
enum class TaxDocumentType(val value: String) {
    TAX_INVOICE("TaxInvoice"),
    TAX_CREDIT_NOTE("TaxCreditNote"),
    TAX_DEBIT_NOTE("TaxDebitNote")
}