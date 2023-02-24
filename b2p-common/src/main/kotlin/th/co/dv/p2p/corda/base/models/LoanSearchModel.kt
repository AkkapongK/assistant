package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoanSearchModel(
        val pageNumber: Int = 1,
        val pageSize: Int = 500,
        val sortField: String = "externalId",
        val sortOrder: Int = 1,
        val linearIds: List<String>? = null,
        val externalId: List<String>? = null,
        val buyerTaxId: String? = null,
        val borrowerTaxId: List<String>? = null,
        val lenderRef: String? = null,
        val partiallyLenderRef: String? = null,
        val buyerName: String? = null,
        val borrowerName: String? = null,
        val loanStatuses: List<String>? = null,
        val paymentDueDateFrom: String? = null,
        val paymentDueDateTo: String? = null,
        val financeableDocumentLinearId: List<String>? = null
)