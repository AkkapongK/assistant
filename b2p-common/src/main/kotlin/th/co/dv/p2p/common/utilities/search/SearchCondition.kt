package th.co.dv.p2p.common.utilities.search

import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.From
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate

interface SearchCondition {
    fun createPredicate(cb: CriteriaBuilder, root: Path<*>, froms: Hashtable<String, From<*, *>>): Predicate?
    fun isAnd(): Boolean
    fun setAnd(and: Boolean)
    fun createWhere(tableName: String, tableClass: Class<*>, sc: SearchCriterias<*>): String?
}