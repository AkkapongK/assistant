package th.co.dv.p2p.common.enums

object Lifecycle {

    enum class InvoiceLifecycle(val stage: Long) {
        // Default status state for a contract
        ISSUED(10L),
        // Invoice matched with at least 1 GR but pending more
        PARTIAL(20L),
        // Invoice did not matched with any GR
        MISSING(20L),
        // Fail in 3 way matching
        UNMATCHED(30L),
        // Pending approval from BU
        PENDING_BUYER(40L),
        // Pending invoice re submit from seller
        PENDING_SELLER(40L),
        // Successful in 3 way matching
        MATCHED(60L),
        // Baseline Date is calculated after success in 3 way matching
        BASELINED(70L),
        // Delegates Of Authority have been assigned
        PENDING_AUTHORITY(80L),
        // One or more Delegates of Authority have been approved
        PARTIALLY_APPROVED(90L),
        // All Delegates Of Authority have been approved
        APPROVED(100L),
        // Invoice is ready for payment
        RESERVED(105L),
        // Bank already paid to seller
        FINANCED(110L),
        // Seller already received the money, but buyer not paid yet
        DECLINED_WITH_FINANCED(130L),
        // Failed payment from interface **/
        DECLINED(140L),
        // Buyer already paid to bank, but seller not received the money yet.
        PAID_WITHOUT_FINANCED(150L),
        // Invoice already paid to seller
        PAID(300L),
        // Invoice already canceled by seller
        CANCELLED(500L),
        // Pending final review
        PENDING_FINAL_REVIEW(95L);
    }

    enum class InvoiceItemLifecycle {
        // Default status state for a contract
        ISSUED,
        /** Invoice matched with at least 1 GR but pending more */
        PARTIAL,
        /** Invoice did not matched with any GR */
        MISSING,
        /** Fail in 3 way matching */
        UNMATCHED,
        /** Successful in 3 way matching. End of lifecycle for invoice item */
        MATCHED,
        CANCELLED
    }

    enum class CreditNoteLifecycle(val stage: Long) {
        // Default status state for a contract
        ISSUED(10L),
        // GR found but not complete to CN quantity
        PARTIAL(20L),
        // CN did not matched with any GR
        MISSING(20L),
        // Fail in 2 way matching
        UNMATCHED(30L),
        // CN is rejected by BU
        REJECTED(40L),
        // CN auto auto matched or manually approved by BU
        MATCHED(50L),
        // CN require Supplier to Re-Submit
        PENDING(100L),
        // CN is Cancelled
        CANCELLED(110L),
        // CN where Payment has been cleared
        SETTLED(110L),
        // Failed payment from interface
        DECLINED(120L),
        // CN already paid to seller
        PAID(130L);
    }

    enum class CreditNoteItemLifecycle {
        /** Default status state for a contract */
        ISSUED,
        PARTIAL,
        // CN did not matched with any GR
        MISSING,
        // Fail in 2 way matching
        UNMATCHED,
        // CN auto auto matched or manually approved by BU
        MATCHED,
        /** CN require Supplier to Re-Submit */
        PENDING,
        // CN is cancelled and will not be used
        CANCELLED,
        /** CN where Payment has been cleared */
        SETTLED;
    }

    enum class DebitNoteLifecycle(val stage: Long) {
        // Default status state for a contract
        ISSUED(10L),
        // Pending debit note re-submit from seller
        PENDING_SELLER(15L),
        // Pending approval from BU
        PENDING_BUYER(20L),
        // DN is approved by BU And Watting for DOA list
        MATCHED(30L),
        // Delegates Of Authority have been assigned fot DN
        PENDING_AUTHORITY(40L),
        // One or more Delegates of Authority have been approved to DN
        PARTIALLY_APPROVED(50L),
        // All Delegates Of Authority have been approved to DN
        APPROVED(60L),
        // Approved payment item (case approve doa on payment level) or issue payment (case approve doa on debit note level)
        RESERVED(70L),
        // DN is Cancelled
        CANCELLED(80L),
        // Failed payment from interface
        DECLINED(90L),
        // DN already paid to seller
        PAID(100L)
    }

    enum class DebitNoteItemLifecycle {

        // Default status state for a contract
        ISSUED,
        // DN require BU to Approve
        PENDING_BUYER,
        // DN require Supplier to Re-Submit
        PENDING_SELLER,
        // DN is cancelled and will not be used
        CANCELLED,
        // DN where Payment has been cleared
        SETTLED;
    }

    enum class GoodsReceivedLifecycle(val stage: Long) {
        ISSUED(10L);
    }

    enum class GoodsReceivedItemLifecycle {
        ISSUED,
        MATCHED,
        UNMATCHED
    }

    enum class PurchaseItemLifecycle {
        APPROVED,
        REJECTED,
        PENDING_SELLER,
        PENDING_BUYER,
        CONFIRMED,
        PENDING_FINAL_INVOICE,
        DELIVERY_COMPLETED;
    }

    enum class PurchaseOrderLifecycle(val stage: Long) {
        /** Default status state for Purchase Order is APPROVED */
        APPROVED(10L),

        /** Purchase Order have been reject by Seller */
        REJECTED(20L),

        /** Purchase Order have been acknowledge by Seller */
        CONFIRMED(30L);

    }

    enum class RequestLifecycle {
        // Default status state for a contract
        ISSUED,
        // Request is in waiting RECEIVER to edit and/or confirm stage
        PENDING_RECEIVER,
        // Request is in waiting REQUESTER to edit and/or confirm stage
        PENDING_REQUESTER,
        // Request closed and ready to create document
        CLOSED,
        // Request is Cancelled
        CANCELLED;
    }

    enum class RequestItemLifecycle {
        // Default status state for a contract
        ISSUED,
        // Request item closed and ready to create document
        CLOSED,
        // Request item is Cancelled
        CANCELLED
    }

    enum class RDLifecycle {
        // Default status
        PENDING,
        // RD xml generated success
        XML_GENERATED,
        // RD xml generated failed
        XML_FAILED,
        // RD zip generated success
        ZIP_GENERATED,
        // RD zip generate failed
        ZIP_FAILED
    }

    enum class TaxDocumentLifecycle(val stage: Long) {
        ISSUED(10L),
        APPROVED(20L),
        REJECTED(30L)
    }

    enum class PaymentLifecycle {
        // Payment is issued
        ISSUED,
        // Payment is in process
        PROCESSING,
        // Payment is closed
        CLOSED

    }
}