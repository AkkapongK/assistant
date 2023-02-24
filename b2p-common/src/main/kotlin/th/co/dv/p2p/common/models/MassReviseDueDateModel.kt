package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Model for mass revise due date
 * @property linearIds: List of invoice linear id
 * @property revisedDueDate: New due date inputted by user, Format: yyyy-MM-dd (ISO8601)
 * @property revisedDueDateReason : Revise due date reason
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class MassReviseDueDateModel(
    val linearIds: List<String>,
    val revisedDueDate: String,
    val revisedDueDateReason: String
)
