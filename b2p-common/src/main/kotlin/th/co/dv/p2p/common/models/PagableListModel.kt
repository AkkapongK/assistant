package th.co.dv.p2p.common.models

data class PagableListModel<T>(
        var rows: List<T>? = emptyList(),
        var page: Int? = 0,
        var pageSize: Int? = 0,
        var totalRecords: Int? = 0
)
