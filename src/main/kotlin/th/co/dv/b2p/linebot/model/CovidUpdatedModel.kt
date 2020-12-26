package th.co.dv.b2p.linebot.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CovidUpdatedModel(
        var Confirmed: Int? = null,
        var Recovered: Int? = null,
        var Hospitalized: Int? = null,
        var Deaths: Int? = null,
        var NewConfirmed: Int? = null,
        var NewRecovered: Int? = null,
        var NewHospitalized: Int? = null,
        var NewDeaths: Int? = null,
        var UpdateDate: String? = null,
        var Source: String? = null,
        var DevBy: String? = null
)
