package th.co.dv.p2p.common.models

/**
 * Data class contain header and item state belong in one transaction
 */
data class TransactionModel<P, C>(
        val headerState: P? = null,
        val itemStates: List<C>? = null
)