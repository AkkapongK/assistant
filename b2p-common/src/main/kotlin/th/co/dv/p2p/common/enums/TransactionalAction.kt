package th.co.dv.p2p.common.enums


/**
 * Type of transaction action
 *
 * BUYER : buyer process transaction
 * SELLER : seller process transaction
 */
enum class TransactionalAction {
    BUYER,
    SELLER
}

val TransactionalAction_BUYER = listOf(TransactionalAction.BUYER)
val TransactionalAction_SELLER = listOf(TransactionalAction.SELLER)
val TransactionalAction_BUYER_OR_SELLER = listOf(TransactionalAction.BUYER, TransactionalAction.SELLER)