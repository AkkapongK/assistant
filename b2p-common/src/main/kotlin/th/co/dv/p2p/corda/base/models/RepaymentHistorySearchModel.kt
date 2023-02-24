package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RepaymentHistorySearchModel(
        val pageNumber: Int = 1,
        val pageSize: Int = 1000,
        val sortField: String = "repaymentDate",
        val sortOrder: Int = 1,
        val loanLinearId: String? = null
)