package th.co.dv.p2p.corda.base.models.corda

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Amount
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal
import java.util.*

/**
 * @property creditNoteLinearId          the linear id of Credit Note tagged to this Credit Note Item
 * @property externalId                  this state's line item number/id in the external world
 * @property invoiceItemExternalId       the Invoice Item Reference No tagged to this Credit Note
 * @property invoiceItemLinearId         the linear id of Invoice Item tagged to this Credit Note
 * @property materialDescription         the product/material's common description in the external world
 * @property quantity                    the Quantity of the Credit Note Item
 * @property subTotal                    Total value of Credit Note excluding tax
 * @property vatRate                     Percent of tax for goods/service
 * @property unitDescription             Description for unit name display
 * @property unitPrice                   Total value of Credit Note Item excluding tax
 * @property vatTotal                    Tax value of Credit Note
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreditNoteItemCordaModel(
    val creditNoteLinearId: String? = null,
    val externalId: String? = null,
    val invoiceItemExternalId: String? = null,
    val invoiceItemLinearId: String? = null,
    val linearId: String? = null,
    val materialDescription: String? = null,
    val quantity: Quantity? = null,
    val subTotal: Amount<Currency>? = null,
    val vatRate: BigDecimal? = null,
    val unitDescription: String? = null,
    val unitPrice: Amount<Currency>? = null,
    val vatTotal: Amount<Currency>? = null,
    val withholdingTaxFormType: String? = null,
    val withholdingTaxIncomeDescription: String? = null,
    val withholdingTaxIncomeType: String? = null,
    val withholdingTaxPayType: String? = null,
    val withholdingTaxRate: BigDecimal? = null,
    val withholdingTaxRemark: String? = null
)
