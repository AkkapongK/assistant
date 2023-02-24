package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

/**
 * Model for current configuration by topicId
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CurrentConfigurationModel(
    val topicConfig: String? = null,
    val ownerTaxId: String? = null,
    val counterPartyTaxId: String? = null,
    val documentType: String? = null,
    val category: String? = null,
    val configValue: List<ConfigValueModel>? = emptyList()
)