package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

/**
 * Model for knocking information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class KnockingTaggedDocumentModel(
        val knockingGroupId: String? = null,
        val linearId: String? = null,
        val type: String? = null,
        val externalId: String? = null,
        val knockedSubTotal: BigDecimal? = null,
        val knockedAmount: BigDecimal? = null
)