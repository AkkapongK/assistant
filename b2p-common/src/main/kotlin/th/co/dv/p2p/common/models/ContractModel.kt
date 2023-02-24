package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ContractModel (
    val linearId: String? = null,
    val sponsor: String? = null,
    val contractNumber: String? = null,
    val retentionRate: BigDecimal? = null,
    val initialRetentionCeilingAmount: BigDecimal? = null,
    val remainingRetentionCeilingAmount: BigDecimal? = null,
    val advanceToBeDeducted: BigDecimal? = null,
    val accumulateAdvanceRedeem: BigDecimal? = null,
    val accumulateAdvanceDeduction: BigDecimal? = null,
    val accumulateRetentionAmount: BigDecimal? = null,
    val advancePercent: BigDecimal? = null,
    val advanceDescription: String? = null,
    val retentionDescription: String? = null,
    val retentionCeilingDescription: String? = null,
    val contractConditions: List<ContractConditionModel>? = emptyList(),
    val createdDate: String? = null,
    val createdBy: String? = null,
    val updatedDate: String? = null,
    val updatedBy: String? = null
)