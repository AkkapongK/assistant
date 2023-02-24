package th.co.dv.p2p.common.utilities.manager

import net.corda.core.node.services.vault.NullOperator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.EMPTY_STRING
import th.co.dv.p2p.common.constants.FROM
import th.co.dv.p2p.common.constants.TO
import th.co.dv.p2p.common.enums.RecordStatus
import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.BaseManagerUtils
import th.co.dv.p2p.common.utilities.DateUtility
import th.co.dv.p2p.common.utilities.enumValueOrNull
import th.co.dv.p2p.common.utilities.isNullOrEmpty
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import th.co.dv.p2p.common.utilities.search.getFullColumnName
import th.co.dv.p2p.common.utilities.search.getTableName
import th.co.dv.p2p.common.utilities.search.paging.PagableList
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import th.co.dv.p2p.corda.base.models.PurchaseSearchModel

abstract class BasePurchaseItemManager<I : ModelableEntity> {

    private val queryableService by lazy { initQueryableService() }
    abstract fun initQueryableService(): QueryableService<I>

    protected open lateinit var itemClass: Class<I>

    protected open var logger: Logger = LoggerFactory.getLogger(BasePurchaseItemManager::class.java)
    protected open var className = BasePurchaseItemManager::class.java.simpleName


    /**
     * Method to search purchase order using condition in purchase search model
     */
    fun searchQuery(searchModel: PurchaseSearchModel, userAuthorization: UserAuthorization, requiredJoin: Boolean = true): PagableList<PurchaseItemModel> {

        logger.info("$className.searchQuery searchModel: $searchModel")
        val (param, operation) = buildCriteria(searchModel)

        val result = nativeQuery(param = param, operation = operation, userAuthorization = userAuthorization, requiredJoin = requiredJoin, searchModel = searchModel)
        logger.info("$className.searchQuery result: ${result.size}")

        return result
    }

