package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Model for document configuration master
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DocumentConfigurationMasterModel (
        val configOption: String? = null,
        val documentType: String? = null,
        val category: String? = null,
        val owner: String? = null,
        val description: String? = null,
        val defaultValue: String? = null,
        val b2pVersion: String? = null,
        val buyerSelfConfigurationTopicId: Long? = null,
        val selfConfigurationDisplay: String? = null
)