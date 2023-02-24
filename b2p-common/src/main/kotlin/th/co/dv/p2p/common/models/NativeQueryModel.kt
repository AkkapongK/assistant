package th.co.dv.p2p.common.models

import javax.persistence.criteria.JoinType

/**
 * Class contain property used to create native query
 *
 * @param customSelect: Specific selection in sql
 * @param fromClause: from clause
 * @param groupBy: group by clause
 * @param having: having clause
 * @param orderBy: order by clause
 * @param offset: offset clause
 * @param fetch: fetch clause
 * @param param: Parameter that keep key mapping with value
 * @param operation: Operation that keep key mapping with operation
 * @param fields: The field that map with select query (in order)
 * @param userAuth: User Authorization to access the data
 * @param byPassAuth: flag to indicate that need to check authorization or not
 * @param joinExtraTable: flag to indicate that need to join extra table or not
 * @param queryItem: flag to indicate that this is for item level query
 * @param customJoin: custom join to be added after generated from clause
 */
data class NativeQueryModel(
        val customSelect: String = "",
        val fromClause: String = "",
        val groupBy: String = "",
        val having: String = "",
        val defaultWhere: String = "",
        val orderBy: String = "",
        val offset: String = "",
        val fetch: String = "",
        val fields: List<String> = emptyList(),
        val userAuth: UserAuthorization? = null,
        val param: Map<String, Any> = mapOf(),
        val operation: Map<String, String> = mapOf(),
        val subQueryValue: MutableList<List<Any?>> = mutableListOf(),
        val byPassAuth: Boolean = false,
        val queryTotalRecords: Boolean = true,
        val node: String? = null,
        val joinExtraTable: Boolean = false,
        val queryItem: Boolean = false,
        val customJoin: String? = null,
        val joinType: JoinType = JoinType.INNER
)