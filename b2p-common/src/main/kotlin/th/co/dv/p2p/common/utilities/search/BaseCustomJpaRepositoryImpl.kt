package th.co.dv.p2p.common.utilities.search

import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.constants.BaseStatements.COUNT_SQL
import th.co.dv.p2p.common.constants.BaseStatements.SUB_SQL_KEY
import th.co.dv.p2p.common.models.JoinModel
import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.*
import th.co.dv.p2p.common.utilities.DateUtility.Companion.convertStringToDate
import th.co.dv.p2p.common.utilities.search.paging.PagableList
import java.lang.reflect.ParameterizedType
import java.util.*
import javax.persistence.*
import javax.persistence.criteria.JoinType
import kotlin.reflect.full.createInstance

/**
 * Class for manage custom criteria
 *
 */
open class BaseCustomJpaRepositoryImpl<T>(private var entityManager: EntityManager) {

    protected val logger = LoggerFactory.getLogger(javaClass)!!
    protected val className: String = BaseCustomJpaRepositoryImpl::class.java.simpleName

    protected var page: String? = null
    protected var rows: String? = null
    protected var sort: String? = null
    protected var order: String? = null
    protected var selectFields: List<String>? = null


    /**
     * The methods for set properties that we cannot get by our self
     * Because we not add spring boot to this project
     */
    fun setPageProperties(page: String?) {
        this.page = page
    }

    fun setRowsProperties(rows: String?) {
        this.rows = rows
    }

    fun setSortProperties(sort: String?) {
        this.sort = sort
    }

    fun setOrderProperties(order: String?) {
        this.order = order
    }

    fun setSelectFieldsProperties(selectFields: List<String>?) {
        this.selectFields = selectFields
    }

    /**
     * Method for set paging to criteria
     */
    private fun setPaging(criterias: SearchCriterias<*>) {

        try {
            if (!StringUtility.isEmpty(page) && !StringUtility.isEmpty(rows) && criterias.isPaging()) {
                if (logger.isDebugEnabled)
                    logger.debug("BaseCustomJpaRepositoryImpl.setPaging Set paging[$page,$rows]")
                criterias.setPaging(rows != "-1")
                criterias.setPageSize(Integer.parseInt(rows!!))
                criterias.setPage(Integer.parseInt(page!!))
            }
        } catch (e: Exception) {
            if (logger.isErrorEnabled)
                logger.error("BaseCustomJpaRepositoryImpl.setPaging Error when setting paging to search criteria[" + page + "," + rows + "]: " + e.message)
            e.printStackTrace()
        }

    }

    /**
     * Method for set ordering to criteria
     */
    private fun setSorting(criterias: SearchCriterias<*>) {

        try {
            if (!StringUtility.isEmpty(sort) && !StringUtility.isEmpty(order)) {
                if (logger.isDebugEnabled)
                    logger.debug("BaseCustomJpaRepositoryImpl.setSorting sorting[$sort $order]")
                criterias.addSort(sort!!, order!!.equals(ASC, ignoreCase = true) || order.equals("1", ignoreCase = true), 0)
            }
        } catch (e: Exception) {
            if (logger.isDebugEnabled)
                logger.debug("BaseCustomJpaRepositoryImpl.setSorting Error when setting sorting to search criteria[" + sort + "," + order + "]: " + e.message)
        }

    }

    /**
     * Method for set select to criteria
     */
    private fun setSelectFields(criterias: SearchCriterias<*>) {

        try {
            if (selectFields.isNullOrEmpty().not()) {
                logger.debug("BaseCustomJpaRepositoryImpl.setSelectFields setSelectFields: $selectFields")
                criterias.setSelect(selectFields!!)
            }
        } catch (e: Exception) {
            logger.debug("BaseCustomJpaRepositoryImpl.setSelectFields Error when setting setSelectFields to search criteria $selectFields: " + e.message)
        }

    }

