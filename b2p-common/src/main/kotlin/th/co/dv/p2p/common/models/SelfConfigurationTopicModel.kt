package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Model for buyer self configuration topic
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SelfConfigurationTopicModel(
        val id: Long? = null,
        val topicConfig: String? = null,
        val topicDetail: String? = null,
        val documentType: String? = null,
        val unitDescription: String? = null,
        val validateType: String? = null,
        val documentConfigurationMasters: List<DocumentConfigurationMasterModel>? = emptyList()
)