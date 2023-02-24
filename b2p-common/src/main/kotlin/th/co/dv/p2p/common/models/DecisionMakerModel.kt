package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Model for decision maker contact
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DecisionMakerModel (
    val companyTaxId: String? = null,
    val decisionFirstName: String? = null,
    val decisionLastName: String? = null,
    val decisionEmail: String? = null,
    val decisionPhone: String? = null,
    val decisionPosition: String? = null,
    val updatedDate: String? = null
)