    /**
     * Method for create criterias from para and operation
     *
     * @param criterias: The criteria that we will convert param and add to the criteria
     * @param param: Parameter that keep key mapping with value
     * @param operation: Operation that keep key mapping with operation
     */
    private fun addCriteriaFromMap(criterias: SearchCriterias<*>, param: Map<String, Any>, operation: Map<String, String>) {
        val classz = criterias.getRootObject()

        // We loop all the key, each key can represent the field that we want to search
        param.keys.forEach { key ->

            // If the current key not a field in an Object we skip to next key
            if (isReadableProperty(classz, key).not()) {
                logger.debug("BaseCustomJpaRepositoryImpl.addCriteriaFromMap Unreadable Property: $key")
                return@forEach
            }
            // If the current key is empty we also skip to next key
            if (StringUtility.isEmpty(key)) return@forEach

            // Else we proceed
            logger.debug("BaseCustomJpaRepositoryImpl.addCriteriaFromMap Readable Property: $key")
            val value = param[key]
            // Get the operation of current field if cannot find we default as EQUAL
            // and try to get data from SearchCriteriaOperation
            val currentOperation = if (operation.containsKey(key)) operation.getValue(key) else SearchCriteriaOperation.EQUAL.name
            val searchCriteriaOperation =
                    try {
                        SearchCriteriaOperation.valueOf(currentOperation)
                    } catch (ex: java.lang.Exception) {
                        SearchCriteriaOperation.EQUAL
                    }
            // We try to find the key that have [from, to] which will be a Date field and substring to get only the field name
            // For the normal field we will do nothing
            val finalKey = getFinalKey(key)

            // For the value if the key is Date type we will convert the String to Date, else we do nothing
            val finalValue = if ((key.lowercase().endsWith(FROM) || key.lowercase().endsWith(TO)) && (value !is Date)) {
                convertStringToDate(value!!.toString())
            } else value

            criterias.addCondition(finalKey, searchCriteriaOperation, finalValue!!)

        }
    }

    /**
     * Method for check the properties in the class can read or not
     *
     * @param claszz: Class
     * @param key: Field name
     */
    private fun isReadableProperty(claszz: Class<*>, key: String): Boolean {

        var claszzWrapper = claszz
        logger.debug("BaseCustomJpaRepositoryImpl.isReadableProperty Checking key: $key")
        val keys = key.split(".")

        keys.forEach { initialKey ->

            logger.debug("BaseCustomJpaRepositoryImpl.isReadableProperty initialKey: $initialKey")

            val finalKey = getFinalKey(initialKey)

            if (claszzWrapper.isReadableProperty(finalKey).not()) return false

            var targetField = claszzWrapper.getDeclaredField(finalKey).type
            val isNotPrimitive = targetField.isPrimitive.not()
            logger.debug("BaseCustomJpaRepositoryImpl.isReadableProperty targetField: $targetField, ${targetField.simpleName}")
            if (isNotPrimitive && !key.endsWith(initialKey)) {
                if (List::class.java.isAssignableFrom(targetField) || Set::class.java.isAssignableFrom(targetField)) {
                    val listType = claszzWrapper.getDeclaredField(finalKey).genericType as ParameterizedType
                    targetField = listType.actualTypeArguments[0] as Class<*>
                }
                claszzWrapper = targetField
            }

        }
        return true
    }

    /**
     * Method for query data by criteria
     * in the method we will call callback method for add authorization in the criteria
     *
     * @param criterias: The criteria that we will convert param and add to the criteria
     * @param param: Parameter that keep key mapping with value
     * @param operation: Operation that keep key mapping with operation
     * @param userAuthCallback: Callback method for add authorization in the criteria
     */
    @Suppress("UNCHECKED_CAST")
    fun list(criterias: SearchCriterias<*>, param: Map<String, Any>, operation: Map<String, String>, userAuthCallback: (SearchCriterias<*>) -> Unit): List<T> {
        addCriteriaFromMap(criterias, param, operation)

        // Add condition for auth by fields
        userAuthCallback(criterias)
        setPaging(criterias)
        setSorting(criterias)
        setSelectFields(criterias)

        val count = count(criterias)
        val list = when (count) {
            0 -> PagableList(mutableListOf<T>())
            else -> {
                val query = criterias.createQuery(entityManager)
                PagableList(query.resultList as MutableList<T>)
            }
        }

        list.setPage(1)
        list.setPageSize(list.size)
        list.setTotalSize(list.size)

        if (criterias.isPaging() && (list.size == criterias.getPageSize() || criterias.getPage() >= 1)) {
            list.setPageSize(criterias.getPageSize())
            list.setPage(criterias.getPage())
            list.setTotalSize(count)
        }

        return list
    }


    /**
     * Method for get total count from the criteria
     *
     * @param criterias: The criteria that we will convert param and add to the criteria
     */
    @Suppress("UNCHECKED_CAST")
    fun count(criterias: SearchCriterias<*>): Int {
        val query = criterias.createTotalQuery(entityManager) as TypedQuery<Long>
        return query.singleResult.toInt()
    }

