package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.constants.comma
import th.co.dv.p2p.common.utilities.splitAndTrim
import th.co.dv.p2p.corda.base.models.CreditNoteStatus.REJECTED_AFTER_RD_SUBMITTED
import java.math.BigDecimal

/**
 * Data model class that is passed in from the Front End that will be translated into Corda State
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreditNoteModel(
        val linearId: String? = null,
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val accounting: PartyModel? = null,
        val bank: PartyModel? = null,
        val externalId: String? = null,
        val invoiceExternalId: String? = null,
        val invoiceLinearId: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorAddress: String? = null,
        val vendorNumber: String? = null,
        val vendorBranchCode: String? = null,
        val vendorBranchName: String? = null,
        val vendorTelephone: String? = null,
        val vendorName: String? = null,
        val companyTelephone: String? = null,
        val companyTaxNumber: String? = null,
        val companyAddress: String? = null,
        val companyBranchCode: String? = null,
        val companyCode: String? = null,
        val companyBranchName: String? = null,
        val companyName: String? = null,
        val creditNoteDate: String? = null,
        val subTotal: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val withholdingTaxTotal: BigDecimal? = null,
        val total: BigDecimal? = null,
        val totalReceivable: BigDecimal? = null,
        val unmatchedCode: List<String>? = null,
        val unmatchedReason: String? = null,
        val lastMatchUpdatedDate: String? = null,
        val currency: String? = null,
        val reason: String? = null,
        val businessPlace: String? = null,
        val remark: String? = null,
        val documentEntryDate: String? = null,
        val documentEntryMethod: String? = null,
        val issuedDate: String? = null,
        val fileAttachments: List<FileAttachmentModel>? = emptyList(),
        val lifecycle: String? = null,
        val status: String? = null,
        val matchingStatus: String? = null,
        val postingStatus: String? = null,
        val creditPostingUpdatedDate: String? = null,
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val adjustmentType: String? = null,
        val purchaseOrder: String? = null,
        val purchaseOrderHeaderNumber: String? = null,
        val creditNoteItems: List<CreditNoteItemModel> = emptyList(),
        val taggedInvoices: List<TaggedDocumentModel>? = null,
        val taggedDebitNotes: List<TaggedDocumentModel>? = null,
        val paymentItemLinearId: String? = null,
        val goodsReceived: String? = null,
        val buyerApprovedDate: String? = null,
        val buyerApprovedRemark: String? = null,
        val buyerApprovedUser: String? = null,
        val buyerRejectedDate: String? = null,
        val buyerRejectedRemark: String? = null,
        val buyerRejectedUser: String? = null,
        val cancelledDate: String? = null,
        val cancelledRemark: String? = null,
        val cancelledUser: String? = null,
        val resubmitCount: Int = 0,
        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        val lastEditedBy: String? = null,
        val lastEditedDate: String? = null,
        @get:JsonProperty("isETaxCreditNote")
        val isETaxCreditNote: Boolean? = null,
        val paymentReferenceNumber: String? = null,
        val paymentDate: String? = null,
        val requestExternalId: String? = null,
        val requestLinearId: String? = null,
        val invoice: InvoiceModel? = null,
        val totalOriginalValue: BigDecimal? = null,
        val totalCorrectedValue: BigDecimal? = null,
        val buyerPostingStatus: String? = null,
        val buyerPostingDate: String? = null,
        val buyerPostingDetail: List<PostingDetailModel>? = emptyList(),
        val taxDocumentNumber: String? = null,
        val taxDocumentLinearId: String? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null,
        val rdSubmittedDate: String? = null,
        val issuedBy: String? = null,
        val correctedAmount: BigDecimal? = null,
        val correctedUnitPrice: BigDecimal? = null,
        val vatTriggerPoint: String? = null,
        val reasonCode: String? = null,
        val creditNoteSubType: String? = null,
        val documentDueDate: String? = null,
        val paymentDescription: String? = null,
        @get:JsonProperty("isEwht")
        val isEwht: Boolean? = null,
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
        val voidVat: Boolean? = null
) {

    val adjustmentMap = mapOf(
            "PRICE" to "Price Adjustment",
            "QUANTITY" to "Goods Return",
            "OTHER" to "Others")

    val disclosedMap = mapOf(
            "ISSUED" to "Submitted",
            "PARTIAL" to "Partial Return GR",
            "MISSING" to "Missing Return GR",
            "UNMATCHED" to "Pending Manual Approval",
            "REJECTED" to "Request Credit Note Resubmission",
            "MATCHED" to "Approved",
            "PENDING" to "Pending",
            "CANCELLED" to "Cancelled By Vendor",
            "SETTLED" to "Settled",
            "DECLINED" to "Payment Failed",
            "PAID" to "Settled"
    )

    val restrictedMap = mapOf(
            "ISSUED" to "Submitted",
            "PARTIAL" to "Verifying",
            "MISSING" to "Verifying",
            "UNMATCHED" to "Verifying",
            "REJECTED" to "Request to Resubmit",
            "MATCHED" to "Waiting for Settlement",
            "PENDING" to "Pending",
            "CANCELLED" to "Cancelled By Vendor",
            "SETTLED" to "Settled",
            "DECLINED" to "Payment Failed",
            "PAID" to "Settled"
    )

    /**
     * Function to Map Corda Status to external world if required
     */
    fun display(isSellerStatus: Boolean = true): CreditNoteModel {
        val mappedStatus = lifecycle?.let { Pair(generateStatus(it, isSellerStatus, rdSubmittedDate), generateMatcherStatus(it, rdSubmittedDate)) }
        val adjustmentStatus = adjustmentType?.let { generateAdjustmentStatus(it) }
        return copy(status = mappedStatus?.first,
                matchingStatus = mappedStatus?.second,
                adjustmentType = adjustmentStatus)
    }

    private fun generateAdjustmentStatus(adjustmentType: String): String {
        return adjustmentMap.getOrDefault(adjustmentType, "Status not found.")
    }

    /**
     * Method for get status from lifecycle that show in display page
     * If lifecycle have multiple display name (have comma ",") check condition to return which status
     * @param lifecycle: lifecycle of this document
     * @param restricted: flag for mapping status to seller site
     * @param rdSubmittedDate: Date that submitted to RD
     * @return String status
     */
    private fun generateStatus(lifecycle: String, restricted: Boolean, rdSubmittedDate: String?): String {
        val displayNameStatus = if (restricted) {
            CreditNoteStatus.Seller().valueOf(lifecycle).displayName
        } else {
            CreditNoteStatus.Buyer().valueOf(lifecycle).displayName
        }

        if (displayNameStatus.contains(comma).not()) return displayNameStatus

        val displayNameStatusList = displayNameStatus.splitAndTrim(comma, trim = true)

        return if (rdSubmittedDate != null && displayNameStatusList[1] == REJECTED_AFTER_RD_SUBMITTED) {
            displayNameStatusList[1]
        } else {
            displayNameStatusList.first()
        }
    }

    private fun generateMatcherStatus(lifecycle: String, rdSubmittedDate: String?): String {
        val displayNameStatus = CreditNoteStatus.Matcher().valueOf(lifecycle).displayName
        if (displayNameStatus.contains(comma).not()) return displayNameStatus

        val displayNameStatusList = displayNameStatus.splitAndTrim(comma, trim = true)

        return if (rdSubmittedDate != null && displayNameStatusList[1] == REJECTED_AFTER_RD_SUBMITTED) {
            displayNameStatusList[1]
        } else {
            displayNameStatusList.first()
        }
    }

    fun updateItem(items: List<CreditNoteItemModel>) = copy(creditNoteItems = items)

    fun updateWithholdingTax(withholdingTaxTotal: BigDecimal) = copy(withholdingTaxTotal = withholdingTaxTotal)

    fun updatePurchaseOrderHeaderNumber(purchaseOrderHeaderNumber: String) = copy(purchaseOrderHeaderNumber = purchaseOrderHeaderNumber)
}