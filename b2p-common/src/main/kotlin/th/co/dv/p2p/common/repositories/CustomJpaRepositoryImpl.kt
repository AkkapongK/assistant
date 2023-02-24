package th.co.dv.p2p.common.repositories

import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.web.context.request.RequestContextHolder
import th.co.dv.p2p.common.constants.cannotCreateJoinClause
import th.co.dv.p2p.common.constants.cannotGenerateSql
import th.co.dv.p2p.common.models.AggregateResponseModel
import th.co.dv.p2p.common.models.JoinModel
import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.AuthorizationUtils
import th.co.dv.p2p.common.utilities.RequestUtility
import th.co.dv.p2p.common.utilities.search.*
import th.co.dv.p2p.common.utilities.search.paging.PagableList
import java.io.Serializable
import javax.persistence.EntityManager
import javax.persistence.PersistenceException
import javax.persistence.Query
import javax.persistence.TypedQuery

class CustomJpaRepositoryImpl<T, ID : Serializable>(domainClass: Class<T>, private var entityManager: EntityManager, private val schema: String) :
    SimpleJpaRepository<T, ID>(domainClass, entityManager), CustomJpaRepository<T, ID> {

    protected val logger = LoggerFactory.getLogger(javaClass)!!

    private val baseCustomJpaRepositoryImpl = BaseCustomJpaRepositoryImpl<T>(entityManager)

    fun getEntityManager(): EntityManager {
        return entityManager
    }

    private fun setPaging(param: Map<String, Any>) {
        if (logger.isTraceEnabled)
            logger.trace("Trying to get paging information from request")

        if (null == RequestContextHolder.getRequestAttributes())
            return

        val page = (param[SearchCriteria.PAGE] ?: param[SearchCriteria.PAGE_NUMBER] ?: RequestUtility.getCurrentRequest().getParameter(SearchCriteria.PAGE) ?: RequestUtility.getCurrentRequest().getParameter(SearchCriteria.PAGE_NUMBER)) as String?
        val rows = (param[SearchCriteria.PAGE_SIZE] ?: RequestUtility.getCurrentRequest().getParameter(SearchCriteria.PAGE_SIZE)) as String?
        baseCustomJpaRepositoryImpl.setPageProperties(page)
        baseCustomJpaRepositoryImpl.setRowsProperties(rows)
    }

    private fun setSorting(param: Map<String, Any>) {
        if (null == RequestContextHolder.getRequestAttributes())
            return

        val sort = (param[SearchCriteria.SORT_FIELD] ?: RequestUtility.getCurrentRequest().getParameter(SearchCriteria.SORT_FIELD)) as String?
        val order = (param[SearchCriteria.SORT_ORDER] ?: RequestUtility.getCurrentRequest().getParameter(SearchCriteria.SORT_ORDER)) as String?

        baseCustomJpaRepositoryImpl.setSortProperties(sort)
        baseCustomJpaRepositoryImpl.setOrderProperties(order)

    }

    @Suppress("UNCHECKED_CAST")
    override fun list(
        criterias: SearchCriterias<*>,
        param: Map<String, Any>,
        operation: Map<String, String>,
        userAuthorization: UserAuthorization,
        headerModelClass: Class<*>,
        headerClass: Class<*>,
        itemClass: Class<*>?,
        skipAuthorization: Boolean
    ): List<T> {

        setPaging(param)
        setSorting(param)

        if (skipAuthorization) return baseCustomJpaRepositoryImpl.list(criterias, param, operation) { } as PagableList

        if (AuthorizationUtils.hasAuthorization(userAuthorization, headerModelClass).not()) return PagableList(mutableListOf())

        val userAuthCallback = baseCustomJpaRepositoryImpl.generateAuthByFieldFn(
            criterias.getRootObject(),
            userAuthorization,
            headerClass,
            itemClass
        )

        return baseCustomJpaRepositoryImpl.list(criterias, param, operation, userAuthCallback) as PagableList
    }

    /**
     * Method for get data from native query result
     * @return list of headerClass
     */
    @Suppress("UNCHECKED_CAST")
    override fun <H : Any, I : Any, M : Any> native(
        nativeQuery: NativeQueryModel,
        criterias: SearchCriterias<*>,
        headerClass: Class<H>,
        itemClass: Class<I>,
        headerModelClass: Class<M>,
        extraClass: Class<Any>?
    ): List<T> {

        val byPassAuth = nativeQuery.byPassAuth
        if (byPassAuth.not() && AuthorizationUtils.hasAuthorization(nativeQuery.userAuth!!, headerModelClass).not()) return PagableList(mutableListOf())

        var fromClause = when {
            nativeQuery.queryItem -> baseCustomJpaRepositoryImpl.getFromCauseItem(nativeQuery, itemClass, schema)
            nativeQuery.joinExtraTable -> baseCustomJpaRepositoryImpl.getFromCause(nativeQuery, headerClass, itemClass, schema, extraClass)
            else -> baseCustomJpaRepositoryImpl.getFromCause(nativeQuery, headerClass, itemClass, schema)
        }

        fromClause = when (nativeQuery.customJoin.isNullOrBlank()) {
            true -> fromClause
            false -> fromClause + nativeQuery.customJoin
        }

        val finalNativeQuery = nativeQuery.copy(fromClause = fromClause)

        val userAuthCallback = when (byPassAuth) {
            true -> { {} }
            false -> baseCustomJpaRepositoryImpl.generateAuthByFieldFn(criterias.getRootObject(), nativeQuery.userAuth, headerClass, itemClass)
        }

        val query = baseCustomJpaRepositoryImpl.native(finalNativeQuery, criterias, userAuthCallback)
        val queryResult = query?.let { convertResultSetToModelWithClass(this.domainClass, query, nativeQuery.fields) } ?: emptyList()

        // TODO: Need condition or not
        // Get number of total record by criteria
        val totalRecordSize = if (nativeQuery.queryTotalRecords) {
            baseCustomJpaRepositoryImpl.countTotalRecord(criterias)
        } else {
            queryResult.size
        }

        val resultPageableList = PagableList(queryResult as MutableList<T>)
        resultPageableList.setTotalSize(totalRecordSize)
        resultPageableList.setPage(criterias.getPage())
        return resultPageableList

    }

    @Suppress("UNCHECKED_CAST")
    override fun executeNative(sqlStatement: String, criterias: SearchCriterias<*>): List<T> {
        val query = entityManager.createNativeQuery(sqlStatement, criterias.getRootObject())
        baseCustomJpaRepositoryImpl.setParameter(criterias, query)
        return query.resultList as MutableList<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun executeNativeWithPaging(sqlStatement: String, criterias: SearchCriterias<*>): List<T> {
        val query = entityManager.createNativeQuery(sqlStatement, criterias.getRootObject())
        baseCustomJpaRepositoryImpl.setParameter(criterias, query)

        val resultList = query.resultList as MutableList<T>
        val totalRecordSize = baseCustomJpaRepositoryImpl.countTotalRecord(criterias)

        val resultPageableList = PagableList(resultList)
        resultPageableList.setTotalSize(totalRecordSize)
        resultPageableList.setPage(criterias.getPage())
        return resultPageableList
    }

    @Suppress("UNCHECKED_CAST")
    override fun count(criterias: SearchCriterias<*>): Int {
        val query = criterias.createTotalQuery(getEntityManager()) as TypedQuery<Long>
        return query.singleResult.toInt()
    }

    /**
     * Method for get total count from the criteria and nativeQuery
     * @param nativeQuery:  Native query model contain SQL clause for query
     * @param criterias: The criteria that we will convert param and add to the criteria
     * @param headerClass : header entity class
     * @param itemClass : item entity class
     * @param headerModelClass : model of header class using for validating authorization
     */
    @Suppress("UNCHECKED_CAST")
    override fun <H : Any, I : Any, M: Any> countByNativeQuery(
        nativeQuery: NativeQueryModel,
        criterias: SearchCriterias<*>,
        headerClass: Class<H>,
        itemClass: Class<I>,
        headerModelClass: Class<M>
    ): Int {

        if (AuthorizationUtils.hasAuthorization(nativeQuery.userAuth!!, headerModelClass).not()) return 0

        val fromClause = baseCustomJpaRepositoryImpl.getFromCause(nativeQuery, headerClass, itemClass, schema)
        val finalNativeQuery = nativeQuery.copy(fromClause = fromClause)

        val countSqlStatement = baseCustomJpaRepositoryImpl.sqlStatement(
            nativeQuery = finalNativeQuery,
            criterias = criterias,
            userAuthCallback = baseCustomJpaRepositoryImpl.generateAuthByFieldFn(criterias.getRootObject(), finalNativeQuery.userAuth, headerClass, itemClass))

        val query = entityManager.createNativeQuery(countSqlStatement)
        baseCustomJpaRepositoryImpl.setParameter(criterias, query)
        return query.singleResult as Int
    }

    /**
     * Method for get purchase order from native query result
     *
     * @return linear id purchase order
     */
    override fun nativeQuery(nativeQuery: NativeQueryModel, criterias: SearchCriterias<*>): Query {
        return baseCustomJpaRepositoryImpl.native(nativeQuery, criterias) { }!!
    }

    /**
     * Method for get sql statement for native query
     *
     * @return sql statement
     */
    override fun sqlStatement(
        nativeQuery: NativeQueryModel,
        criterias: SearchCriterias<*>,
        headerClass: Class<*>?,
        itemClass: Class<*>?,
        extraClass: Class<Any>?
    ): String {

        val haveHeaderAndItemClass = headerClass != null && itemClass != null
        val needExtraClassJoin = nativeQuery.joinExtraTable && extraClass != null
        val finalFromClause = when {
            haveHeaderAndItemClass && needExtraClassJoin -> baseCustomJpaRepositoryImpl.getFromCause(nativeQuery, headerClass!!, itemClass!!, schema, extraClass)
            haveHeaderAndItemClass -> baseCustomJpaRepositoryImpl.getFromCause(nativeQuery, headerClass!!, itemClass!!, schema)
            else -> nativeQuery.fromClause
        }

        val finalNativeQuery = nativeQuery.copy(fromClause = finalFromClause)
        return baseCustomJpaRepositoryImpl.sqlStatement(finalNativeQuery, criterias) {}
            ?: throw IllegalArgumentException(cannotGenerateSql)
    }


    /**
     * Method for get sql statement for native query
     * @return sql statement
     */
    override fun sqlStatementWithAuth(
        nativeQuery: NativeQueryModel,
        criterias: SearchCriterias<*>,
        headerModelClass: Class<*>,
        headerClass: Class<*>?,
        itemClass: Class<*>?,
        extraClass: Class<Any>?,
    ): String? {

        if ((nativeQuery.userAuth?.let { AuthorizationUtils.hasAuthorization(it, headerModelClass) } == true).not()) return null

        val haveHeaderAndItemClass = headerClass != null && itemClass != null
        val needExtraClassJoin = nativeQuery.joinExtraTable && extraClass != null
        val finalFromClause = when {
            haveHeaderAndItemClass && needExtraClassJoin -> baseCustomJpaRepositoryImpl.getFromCause(nativeQuery, headerClass!!, itemClass!!, schema, extraClass)
            haveHeaderAndItemClass -> baseCustomJpaRepositoryImpl.getFromCause(nativeQuery, headerClass!!, itemClass!!, schema)
            else -> nativeQuery.fromClause
        }

        val finalNativeQuery = nativeQuery.copy(fromClause = finalFromClause)
        val userAuthCallback = baseCustomJpaRepositoryImpl.generateAuthByFieldFn(
            criterias.getRootObject(),
            nativeQuery.userAuth,
            headerClass,
            itemClass
        )

        return baseCustomJpaRepositoryImpl.sqlStatement(finalNativeQuery, criterias, userAuthCallback) ?: throw IllegalArgumentException(cannotGenerateSql)
    }

    /**
     * Method for get aggregateQuery query result
     *
     * @return AggregateResponseModel
     */
    override fun <H : Any, I : Any, M : Any> nativeAggregate(
        nativeQuery: NativeQueryModel,
        criterias: SearchCriterias<*>,
        headerClass: Class<H>,
        itemClass: Class<I>,
        headerModelClass: Class<M>
    ): List<AggregateResponseModel> {

        if (nativeQuery.byPassAuth.not() && AuthorizationUtils.hasAuthorization(nativeQuery.userAuth!!, headerModelClass).not()) return emptyList()

        setSorting(nativeQuery.param)
        val joinModels = listOf(
            JoinModel(table = headerClass),
            JoinModel(table = itemClass)
        )
        val fromClause = baseCustomJpaRepositoryImpl.generateJoin(joinModels, schema)
            ?: throw PersistenceException(cannotCreateJoinClause)

        val finalNativeQuery = nativeQuery.copy(fromClause = fromClause)

        val query = baseCustomJpaRepositoryImpl.native(finalNativeQuery, criterias,
            userAuthCallback = baseCustomJpaRepositoryImpl.generateAuthByFieldFn(criterias.getRootObject(), nativeQuery.userAuth, headerClass, itemClass))

        return query?.let { convertResultSetToModel(query, nativeQuery.fields) } ?: emptyList()

    }

    /**
     * Method for get aggregate query result in [AggregateResponseModel] by
     * sql statement
     *
     * @return AggregateResponseModel
     */
    override fun aggregate(sql: String, criterias: SearchCriterias<*>, fields: List<String>): List<AggregateResponseModel> {
        val query = baseCustomJpaRepositoryImpl.native(sql, criterias) ?: return emptyList()
        baseCustomJpaRepositoryImpl.setParameter(criterias, query)
        return convertResultSetToModel(query, fields)
    }

}