    /**
     * Method for add select clause, from clause, group by clause, having clause and
     * criteria that used to create where clause to [SearchCriterias] before get data
     * or generate swl
     */
    private fun prepareCriterias(
            nativeQuery: NativeQueryModel,
            criterias: SearchCriterias<*>,
            userAuthCallback: (SearchCriterias<*>) -> Unit) {

        // Set sub query parameter value
        criterias.appendSubQueryParameterValue(nativeQuery.subQueryValue)

        // Set custom select
        criterias.setSelectClause(completeClause(nativeQuery.customSelect, SELECT_CLAUSE))

        // Set custom select
        criterias.setFromClause(completeClause(nativeQuery.fromClause, FROM_CLAUSE))

        // Set criteria
        addCriteriaFromMap(criterias, nativeQuery.param, nativeQuery.operation)

        // Set default where
        criterias.setDefaultWhere(nativeQuery.defaultWhere)

        // Set group by
        criterias.setGroupBy(completeClause(nativeQuery.groupBy, GROUP_BY))

        // Add condition for auth by fields
        userAuthCallback(criterias)

        // Set having
        criterias.setHaving(completeClause(nativeQuery.having, HAVING))

        // Set sorting
        criterias.setOrderBy(completeClause(nativeQuery.orderBy, ORDER_BY))

        // Set offset
        criterias.setOffset(completeClause(nativeQuery.offset, OFFSET))

        // Set fetch/limit
        criterias.setFetch(completeClause(nativeQuery.fetch, FETCH))
    }

    /**
     * Method for query aggregate data by criteria
     * in the method we will call callback method for add authorization in the criteria
     *
     * @param nativeQuery: Native query model contain SQL clause for query
     * @param criterias: The criteria that we will convert param and add to the criteria
     * @param userAuthCallback: Callback method for add authorization in the criteria
     */
    @Suppress("UNCHECKED_CAST")
    fun native(nativeQuery: NativeQueryModel, criterias: SearchCriterias<*>, userAuthCallback: (SearchCriterias<*>) -> Unit): Query? {

        prepareCriterias(
                nativeQuery = nativeQuery,
                criterias = criterias,
                userAuthCallback = userAuthCallback)

        val query = criterias.createNativeQuery(entityManager)
        query?.let { setParameter(criterias, it) }
        return query

    }

    /**
     * Method for query aggregate data by criteria and native sql
     * the method will add variables in sql
     *
     * @param sql: Native sql
     * @param criterias: The criteria that we will convert param and add to the criteria
     */
    fun native(sql: String, criterias: SearchCriterias<*>): Query?{
        return criterias.createNativeQuery(entityManager, sql)
    }

    /**
     * Method for generate sql from the criterias
     *
     * @param nativeQuery: Native query model contain SQL clause for query
     * @param criterias: The criteria that we will convert param and add to the criteria
     * @param userAuthCallback: Callback method for add authorization in the criteria
     *
     * @return Sql statement
     */
    @Suppress("UNCHECKED_CAST")
    fun sqlStatement(nativeQuery: NativeQueryModel, criterias: SearchCriterias<*>, userAuthCallback: (SearchCriterias<*>) -> Unit): String? {

        prepareCriterias(
                nativeQuery = nativeQuery,
                criterias = criterias,
                userAuthCallback = userAuthCallback)

        return criterias.createSqlStatement(entityManager)

    }

    /**
     * Method for complete clause
     *
     */
    private fun completeClause(content: String, prefixClause: String): String {
        if (content.isEmpty()) return ""

        return when (content.trim().uppercase().startsWith(prefixClause.replace(" ", ""))) {
            true -> content
            false -> prefixClause + content
        }
    }

    /**
     * Method for generate join clause from JoinModel
     *
     * @param joinModels: List of JoinModel
     * @param schema: Schema name
     */
    fun generateJoin(joinModels: List<JoinModel>, schema: String): String? {

        val mainTable = joinModels.first()
        val maniTableName = getTableName(mainTable.table)
        var joinOutput = " $schema.$maniTableName "
        val joinTables = joinModels.drop(1)
        joinTables.forEach { joinModel ->
            val tableName = getTableName(joinModel.table)
            val condition = getJoinCondition(mainTable, joinModel) ?: return null
            // Add join type
            joinOutput += when (joinModel.oper) {
                JoinType.INNER -> INNER_JOIN
                JoinType.LEFT -> LEFT_JOIN
                JoinType.RIGHT -> RIGHT_JOIN
            }

            // Add table
            joinOutput += "$schema.$tableName"

            // Add condition
            joinOutput += "$ON $condition "
        }
        return joinOutput
    }

