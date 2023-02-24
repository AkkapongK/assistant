package th.co.dv.p2p.common.enums

import th.co.dv.p2p.common.constants.DOT

interface Command {
    fun getName() : String
    fun getCommandName(): String
}

enum class PurchaseOrderCommand: Command {
    Issue,
    Update,
    Patch,
    Confirm,
    Reject,
    SimplyIssue,
    SimplyUpdate,
    UpdateCustomisedField,
    UpdateQuantityFromInvoice,
    UpdateProvisionAmountFromInvoice,
    DeductAdvanceAmount,
    RestoreAdvanceAmount,
    TrilateralMatch,
    BuyerApprove,
    UpdateDeleteFlag,
    RestoreRemainingAmount;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class PurchaseItemCommand: Command {
    EditDeliveryDate,
    ReconfirmDeliveryDate,
    UpdateQuantity,
    SimplyIssueAndUpdateItem,
    SimplyIssue,
    SimplyUpdate,
    Deliver,
    DepleteRemainingAmount,
    RestoreAndDeplete;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class GoodsReceivedCommand: Command {
    Issue,
    Edit,
    TagOrUnTagInvoice,
    BuyerApprove,
    Delete;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class GoodsReceivedItemCommand: Command {
    Issue,
    SimplyIssueGoodsReceivedItem,
    Edit,
    TagInvoice,
    Resubmit,
    TrilateralMatch,
    Approve,
    ManualTag,
    BilateralMatch,
    CreateInvoice,
    CancelReturn,
    CancelInvoice,
    ReplaceInvoice,
    LinkedInvoice,
    UnlinkedInvoice,
    UpdateCustomisedField,
    TagOrUnTag,
    Delete,
    UpdateReverseQuantity;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class InvoiceCommand: Command {
    Issue,
    Convert,
    BuyerEditAndConfirm,
    ConvertFromRequest,
    Replace,
    Resubmit,
    Edit,
    IssueFromCancelledInvoice,
    UpdateCustomisedField,
    InvoicePosting,
    TrilateralMatch,
    BuyerApprove,
    BuyerReject,
    BuyerClarify,
    AuthorityRejectToSeller,
    SettlePayment,
    DeclinePayment,
    ChangeBaseline,
    IssuePaymentItems,
    IssueExternalPayment,
    TagCreditNote,
    BuyerEditDueDate,
    UpdateDueDate,
    UpdateCustomIndicator1,
    AssignAuthority,
    AuthorityPartialApprove,
    AuthorityFinalApprove,
    AuthorityAutoApprove,
    AuthorityAutoApproveByPayment,
    AuthorityReject,
    Cancel,
    EditAttachment,
    Hold,
    Unhold,
    ConsumeState,
    AddBank,
    EditFinancing,
    UpdateRetention,
    MigrateData,
    RejectFromPayment,
    UpdateFromPayment,
    UpdatePaymentResult,
    UpdateInvoiceFromRejectPayment,
    UpdateWithholdingTaxBaseAmount,
    SettleInvoiceFinancing,
    RequestAuthority,
    RemoveBank,
    EditWithholdingTax,
    SubmitToRD,
    UpdateRdSubmittedDate,
    RePostInvoiceDocument,
    RemoveFromRD,
    UpdateCustomisedFields,
    BuyerFinalReviewEdit,
    UpdateTaxDocument,
    RejectTaxDocument,
    ReviewInvoice,
    TrilateralMatchRequestGoods,
    ResetDOA,
    UpdateWaitingReceipt,
    UpdateEvaluationFlag,
    UpdateReferenceField;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class InvoiceItemCommand: Command {
    Issue,
    Edit,
    Resubmit,
    Replace,
    ConvertFromRequest,
    Convert,
    UpdateCustomisedField,
    TrilateralMatch,
    CustomTrilateralMatch,
    AdjustQuantity,
    AdjustPrice,
    EditWithholdingTax,
    CancelAdjustment,
    Cancel,
    ReverseQuantity,
    ReversePrice,
    ConsumeState,
    UpdateRetention,
    IssuePaymentItems,
    AdjustDebitPrice,
    CancelDebitPrice;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class DebitNoteCommand: Command {
    AuthorityFinalApprove,
    AuthorityPartialApprove,
    SimplyIssueDebitNote,
    UpdateDueDate,
    Issue,
    IssuePayment,
    IssueExternalPayment,
    RejectPayment,
    CancelDebitNote,
    BuyerApprovePrice,
    BuyerRejectPrice,
    BuyerEditDueDate,
    AuthorityReject,
    AssignAuthority,
    AuthorityAutoApprove,
    EditAttachment,
    Edit,
    DebitNotePosting,
    UpdatePaymentResult,
    ConvertFromRequest,
    SubmitToRD,
    UpdateRdSubmittedDate,
    RePostDebitNoteDocument,
    RemoveFromRD,
    BuyerEditReferenceFields,
    UpdateCustomisedFields,
    UpdateTaxDocument,
    RejectTaxDocument,
    Hold,
    Unhold;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class CreditNoteCommand: Command {
    SimplyIssueCreditNote,
    IssuePayment,
    IssueExternalPayment,
    CancelCreditNote,
    RejectPayment,
    UpdatePaymentResult,
    UpdatePaymentDate,
    BuyerApproveQuantity,
    BuyerRejectQuantity,
    BilateralMatch,
    BuyerApprovePrice,
    Resubmit,
    BuyerRejectPrice,
    EditAttachment,
    CreditNotePosting,
    Edit,
    ConvertFromRequest,
    SubmitToRD,
    UpdateRdSubmittedDate,
    RePostCreditNoteDocument,
    RemoveFromRD,
    BuyerEditReferenceFields,
    UpdateTaxDocument,
    RejectTaxDocument,
    Hold,
    Unhold,
    UpdateTaggedDirectCreditNote,
    EditMatchedCreditNote;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class CreditNoteItemCommand: Command {
    SimplyIssueCreditNoteItem;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class RequestCommand: Command {

    SimplyIssueRequest,
    Edit,
    SendToReceiver,
    SendToRequester,
    Cancel,
    AddOrUpdateRetention,
    Approve,
    Close;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class PaymentCommand: Command {
    PreIssue,
    Issue,
    IssueExternalPayment,
    PregeneratePayment,
    GeneratePayment,
    UpdateCreditNoteFromIssuePayment,
    UpdateDebitNoteFromIssuePayment,
    UpdateInvoiceFromIssuePayment,
    UpdateInvoiceFromPaymentResult,
    PaymentAcknowledgement,
    PaymentFinancing,
    PaymentResult,
    PaymentPosting,
    BankRejectPayment,
    PaymentClearing,
    PaymentReposting;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class InterfaceCommand: Command {
    Snapshot,
    SnapshotItem,
    SaveToCorda,
    ReSync,
    ReSyncItem,
    Sync,
    Share,
    Migrate,
    UpdateStatus,
    UpdateAttachment;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class TaxDocumentCommand: Command {
    Issue,
    Approve,
    Reject,
    UpdateVatTransfer,
    SubmitToRD,
    UpdateRdSubmittedDate,
    UpdateAttachmentPosted,
    RePostTaxDocument,
    RemoveFromRD;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class ContractCommand: Command {
    ContractIssue,
    ContractUpdate;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class FinancingCommand: Command {
    UpdateLoanProfile,
    IssueLoan,
    RepaymentRequestCreated,
    UpdateLoan,
    UpdateRepaymentRequest,
    IssueRepaymentHistory,
    IssueFinanceableDocument,
    UpdateFinanceableDocument;

    override fun getName(): String {
        return this::class.java.simpleName + DOT + name
    }

    override fun getCommandName(): String {
        return name
    }
}

enum class EtaxCommand {
    PostToRD
}