    /**
     * Method to search purchase item using condition in purchase search model
     */
    @Suppress("UNCHECKED_CAST")
    protected fun nativeQuery(param: Map<String, Any>, operation: Map<String, String> = emptyMap(), userAuthorization: UserAuthorization, requiredJoin: Boolean, searchModel: PurchaseSearchModel): PagableList<PurchaseItemModel> {
        val sc = SearchCriterias(this.itemClass)
        sc.setPaging(false)

        val offset = BasePurchaseOrderStatements.OFFSET.replace(BasePurchaseOrderStatements.OFFSET_PLACEHOLDER, ((searchModel.pageNumber - 1) * searchModel.pageSize).toString())
        val limit = BasePurchaseOrderStatements.FETCH.replace(BasePurchaseOrderStatements.FETCH_PLACEHOLDER, searchModel.pageSize.toString())

        val fields = itemClass.declaredFields.filter { it.name != "purchaseOrder" }
        val customSelect = fields.joinToString { it.getFullColumnName() }
        //  if not required join we need to send fromCause else fromCause will auto generate join in customJpaImpl
        val fromCause = if (requiredJoin.not()) getTableName(itemClass) else EMPTY_STRING
        // if gtRemaining not null
        val finalDefaultWhere = if (searchModel.gtRemainingQuantity != null) {
            BasePurchaseOrderStatements.DEFAULT_WHERE.replace(BasePurchaseOrderStatements.DEFAULT_WHERE_VALUE_PLACEHOLDER, searchModel.gtRemainingQuantity.toString())
        } else {
            EMPTY_STRING
        }

        val orderBy = BasePurchaseOrderStatements.ORDER_BY
                .replace(BasePurchaseOrderStatements.ORDER_BY_PLACEHOLDER, computeSortField(searchModel.sortField))
                .replace(BasePurchaseOrderStatements.ORDER_DIRECTION_PLACEHOLDER, BaseManagerUtils.inferSortDirection(searchModel.sortOrder))

        val nativeQueryModel = NativeQueryModel(
            customSelect = customSelect,
            fromClause = fromCause,
            param = param,
            defaultWhere = finalDefaultWhere,
            operation = operation,
            orderBy = orderBy,
            fields = fields.map { it.name },
            userAuth = userAuthorization,
            offset = offset,
            fetch = limit
        )

        val purchaseItemsModel = queryableService.native(nativeQueryModel, sc)

        logger.info("$className.nativeQuery result from native query , total Record : ${purchaseItemsModel.getTotalSize()}")

        return (purchaseItemsModel as PagableList<ModelableEntity>).toPagableModels(PurchaseSearchModel()) as PagableList<PurchaseItemModel>
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
                defaultField = BasePurchaseOrderStatements.defaultSortFieldItem,
                headerClass = itemClass,
                itemClass = itemClass,
                isTableColumn = isTableColumn
        )
    }

    /**
     * Method to build criteria for search purchase item
     */
    protected open fun buildCriteria(searchModel: PurchaseSearchModel): Pair<MutableMap<String, Any>, MutableMap<String, String>> {

        val param = mutableMapOf<String, Any>()
        val operation = mutableMapOf<String, String>()

        param[InterfaceBasePurchaseItem::status.name] = listOf(RecordStatus.INVALID.name)
        operation[InterfaceBasePurchaseItem::status.name] = SearchCriteriaOperation.NOT_IN.name

        if (searchModel.purchaseOrderExternalId.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::poNumber.name] = searchModel.purchaseOrderExternalId!!.first()
            operation[InterfaceBasePurchaseItem::poNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.externalId.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::poItemNo.name] = searchModel.externalId!!
            operation[InterfaceBasePurchaseItem::poItemNo.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.vendorNumber.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::vendorNumber.name] = searchModel.vendorNumber!!
            operation[InterfaceBasePurchaseItem::vendorNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.vendorName.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::vendorName.name] = searchModel.vendorName!!
            operation[InterfaceBasePurchaseItem::vendorName.name] = SearchCriteriaOperation.CONTAIN.name
        }

        if (searchModel.companyCode.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::companyCode.name] = searchModel.companyCode!!
            operation[InterfaceBasePurchaseItem::companyCode.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.companyName.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::companyName.name] = searchModel.companyName!!
            operation[InterfaceBasePurchaseItem::companyName.name] = SearchCriteriaOperation.CONTAIN.name
        }

        if (searchModel.purchaseRequestNumber.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::purchaseRequestNumber.name] = searchModel.purchaseRequestNumber!!
            operation[InterfaceBasePurchaseItem::purchaseRequestNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.businessPlaceTaxNumber.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::businessPlaceTaxNumber.name] = searchModel.businessPlaceTaxNumber!!
            operation[InterfaceBasePurchaseItem::businessPlaceTaxNumber.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.businessPlace.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::businessPlace.name] = searchModel.businessPlace!!
            operation[InterfaceBasePurchaseItem::businessPlace.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.statuses.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::lifecycle.name] = searchModel.statuses!!
            operation[InterfaceBasePurchaseItem::lifecycle.name] = SearchCriteriaOperation.IN.name
        }

        if (enumValueOrNull<NullOperator>(searchModel.deleteFlag) != null) {
            val nullOperation = enumValueOf<NullOperator>(searchModel.deleteFlag!!)
            param[InterfaceBasePurchaseItem::deleteFlag.name] = nullOperation.name
            val isNullOperation = if (nullOperation == NullOperator.IS_NULL) SearchCriteriaOperation.ISNULL.name else SearchCriteriaOperation.NOTNULL.name
            operation[InterfaceBasePurchaseItem::deleteFlag.name] = isNullOperation
        }

        if (searchModel.site.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::site.name] = searchModel.site!!
            operation[InterfaceBasePurchaseItem::site.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.paymentTermCode.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::paymentTermCode.name] = searchModel.paymentTermCode!!
            operation[InterfaceBasePurchaseItem::paymentTermCode.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.taxCode.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::taxCode.name] = searchModel.taxCode!!
            operation[InterfaceBasePurchaseItem::taxCode.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.materialDescription.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::materialDescription.name] = searchModel.materialDescription!!
            operation[InterfaceBasePurchaseItem::materialDescription.name] = SearchCriteriaOperation.STARTS_WITH.name
        }

        if (searchModel.purchaseOrderLinearId.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::purchaseOrderLinearId.name] = searchModel.purchaseOrderLinearId!!
        }

        if (searchModel.proposedRevisedDeliveryDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::proposedRevisedDeliveryDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.proposedRevisedDeliveryDateFrom!!)
            operation[InterfaceBasePurchaseItem::proposedRevisedDeliveryDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.proposedRevisedDeliveryDateTo.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::proposedRevisedDeliveryDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.proposedRevisedDeliveryDateTo!!)
            operation[InterfaceBasePurchaseItem::proposedRevisedDeliveryDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.expectedDeliveryDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::expectedDeliveryDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.expectedDeliveryDateFrom!!)
            operation[InterfaceBasePurchaseItem::expectedDeliveryDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.expectedDeliveryDateTo.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::expectedDeliveryDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.expectedDeliveryDateTo!!)
            operation[InterfaceBasePurchaseItem::expectedDeliveryDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.purchaseUpdatedDateFrom.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::lastUpdatedDate.name + FROM] = DateUtility.convertToStartOfDayTimestamp(searchModel.purchaseUpdatedDateFrom!!)
            operation[InterfaceBasePurchaseItem::lastUpdatedDate.name + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        }

        if (searchModel.purchaseUpdatedDateTo.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::lastUpdatedDate.name + TO] = DateUtility.convertToEndOfDayTimestamp(searchModel.purchaseUpdatedDateTo!!)
            operation[InterfaceBasePurchaseItem::lastUpdatedDate.name + TO] = SearchCriteriaOperation.LESSTHAN.name
        }

        if (searchModel.linearIds.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::linearId.name] = searchModel.linearIds!!
            operation[InterfaceBasePurchaseItem::linearId.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.purchaseOrderLinearIds.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::purchaseOrderLinearId.name] = searchModel.purchaseOrderLinearIds!!
            operation[InterfaceBasePurchaseItem::purchaseOrderLinearId.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.vendorTaxNumbers.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::vendorTaxNumber.name] = searchModel.vendorTaxNumbers!!
            operation[InterfaceBasePurchaseItem::vendorTaxNumber.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.businessPlaceTaxNumbers.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::businessPlaceTaxNumber.name] = searchModel.businessPlaceTaxNumbers!!
            operation[InterfaceBasePurchaseItem::businessPlaceTaxNumber.name] = SearchCriteriaOperation.IN.name
        }

        if (searchModel.itemCategory.isNullOrEmpty().not()) {
            param[InterfaceBasePurchaseItem::itemCategory.name] = searchModel.itemCategory!!
        }

        return param to operation
    }

}