package th.co.dv.b2p.linebot.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GoldUpdatedModel(
        var name: String? = null,
        var bid: String? = null,
        var ask: String? = null,
        var diff: String? = null
)