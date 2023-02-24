package th.co.dv.p2p.common.kafka.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Class for keep notify result that return from other service
 * and we used the result to update state
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class NotifyEventModel (
        val id: String,
        val status: NotifyStatus,
        val externalId: String,
        val relatedServices : List<String>,
        val message: String? = null,
        val priority: Int = 1
)