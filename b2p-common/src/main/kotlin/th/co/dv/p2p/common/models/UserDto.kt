package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * This model transform from UserModel in b2p-user 
 * But ignore Relation with Role and AuthGroup
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
        val username: String? = null,
        val password: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val email: String? = null,
        val isActive: Boolean? = null,
        val contactEmail: String? = null,
        val telephone: String? = null,
        val telephoneCountryCode: String? = null
)
