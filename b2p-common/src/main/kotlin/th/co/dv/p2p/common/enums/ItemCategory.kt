package th.co.dv.p2p.common.enums

object ItemCategory {

    enum class Invoice {
        NORMAL,
        ADVANCE_REDEEM,
        ADVANCE_DEDUCT,
        PROVISION
    }

    enum class Purchase {
        NORMAL,
        ADVANCE,
        PROVISION
    }
}