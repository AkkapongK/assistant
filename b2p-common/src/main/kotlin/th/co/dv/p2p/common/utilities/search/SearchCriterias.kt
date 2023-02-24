package th.co.dv.p2p.common.utilities.search

import org.hibernate.annotations.Formula
import th.co.dv.p2p.common.annotations.FormulaId
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.utilities.DateUtility
import java.lang.reflect.ParameterizedType
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.Tuple
import javax.persistence.criteria.*

class SearchCriterias<T>(private var rootObject: Class<T>) : SearchCondition {

    private var criterias: MutableList<SearchCondition> = mutableListOf()

    private var criteria = SearchCriteria()

    private var sort: MutableList<OrderBy>? = null

    private var first = 0

    private var pageSize = 10

    private var paging = true

    private var asc = true

    private var and = true

    private var distinct = true

    private var select: List<String>? = null
    private var selectClause: String? = null
    private var fromClause: String? = null
    private var defaultWhere: String? = null
    private var groupByClause: String? = null
    private var havingClause: String? = null
    private var orderBy: String? = null
    private var offset: String? = null
    private var fetch: String? = null
    private var parameterValue: MutableList<Any?> = mutableListOf()
    private var subQueryParameterValue: MutableList<MutableList<Any?>> = mutableListOf()
    private var applyParameterValue = true

    fun getCriteriaSize(): Int {
        return criterias.size
    }

    /**
     * @return the criterias
     */
    fun getCriterias(): MutableList<SearchCondition> {
        return criterias
    }

    /**
     * @param criterias the criterias to set
     */
    fun setCriterias(criterias: List<SearchCondition>) {
        this.criterias = criterias.toMutableList()
    }

    fun createCondition(): String {
        return createCondition("")
    }


    fun createCondition(prefix: String?): String {

        val tableName = if (prefix.isNullOrEmpty().not()) "$prefix." else ""

        val sb = StringBuilder()

        criterias.filterIsInstance<SearchCriteria>().forEachIndexed { index, searchCondition ->
            sb.append(" where")

            when {
                index != 0 && searchCondition.isAnd() -> sb.append(" AND")
                index != 0 && searchCondition.isAnd().not() -> sb.append(" OR")
            }

            val criteriaIsList = searchCondition.getValue() is List<*>
            val criteriaIsDate = searchCondition.getType()!!.equals("Date", ignoreCase = true)
            val criteriaIsInOperation = searchCondition.getOp() == SearchCriteriaOperation.IN
            val criteriaIsNotInOperation = searchCondition.getOp() == SearchCriteriaOperation.NOT_IN
            val criteriaIsBetweenOpr = searchCondition.getOp() == SearchCriteriaOperation.BETWEEN
            val criteriaNotContainInOpr = (criteriaIsInOperation || criteriaIsNotInOperation).not()

            when {

                criteriaIsList.not() && criteriaIsInOperation -> {
                    searchCondition.setOp(SearchCriteriaOperation.EQUAL)
                    sb.append(" " + tableName + searchCondition.getField() + searchCondition.getOp() + ":" + searchCondition.getField() + index)
                }

                criteriaIsList.not() && criteriaIsNotInOperation -> {
                    searchCondition.setOp(SearchCriteriaOperation.NOT_EQUAL)
                    sb.append(" " + tableName + searchCondition.getField() + searchCondition.getOp() + ":" + searchCondition.getField() + index)
                }

                criteriaIsDate && criteriaIsBetweenOpr -> {
                    sb.append(" " + tableName + searchCondition.getField() + searchCondition.getOp() + ":" + searchCondition.getField() + index + "s and :" + searchCondition.getField() + index + "e")
                }

                criteriaNotContainInOpr && criteriaIsDate.not() && criteriaIsBetweenOpr.not() -> {
                    sb.append(" " + tableName + searchCondition.getField() + searchCondition.getOp() + ":" + searchCondition.getField() + index)
                }

            }

        }

        return sb.toString()
    }

