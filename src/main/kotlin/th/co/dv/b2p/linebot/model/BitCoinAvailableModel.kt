package th.co.dv.b2p.linebot.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseBitCoinAvailableModel(
        var error: Int? = null,
        var result : List<BitCoinAvailableModel>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BitCoinAvailableModel(
        var id: Int? = null,
        var symbol: String? = null,
        var info: String? = null
)