package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoanProfileSearchModel(
        val pageNumber: Int = 1,
        val pageSize: Int = 500,
        val sortField: String = "interestRate",
        val sortOrder: Int = 1,
        val borrowerTaxId: List<String>? = null,
        val lenderCode: String? = null,
        val lenderFinancingProduct: String? = null
)