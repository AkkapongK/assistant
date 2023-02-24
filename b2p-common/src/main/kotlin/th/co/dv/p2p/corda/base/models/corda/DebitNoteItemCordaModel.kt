package th.co.dv.p2p.corda.base.models.corda

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Amount
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal
import java.util.*


/**
 *  data class that use model oin service convert to corda model
 * @property debitNoteLinearId : debit note linearId
 * @property externalId : external id
 * @property invoiceItemExternalId :invoice item external id
 * @property invoiceItemLinearId :invoice item linear id
 * @property linearId :linear id
 * @property materialDescription :material description
 * @property quantity :quantity
 * @property subTotal :subTotal
 * @property total: total
 * @property unitDescription: unit description
 * @property unitPrice: unit price
 * @property vatRate: vat rate
 * @property vatTotal : vat total
 * @property withholdingTaxFormType: withholding tax form type
 * @property withholdingTaxIncomeDescription : withholding tax income description
 * @property withholdingTaxIncomeType : withholding tax income type
 * @property withholdingTaxPayType : withholding tax pay type
 * @property withholdingTaxRate : withholding tax rate
 * @property withholdingTaxRemark : withholding tax remark
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DebitNoteItemCordaModel(
        val debitNoteLinearId: String? = null,
        val externalId: String? = null,
        val invoiceItemExternalId: String? = null,
        val invoiceItemLinearId: String? = null,
        val linearId: String? = null,
        val materialDescription: String? = null,
        val quantity: Quantity? = null,
        val subTotal: Amount<Currency>? = null,
        val total: Amount<Currency>? = null,
        val unitDescription: String? = null,
        val unitPrice: Amount<Currency>? = null,
        val vatRate: BigDecimal? = null,
        val vatTotal: Amount<Currency>? = null,
        val withholdingTaxFormType: String? = null,
        val withholdingTaxIncomeDescription: String? = null,
        val withholdingTaxIncomeType: String? = null,
        val withholdingTaxPayType: String? = null,
        val withholdingTaxRate: BigDecimal? = null,
        val withholdingTaxRemark: String? = null
)