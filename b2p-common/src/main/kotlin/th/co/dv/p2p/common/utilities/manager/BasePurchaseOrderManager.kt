package th.co.dv.p2p.common.utilities.manager

import net.corda.core.node.services.vault.NullOperator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.enums.RecordStatus
import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.*
import th.co.dv.p2p.common.utilities.AuthorizationUtils.INTERFACE_AUTHORIZATION
import th.co.dv.p2p.common.utilities.BaseManagerUtils.filteredEligibleField
import th.co.dv.p2p.common.utilities.BaseManagerUtils.getColumnName
import th.co.dv.p2p.common.utilities.BaseManagerUtils.inferSortDirection
import th.co.dv.p2p.common.utilities.DateUtility.Companion.convertToEndOfDayTimestamp
import th.co.dv.p2p.common.utilities.DateUtility.Companion.convertToStartOfDayTimestamp
import th.co.dv.p2p.common.utilities.manager.BasePurchaseOrderStatements.DEFAULT_WHERE
import th.co.dv.p2p.common.utilities.search.SearchCriteria
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import th.co.dv.p2p.common.utilities.search.paging.PagableList
import th.co.dv.p2p.corda.base.domain.DeleteFlag
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import th.co.dv.p2p.corda.base.models.PurchaseSearchModel
import java.math.BigDecimal

abstract class BasePurchaseOrderManager<H : ModelableEntity, I : ModelableEntity> {

    private val queryableService by lazy { initQueryableService() }
    private val itemsManager by lazy { initItemsManager() }
    abstract fun initQueryableService(): QueryableService<H>
    abstract fun initItemsManager(): BasePurchaseItemManager<I>

    protected open lateinit var headerClass: Class<H>
    protected open lateinit var itemClass: Class<I>

    protected open var logger: Logger = LoggerFactory.getLogger(BasePurchaseOrderManager::class.java)
    protected open var className = BasePurchaseOrderManager::class.java.simpleName

    protected val subQueryParameterValue = mutableListOf<List<Any?>>()

    /**
     * Method to search purchase order using condition in purchase search model
     */
    fun searchQuery(searchModel: PurchaseSearchModel, selectFields: List<String> = emptyList(), userAuthorization: UserAuthorization, getTotal:Boolean = true): PagableList<PurchaseOrderModel> {

        logger.info("$className.searchQuery searchModel: $searchModel")
        val (param, operation) = buildCriteria(searchModel)

        val extraCriteria = buildExtraSearchCriteria(searchModel)

        val result = when (selectFields.isEmpty()) {
            true -> queryByParam(param, operation, searchModel, extraCriteria, userAuthorization)
            false -> nativeQuery(param, operation, searchModel, extraCriteria, selectFields, userAuthorization, getTotal)
        }
        logger.info("$className.searchQuery result: ${result.size}")

        // Need to clear this list every time finished query
        subQueryParameterValue.clear()
        return result
    }

