package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

/**
 * Returned when get invoice/credit note and debit note configuration from Configuration-Service
 * A ValidatedConfigurationModel contains:
 *  [allowedLifeCycleToEditInvoiceFinancing]: The Invoice lifecycle which is allowed to edit invoice financing.
 *  [attachmentConfiguration]: The sets of allowed file format configured for each type of documents, i.e Invoice.Receipt : JPG,PDF,TIF
 *  [autoPopulateInvoiceItemQuantity]:
 *  [defaultVendorBranch]: Default vendor branch.
 *  [invoiceFinancingIsAllowed]: Check if provided vendor is allowed invoice financing
 *  [minimumDocumentEffectiveDate]: The minimum back date that we allow to add in invoice/credit note date
 *  [minimumDocumentExpiryInDays]: The minimum threshold of document expiry in days i.e 3.0 days
 *  [maximumDocumentEffectiveDate]: The maximum back date that we allow to add in invoice/credit note date
 *  [maximumDocumentExpiryInDays]: The maximum threshold of document expiry in days i.e 3.0 days
 *  [onlyAllowInvoiceCreationBySingleGR]: Allow 1 [`GoodsReceived`] can tied with only 1 [`Invoice`].
 *  [onlyAllowInvoiceTieWithSingleGR]: Allow 1 [`Invoice`] can tied with only 1 [`GoodsReceived`].
 *  [lifecycle]: Invoice life cycle that allow to create [`CreditNote`].
 *  [thresholdConfiguration]: Valid threshold to create [`Invoice`] [`CreditNote`] and [`DebitNote`].
 *  [thresholdConfigurationForMatching]: Valid threshold that using for 3 way matching of [`Invoice`].
 *  [allowEditQtyInvoiceItem]: Allow edit quantity in [`InvoiceItem`].
 *  [allowEditAdvDeductItemAmount]: Allow edit advance deduct item amount.
 *  [interestRate]: Interest rate for [`Invoice`] that allow invoice-financing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValidatedConfigurationModel(
        val allowedLifeCycleToEditInvoiceFinancing: List<String>? = null,
        val attachmentConfiguration: List<AttachmentConfigurationModel>? = null,
        val autoPopulateInvoiceItemQuantity: Boolean? = null,
        val defaultVendorBranch: String? = null,
        val invoiceFinancingIsAllowed: Boolean? = null,
        val maximumDocumentEffectiveDate: String? = null,
        val maximumDocumentExpiryInDays: BigDecimal? = null,
        val minimumDocumentEffectiveDate: String? = null,
        val minimumDocumentExpiryInDays: BigDecimal? = null,
        val onlyAllowInvoiceCreationBySingleGR: Boolean? = null,
        val onlyAllowInvoiceTieWithSingleGR: Boolean? = null,
        val lifecycle: List<String>? = null,
        val thresholdConfiguration: Map<String, BigDecimal>? = null,
        val thresholdConfigurationForMatching: Map<String, BigDecimal>? = null,
        val allowEditQtyInvoiceItem: Boolean? = null,
        val allowEditAdvDeductItemAmount: Boolean? = null,
        val interestRate: String? = null,
        val customInvoiceValidation: List<CustomValidateFieldModel>? = null,
        val allowEditBuyerBranch: String? = null,
        val allowSumInvItemDiffPOItemSubtotal: Boolean? = null,
        val lifecycleAfterDoaReject: String? = null,
        val accountingReviewCheckList: Boolean? = null,
        val defaultAdvanceDeductionAmountValue: String? = null,
        val notReferVatAndWht: String? = null,
        val alwaysRecalculateVatAmount: Boolean? = null,
        val allowSupplierEditWht: Boolean? = null,
        val allowDisplayDueDateInInvoiceCreation: Boolean? = null
)
