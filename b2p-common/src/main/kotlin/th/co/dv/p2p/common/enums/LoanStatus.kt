package th.co.dv.p2p.common.enums

/**
 * Possible status of loan
 */
enum class LoanStatus(val displayName: String) {
    ISSUED("ISSUED"),
    PENDING("PENDING"),
    REJECTED("REJECTED"),
    FINANCED("FINANCED"),
    PARTIALLY_REPAID("PARTIALLY REPAID"),
    FULLY_REPAID("FULLY REPAID");

    companion object {
        fun findByDisplayName(displayName: String): String {
            return values().find { it.displayName == displayName }!!.name
        }
    }
}
