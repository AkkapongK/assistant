package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

/**
 * data class for keep summary information of financeable document
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FinanceableAmountSummaryModel(
        val documentType: String? = null,
        val vendorTaxId: String? = null,
        val buyerTaxId: String? = null,
        val currency: String? = null,
        val amount: BigDecimal? = null,
        val numberOfDocuments: Int? = 0,
        val buyerName: String? = null
)