package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

/**
 * Model for DNReferToInvoiceElastic from aggregate service
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DNReferToInvoiceModel(
        val linearId: String? = null,
        val dnNo: String? = null,
        val dnDate: String? = null,
        val dnReason: String? = null,
        val dnAmount: BigDecimal? = null,
        val currency: String? = null,
        val dnStatus: String? = null
)