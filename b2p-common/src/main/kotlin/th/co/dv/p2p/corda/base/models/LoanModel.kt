package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoanModel(
        val linearId: String? = null,
        val externalId: String? = null,
        val currency: String? = null,
        val loanAmount: BigDecimal? = null,
        val feeAmount: BigDecimal? = null,
        val interestAmount: BigDecimal? = null,
        val loanEffectiveDate: String? = null,
        val loanDueDate: String? = null,
        val loanStatus: String? = null,
        val interestRate: BigDecimal? = null,
        val pastDueInterestRate: BigDecimal? = null,
        val buyerTaxId: String? = null,
        val buyerName: String? = null,
        val borrowerTaxId: String? = null,
        val borrowerName: String? = null,
        val lenderCode: String? = null,
        val lenderRef: String? = null,
        val lenderMessage: String? = null,
        val lenderUpdatedDate: String? = null,
        val lenderFinancingProduct: String? = null,
        val loanOutstandingAmount: BigDecimal? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null,
        var loanDocumentItems: List<LoanDocumentItemModel> = emptyList(),
        val createdBy: String? = null
)