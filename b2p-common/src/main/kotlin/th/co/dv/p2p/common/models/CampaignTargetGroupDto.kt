package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Model for update CampaignTargetGroup information
 *
 * Ignore fields that contain NULL value
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CampaignTargetGroupDto(
        var campaignId: Int? = null,
        var sellerTaxId: String? = null,
        var countRemaining: Int? = null
)