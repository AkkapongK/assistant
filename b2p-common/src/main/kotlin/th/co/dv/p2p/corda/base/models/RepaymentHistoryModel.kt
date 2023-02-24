package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RepaymentHistoryModel(
        val repaymentResultId: String? = null,
        val loanLinearId: String? = null,
        val repaymentDate: String? = null,
        val repaymentAmount: BigDecimal? = null,
        val currency: String? = null,
        val repaymentRequestId: String? = null,
        val createdDate: String? = null
)