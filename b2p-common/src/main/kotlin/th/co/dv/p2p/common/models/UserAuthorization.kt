package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.enums.LEVEL
import th.co.dv.p2p.common.utilities.AuthorizationUtils
import th.co.dv.p2p.common.utilities.AuthorizationUtils.ROLE_INTERFACE
import java.io.Serializable

/**
 * The UserAuthorization stores information about current logged in user
 *
 * @property username username of the logged in user
 * @property companiesCode companies code of the logged in user
 * @property authorities user's authorities (What can this user do)
 * @property userGroups groups of the logged in user
 */

@CordaSerializable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAuthorization(
        val username: String,
        val email: String? = null,
        val companiesCode: List<String> = emptyList(),
        val authorities: List<String> = emptyList(),
        val userGroups: List<UserGroup> = emptyList(),
        val tenants: List<String> = emptyList(),
        val sponsors: List<String> = emptyList(),
        val inActiveList: List<BuyerVendorPKModel>? = null,
        val userType: String? = null
) : Serializable {
    /**
     * To check this user can query data or not
     * */
    inline fun <reified T: Any> hasAuthorization(): Boolean {
        return this.authorities.contains(ROLE_INTERFACE)
                || AuthorizationUtils.hasAuthorization<T>(this)
    }

    /**
     * To check this user can query data or not by check state in userGroups
     */
    fun hasAuthorization(classMap: Map<Class<*>, List<LEVEL>>): Boolean {
        val hasAuthorization = classMap.any { (clazz, levels) ->
            AuthorizationUtils.hasAuthorizationByLevels(this, clazz, levels)
        }

        return this.authorities.contains(ROLE_INTERFACE) || hasAuthorization
    }
}

/**
 * The UserGroup stores information about group of user and what is the criteria for each states
 *
 * @property name group name
 * @property states map to store criteria for each state
 */
@CordaSerializable
data class UserGroup(
        val name: String,
        val states: Map<String, List<Condition>>
) : Serializable

/**
 * The Condition stores information about field, operator and value to restrict query to state
 *
 * @property field field name in state
 * @property operator operater to use in query
 * @property value value for this field
 */
@CordaSerializable
data class Condition(
        val field: String,
        val operator: String,
        val value: String
) : Serializable


