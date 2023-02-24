package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DistrictModel(

        var id:Long? = null,
        var districtCode: String? = null,
        var districtName: String? = null,
        var districtNameEN: String? = null,
        var amphurId:Int? =null,
        var geoId:Int? =null,
        var provinceId:Int? =null

)