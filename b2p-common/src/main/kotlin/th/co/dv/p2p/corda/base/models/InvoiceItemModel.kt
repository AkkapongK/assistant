package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InvoiceItemModel(
        val invoiceLinearId: String? = null,
        val externalId: String? = null,
        val purchaseOrderExternalId: String? = null,
        val purchaseItemLinearId: String? = null,
        val purchaseItemExternalId: String? = null,
        val materialGroup: String? = null,
        val materialDescription: String? = null,
        val materialNumber: String? = null,
        val quantity: Quantity? = null,
        val creditNoteQuantity: Quantity? = null,
        val currency: String? = null,
        val unitPrice: BigDecimal? = null,
        val listUnitPrice: BigDecimal? = null,
        val unitPriceDiscount: BigDecimal? = null,
        val unitPriceSurcharge: BigDecimal? = null,
        val totalDiscount: BigDecimal? = null,
        val totalSurcharge: BigDecimal? = null,
        val itemSubTotal: BigDecimal? = null,
        val withholdingTaxRate: BigDecimal? = null,
        val withholdingTaxFormType: String? = null,
        val withholdingTaxPayType: String? = null,
        val withholdingTaxRemark: String? = null,
        val withholdingTaxIncomeType: String? = null,
        val withholdingTaxIncomeDescription: String? = null,
        val withholdingTaxCode: String? = null,
        val withholdingTaxBaseAmount: BigDecimal? = null,
        val vatCode: String? = null,
        val vatRate: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val total: BigDecimal? = null,
        val matchedCode: Map<String, String> = emptyMap(),
        val unmatchedCode: List<String>? = null,
        val unmatchedReason: String? = null,
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val accounting: PartyModel? = null,
        val bank: PartyModel? = null,
        val issuedDate: String? = null,
        val status: String? = null,
        val lifecycle: String? = null,
        val linearId: String? = null,
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val purchaseItem: PurchaseItemModel? = null,
        val creditNoteAdjustedSubtotal: BigDecimal? = null,
        val debitNoteAdjustedSubTotal: BigDecimal? = null,
        val site: String? = null,
        val siteDescription: String? = null,
        val section: String? = null,
        val sectionDescription: String? = null,
        val unitDescription: String? = null,
        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        val retentionAmount: BigDecimal? = null,
        var accountCode : String? = null,
        var accountCodeDescription: String? = null,
        val itemCategory: String? = null,
        val goodsReceivedItems: List<GoodsReceivedItemModel> = emptyList(),
        val createdDate: String? = null,
        val updatedDate: String? = null,
        val cashPerfGuaranteeAmount: BigDecimal? = null,
        val cashWarrantyAmount: BigDecimal? = null,
        val contractNumber: String? = null,
        val contractLinearId: String? = null,
        @get:JsonProperty("isCalculateTaxPerItem")
        val isCalculateTaxPerItem: Boolean? = null) {

    companion object {
        fun generateStatus(lifecycle: String): String {
            return when (lifecycle) {
                "ISSUED" -> "Submitted"
                "MISSING" -> "Missing GR"
                "PARTIAL" -> "Partial GR"
                "MATCHED" -> "Missing DoA List"
                "UNMATCHED" -> "Pending Manual Approval"
                else -> "Verifying"
            }
        }
    }

    fun display(): InvoiceItemModel {
        val mappedStatus = lifecycle?.let { generateStatus(it) }
        return copy(status = mappedStatus)
    }

    fun updateVatCode(taxCode: String, percentage: Int) = copy(vatCode = taxCode, vatRate = BigDecimal(percentage))

}