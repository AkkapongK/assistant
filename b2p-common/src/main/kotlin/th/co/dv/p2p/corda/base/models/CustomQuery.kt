package th.co.dv.p2p.corda.base.models

/**
 * @property displayName the name where it is used by the API layer.
 */
enum class SelectDataGroupByKeys(val displayName: String) {
    INVOICE_STATUS("invoiceStatus"),
    MATCHING_STATUS("matchingStatus"),
    DAYS_WITHOUT_GOODS("daysWithoutGoods"),
    DAYS_WITHOUT_INVOICES("daysWithoutInvoice"),
    PENDING_TAX_DOC("pendingTaxDocument");

    companion object {
        // Translate displayName back to its corresponding enum, ignoring case sensitivity
        fun fromDisplayName(displayName: String, ignoreCase: Boolean = true) = values()
                .filter { it.displayName.equals(displayName, ignoreCase)  }
    }
}

enum class OPERATORS {
    AND,
    OR
}

enum class DashboardType {
    GLOBAL,
    USER
}