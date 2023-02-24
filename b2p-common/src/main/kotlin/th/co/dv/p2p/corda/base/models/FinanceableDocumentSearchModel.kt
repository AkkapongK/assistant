package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FinanceableDocumentSearchModel(
        val pageNumber: Int = 1,
        val pageSize: Int = 500,
        val sortField: String = "externalId",
        val sortOrder: Int = 1,
        val buyerTaxId: String? = null,
        val vendorTaxNumber: List<String>? = null,
        val status: String? = null,
        val documentType: List<String>? = null
)