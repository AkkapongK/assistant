package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.*

data class TaxModel(
        var companyTaxId: String? = null,
        var taxCode: String? = null,
        var taxType: String? = null,
        var taxRate: BigDecimal? = null,
        var taxDescription: String? = null,
        var incomeType: String? = null,
        var incomeDescription: String? = null,
        @get:JsonProperty("isEwht")
        var isEwht: Boolean? = null,
        var taxTriggerPoint: String? = null,
        var rdTaxType: String? = null,
        var createdDate: String? = null,
        var updatedDate: String? = null
)