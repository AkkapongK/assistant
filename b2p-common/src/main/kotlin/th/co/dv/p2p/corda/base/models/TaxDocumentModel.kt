package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.enums.TaxDocumentType
import java.math.BigDecimal

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaxDocumentModel(
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val vendorNumber: String? = null,
        val vendorName: String? = null,
        val vendorBranchCode: String? = null,
        val vendorBranchName: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorAddress: String? = null,
        val companyCode: String? = null,
        val companyName: String? = null,
        val companyBranchCode: String? = null,
        val companyBranchName: String? = null,
        val companyTaxNumber: String? = null,
        val companyAddress: String? = null,
        val documentType: String? = null,
        val documentNumber: String? = null,
        val documentDate: String? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null,
        val lifecycle: String? = null,
        val status: String? = null,
        val subTotal: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val vatCode: String? = null,
        val total: BigDecimal? = null,
        val currency: String? = null,
        val postingUpdatedDate: String? = null,
        val postingStatus: String? = null,
        val postingDoc: String? = null,
        val postingDocYear: String? = null,
        val postingMessage: String? = null,
        val buyerPostingStatus: String? = null,
        val buyerPostingDate: String? = null,
        val buyerPostingDetail: List<PostingDetailModel>? = emptyList(),
        val approvedRemark: String? = null,
        val approvedUser: String? = null,
        val approvedDate: String? = null,
        val rejectedRemark: String? = null,
        val rejectedUser: String? = null,
        val rejectedDate: String? = null,
        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        val linearId: String? = null,
        val invoiceLinearIds: List<String>? = emptyList(),
        val debitNoteLinearIds: List<String>? = emptyList(),
        val creditNoteLinearIds: List<String>? = emptyList(),
        val fileAttachments: List<FileAttachmentModel>? = emptyList(),
        val attachmentPosted: Boolean? = null,
        val referenceDocuments: List<ReferenceDocument>? = emptyList(),
        val customisedFields: Map<String, Any>? = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val rdSubmittedDate: String? = null,
        val issuedBy: String? = null,
        val documentCode: String? = null
) {
    /**
     * Method for mapping lifecycle to status that show in display page
     */
    fun display(): TaxDocumentModel {
        val mappedStatus = lifecycle?.let { generateStatus(it) }
        return copy(status = mappedStatus)
    }

    fun updateReferenceDocuments(referenceDocuments: List<ReferenceDocument>) = copy(referenceDocuments = referenceDocuments)

    private fun generateStatus(lifecycle: String): String {
        return TaxDocumentStatusModel.StatusMapping.valueOf(lifecycle).displayName
    }
}
/**
 * Model for item of tax document information of referenced document (invoice/CN/DN)
 *
 * @property linearId The linearId of referenced document
 * @property documentNumber The document number of referenced document
 * @property documentType The document type of referenced document
 * @property subTotal The sub total without vat total, after including discount and surcharge
 * @property vatTotal The vat total based on tax code
 * @property total The total after include vat total
 * @property currency Currency
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReferenceDocument(
        val linearId: String? = null,
        val documentNumber: String? = null,
        val documentType: String? = null,
        val subTotal: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val total: BigDecimal? = null,
        val currency: String? = null
)

/**
 * Method for convert invoice model to ReferenceDocument
 */
fun InvoiceModel.toReferenceDocument(): ReferenceDocument {
        return ReferenceDocument(
                linearId = linearId,
                documentNumber = externalId,
                documentType = TaxDocumentType.TAX_INVOICE.value,
                subTotal = subTotal,
                vatTotal = vatTotal,
                total = invoiceTotal,
                currency = currency
        )
}

/**
 * Method for convert invoice model to ReferenceDocument
 */
fun CreditNoteModel.toReferenceDocument(): ReferenceDocument {
        return ReferenceDocument(
                linearId = linearId,
                documentNumber = externalId,
                documentType = TaxDocumentType.TAX_CREDIT_NOTE.value,
                subTotal = subTotal,
                vatTotal = vatTotal,
                total = total,
                currency = currency
        )
}

/**
 * Method for convert invoice model to ReferenceDocument
 */
fun DebitNoteModel.toReferenceDocument(): ReferenceDocument {
        return ReferenceDocument(
                linearId = linearId,
                documentNumber = externalId,
                documentType = TaxDocumentType.TAX_DEBIT_NOTE.value,
                subTotal = subTotal,
                vatTotal = vatTotal,
                total = total,
                currency = currency
        )
}