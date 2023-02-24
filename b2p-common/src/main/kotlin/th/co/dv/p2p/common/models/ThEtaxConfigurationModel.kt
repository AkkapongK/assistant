package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.sql.Date

/**
 * This model use for configuration e-tax
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ThEtaxConfigurationModel(
        val configOption: String? = null,

        val value: String? = null,

        val sellerTaxId: String? = null,

        val updatedDate: Date? = null,

        val updatedBy: String? = null
)