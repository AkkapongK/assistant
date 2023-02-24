package th.co.dv.p2p.common.utilities.search.paging

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import javax.persistence.EntityManager

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
class PagableList<T> : List<T> {

    override var size: Int = 0

    @JsonProperty
    private var page: Int = 0
    private var pageSize: Int = 0
    private var orderBy: String? = null
    private var asc: Boolean = false
    private var totalSize: Int = 0
    private var list: MutableList<T> = mutableListOf()
    private var entityManager: EntityManager? = null
    private var searchCriterias: SearchCriterias<T>? = null
    private var isDeferFetch: Boolean = false

    constructor(list: MutableList<T>) {
        this.list = list
        pageSize = list.size
        size = list.size
    }

    constructor(searchCriterias: SearchCriterias<T>, entityManager: EntityManager) {
        this.searchCriterias = searchCriterias
        this.entityManager = entityManager
        this.isDeferFetch = true
    }


    /**
     * @return the data
     */
    @Suppress("UNCHECKED_CAST")
    fun getData(): MutableList<T> {
        if (this.isDeferFetch && searchCriterias != null && entityManager != null) {

            val pageList: PagableList<T> = if (this.pageSize > 0) {
                PaginationHelper().fetch(searchCriterias!!, this.page, this.pageSize, orderBy, asc, entityManager!!)
            } else {
                val query = searchCriterias!!.createQuery(entityManager!!)
                PagableList(query.resultList) as PagableList<T>
            }

            this.setData(pageList.getData())
            this.setTotalSize(pageList.getTotalSize())

        }

        return this.list
    }

    /**
     * @param data the data to set
     */
    fun setData(data: MutableList<T>) {
        this.list = data
    }

    /**
     * @return the pageSize
     */
    fun getPageSize(): Int {
        return pageSize
    }

    /**
     * @param pageSize the pageSize to set
     */
    fun setPageSize(pageSize: Int) {
        this.pageSize = pageSize
    }

    /**
     * @return the totalSize
     */
    fun getTotalSize(): Int {
        return totalSize
    }

    /**
     * @param totalSize the totalSize to set
     */
    fun setTotalSize(totalSize: Int) {
        this.totalSize = totalSize
    }

    /**
     * @return the partialList
     */
    fun getPartialList(): Boolean {
        return totalSize != list.size
    }

    fun getPage(): Int {
        return page
    }

    fun setPage(page: Int) {
        this.page = page
    }

    fun getOrderBy(): String? {
        return orderBy
    }

    fun setOrderBy(orderBy: String) {
        this.orderBy = orderBy
    }

    fun isAsc(): Boolean {
        return asc
    }

    fun setAsc(asc: Boolean) {
        this.asc = asc
    }

    fun isDeferFetch(): Boolean {
        return isDeferFetch
    }

    fun setDeferFetch(isDeferFetch: Boolean) {
        this.isDeferFetch = isDeferFetch
    }

    fun add(index: Int, element: T) {
        list.add(index, element)
    }

    fun add(o: T): Boolean {
        return list.add(o)
    }

    fun addAll(c: Collection<T>): Boolean {
        return list.addAll(c)
    }

    fun addAll(index: Int, c: Collection<T>): Boolean {
        return list.addAll(index, c)
    }

    fun clear() {
        list.clear()
    }

    /**
     * @param element
     * @return
     * @see List.contains
     */
    override operator fun contains(element: T): Boolean {
        return list.contains(element)
    }

    /**
     * @param elements
     * @return
     * @see List.containsAll
     */
    override fun containsAll(elements: Collection<T>): Boolean {
        return list.containsAll(elements)
    }

    /**
     * @param other
     * @return
     * @see List.equals
     */
    override fun equals(other: Any?): Boolean {
        return list == other
    }

    /**
     * @param index
     * @return
     * @see List.get
     */
    override fun get(index: Int): T {
        return list[index]
    }

    /**
     * @return
     * @see List.hashCode
     */
    override fun hashCode(): Int {
        return list.hashCode()
    }

    /**
     * @param element
     * @return
     * @see List.indexOf
     */
    override fun indexOf(element: T): Int {
        return list.indexOf(element)
    }

    /**
     * @return
     * @see List.isEmpty
     */
    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    /**
     * @return
     * @see List.iterator
     */
    override fun iterator(): Iterator<T> {
        return list.iterator()
    }

    /**
     * @param element
     * @return
     * @see List.lastIndexOf
     */
    override fun lastIndexOf(element: T): Int {
        return list.lastIndexOf(element)
    }

    /**
     * @return
     * @see List.listIterator
     */
    override fun listIterator(): ListIterator<T> {
        return list.listIterator()
    }

    /**
     * @param index
     * @return
     * @see List.listIterator
     */
    override fun listIterator(index: Int): ListIterator<T> {
        return list.listIterator(index)
    }

    fun remove(o: T): Boolean {
        return list.remove(o)
    }

    fun removeAll(c: Collection<T>): Boolean {
        return list.removeAll(c)
    }

    fun retainAll(c: Collection<T>): Boolean {
        return list.retainAll(c)
    }

    operator fun set(index: Int, element: T): T {
        return list.set(index, element)
    }

    /**
     * @param fromIndex
     * @param toIndex
     * @return
     * @see List.subList
     */
    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        return list.subList(fromIndex, toIndex)
    }

    override fun toString(): String {
        return "{page: $page, pageSize: $pageSize, totalSize: $totalSize, list: $list}"
    }

}