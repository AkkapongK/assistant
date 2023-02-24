package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

/**
 * This model use for response interest calculation.
 *
 * @property totalInterestAmount Σ Interest Amount of every invoice in the list
 * @property resultList list of CalculateFinancingInterestModel
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InterestCalculationModel(
        val totalInterestAmount: BigDecimal? = null,
        val resultList: List<CalculateFinancingInterestModel>? = null
)
