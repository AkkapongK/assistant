package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Application information model
 * @property id auto generated-id
 * @property appId application id from token
 * @property sponsor sponsor name
 * @property appType type of application (possible value : UI, SYSTEM, INTERNAL)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AppInfoModel(
    val id: Long? = null,
    val appId: String? = null,
    val sponsor: String? = null,
    val appType: String? = null
)
