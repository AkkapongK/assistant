package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AttachmentConfigurationModel(
        var companyTaxId: String? = null,
        var counterPartyTaxId: String? = null,
        var documentType: String? = null,
        var attachmentType: String? = null,
        var minimumNumberOfFiles: Int? = null,
        var maximumNumberOfFiles: Int? = null,
        var fileType: String? = null,
        var documentFinalStatus: String? = null,
        var allowedActionAfterFinalStatus: String? = null,
        var allowedLifecycle: List<String>? = null,
        var allowedAction: List<String>? = null)