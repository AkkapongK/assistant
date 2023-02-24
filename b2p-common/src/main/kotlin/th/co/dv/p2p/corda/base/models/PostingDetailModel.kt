package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PostingDetailModel(
        val id: Long? = null,
        val fiDocNumber: String? = null,
        val fiDocType: String? = null,
        val fiDocFiscalYear: String? = null,
        val fiDocItemNumber: String? = null,
        val fiDocHeaderText: String? = null,
        val livDocNumber: String? = null,
        val livDocFiscalYear: String? = null,
        val message: String? = null,
        val owner: String? = null,
        val taxDocumentLinearId: String? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null
)
