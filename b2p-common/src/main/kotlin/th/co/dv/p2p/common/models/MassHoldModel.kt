package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude


/**
 * Model for mass revise due date
 * @property linearIds: List of invoice linear id
 * @property lastHeldRemark: remark of hold document
 * @property lastUnheldRemark : remark of un hold document
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class MassHoldModel(
    val linearIds: List<String>,
    val lastHeldRemark: String? = null,
    val lastUnheldRemark: String? = null
)