package th.co.dv.p2p.common.utilities.search.paging

import th.co.dv.p2p.common.utilities.search.SearchCriterias
import javax.persistence.EntityManager
import javax.persistence.TypedQuery

class PaginationHelper {

    @Suppress("UNCHECKED_CAST")
    fun <T> fetch(criterias: SearchCriterias<*>, page: Int, pageSize: Int, orderBy: String?, asc: Boolean, entityManager: EntityManager): PagableList<T> {

        if (page > 0 && pageSize > 0) {
            criterias.setPage(page)
            criterias.setPageSize(pageSize)

            if (orderBy != null && orderBy.isNotEmpty()) {
                criterias.addSort(orderBy, asc)
            }
        }

        val query = criterias.createQuery(entityManager)

        val pList = PagableList(query.resultList as MutableList<T>)

        if (page > 0 && pageSize > 0) {
            val pageQuery = criterias.createTotalQuery(entityManager) as TypedQuery<Long>
            pList.setTotalSize(pageQuery.getSingleResult().toInt())
        }

        return pList
    }
}