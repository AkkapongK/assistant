package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class CountryModel(
        var ccFips:String? =null,
        var ccIso: String? = null,
        var tId: String? = null,
        var countryName: String? = null
)
