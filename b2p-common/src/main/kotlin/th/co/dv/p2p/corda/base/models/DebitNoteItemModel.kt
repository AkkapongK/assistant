package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DebitNoteItemModel(
        val accountCode : String? = null,
        val accountCodeDescription : String? = null,
        val externalId: String? = null,
        val debitNoteLinearId: String? = null,
        val invoiceItemExternalId: String? = null,
        val invoiceItemLinearId: String? = null,
        val purchaseOrderExternalId: String? = null,
        val purchaseItemLinearId: String? = null,
        val purchaseItemExternalId: String? = null,
        val materialGroup: String? = null,
        val materialNumber: String? = null,
        val materialDescription: String? = null,
        val quantity: Quantity? = null,
        val currency: String? = null,
        val unitPrice: BigDecimal? = null,
        val listUnitPrice: BigDecimal? = null,
        val unitPriceDiscount: BigDecimal? = null,
        val unitPriceSurcharge: BigDecimal? = null,
        val totalDiscount: BigDecimal? = null,
        val totalSurcharge: BigDecimal? = null,
        val subTotal: BigDecimal? = null,
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
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val bank: PartyModel? = null,
        val issuedDate: String? = null,
        val status: String? = null,
        val lifecycle: String? = null,
        val linearId: String? = null,
        val invoiceItems: List<InvoiceItemModel>? = null,
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val purchaseItem: PurchaseItemModel? = null,
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
        val requestItemLinearId: String? = null,
        val requestItemExternalId: String? = null,
        val correctedAmount: BigDecimal? = null,
        val correctedUnitPrice: BigDecimal? = null,
        val createdDate: String? = null
) {

    fun display(): DebitNoteItemModel {
        val mappedStatus = lifecycle?.let { generateStatus(it) }
        return copy(status = mappedStatus)
    }

    private fun generateStatus(lifecycle: String): String {
        return when (lifecycle) {
            "ISSUED" -> "Submitted"
            "MATCHED" -> "Approved"
            "PENDING" -> "Request to Resubmit"
            "CANCELLED" -> "Cancelled By Vendor"
            "SETTLED" -> "Settled"
            else -> "Verifying"
        }
    }
}