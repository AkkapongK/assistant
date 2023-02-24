package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class CompanyModel (
        var taxId: String? = null,
        var name: String? = null,
        var nameTH: String? = null,
        var nameEN: String? = null,
        var legalName: String? = null,
        var whtFormType: String? = null,

        @get:JsonProperty("isActive")
        var isActive: Boolean? = null,
        
        @get:JsonProperty("isForeign")
        var isForeign: Boolean? = null,

        val createdDate: Date? = null,
        val createdBy: String? = null,
        val updatedDate: Date? = null,
        val updatedBy: String? = null,
        var companyBranches: MutableList<CompanyBranchModel> = mutableListOf()
)