package th.co.dv.p2p.usernotify.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("retry")
data class RetryProperties(
        var retryPeriod: Long? = null,
        var maxRetryCount: Int? = null
)
