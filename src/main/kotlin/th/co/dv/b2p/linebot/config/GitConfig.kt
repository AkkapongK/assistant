package th.co.dv.b2p.linebot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("git")
data class GitConfig(
        var directory: String? = null,
        var inv: String? = null,
        var po: String? = null,
        var gr: String? = null,
        var cn: String? = null,
        var dn: String? = null,
        var payment: String? = null,
        var request: String? = null,
        var aggregate: String? = null,
        var common: String? = null
)