package th.co.dv.p2p.common.models

/**
 * Model for receiving types and subtypes of request from configuration service.
 */
data class RequestSubTypeModel(
        val requestTypeId: Long? = null,
        val type: String? = null,
        val subtype: String? = null,
        val initiator: String? = null,
        val typeDisplayName: String? = null,
        val subtypeDisplayName: String? = null,
        val subtypeDescription: String? = null,
        val conversionStep: String? = null,
        val targetLifecycle: String? = null
)