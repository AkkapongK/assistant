package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.corda.core.contracts.Amount
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal
import java.util.*

/**
 * Returned when get threshold of sub total, vat and total amount configuration from offchain
 * A ThresholdModel contains:
 *  1) [minimumSubTotal]: The minimum sub total threshold that allow to different
 *  2) [maximumSubTotal]: The maximum sub total threshold that allow to different
 *  3) [minimumVatTotal]: The minimum vat threshold that allow to different
 *  4) [maximumVatTotal]: The maximum vat threshold that allow to different
 *  5) [minimumTotalAmount]: The minimum total amount threshold that allow to different
 *  6) [maximumTotalAmount]: The maximum total amount threshold that allow to different
 *  7) [minimumInvHeaderDiffPercent]: The minimum of percent threshold that allow to different
 *  8) [maximumInvHeaderDiffPercent]: The maximum of percent threshold that allow to different
 *  9) [maximumItemSubTotal]: The maximum of subtotal that can edit in item leve
 *  9) [minimumItemSubTotal]: The minimum of subtotal that can edit in item leve
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
data class ThresholdModel (
        val minimumSubTotal: BigDecimal? = null,
        val maximumSubTotal: BigDecimal? = null,
        val minimumVatTotal: BigDecimal? = null,
        val maximumVatTotal: BigDecimal? = null,
        val minimumTotalAmount: BigDecimal? = null,
        val maximumTotalAmount: BigDecimal? = null,
        val minimumInvHeaderDiffPercent: BigDecimal? = null,
        val maximumInvHeaderDiffPercent: BigDecimal? = null,
        val maximumItemSubTotal: BigDecimal? = null,
        val minimumItemSubTotal: BigDecimal? = null
)

/**
 * The HeaderStateModel is used to convert the header object input
 * into the same object
 *
 * The HeaderStateModel is used in [`ValidateThresholdService`]
 * Now we convert 3 types of input (invoice, creditnote and debitnote)
 */
data class HeaderStateModel(
        val companyTaxNumber: String,
        val buyer: AbstractParty,
        val total: Amount<Currency>,
        val subTotal: Amount<Currency>,
        val vatTotal: Amount<Currency>,
        val isETax: Boolean)

/**
 * The ItemStateModel is used to convert the item object input
 * into the same object
 *
 * The ItemStateModel is used in [`ValidateThresholdService`]
 * Now we convert 3 types of input (invoice item, creditnote item and debitnote item
 */
data class ItemStateModel(
        val total: Amount<Currency>,
        val subTotal: Amount<Currency>,
        val vatRate: BigDecimal,
        val vatTotal: Amount<Currency>)
