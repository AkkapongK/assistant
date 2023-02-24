package th.co.dv.p2p.common.utilities.search.context

import th.co.dv.p2p.common.utilities.search.SearchCriterias

/**
 * Base implementation of {@link SearchContext}.
 * <p>
 * Used by default by {@link SearchContextHolder} and
 * {@link HttpSessionContextIntegrationFilter}.
 * </p>
 *
 * @author Ben Alex
 * @version $Id: SearchContextImpl.java,v 1.1 2017/11/08 03:15:40 wittawai Exp $
 */
class SearchContextImpl : SearchContext {

    private val serialVersionUID = -1594226511936935777L

    // ~ Instance fields
    // ================================================================================================

    private var searchCriterias: SearchCriterias<*>? = null

    // ~ Methods
    // ========================================================================================================

    override fun equals(other: Any?): Boolean {
        if (other is SearchContextImpl) {
            val test = other as SearchContextImpl?

            if (this.getSearchCriterias() == null && test!!.getSearchCriterias() == null) {
                return true
            }

            if (this.getSearchCriterias() != null && test!!.getSearchCriterias() != null && this.getSearchCriterias() == test.getSearchCriterias()) {
                return true
            }
        }

        return false
    }

    override fun getSearchCriterias(): SearchCriterias<*>? {
        return searchCriterias
    }

    override fun hashCode(): Int {
        return if (this.searchCriterias == null) {
            -1
        } else this.searchCriterias!!.hashCode()

    }

    override fun setSearchCriterias(searchCriterias: SearchCriterias<*>) {
        this.searchCriterias = searchCriterias
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(super.toString())

        if (this.searchCriterias == null) {
            sb.append(": Null searchCriterias")
        } else {
            sb.append(": SearchCriterias: ").append(this.searchCriterias)
        }

        return sb.toString()
    }
}