package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProvinceModel(
        var provinceId:Long? = null,
        var provinceCode: String? = null,
        var provinceName: String? = null,
        var provinceNameEN: String? = null,
        var geoId:Int? = null
)
