package th.co.dv.p2p.common.enums

object PaymentStatuses {

    enum class BankPaymentStatus {
        // Payment is Submitted
        ISSUED,
        // S1 file generated
        GENERATED,
        // Not received any acknowledgement from Bank
        IGNORED,
        // Received acknowledgement from Bank
        ACKNOWLEDGED,
        // update invoice financing date to invoice and payment item
        FINANCED,
        // Result of payment is failure
        DECLINED,
        // Result of payment is success
        PAID,
        // Bank reject payment
        REJECTED;
    }

    enum class PayeePaymentStatus {

        // Payment is pending
        PENDING

    }

    enum class PayerPaymentStatus {

        // Pending for action
        PENDING,

        // Payment Posting success
        POSTING_SUCCESS,

        // Payment Posting failed
        POSTING_FAILED,

        // Clearing Payment success
        CLEARING_SUCCESS,

        // Clearing Payment failed
        CLEARING_FAILED;
    }
}