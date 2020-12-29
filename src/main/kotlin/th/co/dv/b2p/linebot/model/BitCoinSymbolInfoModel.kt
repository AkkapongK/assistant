package th.co.dv.b2p.linebot.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BitCoinSymbolInfoModel(
        var id: Int? = null,
        var symbol: String? = null,
        var last: BigDecimal? = null,
        var lowestAsk: BigDecimal? = null,
        var highestBid: BigDecimal? = null,
        var percentChange: BigDecimal? = null,
        var baseVolume: BigDecimal? = null,
        var quoteVolume: BigDecimal? = null,
        var isFrozen: Int? = null,
        var high24hr: BigDecimal? = null,
        var low24hr: BigDecimal? = null,
        var change: BigDecimal? = null,
        var prevClose: BigDecimal? = null,
        var prevOpen: BigDecimal? = null,
        var info: String? = null
)