package th.co.dv.b2p.linebot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("line.bot")
data class LineConfiguration (
    @Value("{#channel-token}")
    var channelToken: String? = null,
    var broadcaster: MutableMap<String, String> = mutableMapOf(),
    var baseUrl: String? = null
)