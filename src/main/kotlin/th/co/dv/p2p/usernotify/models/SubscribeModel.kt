package th.co.dv.p2p.usernotify.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubscribeModel(
    val topicIds: List<Long>,
    val channels: List<String>,
    val token: String? = null
)
