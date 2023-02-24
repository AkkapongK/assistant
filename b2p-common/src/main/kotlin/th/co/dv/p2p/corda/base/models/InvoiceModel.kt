package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.beanutils.BeanUtils
import th.co.dv.p2p.common.constants.comma
import th.co.dv.p2p.common.models.DocumentValidationModel
import th.co.dv.p2p.common.utilities.CommonInvoiceUtils.calculateAdvanceDeduction
import th.co.dv.p2p.common.utilities.splitAndTrim
import th.co.dv.p2p.corda.base.models.InvoiceStatus.PENDING_SELLER_AFTER_RD_SUBMITTED
import java.math.BigDecimal

/**
 *
 * @property cancelledInvoiceNumber this field is in this model to indicate the invoice number to be replace in replace invoice case. But, it will not be kept in state.
 * @property cancelledVendorTaxNumber this field is in this model to indicate the vendor tax number of invoice to be replace in replace invoice case. But, it will not be kept in state.
 * @property cancelledInvoiceDate this field is in this model to indicate the invoice date of invoice to be replace in replace invoice case. But, it will not be kept in state.
 * @property cancelledFileAttachments this field is in this model to indicate the file attachment for cancellation of invoice to be replace in replace invoice case. But, it will not be kept in state.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InvoiceModel(
        val vendorNumber: String? = null,
        val vendorBranchCode: String? = null,
        val vendorBranchName: String? = null,
        val vendorName: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorAddress: String? = null,
        val vendorTelephone: String? = null,
        val companyCode: String? = null,
        val companyName: String? = null,
        val companyTaxNumber: String? = null,
        val companyBranchCode: String? = null,
        val companyBranchName: String? = null,
        val companyAddress: String? = null,
        val companyTelephone: String? = null,
        val businessPlace: String? = null,
        val externalId: String? = null,
        val invoiceDate: String? = null,
        val approvedDate: String? = null,
        val invoiceFinancing: Char? = null,
        val invoiceFinancedDate: String? = null,
        val paymentTermCode: String? = null,
        val paymentTermDesc: String? = null,
        val paymentTermDays: Long? = null,
        val dueDate: String? = null,
        val initialDueDate: String? = null,
        val dueDateLastEditedReason: String? = null,
        val dueDateLastEditedBy: String? = null,
        val dueDateLastEditedDate: String? = null,
        val dueDateIsLocked: Boolean? = false,
        val invoiceCreatedDate: String? = null,
        val listTotal: BigDecimal? = null,
        val totalDiscount: BigDecimal? = null,
        val totalSurcharge: BigDecimal? = null,
        val subTotal: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val withholdingTaxTotal: BigDecimal? = null,
        val invoiceTotal: BigDecimal? = null,
        val totalPayable: BigDecimal? = null,
        val estimatedPayable: BigDecimal? = null,
        val currency: String? = null,
        val matchedCode: Map<String, String> = emptyMap(),
        val unmatchedCode: List<String>? = null,
        val unmatchedReason: String? = null,
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val accounting: PartyModel? = null,
        val bank: PartyModel? = null,
        val resubmitCount: Int? = 0,
        val baselineDate: String? = null,
        val lastMatchUpdatedDate: String? = null,
        val purchaseOrder: String? = null,
        val purchaseOrderHeaderNumber: String? = null,
        val goodsReceived: String? = null,
        val receiptNumber: String? = null,
        val paymentFee: BigDecimal? = null,
        @get:JsonProperty("items")
        val invoiceItems: List<InvoiceItemModel> = emptyList(),
        val status: String? = null,
        val matchingStatus: String? = null,
        val lifecycle: String? = null,
        val linearId: String? = null,
        val buyerApprovedDate: String? = null,
        val buyerApprovedRemark: String? = null,
        val buyerApprovedUser: String? = null,
        val buyerRejectedDate: String? = null,
        val buyerRejectedRemark: String? = null,
        val buyerRejectedUser: String? = null,
        val buyerClarifiedDate: String? = null,
        val buyerClarifiedRemark: String? = null,
        val buyerClarifiedUser: String? = null,
        val newInvoiceLinearId: String? = null,
        val originalInvoiceLinearId: String? = null,
        val originalInvoiceExternalId: String? = null,
        val originalInvoiceDate: String? = null,
        val originalInvoiceCancellationReason: String? = null,
        val originalInvoiceCancellationDescription: String? = null,
        val cancelledUser: String? = null,
        val cancelledDate: String? = null,
        val cancelledRemark: String? = null,
        val cancelledCode: String? = null,
        val cancelledInvoiceNumber: String? = null,
        val cancelledVendorTaxNumber: String? = null,
        val cancelledInvoiceDate: String? = null,
        val cancelledFileAttachments: List<FileAttachmentModel>? = listOf(),
        val cashPerfGuaranteeAmount: BigDecimal? = null,
        val cashWarrantyAmount: BigDecimal? = null,
        val cashPerfGuaranteeFromGr: Boolean? = false,
        val cashWarrantyFromGr: Boolean? = false,
        val calculatedDate: String? = null,
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val currentAuthority: DelegationOfAuthorityModel? = null,
        val nextAuthority: DelegationOfAuthorityModel? = null,
        val previousAuthority: DelegationOfAuthorityModel? = null,
        val delegatedAuthorities: List<DelegationOfAuthorityModel>? = null,
        val taggedCreditNotes: List<TaggedDocumentModel>? = listOf(),
        val paymentDate: String? = null,
        val paymentItemLinearId: String? = null,
        val paymentIsSuccessful: Boolean? = null,
        val paymentStatusUpdateDate: String? = null,
        val fileAttachments: List<FileAttachmentModel>? = listOf(),
        val postingStatus: String? = null,
        val retentionFromGR: Boolean? = null,
        val paymentReferenceNumber: String? = null,
        val invoicePostingUpdatedDate: String? = null,
        val lastEditedBy: String? = null,
        val lastEditedDate: String? = null,
        @get:JsonProperty("isOnHold")
        val isOnHold: Boolean? = null,
        val lastHeldBy: String? = null,
        val lastHeldRemark: String? = null,
        val lastHeldDate: String? = null,
        val lastUnheldBy: String? = null,
        val lastUnheldRemark: String? = null,
        val lastUnheldDate: String? = null,
        val calendarKey: String? = null,
        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        // to handle serialising/deserialising `is` keyword, else `is` will be stripped away after writing to JSON
        @get:JsonProperty("isETaxInvoice")
        val isETaxInvoice: Boolean? = null,
        val withholdingTaxCalculationPoint: String? = null,
        val removeFromPayment: Boolean? = null,
        val retentionAmount: BigDecimal? = null,
        val paymentTermMonths: Int? = null,
        val interestCalculationModel: InterestCalculationModel? = null,
        val customIndicator1: Boolean? = null,
        val vatTriggerPoint: String? = null,
        val buyerPostingStatus: String? = null,
        val buyerPostingDate: String? = null,
        val buyerPostingDetail: List<PostingDetailModel>? = emptyList(),
        val taxDocumentNumber: String? = null,
        val taxDocumentLinearId: String? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null,
        val rdSubmittedDate: String? = null,
        val issuedBy: String? = null,
        val campaignId: Int? = null,
        val subtype: String? = null,
        val advancedVatAmount: BigDecimal? = null,
        val paymentDescription: String? = null,
        val advanceDeduction: BigDecimal? = null,
        @get:JsonProperty("isEwht")
        val isEwht: Boolean? = null,
        val documentValidation: DocumentValidationModel? = null,
        val assignDoaBy: String? = null,
        val vendorSiteId: String? = null,
        val documentCode: String? = null,
        @get:JsonProperty("isService")
        val isService: Boolean? = null,
        val grPostingDate : String? = null,
        val evaluationFlag: Boolean? = null,
        val evaluationResult: Map<String, Any>? = emptyMap(),
        val waitingReceipt: Boolean? = null
) {

    val restrictedMap = mapOf(
            "ISSUED" to "Submitted",
            "PARTIAL" to "Verifying",
            "MISSING" to "Verifying",
            "MATCHED" to "Verifying",
            "UNMATCHED" to "Verifying",
            "BASELINED" to "Verifying",
            "PENDING_SELLER" to "Request to Resubmit",
            "PENDING_AUTHORITY" to "Verifying",
            "PENDING_BUYER" to "Verifying",
            "PARTIALLY_APPROVED" to "Verifying",
            "PENDING_FINAL_REVIEW" to "Verifying",
            "APPROVED" to "Waiting Payment Due Date",
            "RESERVED" to "Waiting Payment Due Date",
            "FINANCED" to "Financed",
            "PAID" to "Paid",
            "DECLINED" to "Waiting Payment Due Date",
            "DECLINED_WITH_FINANCED" to "Paid",
            "PAID_WITHOUT_FINANCED" to "Waiting Payment Due Date",
            "CANCELLED" to "Cancelled")

    val disclosedMap = mapOf(
            "ISSUED" to "Submitted",
            "PARTIAL" to "Partial GR",
            "MISSING" to "Missing GR",
            "MATCHED" to "Missing DoA List",
            "UNMATCHED" to "Pending Manual Approval",
            "BASELINED" to "Requesting DoA List",
            "PENDING_SELLER" to "Request Invoice Resubmission",
            "PENDING_AUTHORITY" to "Pending DoA Approval",
            "PENDING_BUYER" to "Pending Clarification",
            "PARTIALLY_APPROVED" to "Pending DoA Approval",
            "PENDING_FINAL_REVIEW" to "Pending Accounting Review",
            "APPROVED" to "Waiting Payment Due Date",
            "RESERVED" to "Waiting Payment Due Date",
            "FINANCED" to "Waiting Payment Due Date",
            "PAID" to "Paid",
            "DECLINED" to "Payment Failed",
            "DECLINED_WITH_FINANCED" to "Payment Failed",
            "PAID_WITHOUT_FINANCED" to "Paid",
            "CANCELLED" to "Cancelled")

    fun display(isSeller: Boolean = true): InvoiceModel {
        val mappedStatus = lifecycle?.let { Pair(generateStatus(it, isSeller, rdSubmittedDate), generateMatcherStatus(it, rdSubmittedDate)) }
        return copy(
                status = mappedStatus?.first,
                matchingStatus = mappedStatus?.second)
    }

    //  This Function take in a List of Strings eg: List < externalId >
    //  and returns a map with value eg: List < externalId, externalId.value >
    fun summarise(fields: List<String>) = fields.map { it to BeanUtils.getProperty(this, it) }.toMap()

    fun updateItem(items: List<InvoiceItemModel>) = copy(invoiceItems = items)

    fun updatePurchaseOrder(purchaseOrder: String) = copy(purchaseOrder = purchaseOrder)

    fun updatePurchaseOrderHeaderNumber(purchaseOrderHeaderNumber: String) = copy(purchaseOrderHeaderNumber = purchaseOrderHeaderNumber)

    fun updateEstimatedPayable(estimatedPayable: BigDecimal?) = copy(estimatedPayable = estimatedPayable)

    fun updateEstimatedPayable() = copy(estimatedPayable = this.estimatedPayable())

    fun updateGoodsReceived(goodsReceived: String) = copy(goodsReceived = goodsReceived)

    fun updateAdvanceDeduction(items: List<InvoiceItemModel>) = copy(advanceDeduction = calculateAdvanceDeduction(items))

    fun updateWithholdingTax(withholdingTaxTotal: BigDecimal) = copy(withholdingTaxTotal = withholdingTaxTotal)

    // Calculating estimated payable.
    // Formula :    estimated payable   = (subtotal + (subtotal x vatRate)) - withHoldingTax - retentionAmount - CreditNoteTotalAmount - CashPerfGuaranteeAmount - CashWarrantyAmount
    //                                  = total - withHoldingTax - retentionAmount - CreditNoteTotalAmount - CashPerfGuaranteeAmount - CashWarrantyAmount
    // We decide to ignore tagged credit in BLOC-8933
    fun estimatedPayable(): BigDecimal {
        return this.invoiceTotal!!.minus(this.withholdingTaxTotal ?: BigDecimal.ZERO)
                .minus(this.retentionAmount ?: BigDecimal.ZERO).minus(this.cashPerfGuaranteeAmount ?: BigDecimal.ZERO).minus(this.cashWarrantyAmount ?: BigDecimal.ZERO)

    }

    /**
     * Method for get status from lifecycle that show in display page
     * If lifecycle have multiple display name (have comma ",") check condition to return which status
     * @param lifecycle: lifecycle of this document
     * @param isSeller: flag for mapping status to seller site
     * @param rdSubmittedDate: Date that submitted to RD
     * @return String status
     */
    private fun generateStatus(lifecycle: String, isSeller: Boolean = true, rdSubmittedDate: String?): String {
        val displayNameStatus = if (isSeller) {
            InvoiceStatus.Seller().valueOf(lifecycle).displayName
        } else {
            // If isSellerStatus is disabled to give a full view of interaction BU, we do the below
            InvoiceStatus.Buyer().valueOf(lifecycle).displayName
        }

        if (displayNameStatus.contains(comma).not()) return displayNameStatus

        val displayNameStatusList = displayNameStatus.splitAndTrim(comma, trim = true)

        return if (rdSubmittedDate != null && displayNameStatusList[1] == PENDING_SELLER_AFTER_RD_SUBMITTED) {
            displayNameStatusList[1]
        } else {
            displayNameStatusList.first()
        }
    }

    /**
     * Method for get status from lifecycle that show in display page
     * If lifecycle have multiple display name (have comma ",") check condition to return which status
     * @param lifecycle: lifecycle of this document
     * @param rdSubmittedDate: Date that submitted to RD
     * @return String status
     */
    private fun generateMatcherStatus(lifecycle: String, rdSubmittedDate: String?): String {
            val displayNameStatus = InvoiceStatus.Matcher().valueOf(lifecycle).displayName

            if (displayNameStatus.contains(comma).not()) return displayNameStatus

            val displayNameStatusList = displayNameStatus.splitAndTrim(comma, trim = true)

            return if (rdSubmittedDate != null && displayNameStatusList[1] == PENDING_SELLER_AFTER_RD_SUBMITTED) {
                    displayNameStatusList[1]
            } else {
                    displayNameStatusList.first()
            }
    }
}