    /**
     * Method for create join condition
     * In case in JoinModel have condition we use the condition in join clause
     * but if not have we will find condition from entity linked
     *
     * @param mainTable: JoinModel for main table
     * @param joinModel: JoinModel for join table
     */
    private fun getJoinCondition(mainTable: JoinModel, joinModel: JoinModel): String? {
        if (joinModel.condition != null) return joinModel.condition
        val currentEntity = joinModel.table

        val linkedField = currentEntity.declaredFields.find {

            val genericType = it.genericType

            genericType == mainTable.table
        } ?: return null

        val linkedItemFieldName = linkedField.getAnnotation(JoinColumn::class.java).name
        val mainClass = mainTable.table
        val mainTableName = getTableName(mainClass)
        val joinTableName = getTableName(joinModel.table)
        val linkedHeaderField = mainClass.getFieldId() ?: return null
        val linkedHeaderFieldName = StringUtility.camelToSnakeCase(linkedHeaderField.name)


        return "($mainTableName.$linkedHeaderFieldName = $joinTableName.$linkedItemFieldName)"
    }

    /**
     * Method for query data for get total record bt criteria
     *
     * Note: If we run native query before countTotalRecord parameterValue in SearchCriteria is already applied
     *       So we will default applyParameterValue as false to not assign parameterValue to search criteria to prevent duplicate parameterValue
     * @param criterias search criteria to build sql and query data
     * @param applyParameterValue flag to indicate that we will apply parameter value for this query or not
     *
     */
    fun countTotalRecord(criterias: SearchCriterias<*>, applyParameterValue: Boolean = false): Int {
        criterias.setOrderBy("")
        criterias.setFetch("")
        criterias.setOffset("")

        criterias.setApplyParameterValue(applyParameterValue)
        val sqlStatement = criterias.createSqlStatement(entityManager)!!
        val sqlTotalCount = COUNT_SQL.replace(SUB_SQL_KEY, sqlStatement)
        val query = entityManager.createNativeQuery(sqlTotalCount)
        setParameter(criterias, query)
        return query.singleResult as Int
    }

    /**
     * Method for apply parameter value to query statement
     */
    fun setParameter(searchCriteria: SearchCriterias<*>, query: Query) {
        val parameterValue = searchCriteria.getParameterValue()
        logger.info("$className.setParameter parameterValue :$parameterValue")
        parameterValue.forEachIndexed { index, value ->
            query.setParameter(index + 1, value)
        }
    }

    /**
     * Method generate auth per field function for header or item state
     *
     * @param criteriaRootObject : criteria root object
     * @param userAuthorization : user authorization
     * @param headerClass : header class
     * @param itemClass : item class
     */
    fun <H : Any, I : Any> generateAuthByFieldFn(criteriaRootObject: Class<*>, userAuthorization: UserAuthorization?, headerClass: Class<H>?, itemClass: Class<I>?): (SearchCriterias<*>) -> Unit {
        if (userAuthorization == null || headerClass == null) return { }

        if (itemClass != null) {
            val isHeader = criteriaRootObject == headerClass
            return { criteriaList ->
                when (isHeader) {
                    true -> AuthorizationByFields.addCriteriaFromAuthorizationWithClass(
                            criterias = criteriaList,
                            userAuthorization = userAuthorization,
                            mapItemWithName = getHeaderMappingTable(headerClass, itemClass),
                            clazz = headerClass)
                    false -> AuthorizationByFields.addCriteriaFromAuthorizationWithClass(
                            criterias = criteriaList,
                            userAuthorization = userAuthorization,
                            mapItemWithName = getItemMappingTable(headerClass, itemClass),
                            clazz = itemClass)
                }
            }
        } else {
            return { criteriaList ->
                AuthorizationByFields.addCriteriaFromAuthorizationWithClass(
                        criterias = criteriaList,
                        userAuthorization = userAuthorization,
                        clazz = headerClass)
            }
        }
    }

    /**
     * Method for get from clause
     *
     * @param nativeQuery : NativeQueryModel
     * @param itemClass : item entity class
     * @param schema : schema name
     */
    fun <I> getFromCauseItem(nativeQuery: NativeQueryModel, itemClass: Class<I>, schema: String): String {
        if (nativeQuery.fromClause.isBlank().not()) return nativeQuery.fromClause
        val joinModels = mutableListOf(JoinModel(table = itemClass))
        return generateJoin(joinModels, schema)!!
    }

