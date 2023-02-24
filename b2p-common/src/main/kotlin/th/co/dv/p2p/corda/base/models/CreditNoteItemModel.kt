package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal

/**
 * Data model class that is passed in from the Front End that will be translated into Corda State
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreditNoteItemModel(
        val linearId: String? = null,
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val accounting: PartyModel? = null,
        val bank: PartyModel? = null,
        val externalId: String? = null,
        val purchaseItemLinearId : String? = null,
        val invoiceItemLinearId: String? = null,
        val invoiceItemExternalId: String? = null,
        val creditNoteLinearId: String? = null,
        val materialGroup: String? = null,
        val materialNumber: String? = null,
        val materialDescription: String? = null,
        val quantity: Quantity? = null,
        val unit: String? = null,
        val unitDescription: String? = null,
        val unitPrice: BigDecimal? = null,
        val subTotal: BigDecimal? = null,
        val taxRate: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val vatCode: String? = null,
        val withholdingTaxRate: BigDecimal? = null,
        val withholdingTaxFormType: String? = null,
        val withholdingTaxPayType: String? = null,
        val withholdingTaxRemark: String? = null,
        val withholdingTaxIncomeType: String? = null,
        val withholdingTaxIncomeDescription: String? = null,
        val withholdingTaxCode: String? = null,
        val withholdingTaxBaseAmount: BigDecimal? = null,
        val unmatchedCode: List<String>? = null,
        val unmatchedReason: String? = null,
        val serviceDescription: String? = null,
        val currency: String? = null,
        val issuedDate: String? = null,
        val lifecycle: String? = null,
        val invoiceItems: List<InvoiceItemModel>? = null,
        val purchaseItem: PurchaseItemModel? = null,
        val goodsReceivedItems: List<GoodsReceivedItemModel> = emptyList(),
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val status: String? = null,
        val site: String? = null,
        val siteDescription: String? = null,
        val section: String? = null,
        val sectionDescription: String? = null,
        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        val buyerApprovedDate: String? = null,
        val buyerApprovedRemark: String? = null,
        val buyerApprovedUser: String? = null,
        val buyerRejectedDate: String? = null,
        val buyerRejectedRemark: String? = null,
        val buyerRejectedUser: String? = null,
        val requestItemExternalId: String? = null,
        val requestItemLinearId: String? = null,
        val accountCode: String? = null,
        val accountCodeDescription: String? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null,
        val correctedAmount: BigDecimal? = null,
        val correctedUnitPrice: BigDecimal? = null,
        val lastMatchUpdatedDate:  String? = null,
        val contractNumber: String? = null
) {

    val disclosedMap = mapOf(
            "ISSUED" to "Submitted",
            "MATCHED" to "Approved",
            "UNMATCHED" to "Pending Manual Approval",
            "SETTLED" to "Got payment result from bank as successful",
            "MISSING" to "Missing Return GR",
            "PARTIAL" to "Partial Return GR",
            "REJECTED" to "Request Credit Note Resubmission",
            "CANCELLED" to "Cancelled By Vendor"
    )

    val restrictedMap = mapOf(
            "ISSUED" to "Submitted",
            "MATCHED" to "Waiting for Settlement",
            "UNMATCHED" to "Verifying",
            "SETTLED" to "Settled",
            "MISSING" to "Verifying",
            "PARTIAL" to "Verifying",
            "REJECTED" to "Request to Resubmit",
            "CANCELLED" to "Cancelled By Vendor"
    )

    /**
     * Function to map Status to External World if Required
     */
    fun display(restricted: Boolean = true): CreditNoteItemModel {
        val mappedStatus = lifecycle?.let { generateStatus(it, restricted) }
        return copy(status = mappedStatus)
    }

    private fun generateStatus(lifecycle: String, restricted: Boolean): String {
        return if (restricted) {
            this.restrictedMap.getOrDefault(lifecycle, "Status not found")
        } else {
            this.disclosedMap.getOrDefault(lifecycle, "Status not found")
        }
    }
}