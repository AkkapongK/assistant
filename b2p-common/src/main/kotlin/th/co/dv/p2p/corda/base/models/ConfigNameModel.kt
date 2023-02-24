package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * This model is used to generalize and contain the config names of the the thresholds stored in the offchain
 *
 * i.e documentExpiryMinimumInDays can be INVOICE_EXPIRY_MIN_DAYS or CREDIT_NOTE_EXPIRY_MIN_DAYS depending on the caller
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ConfigNameModel (
        val autoPopulateItemQuantity: String? = null,
        val backdateInDay: String? = null,
        val backdateInMonth: String? = null,
        val defaultVendorBranch: String? = null,
        val documentExpiryMaximumInDays: String? = null,
        val documentExpiryMinimumInDays: String? = null,
        val documentType: String? = null,
        val invoiceFinancingIsAllowed: String? = null,
        val lifecycleEditInvoiceFinancing: String? = null,
        val onlyAllowInvoiceCreationBySingleGR: String? = null,
        val onlyAllowInvoiceTieWithSingleGR: String? = null,
        val invoiceLifeCycleForCreateCreditNote : String? = null,
        val thresholdConfig: String? = null,
        val allowEditQtyInvoiceItem: String? = null,
        val allowEditAdvDeductItemAmount: String? = null,
        val interestRate: String? = null,
        val enableCustomInvoiceValidation: String? = null,
        val changeBuyerBranch: String? = null,
        val allowSumInvItemDiffPOItemSubtotal: String? = null,
        val lifecycleAfterDoaReject: String? = null,
        val accountingReviewCheckList: String? = null,
        val invoiceReviewCustomRequiredField: String? = null,
        val defaultAdvanceDeductionAmountValue: String? = null,
        val notReferVatAndWht: String? = null,
        val alwaysRecalculateVatAmount: String? = null,
        val allowSupplierEditWht: String? = null,
        val allowDisplayDueDateInInvoiceCreation: String? = null
)
