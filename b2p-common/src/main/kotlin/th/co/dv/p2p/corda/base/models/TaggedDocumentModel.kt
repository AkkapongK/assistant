package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaggedDocumentModel (
        val linearId: String? = null,
        val itemLinearId: List<String>? = null,
        val externalId: String? = null,
        val knockedAmount: BigDecimal? = null
)