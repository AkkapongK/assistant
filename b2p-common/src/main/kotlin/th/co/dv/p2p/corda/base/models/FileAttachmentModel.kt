package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FileAttachmentModel(
        var id: Long? = null,
        var attachmentId: Long? = null,
        var linearId: String? = null,
        var attachmentHash: String? = null,
        var attachmentName: String? = null,
        var attachmentType: String? = null,
        var uploadDate: String? = null,
        var uploadBy: String? = null,
        var documentNumber: String? = null,
        var documentDate: String? = null,
        var storageLocation: String? = null,
        var attachmentUrl: String? = null
)