package th.co.dv.p2p.common.models

/**
 * Data class for keep properties of retry publish message
 */
data class RetryPropertiesModel(
        var retryPeriod: Long? = null,
        var maxRetryCount: Int? = null
)