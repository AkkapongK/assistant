package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.*

data class LoanProfileModel (
    var borrowerTaxId: String? = null,
    var lenderCode: String? = null,
    var lenderFinancingProduct: String? = null,
    var borrowerBankAccountNumber: String? = null,
    @get:JsonProperty("isActive")
    var isActive: Boolean? = null,
    var interestRate: BigDecimal? = null,
    var availableLimit: BigDecimal? = null,
    var maxLoanPercent: BigDecimal? = null,
    var maxLoanTenor: BigDecimal? = null,
    var pastDueInterestRate: BigDecimal? = null,
    var currency: String? = null,
    var totalLoanOutstandingAmount: BigDecimal? = null,
    var totalInterestOutstandingAmount: BigDecimal? = null,
    var totalFeeOutstandingAmount: BigDecimal? = null,
    var updatedDate: Date? = null,
    var updatedBy: String? = null,
    var sellerName: String? = null,
    var loans: List<LoanModel> = emptyList()
)