    fun setParameter(query: Query) {
        var index = 0
        for (cond in criterias) {
            if (cond is SearchCriteria) {
                val fieldName = cond.getField()!! + index++
                setParameter(query, cond, fieldName)
            }
        }
    }

    private fun setParameter(query: Query, c: SearchCriteria, inputFieldName: String) {

        val fieldName = if (null != c.getPath()) {
            c.getPath() + "." + inputFieldName
        } else {
            inputFieldName
        }

        when {
            c.getType()!!.equals("String", ignoreCase = true) -> query.setParameter(fieldName, c.getValue()!!.toString())
            c.getType()!!.equals("boolean", ignoreCase = true) -> query.setParameter(fieldName, c.getValue()!!.toString().toBoolean())
            c.getType()!!.equals("long", ignoreCase = true) -> query.setParameter(fieldName, c.getValue()!!.toString().toLong())
            c.getType()!!.equals("double", ignoreCase = true) -> query.setParameter(fieldName, c.getValue()!!.toString().toDouble())
            c.getType()!!.equals("Date", ignoreCase = true) -> {

                val dateFormat = SimpleDateFormat("MM/dd/yyyy")
                dateFormat.isLenient = false

                val date = try {
                    dateFormat.parse(c.getValue()!!.toString())
                } catch (e: ParseException) {
                    e.printStackTrace()
                    null
                }

                if (date != null) {
                    val cal = Calendar.getInstance(Locale.US)
                    cal.time = date
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    query.setParameter(fieldName + "s", cal.time)

                    cal.add(Calendar.DATE, 1)
                    query.setParameter(fieldName + "e", cal.time)
                }
            }
        }

    }

    fun createTotalQuery(em: EntityManager): Query {
        return createQuery(em, true)
    }

    fun createQuery(em: EntityManager): Query {
        return createQuery(em, false)
    }

    /**
     * Function to create select criteria
     */
    private fun criteriaQuerySelect(cb: CriteriaBuilder): Pair<CriteriaQuery<Tuple>, Hashtable<String, From<*, *>>> {

        val cqSelect = cb.createTupleQuery()
        val root = cqSelect.from(rootObject)
        var froms = Hashtable<String, From<*, *>>()

        val tupleList = ArrayList<Selection<*>>()

        select!!.forEach { selectItem ->

            val (from, newHashtable, field) = buildFromClause(froms, root, selectItem)

            froms = newHashtable
            val tuple = from.get<Any>(field)
            tuple?.let { tupleList.add(it) }

        }

        if (null == sort)
            addSort(select!![0], true)

        cqSelect.multiselect(tupleList)
        cqSelect.distinct(distinct)

        return Pair(cqSelect, froms)

    }

    /**
     * Function to build join clause
     *
     */
    private fun buildFromClause(froms: Hashtable<String, From<*, *>>, root: Root<*>, str: String): Triple<From<*, *>, Hashtable<String, From<*, *>>, String> {

        var list = str.split(DOT)

        val field = list.last()

        list = list.dropLast(1)

        var from: From<*, *> = root

        list.forEach {
            when (froms.containsKey(it)) {

                true -> from = froms[it]!!

                false -> {
                    from = from.join<Any, Any>(it, JoinType.LEFT)
                    froms[it] = from
                }
            }
        }

        return Triple(from, froms, field)

    }

    /**
     * Method get get expression from
     * 1. Field that have Formula annotation
     * 2. ID
     */
    private fun getExpressionForDistinctCount(root: Root<*>, rootObject: Class<*>): Path<*> {
        val formulaId = rootObject.declaredFields.firstOrNull {
            it.getAnnotation(Formula::class.java) != null && it.getAnnotation(FormulaId::class.java) != null
        }
        return if (formulaId != null)
            root.get<Path<*>>(formulaId.name)
        else root
    }

