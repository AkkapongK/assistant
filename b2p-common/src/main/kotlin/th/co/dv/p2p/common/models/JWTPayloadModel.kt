package th.co.dv.p2p.common.models

/**
 * Class for keep value from jwt payload
 * and we use for get client_id value
 * to define priority of transaction
 */
data class JWTPayloadModel(
        val exp: String? = null,
        val user_name: String? = null,
        val authorities: List<String> = emptyList(),
        val jti: String? = null,
        val client_id: String? = null,
        val scope: List<String> = emptyList()
)