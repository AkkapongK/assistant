package th.co.dv.p2p.common.constants

const val commandNotSupport = "Command not supported."
const val cannotCastToLong = "The value cannot be cast to long."
const val invalidRole = "Role not allow for using invoice api."
const val cannotGetPartiesFromStreamingModel = "Cannot get parties from streaming model."
const val cannotConvertParty = "Cannot convert party to routing key."
const val serviceNotSupport = "Service not supported."
const val adjustmentTypeNotSupport = "Credit note adjustment type not supported."
const val cannotFindPurchaseItemThatTiedWithCn = "Cannot find purchase item that tied with credit note item."
const val noAuthorizationOnTenants = "No authorization on tenants."
const val notFoundDataTenantFieldsForApplyToSaveTransaction = "Not found data tenant fields for apply to save transaction."
const val notFondDataTenantFieldsForApplyToGetTransaction = "Not found data tenant fields for apply to get transaction."
const val stateShouldHaveOnlyOneDataTenantAnnotation = "%s state should have one data tenant annotation [Tenant , NoTenant]."
const val failedRequirement = "Failed requirement: %s"
const val entityMustNotBeNull = "Entity must not be null!"
const val unsupportedTenantState = "Unsupported tenant state."
const val caseFormatsInvalidLength = "Invalid caseFormats, must equal or more than 2"

const val keyIsNull = "Key is null."
const val duplicateRecord = "Cannot create because duplicate record."
const val cannotUpdateRecord = "Cannot update because the record have been locked by another transaction."
const val currencyNotSupport = "Currency not support"
const val neccessaryFieldMissing = "Some necessary fields are missing. Please check your data and try again later."

const val DOCUMENT_NOT_SUPPORT_RD_SUBMIT_DATE = "Document not support rd submit date"

// TAX DOCUMENT
const val rejectedTaxDocumentOnInvoicesNotifyError = "Rejected Tax-document on invoices notify error."
const val rejectedTaxDocumentOnDebitNotesNotifyError = "Rejected Tax-document on debit notes notify error."
const val rejectedTaxDocumentOnCreditNotesNotifyError = "Rejected Tax-document on credit notes notify error."

// ADVANCE
const val advancePaymentToBeDeductedNotEnough = "Deduct advance payment amount must be less than or equal advancePaymentToBeDeducted in purchase item (deduct %s, advancePaymentToBeDeducted %s)."


// Base Event message
const val CANNOT_FIND_RECORD = "Cannot find related record."
const val SPONSOR_CANNOT_BE_NULL = "Sponsor cannot be null."
const val DOCUMENT_NOT_SUPPORT = "Document type is not support."
const val REQUIRED_TRANSACTION_ID_IN_ARG = "Transaction id required in argument."


const val CANNOT_INITIAL_PRODUCER = "Cannot initial producer."
const val MESSAGE_TYPE_NOT_SUPPORT = "The message type is not support."
const val cannotGenerateSql = "Can not generate sql statement."
const val cannotCreateJoinClause = "Cannot create join clause for native sql."