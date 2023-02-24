package th.co.dv.p2p.common.enums

enum class UnmatchedCode(val reason: String) {
    DATE("Date Mismatch"),
    INVOICE_GR_DATE("Invoice-GR Date Mismatch"),
    UNKNOWN_INVOICE_GR_DATE("Unknown Invoice-GR Date"),
    PO_GR_DATE("PO-GR Date Mismatch"),
    QUANTITY("Quantity Mismatch"),
    UNIT("Unit Mismatch"),
    UNIT_PRICE("Unit Price Mismatch"),
    SUB_TOTAL("Sub Total Mismatch"),
    SUB_TOTAL_ITEM("Sub Total Item Mismatch"),
    VAT_TOTAL("VAT Total Mismatch"),
    TOTAL_AMOUNT("Total Amount Mismatch"),
    PURCHASE_QUANTITY("Purchase Quantity Mismatch"),
    INVOICE_GR_AMOUNT("Invoice-GR Amount Mismatch"),
    ADVANCE_AMOUNT("Advance Amount Mismatch"),
    ONBOARD_ISSUED("Supplier bank account doesn't exist"),
    INVALID_ACCOUNT("Supplier bank account should be number"),
    ADVANCE_DEDUCTION ("Advance Deduction"),

    // List of unmatched code in order to keep custom matching.
    SNP_CUSTOM_HEADER_01("Custom Validation Mismatch"),

    SCG_CUSTOM_HEADER_1("Custom Validation Mismatch"),
    SCG_CUSTOM_HEADER_2("Custom Validation Mismatch"),
    SCG_CUSTOM_HEADER_3("Custom Validation Mismatch"),
    GR_REVERSED("Goods receipt has been reversed");

    companion object {
        fun fromReason(reason: String) = values().filter { it.reason == reason }
    }
}
