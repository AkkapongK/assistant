package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

/**
 * Model for CNReferToInvoiceElastic from aggregate service
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CNReferToInvoiceModel(
        val linearId: String? = null,
        val cnNo: String? = null,
        val cnDate: String? = null,
        val cnReason: String? = null,
        val cnAmount: BigDecimal? = null,
        val currency: String? = null,
        val cnStatus: String? = null
)