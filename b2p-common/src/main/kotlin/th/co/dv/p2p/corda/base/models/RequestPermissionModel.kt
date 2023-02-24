package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable


/**
 * Data class for mapping the response that request for permission of the request
 * from the off-chain server to object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestPermissionModel(
        val fieldId: Long,
        val field: String,
        val level: String,
        val taxId: String? = null,
        val required: Boolean,
        val editable: Boolean,
        val displayName: String? = null,
        val defaultValue: String? = null)