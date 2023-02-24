package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Amount
import java.util.*

/**
 * This model use to calculate financing rate from documents.
 *
 * @property index running no.
 * @property principalAmount amount to calculate
 * @property startDate current date
 * @property endDate due date
 * @property buyerTaxId buyerTaxId
 * @property sellerTaxId sellerTaxId
 * @property interestType interestType (INVOICE_FINANCING_RATE)
 * @property interestRate interestRate get from configuration in offchain
 * @property interestAmount calculate from (principalAmount Amount)*(Interest Rate/100)*((End Date - Start Date)/365)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CalculateFinancingInterestModel(
        val index: String? = null,
        val principalAmount: Amount<Currency>? = null,
        val startDate: String? = null,
        val endDate: String? = null,
        val buyerTaxId: String? = null,
        val sellerTaxId: String? = null,
        val interestType: String? = null,
        val interestRate: String? = null,
        val interestAmount: Amount<Currency>? = null
)
