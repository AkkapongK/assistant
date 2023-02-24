package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserModel(
    val firstName: String? = null,
    val lastName: String? = null,
    val jobPosition: String? = null,
    val email: String? = null,
    val phone: String? = null
)