    fun createQuery(em: EntityManager, isTotal: Boolean): Query {
        val cb = em.criteriaBuilder
        var cq = cb.createQuery(rootObject) as CriteriaQuery<*>
        val cqTotal = cb.createQuery(Long::class.java)
        val root: Root<*>
        var froms = Hashtable<String, From<*, *>>()

        when {

            isTotal && distinct -> {
                root = cqTotal.from(rootObject)
                cqTotal.select(cb.countDistinct(getExpressionForDistinctCount(root, rootObject)))
            }

            isTotal && distinct.not() -> {
                root = cqTotal.from(rootObject)
                cqTotal.select(cb.count(root))
            }

            isTotal.not() && select != null -> {
                root = cq.from(rootObject)
                val (criteriaQuery, newHashtable) = criteriaQuerySelect(cb)
                cq = criteriaQuery
                froms = newHashtable
            }

            else -> {
                root = cq.from(rootObject)
                cq.distinct(distinct)
            }

        }

        var predicate: Predicate? = null

        criterias.forEach { cond ->

            var from: From<*, *> = root
            if ((cond is SearchCriteria) && (null != cond.getPath())) {
                val (updateFrom, newHashtable, _) = buildFromClause(froms, root, cond.getPath()!! + DOT + cond.getField())
                from = updateFrom
                froms = newHashtable
            }

            val p = cond.createPredicate(cb, from, froms)

            when {

                predicate == null -> predicate = cb.and(p)
                predicate != null && cond.isAnd() -> predicate = cb.and(predicate, p)
                predicate != null && cond.isAnd().not() -> predicate = cb.and(predicate, p)

            }

        }


        if (isTotal) {

            predicate?.let { cqTotal.where(it) }
            return em.createQuery(cqTotal)

        }

        predicate?.let { cq.where(it) }

        sort?.let {
            it.forEach { order ->

                val (updateFrom, _, field) = buildFromClause(froms, root, order.getField()!!)

                when (order.isAsc()) {

                    true -> cq.orderBy(cb.asc(updateFrom.get<Any>(field)))
                    false -> cq.orderBy(cb.desc(updateFrom.get<Any>(field)))
                }

            }
        }

        val query = em.createQuery(cq)

        if (paging) {
            query.setFirstResult(first)
            query.setMaxResults(pageSize)
        }

        query.setHint("org.hibernate.cacheable", true)

        return query
    }

    fun createSelectAllQuery(em: EntityManager, classx: Class<*>): Query {
        return createSelectAllQuery(em, classx, "id")
    }

    fun createSelectAllQuery(em: EntityManager, classx: Class<*>, sort: String?): Query {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(classx)
        val root = cq.from(classx)

        if (null != sort) {
            try {
                cq.orderBy(cb.asc(root.get<Any>(sort)))
            } catch (e: Exception) {
                /**
                 * Not Implement yet
                 */
            }

        }

        val query = em.createQuery(cq)

        query.setHint("org.hibernate.cacheable", true)

        return query
    }

    /**
     * @return the criteria
     */
    fun getCriteria(): SearchCriteria {
        return criteria
    }

    /**
     * @param criteria the criteria to set
     */
    fun setCriteria(criteria: SearchCriteria) {
        this.criteria = criteria
    }

    /**
     * @return the page
     */
    fun getPage(): Int {
        return first / pageSize + 1
    }

