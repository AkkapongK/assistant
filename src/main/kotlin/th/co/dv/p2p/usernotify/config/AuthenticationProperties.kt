package th.co.dv.p2p.usernotify.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("auth")
data class AuthenticationProperties(
    var accessTokenUri: String? = null,
    var clientId: String? = null,
    var clientSecret: String? = null,
    var timeout: Int = 60 * 3,
    var expiryThreshold: Int = 5
)
