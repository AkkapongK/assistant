package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import th.co.dv.p2p.common.utilities.search.paging.PagableList

class MockPurchaseOrderService: QueryableService<MockPurchaseOrder> {
    override fun findByParam(param: Map<String, Any>, operation: Map<String, String>, userAuthorization: UserAuthorization?): List<MockPurchaseOrder> {
        return listOf()
    }

    override fun native(nativeQuery: NativeQueryModel, criterias: SearchCriterias<*>): PagableList<MockPurchaseOrder> {
        return PagableList(mutableListOf())
    }

}