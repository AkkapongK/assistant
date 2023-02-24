package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

/**
 * The model use for mapping the payment item data to json object and return in the response (API)
 * Or use in argument in the api
 *
 * Note: buyerBankCode, buyerBankAccountNumber, financedDate are the fields were moved from payment header
 * (the payment header had been remove from release 6)
 *
 * In release 6 we add WithholdingTax in paymentItem
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentItemModel(
        val linearId: String? = null,
        val externalId: String? = null,
        val customerReference: String? = null,
        val paymentLinearId: String? = null,
        val companyCode: String? = null,
        val companyName: String? = null,
        val companyTaxNumber: String? = null,
        val buyerBankCode: String? = null,
        val buyerBankAccountNumber: String? = null,
        val financedDate: String? = null,
        val vendorNumber: String? = null,
        val vendorName: String? = null,
        val vendorTaxNumber: String? = null,
        val sellerBankCode: String? = null,
        val sellerBankBranchCode: String? = null,
        val sellerBankAccountNumber: String? = null,
        val sellerBankName: String? = null,
        val paymentSubTotal: BigDecimal? = null,
        val paymentVatTotal: BigDecimal? = null,
        val paymentAmount: BigDecimal? = null,
        val withholdingTax: WithholdingTaxModel? = null,
        val payerPaymentStatus: String? = null,
        val payerPaymentMessage: String? = null,
        val payeePaymentStatus: String? = null,
        val payeePaymentMessage: String? = null,
        val paymentItemDate: String? = null,
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val bank: PartyModel? = null,
        val paymentSystem: String? = null,
        val issuedDate: String? = null,
        val lastGeneratedDate: String? = null,
        val lastPostedDate: String? = null,
        val paymentIsSettled: Boolean? = null,
        val settledMessage: String? = null,
        val settledDate: String? = null,
        val settlementProcessedDate: String? = null,
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val lastUpdatedCommand: String? = null,
        val lastUpdatedCommandSimpleName: String? = null,
        val commandUpdatedDate: String? = null,
        val commandUpdatedBy: String? = null,
        val lifecycle: String? = null,
        val invoiceModel: List<InvoiceModel> = emptyList(),
        val creditNoteModel: List<CreditNoteModel> = emptyList(),
        val debitNoteModel: List<DebitNoteModel> = emptyList(),
        val siblingCreditNotes: List<String>? = null,
        val postingStatus: String? = null,
        val clearingStatus: String? = null,
        val paymentFee: BigDecimal? = null,
        val currency: String? = null,
        val feeChargeTo: String? = null,
        val acknowledgementIsSuccessful: Boolean? = null,
        val fileReference: String? = null,
        val filler: String? = null,
        val beneficiaryNotification: String? = null,
        val vendorEmail: String? = null,
        val vendorTelephone: String? = null,
        val billerCode: String? = null,
        val paymentCutOffDate: String? = null,
        val currentAuthority: DelegationOfAuthorityModel? = null,
        val nextAuthority: DelegationOfAuthorityModel? = null,
        val previousAuthority: DelegationOfAuthorityModel? = null,
        val delegatedAuthorities: List<DelegationOfAuthorityModel>? = null,
        val approvedDate: String? = null,
        val fileAttachments: List<FileAttachmentModel>? = listOf(),
        val remark: String? = null,
        val vendorSiteId: String? = null
)