    /**
     * Method for get from clause
     *
     * @param nativeQuery : NativeQueryModel
     * @param headerClass : header entity class
     * @param itemClass : item entity class
     * @param schema : schema name
     * @param extraClass : extra entity class
     */
    fun <H, I> getFromCause(nativeQuery: NativeQueryModel, headerClass: Class<H>, itemClass: Class<I>, schema: String, extraClass: Class<Any>? = null): String {
        if (nativeQuery.fromClause.isBlank().not()) return nativeQuery.fromClause

        val joinModels = mutableListOf(
                JoinModel(table = headerClass),
                JoinModel(table = itemClass, oper = nativeQuery.joinType)
        )

        if(extraClass != null) joinModels.add(JoinModel(table = extraClass))
        return generateJoin(joinModels, schema)!!
    }

}

/**
 * Method for get map table for header level
 *
 */
fun getHeaderMappingTable(header: Class<*>, item: Class<*>): Map<String, String> {
    val linkedField = header.declaredFields.find {
        try {
            val parameterizedType = it.genericType as ParameterizedType
            val genericType = parameterizedType.actualTypeArguments[0] as Class<*>
            genericType == item
        } catch (e: java.lang.Exception) {
            return@find false
        }
    } ?: return emptyMap()

    return mapOf(
            item.simpleName to linkedField.name
    )
}

/**
 * Method for get map table for item level
 *
 */
fun getItemMappingTable(header: Class<*>, item: Class<*>): Map<String, String> {
    val linkedField = item.declaredFields.find {
        it.genericType == header
    } ?: return emptyMap()

    return mapOf(
            item.simpleName to "",
            "" to linkedField.name
    )

}

/**
 * Method to get final key by remove From - To
 *
 * @param key given field
 * @return final filed for search
 */
fun getFinalKey(key: String): String {
    return when {
        key.lowercase().endsWith(FROM) -> key.substring(0, key.length - FROM.length)
        key.lowercase().endsWith(TO) -> key.substring(0, key.length - TO.length)
        else -> key
    }
}

/**
 * Returns the table name for a given entity type in the [EntityManager].
 * @param entityClass
 * @return
 */
fun getTableName(entityClass: Class<*>): String {
    //Check whether @Table annotation is present on the class.
    val nameFromAnnotation = entityClass.getAnnotation(Table::class.java)?.name
    return when (nameFromAnnotation.isNullOrEmpty()) {
        true -> StringUtility.camelToSnakeCase(entityClass.simpleName)
        false -> nameFromAnnotation
    }
}

/**
 * Method for convert result set to model
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> convertResultSetToModel(query: Query, mapFieldsWithType: List<String>, offset: Int = 0): List<T> {
    return convertResultSetToModel(query.resultList, mapFieldsWithType, offset)
}

fun <T> convertResultSetToModelWithClass(clazz: Class<T>, query: Query, mapFieldsWithType: List<String>, offset: Int = 0): List<T> {
    return convertResultSetToModelWithClass(clazz, query.resultList, mapFieldsWithType, offset)
}

/**
 * Method for convert result set to model
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> convertResultSetToModel(resultList: List<*>, mapFieldsWithType: List<String>, offset: Int = 0): List<T> {
    return convertResultSetToModelWithClass(T::class.java, resultList, mapFieldsWithType, offset)
}

/**
 * Method for convert result set to model
 */
@Suppress("UNCHECKED_CAST")
fun <T> convertResultSetToModelWithClass(clazz: Class<T>, resultList: List<*>, mapFieldsWithType: List<String>, offset: Int = 0): List<T> {
    val aggregateRecords: MutableList<T> = ArrayList()
    val it: Iterator<*> = resultList.iterator()
    while (it.hasNext()) {
        val nextResult = it.next()!! // Iterating through array object
        val isArray = Array<Any>::class.java.isAssignableFrom(nextResult::class.java)
        val result = if (isArray) {
            nextResult as Array<Any>
        } else {
            arrayOf(nextResult)
        }

        val model = clazz.getDeclaredConstructor().newInstance()!!

        var index = offset
        mapFieldsWithType.forEach { field ->
            val targetField = clazz.declaredFields.singleOrNull { it.name.equals(field, ignoreCase = false) }
            val convertAnnotation = targetField?.getAnnotation(Convert::class.java)
            val data = if (convertAnnotation != null) {
                val converterClass = Class.forName(convertAnnotation.converter.qualifiedName).kotlin
                val converter = converterClass.createInstance() as AttributeConverter<*, Any?>
                converter.convertToEntityAttribute(result[index])
            } else result[index]
            model.setFieldValue(field, data)
            index++
        }
        aggregateRecords.add(model)
    }

    return aggregateRecords
}