    /**
     * Method query invoice using native query in case of user not specific select field
     * first query with native query to get linear id and use linear id to get invoice model
     */
    protected fun queryByParam(param: Map<String, Any>,
                               operation: Map<String, String> = emptyMap(),
                               searchModel: PurchaseSearchModel,
                               extraCriteria: SearchCriterias<H>,
                               userAuthorization: UserAuthorization): PagableList<PurchaseOrderModel> {

        val nativeResult = nativeQuery(
            param = param,
            operation = operation,
            searchModel = searchModel,
            extraCriteria = extraCriteria,
            selectFields = listOf(PurchaseOrderModel::initialTotal.name, PurchaseOrderModel::remainingTotal.name),
            userAuthorization = userAuthorization
        )

        if (nativeResult.isEmpty()) return PagableList(mutableListOf())

        val linearIdOperation = mapOf(InterfaceBasePurchaseOrder<*>::linearId.name to SearchCriteriaOperation.IN.name)
        val result = mutableListOf<H>()
        nativeResult.map { it.linearId }.chunked(MAX_WHERE_IN_ITEM).forEach {
            val linearIdParam = mapOf(
                InterfaceBasePurchaseOrder<*>::linearId.name to it,
                PurchaseSearchModel::pageNumber.name to searchModel.pageNumber.toString(),
                PurchaseSearchModel::pageSize.name to searchModel.pageSize.toString(),
                PurchaseSearchModel::sortField.name to computeSortField(searchModel.sortField, false),
                PurchaseSearchModel::sortOrder.name to inferSortDirection(searchModel.sortOrder),
                IS_PAGING to false
            )

            result.addAll(queryableService.findByParam(linearIdParam, linearIdOperation, INTERFACE_AUTHORIZATION))
        }

        // copy initialTotal,remainingTotal and items from nativeResult
        val finalResult = result.toModels(PurchaseSearchModel(returnPurchaseItems = false)).map { data ->
            val purchase = data as PurchaseOrderModel
            val purchaseFromNative = nativeResult.find { it.linearId == purchase.linearId }
            purchase.copy(
                initialTotal = purchaseFromNative?.initialTotal,
                total = purchaseFromNative?.total,
                remainingTotal = purchaseFromNative?.remainingTotal,
                purchaseItems = purchaseFromNative?.purchaseItems ?: emptyList()
            )

        }

        val pageFormat = PagableList(finalResult as MutableList<PurchaseOrderModel>)
        pageFormat.setPage(nativeResult.getPage())
        pageFormat.setPageSize(nativeResult.getPageSize())
        pageFormat.setTotalSize(nativeResult.getTotalSize())
        return pageFormat
    }

    /**
     * Method for native query get initial total include tax
     * will return if field initialTotal in selectFields
     *
     * @param selectFields: list of select field
     * @param purchaseItems: list of purchase item model
     * @return total
     */
    private fun getSumOfInitialTotalIncludeTaxForNative(purchaseItems: List<PurchaseItemModel>): BigDecimal? {
        val eachRateWithTaxTotal = purchaseItems.filter { it.deleteFlag == null }.groupBy { it.taxRate }
            .mapValues {
                val taxRate = it.key
                val groupPurchaseItems = it.value

                // initialTotal of group purchase items by tax rate
                val initialTotal = PurchaseUtils.calculatePurchaseOrderInitialRemaining(groupPurchaseItems).initialTotal ?: BigDecimal.ZERO

                // initialTotal * (1 + taxRate/100)
                initialTotal.times(taxRate?.movePointLeft(2)?.plus(BigDecimal.ONE) ?: BigDecimal.ONE)
            }

        // then sum each of the total include tax
        return eachRateWithTaxTotal.values.sumByDecimal { it }
    }

