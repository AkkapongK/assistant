package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoanDocumentItemSearchModel (
    val pageNumber: Int = 1,
    val pageSize: Int = 500,
    val sortField: String = "externalId",
    val sortOrder: Int = 1,
    val linearId: String? = null,
    val externalId: String? = null,
    val documentType: String? = null,
    val vendorTaxId: List<String>? = null,
    val buyerTaxId: String? = null,
    val financeableDocumentLinearId: String? = null
)