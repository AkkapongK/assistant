package th.co.dv.p2p.corda.common.models

import net.corda.core.serialization.CordaSerializable

/**
 * The UserAuthorization stores information about current logged in user
 *
 * @property username username of the logged in user
 * @property companiesCode companies code of the logged in user
 * @property authorities user's authorities (What can this user do)
 * @property userGroups groups of the logged in user
 */

@CordaSerializable
data class UserAuthorization(
        val username: String,
        val companiesCode: List<String> = emptyList(),
        val authorities: List<String> = emptyList(),
        val userGroups: List<UserGroup> = emptyList()
)

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
)

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
)


