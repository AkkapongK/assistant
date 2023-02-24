package th.co.dv.p2p.corda.common.enum

class CordaLifecycle {

    enum class PaymentLifecycle {
        // Payment is Submitted
        ISSUED,

        APPROVED,
        // S1 file generated
        GENERATED,
        // Not received any acknowledgement from Bank
        IGNORED,
        // Received acknowledgement from Bank
        ACKNOWLEDGED,
        // Result of payment is failure
        DECLINED,
        // Result of payment is success
        PAID;
    }
}