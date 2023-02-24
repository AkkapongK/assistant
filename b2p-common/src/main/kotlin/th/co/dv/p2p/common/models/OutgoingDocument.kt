package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.sql.Timestamp

/**
 * model for rd document posting
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OutgoingDocument(
        val id: Long? = null,
        val rdTaxDocumentId: String? = null,
        val documentLinearId: String? = null,
        val documentType: String? = null,
        val xmlFilename: String? = null,
        val xmlGeneratedDate: Timestamp? = null,
        val xmlGeneratedDescription: String? = null,
        val zipFilename: String? = null,
        val zipGeneratedDate: Timestamp? = null,
        val zipGeneratedDescription: String? = null,
        val rdPostingStatus: String? = null,
        val rdPostingDescription: String? = null,
        val rdPostingDate: Timestamp? = null,
        val rdLifecycle: String? = null,
        val jobId: String? = null
)