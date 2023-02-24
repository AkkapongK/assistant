package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.*

/**
 * Model that contains every fields of
 * [campaign] [campaign_target_group] [advertising_campaign] [financing_campaign]
 * Used for getting result from campaign native query and be an output of campaign api
 *
 * Ignore fields that have NULL value
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CampaignDto(
        var id: Int? = null,

        var code: String? = null,

        var name: String? = null,

        var description: String? = null,

        var type: String? = null,

        var owner: String? = null,

        var startDate: Date? = null,

        var endDate: Date? = null,

        var sellerTaxId: String? = null,

        var countRemaining: Int? = null,

        var bannerImageUrl: String? = null,

        var popupImageUrl: String? = null,

        var advertisingUrl: String? = null,

        var sponsor: String? = null,

        var maxFinancedAmount: BigDecimal? = null,

        var maxFreeTrial: Int? = null
)