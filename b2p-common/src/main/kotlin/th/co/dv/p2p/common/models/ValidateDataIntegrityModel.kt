package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * This class is use to transfer data that use to validate integrity from payment service to corda interface service
 * [documentType]: type of document
 * [linearIds]: list of linear id
 * [hashingField]: fields of model that use to hash
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValidateDataIntegrityModel(
        val documentType: String? = null,
        val linearIds: List<String>? = emptyList(),
        val hashingField: List<String>? = emptyList()
)