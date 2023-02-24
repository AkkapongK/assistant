package th.co.dv.p2p.common.enums

object PostingStatuses {

    enum class InvoicePostingStatus {
        // Invoice is ready for Invoice Posting
        PENDING,
        // Invoice Posting status is failed
        FAILED,
        // Invoice Posting status is success
        SUCCESS,
        // Pending for posting cancel
        PENDING_CANCEL,
        // Cancel posting status is failed
        CANCEL_FAILED,
        // Cancel posting status is success
        CANCELLED;
    }

    enum class CreditNotePostingStatus {
        // Credit Note is ready for Posting
        PENDING,
        // Credit Note Posting status is failed
        FAILED,
        // Credit Note Posting status is success
        SUCCESS,
        // Pending for posting cancel
        PENDING_CANCEL,
        // Cancel posting status is failed
        CANCEL_FAILED,
        // Cancel posting status is success
        CANCELLED;
    }

    enum class DebitNotePostingStatus {
        // Document is ready for Posting
        PENDING,
        // Posting status is failed
        FAILED,
        // Posting status is success
        SUCCESS,
        // Pending for posting cancel
        PENDING_CANCEL,
        // Cancel posting status is failed
        CANCEL_FAILED,
        // Cancel posting status is success
        CANCELLED;
    }

    enum class TaxDocumentPostingStatus {
        // Document is ready for Posting
        PENDING,
        // Posting status is failed
        FAILED,
        // Posting status is success
        SUCCESS,
        // Pending for posting cancel
        PENDING_CANCEL,
        // Cancel posting status is failed
        CANCEL_FAILED,
        // Cancel posting status is success
        CANCELLED;
    }

    enum class RdPostingStatus {
        FAILED,
        SUCCESS
    }
}
