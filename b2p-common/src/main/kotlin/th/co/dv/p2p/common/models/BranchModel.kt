package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class BranchModel(
    val branchCode: String? = null,
    val branchName: String? = null,
    val address: String? = null,
    val district: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    var addressTH: String? = null,
    var districtTH: String? = null,
    var cityTH: String? = null,
    var postalCodeTH: String? = null
)
