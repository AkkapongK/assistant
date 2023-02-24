package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Model to display transaction log
 * The transaction log used for monitoring data added to our system
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TransactionLogModel<T: Any>(
        val status: String? = null,
        val message: String? = null,
        val uploadedDate: String? = null,
        val data: T? = null
)