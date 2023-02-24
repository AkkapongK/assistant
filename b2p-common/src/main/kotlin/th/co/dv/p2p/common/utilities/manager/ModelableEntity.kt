package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.common.utilities.search.paging.PagableList

interface ModelableEntity {
    fun toModel(searchModel: Any): Any
}

fun List<ModelableEntity>.toModels(searchModel: Any): List<Any> {
    return this.map { it.toModel(searchModel) }
}

fun PagableList<ModelableEntity>.toPagableModels(searchModel: Any): PagableList<Any> {
    val pageFormat = PagableList(this.getData().toModels(searchModel) as MutableList<Any>)
    pageFormat.setTotalSize(this.getTotalSize())
    pageFormat.setPage(this.getPage())
    pageFormat.setPageSize(this.getPageSize())
    return pageFormat
}