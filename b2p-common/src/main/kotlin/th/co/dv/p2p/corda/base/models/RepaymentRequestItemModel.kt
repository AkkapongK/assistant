package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RepaymentRequestItemModel(
        val repaymentRequestId: String? = null,
        val loanLinearId: String? = null,
        val repaymentAmount: BigDecimal? = null,
        val currency: String? = null,
        val refPoNumber: String? = null,
        val refPoLinearIds: String? = null,
        val refInvoiceLinearIds: String? = null
)