    /**
     * @param page the page to set
     */
    fun setPage(page: Int) {
        this.first = (page - 1) * pageSize
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
     * @return the paging
     */
    fun isPaging(): Boolean {
        return paging
    }

    /**
     * @param paging the paging to set
     */
    fun setPaging(paging: Boolean) {
        this.paging = paging
    }

    /**
     * @return the first
     */
    fun getFirst(): Int {
        return first
    }

    /**
     * @param first the first to set
     */
    fun setFirst(first: Int) {
        this.first = first
    }

    /**
     * @return the sorts
     */
    fun getSort(): List<OrderBy>? {
        return sort
    }

    /**
     * @param sorts the sorts to set
     */
    fun setSort(sorts: MutableList<OrderBy>) {
        this.sort = sorts
    }

    fun addSort(field: String, asc: String) {
        if (null == sort) {
            sort = ArrayList()
        }
        sort!!.add(OrderBy(field, asc))
    }

    fun addSort(field: String, asc: Boolean) {
        if (null == sort) {
            sort = ArrayList()
        }
        sort!!.add(OrderBy(field, asc))
    }

    fun addSort(field: String, asc: Boolean, index: Int) {
        if (null == sort || sort!!.size < index) {
            addSort(field, asc)
        }
        sort!!.add(index, OrderBy(field, asc))
    }


    /**
     * @return the asc
     */
    fun isAsc(): Boolean {
        return asc
    }

    /**
     * @param asc the asc to set
     */
    fun setAsc(asc: Boolean) {
        this.asc = asc
    }

    fun addConditionPeriodDate(path: String, from: Any?, to: Any?) {
        if (null != from && null != to) {
            val scDate = SearchCriterias(rootObject::class.java)
            /*
			Case 1     O---------O
			Case 2				           O---------O
			Case 3               O---------O
			Case 4     O----------------------------O
					      From                 To
		*/
            //Case 1
            val scDateCase1 = SearchCriterias(rootObject::class.java)
            var c = SearchCriteria()
            c.setPath(path)
            c.setField("dateFrom")
            c.setOp(SearchCriteriaOperation.BETWEEN)
            c.setValue(DateUtility.getStartDate(from as Date))
            c.setValue2(DateUtility.getEndDate(to as Date))
            scDateCase1.setAnd(false)
            scDateCase1.getCriterias().add(c)

            scDate.getCriterias().add(scDateCase1)

            //Case 2
            val scDateCase2 = SearchCriterias(rootObject::class.java)
            c = SearchCriteria()
            c.setPath(path)
            c.setField("dateTo")
            c.setOp(SearchCriteriaOperation.BETWEEN)
            c.setValue(DateUtility.getStartDate(from))
            c.setValue2(DateUtility.getEndDate(to))
            scDateCase2.setAnd(false)
            scDateCase2.getCriterias().add(c)

            scDate.getCriterias().add(scDateCase2)

            //Case 3
            val scDateCase3 = SearchCriterias(rootObject::class.java)
            c = SearchCriteria()
            c.setPath(path)
            c.setField("dateFrom")
            c.setOp(SearchCriteriaOperation.GREATERTHAN_OR_EQUAL)
            c.setValue(DateUtility.getStartDate(from))
            scDateCase3.getCriterias().add(c)

            c = SearchCriteria()
            c.setPath(path)
            c.setField("dateTo")
            c.setOp(SearchCriteriaOperation.LESSTHAN_OR_EQUAL)
            c.setValue(DateUtility.getEndDate(to))
            scDateCase3.setAnd(false)
            scDateCase3.getCriterias().add(c)

            scDate.getCriterias().add(scDateCase3)

            //Case 4
            val scDateCase4 = SearchCriterias(rootObject::class.java)
            c = SearchCriteria()
            c.setPath(path)
            c.setField("dateFrom")
            c.setOp(SearchCriteriaOperation.LESSTHAN_OR_EQUAL)
            c.setValue(DateUtility.getStartDate(from))
            scDateCase4.getCriterias().add(c)

            c = SearchCriteria()
            c.setPath(path)
            c.setField("dateTo")
            c.setOp(SearchCriteriaOperation.GREATERTHAN_OR_EQUAL)
            c.setValue(DateUtility.getEndDate(to))
            scDateCase4.setAnd(false)
            scDateCase4.getCriterias().add(c)

            scDate.getCriterias().add(scDateCase4)

            getCriterias().add(scDate)
        } else if (null != from && null == to) {
            var c = SearchCriteria()
            c.setPath(path)
            c.setField("dateFrom")
            c.setOp(SearchCriteriaOperation.LESSTHAN_OR_EQUAL)
            c.setValue(DateUtility.getStartDate(from as Date))
            getCriterias().add(c)

            c = SearchCriteria()
            c.setPath(path)
            c.setField("dateTo")
            c.setOp(SearchCriteriaOperation.GREATERTHAN_OR_EQUAL)
            c.setValue(DateUtility.getStartDate(from))
            getCriterias().add(c)
        } else if (null == from && null != to) {
            var c = SearchCriteria()
            c.setPath(path)
            c.setField("dateFrom")
            c.setOp(SearchCriteriaOperation.LESSTHAN_OR_EQUAL)
            c.setValue(DateUtility.getEndDate(to as Date))
            getCriterias().add(c)

            c = SearchCriteria()
            c.setPath(path)
            c.setField("dateTo")
            c.setOp(SearchCriteriaOperation.GREATERTHAN_OR_EQUAL)
            c.setValue(DateUtility.getEndDate(to))
            getCriterias().add(c)
        }
    }

    fun addCondition(field: String, operation: String, value: Any) {
        val c = SearchCriteria()
        c.setField(field)
        c.setOp(operation)
        c.setValue(value)
        getCriterias().add(c)
    }

    fun addCondition(path: String, field: String, operation: SearchCriteriaOperation, value: Any) {
        addCondition(path, field, operation, value, true)
    }

    fun addCondition(path: String, field: String, operation: SearchCriteriaOperation, value: Any, isAnd: Boolean) {
        val c = SearchCriteria()
        c.setPath(path)
        c.setField(field)
        c.setOp(operation)
        c.setValue(value)
        c.setAnd(isAnd)
        getCriterias().add(c)
    }

    fun addCondition(field: String, operation: SearchCriteriaOperation, value: Any) {
        if (!field.contains(".")) {
            addCondition(field, operation, value, true)
        } else {
            val idx = field.lastIndexOf(".")
            addCondition(field.substring(0, idx), field.substring(idx + 1), operation, value, true)
        }
    }

    fun addCondition(field: String, operation: SearchCriteriaOperation, value: Any, isAnd: Boolean) {
        val c = SearchCriteria()
        c.setField(field)
        c.setOp(operation)
        c.setValue(value)
        c.setAnd(isAnd)
        getCriterias().add(c)
    }

    fun addCondition(field: String, operation: SearchCriteriaOperation, value: Any, value2: Any, isAnd: Boolean) {
        val c = SearchCriteria()
        if (!field.contains(".")) {
            c.setField(field)
        } else {
            val idx = field.lastIndexOf(".")
            c.setField(field.substring(idx + 1))
            c.setPath(field.substring(0, idx))
        }
        c.setOp(operation)
        c.setValue(value)
        c.setValue2(value2)
        c.setAnd(isAnd)
        getCriterias().add(c)
    }

    /**
     * @return the rootObject
     */
    fun getRootObject(): Class<*> {
        return rootObject
    }

    override fun createPredicate(cb: CriteriaBuilder, root: Path<*>, froms: Hashtable<String, From<*, *>>): Predicate? {
        //Root<?> root = cq.from(rootObject);

        var finalPredicate: Predicate? = null

        criterias.forEach { searchCondition ->

            val eachPredicate = when (searchCondition) {

                is SearchCriteria -> {

                    var from = root as From<*, *>
                    if (searchCondition.getPath() != null) {

                        val (generatedFrom, updatedList) = createFromClause(searchCondition, root, froms)
                        from = generatedFrom
                        froms.putAll(updatedList)

                    }

                    searchCondition.createPredicate(cb, from, froms)

                }
                is SearchCriterias<*> -> searchCondition.createPredicate(cb, root, froms)
                else -> null
            }

            finalPredicate = when {
                finalPredicate == null -> cb.and(eachPredicate)
                searchCondition.isAnd() -> cb.and(finalPredicate, eachPredicate)
                searchCondition.isAnd().not() -> cb.or(finalPredicate, eachPredicate)
                else -> throw IllegalArgumentException("Unhandled case.")
            }

        }


        return finalPredicate
    }

    private fun createFromClause(searchCondition: SearchCriteria, root: Path<*>, fromList: Hashtable<String, From<*, *>>): Pair<From<*, *>, Hashtable<String, From<*, *>>> {

        var from = root as From<*, *>

        val st = StringTokenizer(searchCondition.getPath()!!, ".")
        while (st.hasMoreTokens()) {

            val fromTable = st.nextToken()
            if (fromList.containsKey(fromTable).not()) {
                fromList[fromTable] = from.join<Any, Any>(fromTable, JoinType.LEFT)
            }

            from = fromList[fromTable]!!
        }

        return from to fromList

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

    /**
     * @return the distinct
     */
    fun isDistinct(): Boolean {
        return distinct
    }

    /**
     * @param distinct the distinct to set
     */
    fun setDistinct(distinct: Boolean) {
        this.distinct = distinct
    }

    /**
     * @return the select
     */
    fun getSelect(): List<String>? {
        return select
    }

    /**
     * @param select the select to set
     */
    fun setSelect(select: List<String>) {
        this.select = select
    }

    /**
     * @param customSelect custom selection
     */
    fun setSelectClause(customSelect: String) {
        this.selectClause = customSelect
    }

    /**
     * @param groupBy group by
     */
    fun setGroupBy(groupBy: String) {
        this.groupByClause = groupBy
    }

    /**
     * @param from custom from
     */
    fun setFromClause(from: String) {
        this.fromClause = from
    }

    fun setDefaultWhere(defaultWhere: String) {
        this.defaultWhere = defaultWhere
    }

    /**
     * @param having having
     */
    fun setHaving(having: String) {
        this.havingClause = having
    }

    /**
     * @param orderBy having
     */
    fun setOrderBy(orderBy: String) {
        this.orderBy = orderBy
    }

    /**
     * @param offset having
     */
    fun setOffset(offset: String) {
        this.offset = offset
    }

    /**
     * @param fetch having
     */
    fun setFetch(fetch: String) {
        this.fetch = fetch
    }

    /**
     * @param applyParameterValue flag to indicate that we will add parameter value to this search criteria or not
     */
    fun setApplyParameterValue(applyParameterValue: Boolean = true) {
        this.applyParameterValue = applyParameterValue
    }

    /**
     * @return flag to indicate that we will add parameter value to this search criteria or not
     */
    fun getApplyParameterValue(): Boolean {
        return this.applyParameterValue
    }

    /**
     * Append parameter value
     * @param parameterValue value of field to apply condition
     */
    fun appendParameterValue(parameterValue: Any?) {
        this.parameterValue.add(parameterValue)
    }

    /**
     * @return return list of current parameterValue
     */
    fun getParameterValue(): List<Any?> {
        return this.parameterValue.toList()
    }

    /**
     * Append list of parameter value
     * @param parameterValues list of value to apply condition
     */
    fun appendParameterValues(parameterValues: List<Any?>) {
        parameterValues.forEach { this.parameterValue.add(it) }
    }

    /**
     * @return return list of sub query parameterValue
     */
    fun getSubQueryParameterValue(): List<MutableList<Any?>> {
        return this.subQueryParameterValue.toList()
    }

    /**
     * Append sub query parameter value
     * @param parameterValues list of subquery parameter
     */
    fun appendSubQueryParameterValue(parameterValues: List<List<Any?>>) {
        parameterValues.forEach { this.subQueryParameterValue.add(it.toMutableList()) }
    }

    /**
     * Remove first sub query from the list of subQueryParameter
     */
    fun removeFirstSubQueryParameterValue() {
        this.subQueryParameterValue.removeAt(0)
    }

    /**
     * Create native sql statement
     * @return String: Sql clause
     */
    fun createSqlStatement(em: EntityManager): String? {
        if (selectClause == null || groupByClause == null) return null

        // Get from and where from criteria
        var whereClause = generateWhereClause(em, "", this)

        // add default where if exist
        if (defaultWhere.isNullOrEmpty().not()) {
            whereClause = if (whereClause.isEmpty()) defaultWhere!! else (whereClause + AND + defaultWhere)
        }

        //Complete where
        whereClause = if (whereClause.isEmpty()) "" else WHERE_CLAUSE + whereClause

        return selectClause + fromClause + whereClause + groupByClause + havingClause + orderBy + offset + fetch
    }

    /**
     * Create native sql
     * @return Query: the Query object the used to fet result set
     */
    fun createNativeQuery(em: EntityManager): Query? {

        val sql = createSqlStatement(em) ?: return null
        return em.createNativeQuery(sql)
    }

    /**
     * Create Query by using native sql
     * @return Query: the Query object the used to fet result set
     */
    fun createNativeQuery(em: EntityManager, sql: String): Query? {
        return em.createNativeQuery(sql)
    }

    /**
     * Method for generate where clause
     * into raw query (using in aggregate)
     *
     * Note: In target [`searchCriteriasA`] it can have another [`searchCriteriasB`] as a condition
     * but we want to add all the parameter value to [`searchCriteriasA`] not [`searchCriteriasB`] so we need to define target SeachCriterias
     * @param mainSearchCriteria define the target search criteria that we will add parameter value to
     *
     */
    private fun generateWhereClause(em: EntityManager, whereClauseFinal: String = "", mainSearchCriteria: SearchCriterias<*>): String {
        var whereClauseFinalInFn = whereClauseFinal
        criterias.forEach { cond ->

            // Recursive
            if (cond is SearchCriterias<*>) {
                val groupWhereClause = cond.generateWhereClause(em, "", mainSearchCriteria)
                if (groupWhereClause.isEmpty().not()) {
                    whereClauseFinalInFn = combineWhere("($groupWhereClause)", cond.isAnd(), whereClauseFinalInFn)
                }
            }

            if (cond !is SearchCriteria) return@forEach

            val key = cond.getField()!!
            val finalKey = when {
                key.lowercase().endsWith(FROM) -> key.substring(0, key.length - FROM.length)
                key.lowercase().endsWith(TO) -> key.substring(0, key.length - TO.length)
                else -> key
            }
            cond.setField(finalKey)

            val pathName = cond.getPath()
            val (tableClass, tableName) = when (pathName !=  null) {
                // item
                true -> {
                    val items = rootObject.getDeclaredField(pathName)
                    val genericType = if (List::class.java.isAssignableFrom(items.type)) {
                        val parameterizedType = items.genericType as ParameterizedType
                        parameterizedType.actualTypeArguments[0] as Class<*>
                    } else items.type
                    genericType to getTableName(genericType)
                }
                // header
                false -> rootObject to getTableName(rootObject)
            }

            val condToWhere = cond.createWhere(tableName, tableClass, mainSearchCriteria) ?: return@forEach

            // Combine where clause
            whereClauseFinalInFn = combineWhere(condToWhere, cond.isAnd(), whereClauseFinalInFn)
        }

        return whereClauseFinalInFn
    }

    private fun combineWhere(condToWhere: String, isAnd: Boolean, whereClauseFinal: String): String {
        return when {
            whereClauseFinal.isEmpty() -> condToWhere
            whereClauseFinal.isEmpty().not() && isAnd -> whereClauseFinal + AND + condToWhere
            whereClauseFinal.isEmpty().not() && isAnd.not() -> whereClauseFinal + OR + condToWhere
            else -> whereClauseFinal + AND + condToWhere
        }
    }
}