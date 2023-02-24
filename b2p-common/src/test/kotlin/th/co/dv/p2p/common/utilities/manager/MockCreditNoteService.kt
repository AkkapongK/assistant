package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import th.co.dv.p2p.common.utilities.search.paging.PagableList

class MockCreditNoteService : QueryableService<MockCreditNote> {
    override fun findByParam(param: Map<String, Any>, operation: Map<String, String>, userAuthorization: UserAuthorization?): List<MockCreditNote> {
        return listOf()
    }

    override fun native(nativeQuery: NativeQueryModel, criterias: SearchCriterias<*>): PagableList<MockCreditNote> {
        return PagableList(mutableListOf())
    }

}