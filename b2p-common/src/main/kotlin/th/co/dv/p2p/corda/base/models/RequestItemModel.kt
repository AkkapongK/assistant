package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestItemModel(
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val bank: PartyModel? = null,

        val issuedDate: String? = null,
        val description: String? = null,
        val externalId: String? = null,
        val linearId: String? = null,
        val lifecycle: String? = null,
        val requestLinearId: String? = null,
        val referenceItemNumber: String? = null,
        val referenceItemLinearId: String? = null,

        val site: String? = null,
        val siteDescription: String? = null,
        val section: String? = null,
        val sectionDescription: String? = null,

        val currency: String? = null,
        val quantity: Quantity? = null,
        val unitPrice: BigDecimal? = null,
        val unitDescription: String? = null,

        val vatCode: String? = null,
        val vatRate: BigDecimal? = null,
        val subTotal: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val total: BigDecimal? = null,

        val withholdingTaxRate: BigDecimal? = null,
        val withholdingTaxFormType: String? = null,
        val withholdingTaxPayType: String? = null,
        val withholdingTaxRemark: String? = null,
        val withholdingTaxIncomeType: String? = null,
        val withholdingTaxIncomeDescription: String? = null,
        val withholdingTaxCode: String? = null,
        val withholdingTaxAmount: BigDecimal? = null,

        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val status: String? = null
){
    companion object {
        fun generateStatus(lifecycle: String): String {
            return when (lifecycle) {
                "ISSUED" -> "Issued"
                "CLOSED" -> "Closed"
                else -> "Cancelled"
            }
        }
    }

    fun display(): RequestItemModel {
        val mappedStatus = lifecycle?.let { generateStatus(it) }
        return copy(status = mappedStatus)
    }
}