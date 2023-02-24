package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoanDocumentItemModel(
        val linearId: String? = null,
        val externalId: String? = null,
        val documentType: String? = null,
        val documentIssuedDate: String? = null,
        val vendorTaxId: String? = null,
        val vendorName: String? = null,
        val buyerTaxId: String? = null,
        val buyerName: String? = null,
        val amount: BigDecimal? = null,
        val currency: String? = null,
        val loanLinearId: String? = null,
        val financeableDocumentLinearId: String? = null
)