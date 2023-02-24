package th.co.dv.p2p.corda.base.models.corda

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Amount
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal
import java.util.*

/**
 * data class use for convert invoice item model to corda model
 *
 * @property externalId this state's line item number/id in the external world
 * @property invoiceLinearId Unique Identifier of the invoice parent object
 * @property purchaseOrderExternalId Purchase order number
 * @property purchaseItemExternalId Purchase item number
 * @property materialDescription the description of material
 * @property quantity invoice item quantity, contains quantity value and unit of measure
 * @property unitPrice the price per unit of invoice item without vat, after discount and surcharge included
 * @property subTotal the sub total of the invoice item without vat total, after discount and surcharge included
 * @property withholdingTaxRate the withholding tax rate is a deduction from payments made to suppliers who provide a service.
 * @property withholdingTaxFormType the withholding tax form type
 * @property withholdingTaxPayType the withholding tax pay type
 * @property withholdingTaxRemark the withholding tax remark
 * @property withholdingTaxIncomeType the withholding tax income type
 * @property withholdingTaxIncomeDescription the withholding tax income description
 * @property withholdingTaxBaseAmount the code of withholding tax base amount
 * @property vatRate the vat rate of invoice item
 * @property vatTotal the vat total of the invoice item based on tax code
 * @property total the total of the invoice item after vat total included
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InvoiceItemCordaModel(
    val externalId: String? = null,
    val invoiceLinearId: String? = null,
    val purchaseOrderExternalId: String? = null,
    val purchaseItemExternalId: String? = null,
    val materialDescription: String? = null,
    val quantity: Quantity? = null,
    val unitPrice: Amount<Currency>? = null,
    val subTotal: Amount<Currency>? = null,
    val withholdingTaxRate: BigDecimal? = null,
    val withholdingTaxFormType: String? = null,
    val withholdingTaxPayType: String? = null,
    val withholdingTaxRemark: String? = null,
    val withholdingTaxIncomeType: String? = null,
    val withholdingTaxIncomeDescription: String? = null,
    val withholdingTaxBaseAmount: Amount<Currency>? = null,
    val vatRate: BigDecimal? = null,
    val vatTotal: Amount<Currency>? = null,
    val total: Amount<Currency>? = null,
    val unitDescription: String? = null,
    val itemCategory: String? = null,
    val linearId: String? = null
)
