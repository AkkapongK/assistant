package th.co.dv.p2p.common.models

import javax.persistence.criteria.JoinType

/**
 * Model for keep Join model object that used to create join clause
 */
data class JoinModel(
        val table: Class<*>,
        val oper: JoinType = JoinType.INNER,
        val condition: String? = null
)