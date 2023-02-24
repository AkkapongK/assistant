package th.co.dv.p2p.common.utilities.manager

import net.corda.core.node.services.vault.NullOperator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.FROM
import th.co.dv.p2p.common.constants.IS_PAGING
import th.co.dv.p2p.common.constants.NULL
import th.co.dv.p2p.common.constants.TO
import th.co.dv.p2p.common.enums.Lifecycle
import th.co.dv.p2p.common.enums.RecordStatus
import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.*
import th.co.dv.p2p.common.utilities.AuthorizationUtils.INTERFACE_AUTHORIZATION
import th.co.dv.p2p.common.utilities.BaseManagerUtils.filteredEligibleField
import th.co.dv.p2p.common.utilities.BaseManagerUtils.generateCriteriaMap
import th.co.dv.p2p.common.utilities.BaseManagerUtils.getColumnName
import th.co.dv.p2p.common.utilities.BaseManagerUtils.inferSortDirection
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.DEFAULT_GROUP
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.FETCH
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.FETCH_PLACEHOLDER
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.NATIVE_SELECT
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.OFFSET
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.OFFSET_PLACEHOLDER
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.ORDER_BY
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.ORDER_BY_PLACEHOLDER
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.ORDER_DIRECTION_PLACEHOLDER
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.defaultField
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.defaultSortField
import th.co.dv.p2p.common.utilities.search.SearchCriteria
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import th.co.dv.p2p.common.utilities.search.paging.PagableList
import th.co.dv.p2p.corda.base.models.CreditNoteModel
import th.co.dv.p2p.corda.base.models.CreditNoteSearchModel
import java.util.*

abstract class BaseCreditNoteManager<H : ModelableEntity, I> {

    private val queryableService by lazy { initQueryableService() }
    abstract fun initQueryableService(): QueryableService<H>

    protected open lateinit var headerClass: Class<H>
    protected open lateinit var itemClass: Class<I>

    protected open var logger: Logger = LoggerFactory.getLogger(BaseCreditNoteManager::class.java)
    protected open var className: String = BaseCreditNoteManager::class.java.simpleName

    protected val subQueryParameterValue = mutableListOf<List<Any?>>()

    /**
     * Method to search credit note using condition in credit note search model
     */
    fun searchQuery(searchModel: CreditNoteSearchModel, selectFields: List<String> = emptyList(), userAuthorization: UserAuthorization, searchCriterias: SearchCriterias<*>? = null): PagableList<CreditNoteModel> {
        logger.info(("$className.searchQuery searchModel: $searchModel"))

        val (param, operation) = buildCriteria(searchModel)

        val result = when (selectFields.isEmpty()) {
            true -> queryByParam(param, operation, searchModel, userAuthorization, searchCriterias)
            false -> nativeQuery(param, operation, searchModel, selectFields, userAuthorization, searchCriterias)
        }
        logger.info("$className.searchQuery result: ${result.size}")

        // Need to clear this list every time finished query
        subQueryParameterValue.clear()
        return result
    }

