package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ContractConditionModel(
        val linearId: String? = null,
        val contractLinearId: String? = null,
        val conditionItemNumber: String? = null,
        val conditionType: String? = null,
        val conditionPercent: BigDecimal? = null,
        val conditionDescription: String? = null,
        val createdDate: String? = null,
        val createdBy: String? = null,
        val updatedDate: String? = null,
        val updatedBy: String? = null
)