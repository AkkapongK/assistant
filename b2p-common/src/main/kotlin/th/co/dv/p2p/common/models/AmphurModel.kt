package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AmphurModel(
        var id: Long? = null,
        var amphurCode: String? = null,
        var amphurName: String? = null,
        var amphurNameEN: String? = null,
        var geoId: Int? = null,
        var provinceId: Int? = null

)