    /**
     * Method to search credit note using condition in credit note search model
     */
    @Suppress("UNCHECKED_CAST")
    protected fun nativeQuery(param: Map<String, Any>,
                              operation: MutableMap<String, String>,
                              searchModel: CreditNoteSearchModel,
                              selectFields: List<String> = emptyList(),
                              userAuthorization: UserAuthorization,
                              searchCriterias: SearchCriterias<*>? = null): PagableList<CreditNoteModel> {

        logger.info("$className.nativeQuery")

        val sc = searchCriterias ?: SearchCriterias(this.headerClass)
        sc.setPage(searchModel.pageNumber)

        val offset = OFFSET.replace(OFFSET_PLACEHOLDER, ((searchModel.pageNumber - 1) * searchModel.pageSize).toString())
        val limit = FETCH.replace(FETCH_PLACEHOLDER, searchModel.pageSize.toString())

        // Subtract default field from select field to prevent duplication grouping
        val filteredSelectFields = selectFields.subtract(defaultField.toSet()).toList()
        // Convert select field to table column
        val convertedSelectFields = getColumnName(filteredSelectFields, headerClass = headerClass, itemClass = itemClass)
        val eligibleSelectField = filteredEligibleField(selectFields, headerClass = headerClass, itemClass = itemClass, itemFieldName = InterfaceBaseCreditNote<*>::creditNoteItems.name)

        // customSelect is database column name used for SELECT clause in sql
        // groupByClause is database column name used for GROUP BY clause in sql
        // fields is list of field name in entity used for convert sql result to model
        val (customSelect, groupByClause, fields) = when (convertedSelectFields.isEmpty()) {
            true -> Triple(NATIVE_SELECT, DEFAULT_GROUP, defaultField)
            false -> Triple(NATIVE_SELECT + convertedSelectFields.joinToString(separator = ", ", prefix = ", "),
                    DEFAULT_GROUP + convertedSelectFields.joinToString(separator = ", ", prefix = ", "),
                    (defaultField + eligibleSelectField).distinct()
            )
        }

        // We need to append sort field to group by if sort field is not group by field
        val (groupBy, sortField) = computeGroupByClause(groupByClause, searchModel.sortField)
        val orderBy = ORDER_BY.replace(ORDER_BY_PLACEHOLDER, sortField).replace(ORDER_DIRECTION_PLACEHOLDER, inferSortDirection(searchModel.sortOrder))

        val nativeQueryModel = completeNativeQuery(
            NativeQueryModel(
                customSelect = customSelect,
                fetch = limit,
                fields = fields,
                groupBy = groupBy,
                offset = offset,
                operation = operation,
                orderBy = orderBy,
                param = param,
                subQueryValue = subQueryParameterValue,
                userAuth = userAuthorization
            )
        )

        val creditNoteModels = queryableService.native(nativeQueryModel, sc)
        logger.info("$className.nativeQuery result from native query , total Record : ${creditNoteModels.getTotalSize()}, returns item ${searchModel.returnCreditNoteItems}")

        return (creditNoteModels as PagableList<ModelableEntity>).toPagableModels(searchModel) as PagableList<CreditNoteModel>
    }

    /**
     * Method to complete native query model
     */
    protected open fun completeNativeQuery(nativeQueryModel: NativeQueryModel): NativeQueryModel {
        return nativeQueryModel
    }

    /**
     * Method to compute group by with sort field
     * First we check whether sort field is exist in group by or not
     * if not we need to add more group by field then return final group by clause and formatted sort field
     */
    protected fun computeGroupByClause(groupByClause: String, sortField: String): Pair<String, String> {
        val listOfGroupBy = groupByClause.split(",").map { it.trim() }

        // Convert sort field from input
        val finalSortField = computeSortField(sortField)
        val isSortInGroupBy = finalSortField in listOfGroupBy

        if (isSortInGroupBy) return groupByClause to finalSortField

        val updateGroupBy = "$groupByClause, $finalSortField"
        logger.info("$className.validateSortField updateGroupBy: $updateGroupBy, formattedSortField: $finalSortField")

        return updateGroupBy to finalSortField
    }

    /**
     * Method to compute sort field first convert input field to database field
     * If result is empty means sort field is at item level so we use default sort field instead
     *
     * @param sortField input sort field from search model
     * @param isTableColumn flag to return field in table format or model format
     * @return an eligible sort field for query
     */
    protected open fun computeSortField(sortField: String, isTableColumn: Boolean = true): String {
        return BaseManagerUtils.computeSortField(
                sortField = sortField,
                defaultField = defaultSortField,
                headerClass = headerClass,
                itemClass = itemClass,
                isTableColumn = isTableColumn
        )
    }

    /**
     * Method query credit note using native query in case of user not specific select field
     * first query with native query to get linear id and use linear id to get credit note model
     */
    @Suppress("UNCHECKED_CAST")
    protected fun queryByParam(param: Map<String, Any>,
                               operation: MutableMap<String, String>,
                               searchModel: CreditNoteSearchModel,
                               userAuthorization: UserAuthorization,
                               searchCriterias: SearchCriterias<*>? = null): PagableList<CreditNoteModel> {
        logger.info("no selectField then query by param")
        val nativeResult = nativeQuery(param, operation, searchModel.copy(returnCreditNoteItems = false), userAuthorization = userAuthorization, searchCriterias = searchCriterias)

        if (nativeResult.isEmpty()) return PagableList(mutableListOf())

        val linearIdParam = mapOf(
                InterfaceBaseCreditNote<*>::linearId.name to nativeResult.map { it.linearId!! },
                CreditNoteSearchModel::pageNumber.name to searchModel.pageNumber.toString(),
                CreditNoteSearchModel::pageSize.name to searchModel.pageSize.toString(),
                CreditNoteSearchModel::sortField.name to computeSortField(searchModel.sortField, false),
                CreditNoteSearchModel::sortOrder.name to inferSortDirection(searchModel.sortOrder),
                IS_PAGING to false)
        val linearIdOperation = mapOf(InterfaceBaseCreditNote<*>::linearId.name to SearchCriteriaOperation.IN.name)

        val result = queryableService.findByParam(linearIdParam, linearIdOperation, INTERFACE_AUTHORIZATION)

        val finalResult = result.toModels(searchModel)

        val pageFormat = PagableList(finalResult as MutableList<CreditNoteModel>)
        pageFormat.setPage(nativeResult.getPage())
        pageFormat.setPageSize(nativeResult.getPageSize())
        pageFormat.setTotalSize(nativeResult.getTotalSize())
        return pageFormat

    }

