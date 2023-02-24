package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

/**
 *
 * The model use for mapping the WithholdingItemTax data to json object and return in the response (API)
 * This model is for release 6 onward
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class WithholdingTaxItemModel(
        val whtAmount: BigDecimal? = null,
        val whtIncomeType: String? = null,
        val whtIncomeTypeDescription: String? = null,
        val whtRate: BigDecimal? = null,
        val whtCode: String? = null,
        val incomeTypeAmount: BigDecimal? = null,
        val currency: String ? = null
)