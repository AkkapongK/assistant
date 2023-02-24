package th.co.dv.p2p.common.models

import java.util.*

data class FinancingConfigurationModel (
        var buyerTaxId: String? = null,
        var vendorTaxId: String? = null,
        var bankCode: String? = null,
        var financingProduct: String? = null,
        var allowFinancing: Boolean? = null,
        var createdBy: String? = null,
        val createdDate: Date? = null,
        var updatedBy: String? = null,
        val updatedDate: Date? = null
)
