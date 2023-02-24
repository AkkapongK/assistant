package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DoaCriteriaModel(
    val groupId: Int? = null,
    val sequence: Int? = null,
    val lowerAmount: BigDecimal? = null,
    val upperAmount: BigDecimal? = null,
    val enableSendEmail: Boolean? = null,
    val documentType: String? = null,
    val documentSubType: String? = null
)
