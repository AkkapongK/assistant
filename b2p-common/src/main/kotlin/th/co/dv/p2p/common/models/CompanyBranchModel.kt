package th.co.dv.p2p.common.models

import java.util.*

data class CompanyBranchModel (

        var taxId: String? = null,
        var branchCode: String? = null,
        var branchName: String? = null,
        var address: String? = null,
        var street: String? = null,
        var street2: String? = null,
        var district: String? = null,
        var city: String? = null,
        var postalCode: String? = null,
        var addressTH: String? = null,
        var districtTH: String? = null,
        var cityTH: String? = null,
        var postalCodeTH: String? = null,
        var country: String? = null,
        var buildingNumber: String? = null,
        var subDistrictCode: String? = null,
        var districtCode: String? = null,
        var cityCode: String? = null,
        var origin: String? = null,
        var createdBy: String? = null,
        val createdDate: Date? = null,
        val updatedBy: String? = null,
        val updatedDate: Date? = null
)