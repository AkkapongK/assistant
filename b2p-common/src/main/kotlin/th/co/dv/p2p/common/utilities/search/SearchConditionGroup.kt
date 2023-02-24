package th.co.dv.p2p.common.utilities.search

import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.From
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate

class SearchConditionGroup : SearchCondition {

    private var searchConditions: List<SearchCondition>? = null

    private var and = false

    /* (non-Javadoc)
	 * @see th.co.dv.p2p.corda.base.web.SearchCondition#createPredicate(javax.persistence.criteria.CriteriaBuilder, javax.persistence.criteria.Path)
	 */
    override fun createPredicate(cb: CriteriaBuilder, root: Path<*>, froms: Hashtable<String, From<*, *>>): Predicate? {
        // TODO Auto-generated method stub
        return null
    }

    /**
     * @return the searchConditions
     */
    fun getSearchConditions(): List<SearchCondition>? {
        return searchConditions
    }

    /**
     * @param searchConditions the searchConditions to set
     */
    fun setSearchConditions(searchConditions: List<SearchCondition>) {
        this.searchConditions = searchConditions
    }

    /**
     * @return the and
     */
    override fun isAnd(): Boolean {
        return and
    }

    /**
     * @param and the and to set
     */
    override fun setAnd(and: Boolean) {
        this.and = and
    }

    override fun createWhere(tableName: String, tableClass: Class<*>, sc: SearchCriterias<*>): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}