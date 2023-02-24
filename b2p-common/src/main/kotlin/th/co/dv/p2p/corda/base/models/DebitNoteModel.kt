package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.constants.comma
import th.co.dv.p2p.common.utilities.splitAndTrim
import th.co.dv.p2p.corda.base.models.DebitNoteStatus.PENDING_SELLER_AFTER_RD_SUBMITTED
import java.math.BigDecimal

/**
 * The Debit note object that related with debit note state.
 * We use the DebitNoteModel for mapping the value with state and return to response
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DebitNoteModel(
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
        val invoiceLinearId: String? = null,
        val invoiceExternalId: String? = null,
        val debitNoteDate: String? = null,
        val approvedDate: String? = null,
        val paymentTermCode: String? = null,
        val paymentTermDesc: String? = null,
        val paymentTermDays: Long? = null,
        val dueDate: String? = null,
        val initialDueDate: String? = null,
        val dueDateLastEditedReason: String? = null,
        val dueDateLastEditedBy: String? = null,
        val dueDateLastEditedDate: String? = null,
        val dueDateIsLocked: Boolean? = false,
        val debitNoteCreatedDate: String? = null,
        val listTotal: BigDecimal? = null,
        val totalDiscount: BigDecimal? = null,
        val totalSurcharge: BigDecimal? = null,
        val subTotal: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val total: BigDecimal? = null,
        val withholdingTaxTotal: BigDecimal? = null,
        val totalPayable: BigDecimal? = null,
        val currency: String? = null,
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val bank: PartyModel? = null,
        val baselineDate: String? = null,
        val receiptNumber: String? = null,
        val receiptDate: String? = null,
        val paymentFee: BigDecimal? = null,
        val reason: String? = null,
        val debitNoteItems: List<DebitNoteItemModel> = emptyList(),
        val status: String? = null,
        val lifecycle: String? = null,
        val linearId: String? = null,
        val buyerApprovedDate: String? = null,
        val buyerApprovedRemark: String? = null,
        val buyerApprovedUser: String? = null,
        val buyerRejectedDate: String? = null,
        val buyerRejectedRemark: String? = null,
        val buyerRejectedUser: String? = null,
        val cancelledUser: String? = null,
        val cancelledDate: String? = null,
        val cancelledRemark: String? = null,
        val cancelledCode: String? = null,
        val cancelledInvoiceNumber: String? = null,
        val cancelledVendorTaxNumber: String? = null,
        val cancelledInvoiceDate: String? = null,
        val calculatedDate: String? = null,
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val currentAuthority: DelegationOfAuthorityModel? = null,
        val nextAuthority: DelegationOfAuthorityModel? = null,
        val previousAuthority: DelegationOfAuthorityModel? = null,
        val delegatedAuthorities: List<DelegationOfAuthorityModel>? = null,
        val taggedCreditNotes: List<TaggedDocumentModel>? = null,
        val paymentDate: String? = null,
        val paymentItemLinearId: String? = null,
        val paymentIsSuccessful: Boolean? = null,
        val paymentStatusUpdateDate: String? = null,
        val fileAttachments: List<FileAttachmentModel>? = listOf(),
        val paymentReferenceNumber: String? = null,
        val postingStatus: String? = null,
        val debitNotePostingUpdatedDate: String? = null,
        val issuedBy: String? = null,
        val lastEditedBy: String? = null,
        val lastEditedDate: String? = null,
        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        @get:JsonProperty("isETaxDebitNote")
        val isETaxDebitNote: Boolean? = null,
        val adjustmentType: String? = null,
        val removeFromPayment: Boolean? = null,
        val invoice: InvoiceModel? = null,
        val remark: String? = null,
        val requestLinearId: String? = null,
        val requestExternalId: String? = null,
        val totalOriginalValue: BigDecimal? = null,
        val totalCorrectedValue: BigDecimal? = null,
        @get:JsonProperty("isEwht")
        val isEwht: Boolean? = null,
        val customIndicator1: Boolean? = null,
        val calendarKey: String? = null,
        val buyerPostingStatus: String? = null,
        val buyerPostingDate: String? = null,
        val buyerPostingDetail: List<PostingDetailModel>? = emptyList(),
        val taxDocumentNumber: String? = null,
        val taxDocumentLinearId: String? = null,
        val rdSubmittedDate: String? = null,
        val correctedAmount: BigDecimal? = null,
        val correctedUnitPrice: BigDecimal? = null,
        val vatTriggerPoint: String? = null,
        val reasonCode: String? = null,
        val createdDate: String? = null,
        val withholdingTaxCalculationPoint: String? = null,
        val paymentDescription: String? = null,
        val assignDoaBy: String? = null,
        val vendorSiteId: String? = null,
        val documentCode: String? = null,
        @get:JsonProperty("isOnHold")
        val isOnHold: Boolean? = null,
        val lastHeldBy: String? = null,
        val lastHeldRemark: String? = null,
        val lastHeldDate: String? = null,
        val lastUnheldBy: String? = null,
        val lastUnheldRemark: String? = null,
        val lastUnheldDate: String? = null,
        val updatedDate: String? = null
) {
    /**
     * Method for mapping lifecycle to status that show in display page
     * @param isSeller: flag for mapping status to seller site
     */
    fun display(isSeller: Boolean = true): DebitNoteModel {
        val mappedStatus = lifecycle?.let { generateStatus(it, isSeller, rdSubmittedDate) }
        return copy(
                status = mappedStatus)
    }

    fun updateWithholdingTax(withholdingTaxTotal: BigDecimal) = copy(withholdingTaxTotal = withholdingTaxTotal)

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
            DebitNoteStatus.Seller().valueOf(lifecycle).displayName
        } else {
            // If isSeller is disabled to give a full view of interaction BU, we do the below
            DebitNoteStatus.Buyer().valueOf(lifecycle).displayName
        }

        if (displayNameStatus.contains(comma).not()) return displayNameStatus

        val displayNameStatusList = displayNameStatus.splitAndTrim(comma, trim = true)

        return if (rdSubmittedDate != null && displayNameStatusList[1] == PENDING_SELLER_AFTER_RD_SUBMITTED) {
            displayNameStatusList[1]
        } else {
            displayNameStatusList.first()
        }
    }

    fun updateItem(items: List<DebitNoteItemModel>) = copy(debitNoteItems = items)
}
