package th.co.dv.p2p.usernotify.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("line.bot")
data class LineConfiguration (
    @Value("{#channel-token}")
    var channelToken: String? = null,
    var baseUrl: String? = null
)