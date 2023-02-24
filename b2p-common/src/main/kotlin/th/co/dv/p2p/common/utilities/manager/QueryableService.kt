package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import th.co.dv.p2p.common.utilities.search.paging.PagableList

interface QueryableService<T : ModelableEntity> {
    fun findByParam(param: Map<String, Any>, operation: Map<String, String> = emptyMap(), userAuthorization: UserAuthorization? = null): List<T>
    fun native(nativeQuery: NativeQueryModel, criterias: SearchCriterias<*>): PagableList<T>
}