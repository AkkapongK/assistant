package th.co.dv.p2p.common.utilities.manager

import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertTrue
import net.corda.core.node.services.vault.NullOperator
import org.junit.Before
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.RecordStatus
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.*
import th.co.dv.p2p.common.utilities.AuthorizationUtils.INTERFACE_AUTHORIZATION
import th.co.dv.p2p.common.utilities.manager.BasePurchaseOrderStatements.DEFAULT_WHERE
import th.co.dv.p2p.common.utilities.manager.BasePurchaseOrderStatements.GROUP_BY
import th.co.dv.p2p.common.utilities.manager.BasePurchaseOrderStatements.defaultSortField
import th.co.dv.p2p.common.utilities.search.SearchCriteria
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import th.co.dv.p2p.common.utilities.search.paging.PagableList
import th.co.dv.p2p.corda.base.domain.DeleteFlag
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import th.co.dv.p2p.corda.base.models.PurchaseSearchModel
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class BasePurchaseOrderManagerTest {

    @MockK
    lateinit var purchaseOrderService: MockPurchaseOrderService

    @MockK
    lateinit var itemManager: MockPurchaseItemManager

    @Before
    fun setup() = MockKAnnotations.init(this)

    private val initialTotal = 50.toBigDecimal()
    private val total = 50.toBigDecimal()
    private val remainingTotal = 50.toBigDecimal()

    private val purchaseItem = MockPurchaseItem(
            linearId = "item1",
            poNumber = "PO01",
            poItemNo = "1",
            itemCategory = ItemCategory.Purchase.NORMAL.name,
            poItemUnitPrice = BigDecimal(10),
            quantity = Quantity(BigDecimal(5), "BAG")
    )

    private val purchaseItemModel = purchaseItem.toPurchaseItemModel()

    private val purchaseOrder = MockPurchaseOrder(
            linearId = "id1",
            purchaseOrderNumber = "PO01",
            initialTotal = initialTotal,
            remainingTotal = remainingTotal,
            purchaseItems = mutableListOf(purchaseItem),
            currency = "THB"
    )

    private val purchaseOrderModel = PurchaseOrderModel(
            linearId = "id1",
            purchaseOrderNumber = "PO01",
            initialTotal = initialTotal,
            total = total,
            remainingTotal = remainingTotal,
            purchaseItems = mutableListOf(purchaseItemModel)
    )


    @Test
    fun testSearchQuery() {
        val purchaseOrderManager = spyk<MockPurchaseOrderManager>(recordPrivateCalls = true)
        val searchModel = PurchaseSearchModel()
        val selectFields = listOf("purchaseOrderNumber")
        val buildCriteria = Pair(mutableMapOf<String, Any>(), mutableMapOf<String, String>())
        val extraCriteria = mockk<SearchCriterias<MockPurchaseOrder>>()

        every { purchaseOrderManager["buildCriteria"](searchModel) } returns buildCriteria
        every { purchaseOrderManager["buildExtraSearchCriteria"](searchModel) } returns extraCriteria

        every {
            purchaseOrderManager["nativeQuery"](
                    buildCriteria.first,
                    buildCriteria.second,
                    searchModel,
                    extraCriteria,
                    selectFields,
                    INTERFACE_AUTHORIZATION,
                    true
            )
        } returns PagableList(listOf(purchaseOrderModel.copy(linearId = "id2")) as MutableList<PurchaseOrderModel>)

        // Case specific select field
        var result = purchaseOrderManager.searchQuery(searchModel, selectFields, INTERFACE_AUTHORIZATION)
        assertEquals(1, result.size)
        assertEquals("id2", result.first().linearId)
        assertEquals(initialTotal, result.first().initialTotal)
        assertEquals(total, result.first().total)
        assertEquals(remainingTotal, result.first().remainingTotal)
        verify(exactly = 0) { purchaseOrderManager["queryByParam"](any<Map<String, Any>>(), any<Map<String, String>>(), any<PurchaseSearchModel>(), any<SearchCriterias<MockPurchaseOrder>>(), any<UserAuthorization>()) }
        verify(exactly = 1) { purchaseOrderManager["nativeQuery"](buildCriteria.first, buildCriteria.second, searchModel, extraCriteria, selectFields, INTERFACE_AUTHORIZATION, true) }
        clearAllMocks(answers = false)

        // Case not specific select field
        every {
            purchaseOrderManager["queryByParam"](
                buildCriteria.first,
                buildCriteria.second,
                searchModel,
                extraCriteria,
                INTERFACE_AUTHORIZATION
            )
        } returns PagableList(listOf(purchaseOrderModel) as MutableList<PurchaseOrderModel>)

        result = purchaseOrderManager.searchQuery(searchModel, userAuthorization = INTERFACE_AUTHORIZATION)
        assertEquals(1, result.size)
        assertEquals("id1", result.first().linearId)
        assertEquals(initialTotal, result.first().initialTotal)
        assertEquals(total, result.first().total)
        assertEquals(remainingTotal, result.first().remainingTotal)
        verify(exactly = 1) { purchaseOrderManager["queryByParam"](buildCriteria.first, buildCriteria.second, searchModel, extraCriteria, INTERFACE_AUTHORIZATION) }
        verify(exactly = 0) { purchaseOrderManager["nativeQuery"](any<Map<String, Any>>(), any<Map<String, String>>(), any<PurchaseSearchModel>(), any<SearchCriterias<MockPurchaseOrder>>(), any<List<String>>(), any<UserAuthorization>(), any<Boolean>()) }
    }

    @Test
    fun testQueryByParam() {
        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()
        val extraCriteria = mockk<SearchCriterias<InterfaceBasePurchaseOrder<*>>>()

        every {
            purchaseOrderManager["nativeQuery"](
                    any<Map<String, Any>>(),
                    any<Map<String, String>>(),
                    match<PurchaseSearchModel> { it.externalId == "PO1" },
                    extraCriteria,
                    match<List<String>> { it.containsAll(listOf(PurchaseOrderModel::initialTotal.name, PurchaseOrderModel::remainingTotal.name)) },
                    any<UserAuthorization>(),
                    any<Boolean>())
        } returns PagableList(listOf(purchaseOrderModel) as MutableList<PurchaseOrderModel>)

        every {
            purchaseOrderManager["nativeQuery"](
                    any<Map<String, Any>>(),
                    any<Map<String, String>>(),
                    match<PurchaseSearchModel> { it.externalId == "PO2" },
                    extraCriteria,
                    match<List<String>> { it.containsAll(listOf(PurchaseOrderModel::initialTotal.name, PurchaseOrderModel::remainingTotal.name)) },
                    any<UserAuthorization>(),
                    any<Boolean>())
        } returns PagableList(mutableListOf<PurchaseOrderModel>())

        every {
            purchaseOrderService.findByParam(match {
                it[PurchaseSearchModel::pageNumber.name] != null &&
                        it[PurchaseSearchModel::pageSize.name] != null &&
                        it[PurchaseSearchModel::sortField.name] != null &&
                        it[PurchaseSearchModel::sortOrder.name] != null
            }, any(), any())
        } returns listOf(MockPurchaseOrder(linearId = "id1"))

        every { purchaseOrderManager.purchaseOrderService } returns purchaseOrderService
        every { purchaseOrderManager["initQueryableService"]() } returns purchaseOrderService
        every { purchaseOrderManager["computeSortField"](any<String>(), false) } returns defaultSortField

        mockkObject(BaseManagerUtils)
        every { BaseManagerUtils.inferSortDirection(any()) } returns ASC

        // Case find record from native query
        var result = callMethod<MockPurchaseOrderManager, List<PurchaseOrderModel>>(purchaseOrderManager, "queryByParam",
                mapOf<String, Any>(),
                mapOf<String, String>(),
                PurchaseSearchModel(externalId = "PO1", returnPurchaseItems = true),
                extraCriteria,
                INTERFACE_AUTHORIZATION
        )

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("id1", result.first().linearId)
        assertEquals(initialTotal, result.first().initialTotal)
        assertEquals(total, result.first().total)
        assertEquals(remainingTotal, result.first().remainingTotal)
        assertEquals(listOf(purchaseItemModel), result.first().purchaseItems)

        // Case not find record from native query
        result = callMethod<MockPurchaseOrderManager, List<PurchaseOrderModel>>(purchaseOrderManager, "queryByParam",
                mapOf<String, Any>(),
                mapOf<String, String>(),
                PurchaseSearchModel(externalId = "PO2"),
                extraCriteria,
                INTERFACE_AUTHORIZATION
        )

        assertNotNull(result)
        assertEquals(0, result.size)

        unmockkObject(BaseManagerUtils)
    }

    @Test
    fun `Test nativeQuery`() {
        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()
        val extraCriteria = mockk<SearchCriterias<InterfaceBasePurchaseOrder<*>>>()
        every { purchaseOrderManager["initQueryableService"]() } returns purchaseOrderService

        mockkObject(BaseManagerUtils)
        mockkObject(PurchaseUtils)

        // Case1 no select field , no return items , no initialTotal and no remainingTotal
        val searchModelCase1 = PurchaseSearchModel(gtRemainingQuantity = BigDecimal.ZERO)
        every { BaseManagerUtils.getColumnName(emptyList(), headerClass = MockPurchaseOrder::class.java, itemClass = MockPurchaseItem::class.java) } returns emptyList()
        every { BaseManagerUtils.filteredEligibleField(emptyList(), headerClass = MockPurchaseOrder::class.java, itemClass = MockPurchaseItem::class.java, itemFieldName = InterfaceBasePurchaseOrder<*>::purchaseItems.name) } returns emptyList()
        every { purchaseOrderManager["computeGroupByClause"](GROUP_BY, searchModelCase1.sortField) } returns Pair("groupBy", "sortField")
        val resultQueryPurchaseOrderCase1 = PagableList(mutableListOf(purchaseOrder))
        val linearIdsCase1 = resultQueryPurchaseOrderCase1.map { it.linearId }
        val finalDefaultWhere = DEFAULT_WHERE.replace(BasePurchaseOrderStatements.DEFAULT_WHERE_VALUE_PLACEHOLDER, searchModelCase1.gtRemainingQuantity.toString())
        every {
            purchaseOrderService.native(
                    match {
                        it.customSelect == BasePurchaseOrderStatements.PURCHASE_ORDER_SELECT_SQL &&
                                it.groupBy == "groupBy" &&
                                it.userAuth == INTERFACE_AUTHORIZATION &&
                                it.param == mapOf<String, Any>() &&
                                it.defaultWhere == finalDefaultWhere &&
                                it.fields == BasePurchaseOrderStatements.defaultField &&
                                it.operation == mapOf<String, String>() &&
                                it.orderBy.contains("sortField asc") &&
                                it.offset.contains("OFFSET 0 ROWS") &&
                                it.fetch.contains("FETCH FIRST 500 ROWS ONLY") &&
                                it.queryTotalRecords
                    },
                    match {
                        it.getRootObject() == MockPurchaseOrder::class.java &&
                                it.getCriterias().contains(extraCriteria)
                    }
            )
        } returns resultQueryPurchaseOrderCase1
        every {
            purchaseOrderManager["getRelatedPurchaseItems"](
                    searchModelCase1,
                    match<List<PurchaseOrderModel>> { poModels->
                        poModels.map { it.linearId }.containsAll(linearIdsCase1) && poModels.size == resultQueryPurchaseOrderCase1.size }
            )
        } returns listOf(purchaseItemModel)

        var result = callMethod<MockPurchaseOrderManager, PagableList<PurchaseOrderModel>>(purchaseOrderManager, "nativeQuery",
                mapOf<String, Any>(),
                mapOf<String, String>(),
                searchModelCase1,
                extraCriteria,
                emptyList<String>(),
                INTERFACE_AUTHORIZATION,
                true
        )
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("id1", result.first().linearId)
        assertEquals(BigDecimal(50), result.first().initialTotal)
        assertEquals(BigDecimal.ZERO, result.first().total)
        assertEquals(BigDecimal(50), result.first().remainingTotal)

        // Case2 have select field , return items is true , have sort field , have initialTotal , have remainingTotal, have gtRemainingAmount 0
        val searchModelCase2 = PurchaseSearchModel(returnPurchaseItems = true, sortField = "vendorTaxNumber", sortOrder = 0, gtRemainingAmount = BigDecimal.ZERO)
        val selectFieldsCase2 = listOf("purchaseOrderNumber", "vendorTaxNumber", "initialTotal", "remainingTotal")
        val columnNameCase2 = selectFieldsCase2.map { "mock_purchase_order${DOT}${StringUtility.camelToSnakeCase(it)}" }
        val joinColumnCase2 = columnNameCase2.joinToString(separator = ", ", prefix = ", ")
        every { BaseManagerUtils.getColumnName(selectFieldsCase2, headerClass = MockPurchaseOrder::class.java, itemClass = MockPurchaseItem::class.java) } returns columnNameCase2
        every { BaseManagerUtils.filteredEligibleField(selectFieldsCase2, headerClass = MockPurchaseOrder::class.java, itemClass = MockPurchaseItem::class.java, itemFieldName = InterfaceBasePurchaseOrder<*>::purchaseItems.name) } returns selectFieldsCase2
        every { purchaseOrderManager["computeGroupByClause"](GROUP_BY + joinColumnCase2, searchModelCase2.sortField) } returns Pair("groupBy", "sortField")
        val resultQueryPurchaseOrderCase2 = PagableList(mutableListOf(purchaseOrder.copy(linearId = "id2")))
        val linearIdsCase2 = resultQueryPurchaseOrderCase2.map { it.linearId }
        every {
            purchaseOrderService.native(
                    match {
                        it.customSelect == BasePurchaseOrderStatements.PURCHASE_ORDER_SELECT_SQL + joinColumnCase2 &&
                                it.groupBy == "groupBy" &&
                                it.userAuth == INTERFACE_AUTHORIZATION &&
                                it.param == mapOf<String, Any>() &&
                                it.defaultWhere == EMPTY_STRING &&
                                it.fields == (BasePurchaseOrderStatements.defaultField + selectFieldsCase2).distinct() &&
                                it.operation == mapOf<String, String>() &&
                                it.orderBy.contains("sortField desc") &&
                                it.offset.contains("OFFSET 0 ROWS") &&
                                it.fetch.contains("FETCH FIRST 500 ROWS ONLY") &&
                                it.queryTotalRecords.not()
                    },
                    match {
                        it.getRootObject() == MockPurchaseOrder::class.java &&
                            it.getCriterias().contains(extraCriteria)
                    }
            )
        } returns resultQueryPurchaseOrderCase2
        every {
            purchaseOrderManager["getRelatedPurchaseItems"](
                    searchModelCase2,
                    match<List<PurchaseOrderModel>> { poModels ->
                        poModels.map { it.linearId }.containsAll(linearIdsCase2) && poModels.size == resultQueryPurchaseOrderCase2.size }
            )
        } returns listOf(purchaseItemModel.copy(purchaseOrderLinearId = "id2"))

        result = callMethod<MockPurchaseOrderManager, PagableList<PurchaseOrderModel>>(purchaseOrderManager, "nativeQuery",
                mapOf<String, Any>(),
                mapOf<String, String>(),
                searchModelCase2,
                extraCriteria,
                selectFieldsCase2,
                INTERFACE_AUTHORIZATION,
                false
        )
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("id2", result.first().linearId)
        assertEquals(0, initialTotal.compareTo(result.first().initialTotal))
        assertEquals(0, total.compareTo(result.first().total))
        assertEquals(0, remainingTotal.compareTo(result.first().remainingTotal))

        // Case3 like Case2 but have gtRemainingAmount 100
        val searchModelCase3 = searchModelCase2.copy(gtRemainingAmount = "100".toBigDecimal())
        every { purchaseOrderManager["computeGroupByClause"](GROUP_BY + joinColumnCase2, searchModelCase3.sortField) } returns Pair("groupBy", "sortField")
        every {
            purchaseOrderService.native(
                match {
                    it.customSelect == BasePurchaseOrderStatements.PURCHASE_ORDER_SELECT_SQL + joinColumnCase2 &&
                            it.groupBy == "groupBy" &&
                            it.userAuth == INTERFACE_AUTHORIZATION &&
                            it.param == mapOf<String, Any>() &&
                            it.defaultWhere == EMPTY_STRING &&
                            it.fields == (BasePurchaseOrderStatements.defaultField + selectFieldsCase2).distinct() &&
                            it.operation == mapOf<String, String>() &&
                            it.orderBy.contains("sortField desc") &&
                            it.offset.contains("OFFSET 0 ROWS") &&
                            it.fetch.contains("FETCH FIRST 500 ROWS ONLY") &&
                            it.queryTotalRecords
                },
                match { it.getRootObject() == MockPurchaseOrder::class.java }
            )
        } returns resultQueryPurchaseOrderCase2
        every {
            purchaseOrderManager["getRelatedPurchaseItems"](
                searchModelCase3,
                match<List<PurchaseOrderModel>> { poModels ->
                    poModels.map { it.linearId }.containsAll(linearIdsCase2) && poModels.size == resultQueryPurchaseOrderCase2.size }
            )
        } returns listOf(purchaseItemModel.copy(purchaseOrderLinearId = "id2"))

        result = callMethod<MockPurchaseOrderManager, PagableList<PurchaseOrderModel>>(purchaseOrderManager, "nativeQuery",
            mapOf<String, Any>(),
            mapOf<String, String>(),
            searchModelCase3,
            extraCriteria,
            selectFieldsCase2,
            INTERFACE_AUTHORIZATION,
            true
        )
        assertNotNull(result)
        assertEquals(0, result.size)

        unmockkAll()
    }

    @Test
    fun `Test getSumOfInitialTotalIncludeTaxForNative`() {
        val purchaseOrderManager = spyk<MockPurchaseOrderManager>(recordPrivateCalls = true)
        mockkObject(PurchaseUtils)

        val quantity = Quantity(
            initial = 100.0.toBigDecimal(),
            remaining = 50.0.toBigDecimal(),
            unit = "TON"
        )
        val purchaseItemsTax0 = listOf(
            PurchaseItemModel(itemCategory = ItemCategory.Purchase.ADVANCE.name, poItemUnitPrice = 10.0.toBigDecimal(), quantity = quantity, taxRate = BigDecimal.ZERO),
            PurchaseItemModel(itemCategory = ItemCategory.Purchase.NORMAL.name, poItemUnitPrice = 12.0.toBigDecimal(), quantity = quantity, taxRate = BigDecimal.ZERO)
        )
        val purchaseItemsTax7 = listOf(
            PurchaseItemModel(itemCategory = ItemCategory.Purchase.ADVANCE.name, poItemUnitPrice = 15.0.toBigDecimal(), quantity = quantity, taxRate = BigDecimal(7)),
            PurchaseItemModel(itemCategory = ItemCategory.Purchase.NORMAL.name, poItemUnitPrice = 10.0.toBigDecimal(), quantity = quantity, taxRate = BigDecimal(7))
        )
        val purchaseItemsDelete = listOf(
            PurchaseItemModel(itemCategory = ItemCategory.Purchase.NORMAL.name, poItemUnitPrice = 10.0.toBigDecimal(), quantity = quantity, taxRate = BigDecimal(7), deleteFlag = "DELETED")
        )
        val purchaseItems = listOf(purchaseItemsTax0, purchaseItemsTax7, purchaseItemsDelete).flatten()

        every { PurchaseUtils.calculatePurchaseOrderInitialRemaining(purchaseItemsTax0) } returns PurchaseOrderModel(initialTotal = 1200.0.toBigDecimal())
        every { PurchaseUtils.calculatePurchaseOrderInitialRemaining(purchaseItemsTax7) } returns PurchaseOrderModel(initialTotal = 2000.0.toBigDecimal())

        // Case return total
        val result = callMethod<MockPurchaseOrderManager, BigDecimal?>(
            purchaseOrderManager,
            "getSumOfInitialTotalIncludeTaxForNative",
            purchaseItems
        )!!
        assertTrue(3340.0.toBigDecimal().compareTo(result) == 0)
        verify(exactly = 1) {
            PurchaseUtils.calculatePurchaseOrderInitialRemaining(purchaseItemsTax0)
            PurchaseUtils.calculatePurchaseOrderInitialRemaining(purchaseItemsTax7)
        }
        verify(exactly = 0) {
            PurchaseUtils.calculatePurchaseOrderInitialRemaining(purchaseItemsDelete)
        }
        unmockkObject(PurchaseUtils)
    }

    @Test
    fun testBuildCriteria() {

        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()

        // Not specific search param
        var searchModel = PurchaseSearchModel()

        var result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        var resultParam = result.first
        var resultOperation = result.second

        assert(resultParam.size == 1)
        assert(resultOperation.size == 1)
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseOrder::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // Search param for case insensitive case
        val caseInsensitive = SearchCriteriaOperation.STARTS_WITH.name
        searchModel = PurchaseSearchModel(
                externalId = "PO1",
                purchaseRequestNumber = "test",
                vendorNumber = "vendorNumber",
                vendorName = "vendorName",
                companyCode = "companyCode",
                companyName = "companyName",
                businessPlaceOfficerName = "businessPlaceOfficerName",
                businessPlaceTaxNumber = "businessPlaceTaxNumber",
                businessPlace = "businessPlace",
                paymentTermCode = "paymentTermCode",
                taxCode = "taxCode",
                companyBranchCode = "companyBranchCode",
                purchasingGroup = "purchasingGroup",
                deleteFlag = "deleteFlag",
                referenceField1 = "referenceField1",
                referenceField2 = "referenceField2",
                referenceField3 = "referenceField3",
                referenceField4 = "referenceField4",
                referenceField5 = "referenceField5",
                vendorOfficerName = "vendorOfficerName",
                materialDescription = "materialDescription",
                site = "site",
                itemCategory = "itemCategory",
                exactContractNumber = "exactContractNumber"
        )

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals("PO1", resultParam[MockPurchaseOrder::purchaseOrderNumber.name])
        assertEquals("test", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::purchaseRequestNumber.name])
        assertEquals("vendorNumber", resultParam[MockPurchaseOrder::vendorNumber.name])
        assertEquals("vendorName", resultParam[MockPurchaseOrder::vendorName.name])
        assertEquals("companyCode", resultParam[MockPurchaseOrder::companyCode.name])
        assertEquals("companyName", resultParam[MockPurchaseOrder::companyName.name])
        assertEquals("businessPlaceOfficerName", resultParam[MockPurchaseOrder::businessPlaceOfficerName.name])
        assertEquals("businessPlaceTaxNumber", resultParam[MockPurchaseOrder::businessPlaceTaxNumber.name])
        assertEquals("businessPlace", resultParam[MockPurchaseOrder::businessPlaceAddress1.name])
        assertEquals("paymentTermCode", resultParam[MockPurchaseOrder::paymentTermCode.name])
        assertEquals("taxCode", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::taxCode.name])
        assertEquals("companyBranchCode", resultParam[MockPurchaseOrder::companyBranchCode.name])
        assertEquals("purchasingGroup", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::purchasingGroup.name])
        assertEquals("deleteFlag", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::deleteFlag.name])
        assertEquals("referenceField1", resultParam[MockPurchaseOrder::referenceField1.name])
        assertEquals("referenceField2", resultParam[MockPurchaseOrder::referenceField2.name])
        assertEquals("referenceField3", resultParam[MockPurchaseOrder::referenceField3.name])
        assertEquals("referenceField4", resultParam[MockPurchaseOrder::referenceField4.name])
        assertEquals("referenceField5", resultParam[MockPurchaseOrder::referenceField5.name])
        assertEquals("vendorOfficerName", resultParam[MockPurchaseOrder::vendorOfficerName.name])
        assertEquals("materialDescription", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::materialDescription.name])
        assertEquals("site", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::site.name])
        assertEquals("itemCategory", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::itemCategory.name])
        assertEquals("exactContractNumber", resultParam[MockPurchaseOrder::contractNumber.name])

        // check operation for all field should be contain insensitive
        assert(resultOperation.filterNot { it.key == PurchaseOrderModel::vendorName.name || it.key == PurchaseOrderModel::companyName.name }.filterNot { it.key == MockPurchaseItem::status.name }.all { it.value == caseInsensitive })
        assert(resultOperation.filter { it.key == PurchaseOrderModel::vendorName.name || it.key == PurchaseOrderModel::companyName.name }.filterNot { it.key == MockPurchaseItem::status.name }.all { it.value == SearchCriteriaOperation.CONTAIN.name })

        // check record status
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseOrder::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // Search param for IN case
        val caseIn = SearchCriteriaOperation.IN.name

        searchModel = PurchaseSearchModel(
                purchaseOrderExternalId = listOf("purchaseOrderExternalId"),
                statuses = listOf("CONFIRMED"),
                linearIds = listOf("linearIds"),
                purchaseItemLinearIds = listOf("purchaseItemLinearId1", "purchaseItemLinearId2"),
                businessPlaceTaxNumbers = listOf("1234","5678"),
                vendorTaxNumbers = listOf("1234","5678")
        )

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals(listOf("purchaseOrderExternalId"), resultParam[MockPurchaseOrder::purchaseOrderNumber.name])
        assertEquals(listOf("CONFIRMED"), resultParam[MockPurchaseOrder::lifecycle.name])
        assertEquals(listOf("linearIds"), resultParam[MockPurchaseOrder::linearId.name])
        assertEquals(listOf("purchaseItemLinearId1", "purchaseItemLinearId2"), resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::linearId.name])
        assertEquals(listOf("1234","5678"), resultParam[MockPurchaseOrder::vendorTaxNumber.name])
        assertEquals(listOf("1234","5678"), resultParam[MockPurchaseOrder::businessPlaceTaxNumber.name])


        // check operation for all field should be IN
        assert(resultOperation.filterNot { it.key == MockPurchaseOrder::status.name }.all { it.value == caseIn })

        // check record status
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseOrder::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // Search param for GREATER_THAN_OR_EQUAL case
        val caseGreaterThanOrEqual = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name

        searchModel = PurchaseSearchModel(
                purchaseUpdatedDateFrom = "03/02/2020",
                documentEntryDateFrom = "03/02/2020",
                proposedRevisedDeliveryDateFrom = "03/02/2020",
                itemDeliveryDateFrom = "03/02/2020"
        )

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockPurchaseOrder::lastUpdatedDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockPurchaseOrder::documentEntryDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::proposedRevisedDeliveryDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::expectedDeliveryDate.name + FROM]?.toString())

        // check operation for all field should be greater than or equal
        assertTrue(resultOperation.filterNot { it.key == MockPurchaseOrder::status.name }.all { it.value == caseGreaterThanOrEqual })

        // check record status
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseOrder::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // Search param for LESS_THAN case
        val caseLessThan = SearchCriteriaOperation.LESSTHAN.name

        searchModel = PurchaseSearchModel(
                purchaseUpdatedDateTo = "03/02/2020",
                proposedRevisedDeliveryDateTo = "03/02/2020",
                documentEntryDateTo = "03/02/2020",
                itemDeliveryDateTo = "03/02/2020",
                itemEffectiveDateTo = "03/02/2020"
        )

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockPurchaseOrder::lastUpdatedDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockPurchaseOrder::documentEntryDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::proposedRevisedDeliveryDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::expectedDeliveryDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::effectiveDate.name + TO]?.toString())

        // check operation for all field should be less than
        assert(resultOperation.filterNot { it.key == MockPurchaseOrder::status.name }.all { it.value == caseLessThan })

        // check record status
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseOrder::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // Search param for GREATER_THAN case
        val caseGreaterThan = SearchCriteriaOperation.GREATERTHAN.name

        searchModel = PurchaseSearchModel(isItemAdvanceRemainingAmountGreaterThanZero = true)

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals(BigDecimal.ZERO, resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::advancePaymentRemainingAmount.name])

        // check operation for all field should be greater than
        assert(resultOperation.filterNot { it.key == MockPurchaseOrder::status.name }.all { it.value == caseGreaterThan })

        // check record status
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseOrder::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // Search param for EQUAL case
        searchModel = PurchaseSearchModel(
                exactPurchaseOrderExternalId = "PO1",
                linearId = "PO0001",
                exactBusinessPlaceTaxNumber = "400",
                vendorTaxNumber = "vendorTaxNumber",
                paymentTermDays = 1,
                paymentTermMonths = 1
        )

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals("PO1", resultParam[MockPurchaseOrder::purchaseOrderNumber.name])
        assertEquals("PO0001", resultParam[MockPurchaseOrder::linearId.name])
        assertEquals("400", resultParam[MockPurchaseOrder::businessPlaceTaxNumber.name])
        assertEquals("vendorTaxNumber", resultParam[MockPurchaseOrder::vendorTaxNumber.name])
        assertEquals(1L, resultParam[MockPurchaseOrder::paymentTermDays.name])
        assertEquals(1, resultParam[MockPurchaseOrder::paymentTermMonths.name])

        // check operation must be empty
        assert(resultOperation.filterNot { it.key == MockPurchaseOrder::status.name }.isEmpty())

        // check record status
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseOrder::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // Search po header deleteFlag
        // Case deleteFlag is DELETED
        searchModel = PurchaseSearchModel(
            headerDeleteFlag = DeleteFlag.DELETED.name
        )

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
            purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals(DeleteFlag.DELETED.name, resultParam[MockPurchaseOrder::deleteFlag.name])

        // check record status
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseOrder::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // Case deleteFlag is IS_NULL
        searchModel = PurchaseSearchModel(
            headerDeleteFlag = NullOperator.IS_NULL.name
        )

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
            purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals(NullOperator.IS_NULL.name, resultParam[MockPurchaseOrder::deleteFlag.name])

        // check operation must be ISNULL
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // check record status
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseOrder::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseOrder::status.name])

        // Search param for NullOperator case
        // Case IS_NULL
        searchModel = PurchaseSearchModel(
                deleteFlag = NullOperator.IS_NULL.name
        )

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and operation
        assertEquals(NullOperator.IS_NULL.name, resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::deleteFlag.name])
        assertEquals(SearchCriteriaOperation.ISNULL.name, resultOperation[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::deleteFlag.name])

        // Case NOT_NULL
        searchModel = PurchaseSearchModel(
                deleteFlag = NullOperator.NOT_NULL.name
        )

        result = callMethod<MockPurchaseOrderManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseOrderManager, "buildCriteria", searchModel)

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and operation
        assertEquals(NullOperator.NOT_NULL.name, resultParam[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::deleteFlag.name])
        assertEquals(SearchCriteriaOperation.NOTNULL.name, resultOperation[MockPurchaseOrder::purchaseItems.name + "." + MockPurchaseItem::deleteFlag.name])
    }

    @Test
    fun testComputeGroupByClause() {

        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()

        val groupBy = "purchase_order.linear_id, purchase_order.purchase_order_number"

        every { purchaseOrderManager["computeSortField"]("purchaseOrderNumber", true) } returns "purchase_order.purchase_order_number"
        every { purchaseOrderManager["computeSortField"]("companyName", true) } returns "purchase_order.company_name"

        // Case sort field already in group by
        var result = callMethod<MockPurchaseOrderManager, Pair<String, String>>(purchaseOrderManager, "computeGroupByClause", groupBy, "purchaseOrderNumber")

        assertNotNull(result)
        assertEquals(groupBy, result.first)
        assertEquals("purchase_order.purchase_order_number", result.second)

        // Case sort field is not exist in group by
        result = callMethod<MockPurchaseOrderManager, Pair<String, String>>(purchaseOrderManager, "computeGroupByClause", groupBy, "companyName")

        assertNotNull(result)
        assertEquals("purchase_order.linear_id, purchase_order.purchase_order_number, purchase_order.company_name", result.first)
        assertEquals("purchase_order.company_name", result.second)
    }

    @Test
    fun testComputeSortField() {

        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()
        mockkObject(BaseManagerUtils)

        every {
            BaseManagerUtils.computeSortField(
                    sortField = any(),
                    defaultField = defaultSortField,
                    headerClass = MockPurchaseOrder::class.java,
                    itemClass = MockPurchaseItem::class.java,
                    isTableColumn = true)
        } returns "purchase_order.business_place_tax_number"

        every {
            BaseManagerUtils.computeSortField(
                    sortField = any(),
                    defaultField = defaultSortField,
                    headerClass = MockPurchaseOrder::class.java,
                    itemClass = MockPurchaseItem::class.java,
                    isTableColumn = false)
        } returns defaultSortField

        // Case return table column
        var result = callMethod<MockPurchaseOrderManager, String>(purchaseOrderManager, "computeSortField", "businessPlaceTaxNumber", true)
        assertNotNull(result)
        assertEquals("purchase_order.business_place_tax_number", result)

        // Case return model column
        result = callMethod<MockPurchaseOrderManager, String>(purchaseOrderManager, "computeSortField", "creditNote.externalId", false)
        assertNotNull(result)
        assertEquals(defaultSortField, result)

        verify(exactly = 1) {
            BaseManagerUtils.computeSortField(
                    sortField = any(),
                    defaultField = defaultSortField,
                    headerClass = MockPurchaseOrder::class.java,
                    itemClass = MockPurchaseItem::class.java,
                    isTableColumn = true)
        }

        verify(exactly = 1) {
            BaseManagerUtils.computeSortField(
                    sortField = any(),
                    defaultField = defaultSortField,
                    headerClass = MockPurchaseOrder::class.java,
                    itemClass = MockPurchaseItem::class.java,
                    isTableColumn = false)
        }

        unmockkObject(BaseManagerUtils)
    }

    @Test
    fun `Test needInitialTotalField`() {
        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()

        // Case true
        val selectFields = listOf(PurchaseOrderModel::initialTotal.name)

        var result = callMethod<MockPurchaseOrderManager, Boolean>(purchaseOrderManager, "needInitialTotalField", selectFields)
        assertTrue(result!!)

        // Case false
        result = callMethod<MockPurchaseOrderManager, Boolean>(purchaseOrderManager, "needInitialTotalField", emptyList<String>())
        assertFalse(result!!)

    }

    @Test
    fun `Test needRemainingTotalField`() {
        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()

        // Case true
        val selectFields = listOf(PurchaseOrderModel::remainingTotal.name)

        var result = callMethod<MockPurchaseOrderManager, Boolean>(purchaseOrderManager, "needRemainingTotalField", selectFields)
        assertTrue(result!!)

        // Case false
        result = callMethod<MockPurchaseOrderManager, Boolean>(purchaseOrderManager, "needRemainingTotalField", emptyList<String>())
        assertFalse(result!!)

    }

    @Test
    fun `Test needToGetItems`() {
        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()

        // Case true
        var result = callMethod<MockPurchaseOrderManager, Boolean>(purchaseOrderManager, "needToGetItems", PurchaseSearchModel(returnPurchaseItems = true))
        assertTrue(result!!)

        //Case false
        result = callMethod<MockPurchaseOrderManager, Boolean>(purchaseOrderManager, "needToGetItems", PurchaseSearchModel(returnPurchaseItems = false))
        assertFalse(result!!)
    }

    @Test
    fun `Test getRelatedPurchaseItems`() {
        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()
        every { purchaseOrderManager.initItemsManager() } returns itemManager
        val twoHundredOnePurchaseOrderModel = mutableListOf<PurchaseOrderModel>()
        (1..201).forEach {
            twoHundredOnePurchaseOrderModel.add(purchaseOrderModel.copy(linearId = "$it"))
        }

        val searchModel = PurchaseSearchModel()
        val selectFields = listOf(
                PurchaseOrderModel::remainingTotal.name,
                PurchaseOrderModel::initialTotal.name
        )

        // Case not query item
        every { purchaseOrderManager["needToGetItems"](searchModel) } returns false
        var result = callMethod<MockPurchaseOrderManager, List<PurchaseItemModel>>(purchaseOrderManager, "getRelatedPurchaseItems", searchModel, listOf(purchaseOrderModel))!!
        assertTrue(result.isEmpty())

        // Case query item
        val firstRound = (1..200).map { it.toString() }
        val secondRound = listOf("201")
        every { purchaseOrderManager["needToGetItems"](searchModel) } returns true
        every { itemManager.searchQuery(PurchaseSearchModel(purchaseOrderLinearIds = listOf("id1"), pageSize = Int.MAX_VALUE), INTERFACE_AUTHORIZATION, false) } returns PagableList(mutableListOf(purchaseItemModel))
        result = callMethod<MockPurchaseOrderManager, List<PurchaseItemModel>>(purchaseOrderManager, "getRelatedPurchaseItems", searchModel, listOf(purchaseOrderModel))!!
        assertTrue(result.contains(purchaseItemModel))
        verify(exactly = 1) { itemManager.searchQuery(PurchaseSearchModel(purchaseOrderLinearIds = listOf("id1"), pageSize = Int.MAX_VALUE), INTERFACE_AUTHORIZATION, false) }

        // Case listPurchaseOrderModel more than 200
        every {
            itemManager.searchQuery(
                    match {
                        it.purchaseOrderLinearIds!!.containsAll(firstRound) && it.pageSize == Int.MAX_VALUE
                    },
                    INTERFACE_AUTHORIZATION,
                    false)
        } returns PagableList(mutableListOf(purchaseItemModel.copy(linearId = "FIRST-ROUND")))

        every {
            itemManager.searchQuery(
                    match {
                        it.purchaseOrderLinearIds!!.containsAll(secondRound) && it.pageSize == Int.MAX_VALUE
                    },
                    INTERFACE_AUTHORIZATION,
                    false)
        } returns PagableList(mutableListOf(purchaseItemModel.copy(linearId = "SECOND-ROUND")))

        result = callMethod<MockPurchaseOrderManager, List<PurchaseItemModel>>(purchaseOrderManager, "getRelatedPurchaseItems", searchModel, twoHundredOnePurchaseOrderModel)!!
        assertTrue(result.map { it.linearId }.containsAll(listOf("FIRST-ROUND", "SECOND-ROUND")))
        verify(exactly = 1) {
            itemManager.searchQuery(
                    match {
                        it.purchaseOrderLinearIds!!.containsAll(firstRound) && it.pageSize == Int.MAX_VALUE
                    }, INTERFACE_AUTHORIZATION, false)
        }
        verify(exactly = 1) {
            itemManager.searchQuery(
                    match {
                        it.purchaseOrderLinearIds!!.containsAll(secondRound) && it.pageSize == Int.MAX_VALUE
                    }, INTERFACE_AUTHORIZATION, false)
        }
    }


    @Test
    @Suppress("UNCHECKED_CAST")
    fun `Test buildExtraSearchCriteria`() {
        val purchaseOrderManager = spyk<MockPurchaseOrderManager>()
        var purchaseSearchModel = PurchaseSearchModel(
            contractNumber = "contractNumber-1"
        )

        // Case have all extra criteria
        var result = callMethod<MockPurchaseOrderManager, SearchCriterias<MockPurchaseOrder>>(purchaseOrderManager, "buildExtraSearchCriteria", purchaseSearchModel)!!
        assertEquals(1, result.getCriteriaSize())

        val resultContractNumberCriteria =  (result.getCriterias().find { outer -> ( outer as SearchCriterias<*>).getCriterias().any { inner -> (inner as SearchCriteria).getField() == PurchaseOrderModel::contractNumber.name } } as SearchCriterias<*>).getCriterias() as List<SearchCriteria>
        assertEquals(2, resultContractNumberCriteria.size)
        assertTrue(resultContractNumberCriteria.map { it.getOp() }.containsAll(listOf(SearchCriteriaOperation.EQUAL, SearchCriteriaOperation.ISNULL)))
        assertTrue(resultContractNumberCriteria.map { it.getValue() }.containsAll(listOf("contractNumber-1", NULL)))

        purchaseSearchModel = PurchaseSearchModel(
            vendorOfficerEmailIsNull = true
        )
        result = callMethod<MockPurchaseOrderManager, SearchCriterias<MockPurchaseOrder>>(purchaseOrderManager, "buildExtraSearchCriteria", purchaseSearchModel)!!
        assertEquals(1, result.getCriteriaSize())

        var vendorOfficerEmailCriteria =  (result.getCriterias().find { outer -> ( outer as SearchCriterias<*>).getCriterias().any { inner -> (inner as SearchCriteria).getField() == PurchaseOrderModel::vendorOfficerEmail.name } } as SearchCriterias<*>).getCriterias() as List<SearchCriteria>
        assertEquals(2, resultContractNumberCriteria.size)
        assertTrue(vendorOfficerEmailCriteria.map { it.getOp() }.containsAll(listOf(SearchCriteriaOperation.EQUAL, SearchCriteriaOperation.ISNULL)))
        assertTrue(vendorOfficerEmailCriteria.map { it.getValue() }.containsAll(listOf("", NULL)))

        purchaseSearchModel = PurchaseSearchModel(
            vendorOfficerEmailIsNull = false
        )
        result = callMethod<MockPurchaseOrderManager, SearchCriterias<MockPurchaseOrder>>(purchaseOrderManager, "buildExtraSearchCriteria", purchaseSearchModel)!!
        assertEquals(1, result.getCriteriaSize())

        vendorOfficerEmailCriteria =  (result.getCriterias().find { outer -> ( outer as SearchCriterias<*>).getCriterias().any { inner -> (inner as SearchCriteria).getField() == PurchaseOrderModel::vendorOfficerEmail.name } } as SearchCriterias<*>).getCriterias() as List<SearchCriteria>
        assertEquals(2, resultContractNumberCriteria.size)
        assertTrue(vendorOfficerEmailCriteria.map { it.getOp() }.containsAll(listOf(SearchCriteriaOperation.NOTNULL, SearchCriteriaOperation.NOT_EQUAL)))
        assertTrue(vendorOfficerEmailCriteria.map { it.getValue() }.containsAll(listOf(NULL, "")))

        // Case don't have any extra criteria
        result = callMethod<MockPurchaseOrderManager, SearchCriterias<MockPurchaseOrder>>(purchaseOrderManager, "buildExtraSearchCriteria", PurchaseSearchModel())!!
        assertEquals(0, result.getCriteriaSize())
    }

}
