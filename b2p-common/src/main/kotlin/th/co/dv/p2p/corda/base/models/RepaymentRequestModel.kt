package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RepaymentRequestModel(
        val repaymentRequestId: String? = null,
        val borrowerTaxId: String? = null,
        val lenderCode: String? = null,
        val lenderFinancingProduct: String? = null,
        val repaymentRequestAmount: BigDecimal? = null,
        val currency: String? = null,
        val repaymentEffectiveDate: String? = null,
        val repaymentReason: String? = null,
        val repaymentRequestStatus: String? = null,
        val repaymentRequestItems: List<RepaymentRequestItemModel> = emptyList(),
        val repaymentHistories: List<RepaymentHistoryModel> = emptyList(),
        val createdDate: String? = null,
        val createdBy: String? = null,
        val repaymentRequestStatusMessage: String? = null,
        val scfLoanId: String? = null
)