    /**
     * Method to search purchase order using condition in purchase search model
     */
    @Suppress("UNCHECKED_CAST")
    protected fun nativeQuery(param: Map<String, Any>,
                              operation: Map<String, String> = emptyMap(),
                              searchModel: PurchaseSearchModel,
                              extraCriteria: SearchCriterias<H>,
                              selectFields: List<String> = emptyList(),
                              userAuthorization: UserAuthorization,
                              getTotal: Boolean = true): PagableList<PurchaseOrderModel> {

        val sc = SearchCriterias(this.headerClass)
        sc.getCriterias().add(extraCriteria)
        sc.setPage(searchModel.pageNumber)

        val offset = BasePurchaseOrderStatements.OFFSET.replace(BasePurchaseOrderStatements.OFFSET_PLACEHOLDER, ((searchModel.pageNumber - 1) * searchModel.pageSize).toString())
        val limit = BasePurchaseOrderStatements.FETCH.replace(BasePurchaseOrderStatements.FETCH_PLACEHOLDER, searchModel.pageSize.toString())

        // Subtract default field from select field to prevent duplication grouping
        val filteredSelectFields = selectFields.subtract(BasePurchaseOrderStatements.defaultField.toSet()).toList()
        // Convert select field to table column
        val convertedSelectFields = getColumnName(filteredSelectFields, headerClass = headerClass, itemClass = itemClass)
        val eligibleSelectField = filteredEligibleField(selectFields, headerClass = headerClass, itemClass = itemClass, itemFieldName = InterfaceBasePurchaseOrder<*>::purchaseItems.name)

        // customSelect is database column name used for SELECT clause in sql
        // groupByClause is database column name used for GROUP BY clause in sql
        // fields is list of field name in entity used for convert sql result to model
        val (customSelect, groupByClause, fields) = when (convertedSelectFields.isEmpty()) {
            true -> Triple(BasePurchaseOrderStatements.PURCHASE_ORDER_SELECT_SQL, BasePurchaseOrderStatements.GROUP_BY, BasePurchaseOrderStatements.defaultField)

            false -> Triple(BasePurchaseOrderStatements.PURCHASE_ORDER_SELECT_SQL + convertedSelectFields.joinToString(separator = ", ", prefix = ", "),

                BasePurchaseOrderStatements.GROUP_BY + convertedSelectFields.joinToString(separator = ", ", prefix = ", "),
                (BasePurchaseOrderStatements.defaultField + eligibleSelectField).distinct())
        }

        // We need to append sort field to group by if sort field is not group by field
        val (groupBy, sortField) = computeGroupByClause(groupByClause, searchModel.sortField)

        val orderBy = BasePurchaseOrderStatements.ORDER_BY
            .replace(BasePurchaseOrderStatements.ORDER_BY_PLACEHOLDER, sortField)
            .replace(BasePurchaseOrderStatements.ORDER_DIRECTION_PLACEHOLDER, inferSortDirection(searchModel.sortOrder))

        // if gtRemaining not null
        val finalDefaultWhere = if (searchModel.gtRemainingQuantity != null) {
            DEFAULT_WHERE.replace(BasePurchaseOrderStatements.DEFAULT_WHERE_VALUE_PLACEHOLDER, searchModel.gtRemainingQuantity.toString())
        } else {
            EMPTY_STRING
        }

        val nativeQueryModel = NativeQueryModel(
            customSelect = customSelect,
            groupBy = groupBy,
            userAuth = userAuthorization,
            param = param,
            subQueryValue = subQueryParameterValue,
            defaultWhere = finalDefaultWhere,
            fields = fields,
            operation = operation,
            orderBy = orderBy,
            offset = offset,
            fetch = limit,
            queryTotalRecords = getTotal
        )

        val purchaseOrderDataList = (queryableService.native(nativeQueryModel, sc) as PagableList<ModelableEntity>)
            .toPagableModels(PurchaseSearchModel(returnPurchaseItems = false)) as PagableList<PurchaseOrderModel>

        val relatedPurchaseItems = getRelatedPurchaseItems(searchModel, purchaseOrderDataList)
        val relatedPurchaseItemsByPurchaseId = relatedPurchaseItems.groupBy { it.purchaseOrderLinearId }


        val result = purchaseOrderDataList.map { purchaseOrderModel ->
            val items = relatedPurchaseItemsByPurchaseId[purchaseOrderModel.linearId] ?: emptyList()
            val total = getSumOfInitialTotalIncludeTaxForNative(items)
            purchaseOrderModel.copy(
                total = total,
                purchaseItems = if (searchModel.returnPurchaseItems) items else emptyList()
            )
        }.let { list ->
            if (searchModel.gtRemainingAmount != null) {
                list.filter { (it.remainingTotal ?: BigDecimal.ZERO) > searchModel.gtRemainingAmount }
            } else list
        }

        logger.info("$className.nativeQuery result from native query , total Record : ${purchaseOrderDataList.getTotalSize()}, returns item ${searchModel.returnPurchaseItems}")

        val pageFormat = PagableList(result as MutableList<PurchaseOrderModel>)
        pageFormat.setPage(purchaseOrderDataList.getPage())
        pageFormat.setPageSize(purchaseOrderDataList.getPageSize())
        pageFormat.setTotalSize(purchaseOrderDataList.getTotalSize())
        return pageFormat
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
        logger.info("$className.computeGroupByClause updateGroupBy: $updateGroupBy, formattedSortField: $finalSortField")

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
            defaultField = BasePurchaseOrderStatements.defaultSortField,
            headerClass = headerClass,
            itemClass = itemClass,
            isTableColumn = isTableColumn
        )
    }

    /**
     * Method for check the selectFields need initialTotal field or not
     */
    protected fun needInitialTotalField(selectFields: List<String>): Boolean = selectFields.contains(PurchaseOrderModel::initialTotal.name)

    /**
     * Method for check the selectFields need remainingTotal field or not
     */
    protected fun needRemainingTotalField(selectFields: List<String>): Boolean = selectFields.contains(PurchaseOrderModel::remainingTotal.name)

    /**
     * Method for check the current process need to get items or not
     *
     * @param searchModel : PurchaseSearchModel
     * @param selectFields : list of select fields
     */
    protected fun needToGetItems(searchModel: PurchaseSearchModel): Boolean {
        return searchModel.returnPurchaseItems
    }

    /**
     * Method for ger related purchaseItem
     *
     * @param searchModel : PurchaseSearchModel
     * @param selectFields : list of select fields
     * @param listPurchaseOrderModel : list of purchaseOrder model
     */
    protected fun getRelatedPurchaseItems(searchModel: PurchaseSearchModel, listPurchaseOrderModel: List<PurchaseOrderModel>): List<PurchaseItemModel> {
        if (!needToGetItems(searchModel)) return emptyList()
        val chunkedPurchaseOrderLinearIds = listPurchaseOrderModel.mapNotNull { it.linearId }.chunked(maxQuerySize)

        return chunkedPurchaseOrderLinearIds.flatMap { itemsManager.searchQuery(PurchaseSearchModel(purchaseOrderLinearIds = it, pageSize = Int.MAX_VALUE), userAuthorization = INTERFACE_AUTHORIZATION, requiredJoin = false) }
    }

    /**
     * Method to build criteria for search purchase order
     */
    protected open fun buildCriteria(searchModel: PurchaseSearchModel): Pair<MutableMap<String, Any>, MutableMap<String, String>> {

        val param = mutableMapOf<String, Any>()
        val operation = mutableMapOf<String, String>()

        param[InterfaceBasePurchaseOrder<*>::status.name] = listOf(RecordStatus.INVALID.name)
        operation[InterfaceBasePurchaseOrder<*>::status.name] = SearchCriteriaOperation.NOT_IN.name

        if (searchModel.purchaseOrderExternalId.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::purchaseOrderNumber.name] = searchModel.purchaseOrderExternalId!!
            operation[InterfaceBasePurchaseOrder<*>::purchaseOrderNumber.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.exactPurchaseOrderExternalId.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::purchaseOrderNumber.name] = searchModel.exactPurchaseOrderExternalId!!
        }

        if (searchModel.linearId.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::linearId.name] = searchModel.linearId!!
        }

        if (searchModel.statuses.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::lifecycle.name] = searchModel.statuses!!
            operation[InterfaceBasePurchaseOrder<*>::lifecycle.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.vendorTaxNumbers.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::vendorTaxNumber.name] = searchModel.vendorTaxNumbers!!
            operation[InterfaceBasePurchaseOrder<*>::vendorTaxNumber.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.businessPlaceTaxNumbers.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::businessPlaceTaxNumber.name] = searchModel.businessPlaceTaxNumbers!!
            operation[InterfaceBasePurchaseOrder<*>::businessPlaceTaxNumber.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.externalId.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::purchaseOrderNumber.name] = searchModel.externalId!!
            operation[InterfaceBasePurchaseOrder<*>::purchaseOrderNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.purchaseRequestNumber.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::purchaseRequestNumber.name
            param[itemField] = searchModel.purchaseRequestNumber!!
            operation[itemField] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.vendorNumber.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::vendorNumber.name] = searchModel.vendorNumber!!
            operation[InterfaceBasePurchaseOrder<*>::vendorNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.vendorName.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::vendorName.name] = searchModel.vendorName!!
            operation[InterfaceBasePurchaseOrder<*>::vendorName.name] = SearchCriteriaOperation.CONTAIN.name
        }

        if (searchModel.companyCode.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::companyCode.name] = searchModel.companyCode!!
            operation[InterfaceBasePurchaseOrder<*>::companyCode.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.companyName.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::companyName.name] = searchModel.companyName!!
            operation[InterfaceBasePurchaseOrder<*>::companyName.name] = SearchCriteriaOperation.CONTAIN.name
        }

        if (searchModel.businessPlaceOfficerName.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::businessPlaceOfficerName.name] = searchModel.businessPlaceOfficerName!!
            operation[InterfaceBasePurchaseOrder<*>::businessPlaceOfficerName.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.businessPlaceTaxNumber.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::businessPlaceTaxNumber.name] = searchModel.businessPlaceTaxNumber!!
            operation[InterfaceBasePurchaseOrder<*>::businessPlaceTaxNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.exactBusinessPlaceTaxNumber.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::businessPlaceTaxNumber.name] = searchModel.exactBusinessPlaceTaxNumber!!
        }

        if (searchModel.businessPlace.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::businessPlaceAddress1.name] = searchModel.businessPlace!!
            operation[InterfaceBasePurchaseOrder<*>::businessPlaceAddress1.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.paymentTermCode.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::paymentTermCode.name] = searchModel.paymentTermCode!!
            operation[InterfaceBasePurchaseOrder<*>::paymentTermCode.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.taxCode.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::taxCode.name
            param[itemField] = searchModel.taxCode!!
            operation[itemField] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.purchaseUpdatedDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::lastUpdatedDate.name + FROM] = convertToStartOfDayTimestamp(searchModel.purchaseUpdatedDateFrom!!)
            operation[InterfaceBasePurchaseOrder<*>::lastUpdatedDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.purchaseUpdatedDateTo.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::lastUpdatedDate.name + TO] = convertToEndOfDayTimestamp(searchModel.purchaseUpdatedDateTo!!)
            operation[InterfaceBasePurchaseOrder<*>::lastUpdatedDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.vendorTaxNumber.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::vendorTaxNumber.name] = searchModel.vendorTaxNumber!!
        }

        if (searchModel.paymentTermDays != null) {
            param[InterfaceBasePurchaseOrder<*>::paymentTermDays.name] = searchModel.paymentTermDays
        }

        if (searchModel.paymentTermMonths != null) {
            param[InterfaceBasePurchaseOrder<*>::paymentTermMonths.name] = searchModel.paymentTermMonths
        }

        if (searchModel.exactContractNumber != null) {
            param[InterfaceBasePurchaseOrder<*>::contractNumber.name] = searchModel.exactContractNumber
        }

        if (searchModel.companyBranchCode.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::companyBranchCode.name] = searchModel.companyBranchCode!!
            operation[InterfaceBasePurchaseOrder<*>::companyBranchCode.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.purchasingGroup.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::purchasingGroup.name
            param[itemField] = searchModel.purchasingGroup!!
            operation[itemField] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.deleteFlag.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::deleteFlag.name
            when(enumValueOrNull<NullOperator>(searchModel.deleteFlag!!)){
                NullOperator.IS_NULL -> {
                    param[itemField] = NullOperator.IS_NULL.name
                    operation[itemField] = SearchCriteriaOperation.ISNULL.name
                }
                NullOperator.NOT_NULL -> {
                    param[itemField] = NullOperator.NOT_NULL.name
                    operation[itemField] = SearchCriteriaOperation.NOTNULL.name
                }
                null -> {
                    param[itemField] = searchModel.deleteFlag
                    operation[itemField] = SearchCriteriaOperation.STARTS_WITH.name
                }
            }
        }

        if (searchModel.referenceField1.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::referenceField1.name] = searchModel.referenceField1!!
            operation[InterfaceBasePurchaseOrder<*>::referenceField1.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.referenceField2.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::referenceField2.name] = searchModel.referenceField2!!
            operation[InterfaceBasePurchaseOrder<*>::referenceField2.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.referenceField3.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::referenceField3.name] = searchModel.referenceField3!!
            operation[InterfaceBasePurchaseOrder<*>::referenceField3.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.referenceField4.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::referenceField4.name] = searchModel.referenceField4!!
            operation[InterfaceBasePurchaseOrder<*>::referenceField4.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.referenceField5.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::referenceField5.name] = searchModel.referenceField5!!
            operation[InterfaceBasePurchaseOrder<*>::referenceField5.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.vendorOfficerName.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::vendorOfficerName.name] = searchModel.vendorOfficerName!!
            operation[InterfaceBasePurchaseOrder<*>::vendorOfficerName.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.issuedDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::issuedDate.name + FROM] = convertToStartOfDayTimestamp(searchModel.issuedDateFrom!!)
            operation[InterfaceBasePurchaseOrder<*>::issuedDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.issuedDateTo.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::issuedDate.name + TO] = convertToEndOfDayTimestamp(searchModel.issuedDateTo!!)
            operation[InterfaceBasePurchaseOrder<*>::issuedDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.documentEntryDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::documentEntryDate.name + FROM] = convertToStartOfDayTimestamp(searchModel.documentEntryDateFrom!!)
            operation[InterfaceBasePurchaseOrder<*>::documentEntryDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.documentEntryDateTo.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::documentEntryDate.name + TO] = convertToEndOfDayTimestamp(searchModel.documentEntryDateTo!!)
            operation[InterfaceBasePurchaseOrder<*>::documentEntryDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.linearIds.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseOrder<*>::linearId.name] = searchModel.linearIds!!
            operation[InterfaceBasePurchaseOrder<*>::linearId.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.materialDescription.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::materialDescription.name
            param[itemField] = searchModel.materialDescription!!
            operation[itemField] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.site.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::site.name
            param[itemField] = searchModel.site!!
            operation[itemField] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.proposedRevisedDeliveryDateFrom.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::proposedRevisedDeliveryDate.name + FROM
            param[itemField] = convertToStartOfDayTimestamp(searchModel.proposedRevisedDeliveryDateFrom!!)
            operation[itemField] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.proposedRevisedDeliveryDateTo.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::proposedRevisedDeliveryDate.name + TO
            param[itemField] = convertToEndOfDayTimestamp(searchModel.proposedRevisedDeliveryDateTo!!)
            operation[itemField] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.itemDeliveryDateFrom.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::expectedDeliveryDate.name + FROM
            param[itemField] = convertToStartOfDayTimestamp(searchModel.itemDeliveryDateFrom!!)
            operation[itemField] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.itemDeliveryDateTo.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::expectedDeliveryDate.name + TO
            param[itemField] = convertToEndOfDayTimestamp(searchModel.itemDeliveryDateTo!!)
            operation[itemField] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.isItemAdvanceRemainingAmountGreaterThanZero == true) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::advancePaymentRemainingAmount.name
            param[itemField] = BigDecimal.ZERO
            operation[itemField] = SearchCriteriaOperation.GREATERTHAN.name
        }

        if (searchModel.itemEffectiveDateTo.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::effectiveDate.name + TO
            param[itemField] = convertToEndOfDayTimestamp(searchModel.itemEffectiveDateTo!!)
            operation[itemField] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.itemCategory.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::itemCategory.name
            param[itemField] = searchModel.itemCategory!!
        }

        if (searchModel.purchaseItemLinearIds.isNullOrEmpty().not()) {
            val itemField = InterfaceBasePurchaseOrder<*>::purchaseItems.name + DOT + InterfaceBasePurchaseItem::linearId.name
            param[itemField] = searchModel.purchaseItemLinearIds!!
            operation[itemField] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.headerDeleteFlag.isNullOrEmpty().not()) {
            when (searchModel.headerDeleteFlag) {
                NullOperator.IS_NULL.name -> {
                    param[InterfaceBasePurchaseOrder<*>::deleteFlag.name] = NullOperator.IS_NULL.name
                    operation[InterfaceBasePurchaseOrder<*>::deleteFlag.name] = SearchCriteriaOperation.ISNULL.name
                }
                DeleteFlag.DELETED.name -> param[InterfaceBasePurchaseOrder<*>::deleteFlag.name] =
                    DeleteFlag.DELETED.name
            }
        }

        if (searchModel.gtRemainingTotal != null) {
            param[InterfaceBasePurchaseOrder<*>::remainingTotal.name] = searchModel.gtRemainingTotal
            operation[InterfaceBasePurchaseOrder<*>::remainingTotal.name] =
                SearchCriteriaOperation.GREATERTHAN.name
        }

        return param to operation
    }

    /**
     * Method to build criteria with extra condition for search purchase order
     */
    protected fun buildExtraSearchCriteria(searchModel: PurchaseSearchModel) : SearchCriterias<H> {

        val searchCriterias = SearchCriterias(this.headerClass)

        if (searchModel.contractNumber.isNullOrEmpty().not()) {
            val contractNumberCriteria = SearchCriterias(this.headerClass)

            val criteria1 = SearchCriteria()
            criteria1.setField(InterfaceBasePurchaseOrder<*>::contractNumber.name)
            criteria1.setValue(searchModel.contractNumber!!)

            val criteria2 = SearchCriteria()
            criteria2.setField(InterfaceBasePurchaseOrder<*>::contractNumber.name)
            criteria2.setValue(NULL)
            criteria2.setOp(SearchCriteriaOperation.ISNULL)
            criteria2.setAnd(false)

            contractNumberCriteria.getCriterias().addAll(listOf(criteria1, criteria2))
            searchCriterias.getCriterias().add(contractNumberCriteria)
        }

        if (searchModel.vendorOfficerEmailIsNull != null) {
            val vendorOfficerEmailCriteria = SearchCriterias(this.headerClass)
            when (searchModel.vendorOfficerEmailIsNull) {
                true -> {
                    val criteria1 = SearchCriteria()
                    criteria1.setField(InterfaceBasePurchaseOrder<*>::vendorOfficerEmail.name)
                    criteria1.setValue("")

                    val criteria2 = SearchCriteria()
                    criteria2.setField(InterfaceBasePurchaseOrder<*>::vendorOfficerEmail.name)
                    criteria2.setValue(NULL)
                    criteria2.setOp(SearchCriteriaOperation.ISNULL)
                    criteria2.setAnd(false)

                    vendorOfficerEmailCriteria.getCriterias().addAll(listOf(criteria1, criteria2))
                    searchCriterias.getCriterias().add(vendorOfficerEmailCriteria)
                }
                false -> {
                    val criteria1 = SearchCriteria()
                    criteria1.setField(InterfaceBasePurchaseOrder<*>::vendorOfficerEmail.name)
                    criteria1.setValue(NULL)
                    criteria1.setOp(SearchCriteriaOperation.NOTNULL)

                    val criteria2 = SearchCriteria()
                    criteria2.setField(InterfaceBasePurchaseOrder<*>::vendorOfficerEmail.name)
                    criteria2.setValue("")
                    criteria2.setOp(SearchCriteriaOperation.NOT_EQUAL)

                    vendorOfficerEmailCriteria.getCriterias().addAll(listOf(criteria1, criteria2))
                    searchCriterias.getCriterias().add(vendorOfficerEmailCriteria)
                }
            }
        }

        return searchCriterias
    }

}