    /**
     * Method to build criteria for search credit note
     */
    protected open fun buildCriteria(searchModel: CreditNoteSearchModel): Pair<MutableMap<String, Any>, MutableMap<String, String>> {

        val param = mutableMapOf<String, Any>()
        val operation = mutableMapOf<String, String>()

        // Default status for UI to query data
        param[InterfaceBaseCreditNote<*>::status.name] = listOf(RecordStatus.INVALID.name)
        operation[InterfaceBaseCreditNote<*>::status.name] = SearchCriteriaOperation.NOT_IN.name

        if (searchModel.statuses.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::status.name] = searchModel.statuses!!
            operation[InterfaceBaseCreditNote<*>::status.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.isNormalSubtype == true) {
            param[InterfaceBaseCreditNote<*>::creditNoteSubType.name] = NullOperator.IS_NULL.name
            operation[InterfaceBaseCreditNote<*>::creditNoteSubType.name] = SearchCriteriaOperation.ISNULL.name
        }

        if (searchModel.isNormalSubtype == false) {
            param[InterfaceBaseCreditNote<*>::creditNoteSubType.name] = NullOperator.NOT_NULL.name
            operation[InterfaceBaseCreditNote<*>::creditNoteSubType.name] = SearchCriteriaOperation.NOTNULL.name
        }

        if (searchModel.lifecycles.isNullOrEmpty().not() && searchModel.isRdSubmitted == null) {
            param[InterfaceBaseCreditNote<*>::lifecycle.name] = searchModel.lifecycles!!
            operation[InterfaceBaseCreditNote<*>::lifecycle.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.documentEntryDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::documentEntryDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.documentEntryDateFrom!!)
            operation[InterfaceBaseCreditNote<*>::documentEntryDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.documentEntryDateTo.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::documentEntryDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.documentEntryDateTo!!)
            operation[InterfaceBaseCreditNote<*>::documentEntryDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.linearIds.isNullOrEmpty().not()) {
            val finalLinearIds = searchModel.linearIds!!.distinct()
            param[InterfaceBaseCreditNote<*>::linearId.name] = finalLinearIds
            operation[InterfaceBaseCreditNote<*>::linearId.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.invoiceExternalId.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::invoiceExternalId.name] = searchModel.invoiceExternalId!!
            operation[InterfaceBaseCreditNote<*>::invoiceExternalId.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.vendorName.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::vendorName.name] = searchModel.vendorName!!
            operation[InterfaceBaseCreditNote<*>::vendorName.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.vendorNumber.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::vendorNumber.name] = searchModel.vendorNumber!!
            operation[InterfaceBaseCreditNote<*>::vendorNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.vendorTaxNumber.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::vendorTaxNumber.name] = searchModel.vendorTaxNumber!!
            operation[InterfaceBaseCreditNote<*>::vendorTaxNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.companyTaxNumber.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::companyTaxNumber.name] = searchModel.companyTaxNumber!!
            operation[InterfaceBaseCreditNote<*>::companyTaxNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.companyCode.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::companyCode.name] = searchModel.companyCode!!
            operation[InterfaceBaseCreditNote<*>::companyCode.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.companyName.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::companyName.name] = searchModel.companyName!!
            operation[InterfaceBaseCreditNote<*>::companyName.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.creditNoteDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::creditNoteDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.creditNoteDateFrom!!)
            operation[InterfaceBaseCreditNote<*>::creditNoteDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.creditNoteDateTo.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::creditNoteDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.creditNoteDateTo!!)
            operation[InterfaceBaseCreditNote<*>::creditNoteDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.creditNotePostingDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::creditPostingUpdatedDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.creditNotePostingDateFrom!!)
            operation[InterfaceBaseCreditNote<*>::creditPostingUpdatedDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.creditNotePostingDateTo.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::creditPostingUpdatedDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.creditNotePostingDateTo!!)
            operation[InterfaceBaseCreditNote<*>::creditPostingUpdatedDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.creditNoteExternalId.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::externalId.name] = searchModel.creditNoteExternalId!!
            operation[InterfaceBaseCreditNote<*>::externalId.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.invoiceLinearId.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::invoiceLinearId.name] = searchModel.invoiceLinearId!!
            operation[InterfaceBaseCreditNote<*>::invoiceLinearId.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.referenceField1.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::referenceField1.name] = searchModel.referenceField1!!
            operation[InterfaceBaseCreditNote<*>::referenceField1.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.adjustmentType.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::adjustmentType.name] = searchModel.adjustmentType!!
            operation[InterfaceBaseCreditNote<*>::adjustmentType.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.matchingDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::lastMatchUpdatedDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.matchingDateFrom!!)
            operation[InterfaceBaseCreditNote<*>::lastMatchUpdatedDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.matchingDateTo.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::lastMatchUpdatedDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.matchingDateTo!!)
            operation[InterfaceBaseCreditNote<*>::lastMatchUpdatedDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        // query by using field sibling payment items
        if (searchModel.isIssuePayment == true) {
            param[InterfaceBaseCreditNote<*>::paymentItemLinearId.name] = NullOperator.NOT_NULL.name
            operation[InterfaceBaseCreditNote<*>::paymentItemLinearId.name] = SearchCriteriaOperation.NOTNULL.name
        }

        if (searchModel.isIssuePayment == false) {
            param[InterfaceBaseCreditNote<*>::paymentItemLinearId.name] = NullOperator.IS_NULL.name
            operation[InterfaceBaseCreditNote<*>::paymentItemLinearId.name] = SearchCriteriaOperation.ISNULL.name
        }

        if (searchModel.paymentDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::paymentDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.paymentDateFrom!!)
            operation[InterfaceBaseCreditNote<*>::paymentDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.paymentDateTo.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::paymentDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.paymentDateTo!!)
            operation[InterfaceBaseCreditNote<*>::paymentDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.buyerPostingDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::buyerPostingDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.buyerPostingDateFrom!!)
            operation[InterfaceBaseCreditNote<*>::buyerPostingDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.buyerPostingDateTo.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::buyerPostingDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.buyerPostingDateTo!!)
            operation[InterfaceBaseCreditNote<*>::buyerPostingDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.creditPostingUpdatedDate.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::creditPostingUpdatedDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.creditPostingUpdatedDate!!)
            operation[InterfaceBaseCreditNote<*>::creditPostingUpdatedDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
            param[InterfaceBaseCreditNote<*>::creditPostingUpdatedDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.creditPostingUpdatedDate)
            operation[InterfaceBaseCreditNote<*>::creditPostingUpdatedDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.paymentItemLinearIds.isNullOrEmpty().not()) {
            when (searchModel.paymentItemLinearIds!!.size) {
                1 -> {
                    param[InterfaceBaseCreditNote<*>::paymentItemLinearId.name] = searchModel.paymentItemLinearIds.single()
                    operation[InterfaceBaseCreditNote<*>::paymentItemLinearId.name] = SearchCriteriaOperation.EQUAL.name
                }
                else -> {
                    param[InterfaceBaseCreditNote<*>::paymentItemLinearId.name] = searchModel.paymentItemLinearIds
                    operation[InterfaceBaseCreditNote<*>::paymentItemLinearId.name] = SearchCriteriaOperation.IN.name
                }
            }
        }

        if (searchModel.vatTriggerPoints.isNullOrEmpty().not()) {
            param[CreditNoteModel::vatTriggerPoint.name] = searchModel.vatTriggerPoints!!
            operation[CreditNoteModel::vatTriggerPoint.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.taxDocumentLinearIds.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::taxDocumentLinearId.name] = searchModel.taxDocumentLinearIds!!
            operation[InterfaceBaseCreditNote<*>::taxDocumentLinearId.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.taxDocumentNumber.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::taxDocumentNumber.name] = searchModel.taxDocumentNumber!!
            operation[InterfaceBaseCreditNote<*>::taxDocumentNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.paymentDescription.isNullOrEmpty().not()) {
            val (value, searchOperation) = determineOperationFromValue(searchModel.paymentDescription!!)
            param[InterfaceBaseCreditNote<*>::paymentDescription.name] = value
            searchOperation?.let { operation[InterfaceBaseCreditNote<*>::paymentDescription.name] = it }
        }

        if (searchModel.isOnHold != null) {
            param[InterfaceBaseCreditNote<*>::isOnHold.name]  = searchModel.isOnHold
        }

        if (searchModel.creditNoteSubType.isNullOrEmpty().not()) {
            param[InterfaceBaseCreditNote<*>::creditNoteSubType.name] = searchModel.creditNoteSubType!!
            operation[InterfaceBaseCreditNote<*>::creditNoteSubType.name] = SearchCriteriaOperation.EQUAL.name
        }

        if (searchModel.contractNumber.isNullOrEmpty().not()) {
            val itemField = InterfaceBaseCreditNote<*>::creditNoteItems.name + "." + InterfaceBaseCreditNoteItem::contractNumber.name
            param[itemField] = searchModel.contractNumber!!
            operation[itemField] = SearchCriteriaOperation.EQUAL.name
        }

        return param to operation
    }

    /**
     * Method to build criteria with extra condition for search credit note
     */
    protected open fun buildExtraSearchCriteria(creditNoteSearchModel: CreditNoteSearchModel): SearchCriterias<H>? {
        val baseSearchCriteria = SearchCriterias(headerClass)

        if (creditNoteSearchModel.exactCreditNoteBuyerPostingDate.isNullOrEmpty().not()) {
            val criteria = SearchCriterias(headerClass)

            val lessThanSearchCriteria = SearchCriteria()
            lessThanSearchCriteria.setField(InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerPostingDate.name)
            lessThanSearchCriteria.setValue(DateUtility.convertDateToString(Date.from(creditNoteSearchModel.exactCreditNoteBuyerPostingDate!!.toZonedDateTime().toInstant()), INSTANT_FORMAT))
            lessThanSearchCriteria.setOp(SearchCriteriaOperation.LESSTHAN)
            criteria.getCriterias().add(lessThanSearchCriteria)

            val nullSearchCriteria = SearchCriteria()
            nullSearchCriteria.setField(InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerPostingDate.name)
            nullSearchCriteria.setValue("null")
            nullSearchCriteria.setOp(SearchCriteriaOperation.ISNULL)
            nullSearchCriteria.setAnd(false)
            criteria.getCriterias().add(nullSearchCriteria)

            baseSearchCriteria.getCriterias().add(criteria)
        }

        if (creditNoteSearchModel.postingStatus.isNullOrEmpty().not()) {
            val sc = generateCriteriaMap(headerClass, InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerPostingStatus.name, creditNoteSearchModel.postingStatus!!, SearchCriteriaOperation.IN)
            baseSearchCriteria.getCriterias().add(sc)
        }

        if (creditNoteSearchModel.postingStatusNotIn.isNullOrEmpty().not()) {
            val sc = generateCriteriaMap(headerClass, InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerPostingStatus.name, creditNoteSearchModel.postingStatusNotIn!!, SearchCriteriaOperation.NOT_IN)
            baseSearchCriteria.getCriterias().add(sc)
        }

        if (creditNoteSearchModel.lifecycles?.contains(Lifecycle.CreditNoteLifecycle.REJECTED.name) == true &&
            creditNoteSearchModel.isRdSubmitted != null) {

            val searchRejected = generateCriteriaMap(
                headerClass,
                InterfaceBaseCreditNote<*>::lifecycle.name,
                Lifecycle.CreditNoteLifecycle.REJECTED.name,
                SearchCriteriaOperation.EQUAL
            )

            val searchRdSubmittedDate = generateCriteriaMap(
                headerClass,
                InterfaceBaseCreditNote<*>::rdSubmittedDate.name,
                NULL,
                when(creditNoteSearchModel.isRdSubmitted) {
                    true -> SearchCriteriaOperation.NOTNULL
                    false -> SearchCriteriaOperation.ISNULL
                }
            )

            val filteredLifecycles = creditNoteSearchModel.lifecycles.filter { it != Lifecycle.CreditNoteLifecycle.REJECTED.name }
            if (filteredLifecycles.isNullOrEmpty().not()) {
                val criteria = SearchCriterias(headerClass)
                val subCriterias = SearchCriterias(headerClass)
                val searchExceptRejected = generateCriteriaMap(
                    headerClass,
                    InterfaceBaseCreditNote<*>::lifecycle.name,
                    filteredLifecycles,
                    SearchCriteriaOperation.IN
                )
                subCriterias.getCriterias().addAll(listOf(searchRejected, searchRdSubmittedDate))
                subCriterias.setAnd(false)
                criteria.getCriterias().addAll(listOf(searchExceptRejected, subCriterias))
                baseSearchCriteria.getCriterias().add(criteria)

            }
            else {
                baseSearchCriteria.getCriterias().addAll(listOf(searchRejected, searchRdSubmittedDate))
            }
        }

        return baseSearchCriteria
    }
}