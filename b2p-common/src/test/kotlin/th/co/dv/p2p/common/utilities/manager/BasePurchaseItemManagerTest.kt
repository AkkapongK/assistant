package th.co.dv.p2p.common.utilities.manager

import io.mockk.*
import io.mockk.impl.annotations.MockK
import net.corda.core.node.services.vault.NullOperator
import org.junit.Before
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.constants.EMPTY_STRING
import th.co.dv.p2p.common.constants.FROM
import th.co.dv.p2p.common.constants.TO
import th.co.dv.p2p.common.enums.RecordStatus
import th.co.dv.p2p.common.utilities.AuthorizationUtils.INTERFACE_AUTHORIZATION
import th.co.dv.p2p.common.utilities.BaseManagerUtils
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.common.utilities.search.getFullColumnName
import th.co.dv.p2p.common.utilities.search.paging.PagableList
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import th.co.dv.p2p.corda.base.models.PurchaseSearchModel
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BasePurchaseItemManagerTest {

    @MockK
    lateinit var purchaseItemService: MockPurchaseItemService


    private val purchaseItem = MockPurchaseItem(
            linearId = "item1"
    )

    private val purchaseItemModel = PurchaseItemModel(
            linearId = "item1"
    )

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun `Test searchQuery`() {

        val purchaseItemManager = spyk<MockPurchaseItemManager>()

        val buildCriteria = Pair(mutableMapOf<String, Any>(), mutableMapOf<String, String>())

        every { purchaseItemManager["buildCriteria"](any<PurchaseSearchModel>()) } returns buildCriteria

        every {
            purchaseItemManager["nativeQuery"](
                    buildCriteria.first,
                    buildCriteria.second,
                    INTERFACE_AUTHORIZATION,
                    true,
                    any<PurchaseSearchModel>()
            )
        } returns PagableList(listOf(purchaseItemModel) as MutableList<PurchaseItemModel>)

        val result = purchaseItemManager.searchQuery(PurchaseSearchModel(), INTERFACE_AUTHORIZATION, true)
        assertEquals(1, result.size)
        assertEquals(purchaseItemModel, result.first())

    }

    @Test
    fun `Test nativeQuery`() {
        val purchaseItemManager = spyk<MockPurchaseItemManager>(recordPrivateCalls = true)
        val (param, operation) = Pair(mutableMapOf<String, Any>(), mutableMapOf<String, String>())
        val fields = MockPurchaseItem::class.java.declaredFields.dropWhile { it.name == "purchaseOrder" }
        val searchModel = PurchaseSearchModel()

        every { purchaseItemManager.purchaseItemService } returns purchaseItemService
        every { purchaseItemManager["initQueryableService"]() } returns purchaseItemService
        every { purchaseItemManager["computeSortField"](searchModel.sortField, any<Boolean>()) } returns "poItemNo"

        mockkObject(BaseManagerUtils)
        every { BaseManagerUtils.inferSortDirection(searchModel.sortOrder) } returns "asc"

        // Case requiredJoin is true
        every {
            purchaseItemService.native(
                    match { nativeQueryModel ->
                        nativeQueryModel.customSelect == fields.joinToString { it.getFullColumnName() } &&
                                nativeQueryModel.fromClause == EMPTY_STRING &&
                                nativeQueryModel.param == param &&
                                nativeQueryModel.operation == operation &&
                                nativeQueryModel.defaultWhere == ""
                                nativeQueryModel.fields == fields.map { it.name } &&
                                nativeQueryModel.orderBy.contains("poItemNo asc") &&
                                nativeQueryModel.userAuth == INTERFACE_AUTHORIZATION
                    }, any())
        } returns PagableList(listOf(purchaseItem) as MutableList<MockPurchaseItem>)

        var result = callMethod<MockPurchaseItemManager, PagableList<PurchaseItemModel>>(purchaseItemManager, "nativeQuery", param, operation, INTERFACE_AUTHORIZATION, true,searchModel)
        assertEquals(1, result!!.size)
        assertEquals(purchaseItemModel, result.first())

        every {
            purchaseItemService.native(
                    match { nativeQueryModel ->
                        nativeQueryModel.customSelect == fields.joinToString { it.getFullColumnName() } &&
                                nativeQueryModel.fromClause == "mock_purchase_item" &&
                                nativeQueryModel.param == param &&
                                nativeQueryModel.operation == operation &&
                                nativeQueryModel.defaultWhere == ""
                                nativeQueryModel.fields == fields.map { it.name } &&
                                nativeQueryModel.orderBy.contains("poItemNo asc") &&
                                nativeQueryModel.userAuth == INTERFACE_AUTHORIZATION
                    }, any())
        } returns PagableList(listOf(purchaseItem) as MutableList<MockPurchaseItem>)

        result = callMethod<MockPurchaseItemManager, PagableList<PurchaseItemModel>>(purchaseItemManager, "nativeQuery", param, operation, INTERFACE_AUTHORIZATION, true,searchModel)
        assertEquals(1, result!!.size)
        assertEquals(purchaseItemModel, result.first())

        // Case gtRemainingQuantity is not empty
        val searchModelWithGtRemainingQuantity = PurchaseSearchModel(gtRemainingQuantity = BigDecimal.ZERO)
        val finalDefaultWhere = BasePurchaseOrderStatements.DEFAULT_WHERE.replace(BasePurchaseOrderStatements.DEFAULT_WHERE_VALUE_PLACEHOLDER, searchModelWithGtRemainingQuantity.gtRemainingQuantity.toString())
        every {
            purchaseItemService.native(
                    match { nativeQueryModel ->
                        nativeQueryModel.customSelect == fields.joinToString { it.getFullColumnName() } &&
                                nativeQueryModel.fromClause == EMPTY_STRING &&
                                nativeQueryModel.param == param &&
                                nativeQueryModel.operation == operation &&
                                nativeQueryModel.defaultWhere == finalDefaultWhere &&
                                nativeQueryModel.fields == fields.map { it.name } &&
                                nativeQueryModel.orderBy.contains("poItemNo asc")
                                nativeQueryModel.userAuth == INTERFACE_AUTHORIZATION
                    }, any())
        } returns PagableList(listOf(purchaseItem) as MutableList<MockPurchaseItem>)

        result = callMethod<MockPurchaseItemManager, PagableList<PurchaseItemModel>>(purchaseItemManager, "nativeQuery", param, operation, INTERFACE_AUTHORIZATION, true,searchModelWithGtRemainingQuantity)
        assertEquals(1, result!!.size)
        assertEquals(purchaseItemModel, result.first())

        unmockkObject(BaseManagerUtils)
    }

    @Test
    fun testComputeSortField() {

        val purchaseItemManager = spyk<MockPurchaseItemManager>()
        mockkObject(BaseManagerUtils)

        every { BaseManagerUtils.computeSortField(
                sortField = any(),
                defaultField = BasePurchaseOrderStatements.defaultSortFieldItem,
                headerClass = MockPurchaseItem::class.java,
                itemClass = MockPurchaseItem::class.java,
                isTableColumn = true) } returns "purchase_item.po_item_unit_price"

        every { BaseManagerUtils.computeSortField(
                sortField = any(),
                defaultField = BasePurchaseOrderStatements.defaultSortFieldItem,
                headerClass = MockPurchaseItem::class.java,
                itemClass = MockPurchaseItem::class.java,
                isTableColumn = false) } returns BasePurchaseOrderStatements.defaultSortFieldItem

        // Case return table column
        var result = callMethod<MockPurchaseItemManager, String>(purchaseItemManager, "computeSortField", "poItemUnitPrice", true)
        assertNotNull(result)
        assertEquals("purchase_item.po_item_unit_price", result)

        // Case return model column
        result = callMethod<MockPurchaseItemManager, String>(purchaseItemManager, "computeSortField", "purchaseOrderNumber", false)
        assertNotNull(result)
        assertEquals(BasePurchaseOrderStatements.defaultSortFieldItem, result)

        verify(exactly = 1) { BaseManagerUtils.computeSortField(
                sortField = any(),
                defaultField = BasePurchaseOrderStatements.defaultSortFieldItem,
                headerClass = MockPurchaseItem::class.java,
                itemClass = MockPurchaseItem::class.java,
                isTableColumn = true) }

        verify(exactly = 1) { BaseManagerUtils.computeSortField(
                sortField = any(),
                defaultField = BasePurchaseOrderStatements.defaultSortFieldItem,
                headerClass = MockPurchaseItem::class.java,
                itemClass = MockPurchaseItem::class.java,
                isTableColumn = false) }

        unmockkObject(BaseManagerUtils)
    }

    @Test
    fun `Test buildCriteria`() {
        val purchaseItemManager = spyk<MockPurchaseItemManager>()


        // Not specific search param
        var searchModel = PurchaseSearchModel()

        var result = callMethod<MockPurchaseItemManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseItemManager, "buildCriteria", searchModel
        )

        assertNotNull(result)

        var resultParam = result.first
        var resultOperation = result.second

        assert(resultParam.size == 1)
        assert(resultOperation.size == 1)
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseItem::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseItem::status.name])

        // Search param for case STARTS_WITH
        val caseInsensitive = SearchCriteriaOperation.STARTS_WITH.name
        searchModel = PurchaseSearchModel(
                purchaseOrderExternalId = listOf("PO-ITEM1"),
                externalId = "0001",
                vendorNumber = "vendorNumber",
                vendorName = "vendorName",
                companyCode = "companyCode",
                companyName = "companyName",
                purchaseRequestNumber = "purchaseRequestNumber",
                businessPlaceTaxNumber = "businessPlaceTaxNumber",
                businessPlace = "businessPlace",
                site = "site",
                paymentTermCode = "paymentTermCode",
                taxCode = "taxCode",
                materialDescription = "materialDescription"
        )

        result = callMethod<MockPurchaseItemManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseItemManager, "buildCriteria", searchModel
        )

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals("PO-ITEM1", resultParam[MockPurchaseItem::poNumber.name])
        assertEquals("0001", resultParam[MockPurchaseItem::poItemNo.name])
        assertEquals("vendorNumber", resultParam[MockPurchaseItem::vendorNumber.name])
        assertEquals("vendorName", resultParam[MockPurchaseItem::vendorName.name])
        assertEquals("companyCode", resultParam[MockPurchaseItem::companyCode.name])
        assertEquals("companyName", resultParam[MockPurchaseItem::companyName.name])
        assertEquals("purchaseRequestNumber", resultParam[MockPurchaseItem::purchaseRequestNumber.name])
        assertEquals("businessPlaceTaxNumber", resultParam[MockPurchaseItem::businessPlaceTaxNumber.name])
        assertEquals("businessPlace", resultParam[MockPurchaseItem::businessPlace.name])
        assertEquals("site", resultParam[MockPurchaseItem::site.name])
        assertEquals("paymentTermCode", resultParam[MockPurchaseItem::paymentTermCode.name])
        assertEquals("taxCode", resultParam[MockPurchaseItem::taxCode.name])
        assertEquals("materialDescription", resultParam[MockPurchaseItem::materialDescription.name])

        // check operation for all field should be contain insensitive
        assert(resultOperation.filterNot { it.key == PurchaseOrderModel::vendorName.name || it.key == PurchaseOrderModel::companyName.name }.filterNot { it.key == MockPurchaseItem::status.name }.all { it.value == caseInsensitive })
        assert(resultOperation.filter { it.key == PurchaseOrderModel::vendorName.name || it.key == PurchaseOrderModel::companyName.name }.filterNot { it.key == MockPurchaseItem::status.name }.all { it.value == SearchCriteriaOperation.CONTAIN.name })

        // check status not invalid
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseItem::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseItem::status.name])

        // Search param for case IN
        val caseIn = SearchCriteriaOperation.IN.name
        searchModel = PurchaseSearchModel(
                statuses = listOf("APPROVED"),
                linearIds = listOf("linearIds"),
                purchaseOrderLinearIds = listOf("purchaseOrderLinearIds"),
                businessPlaceTaxNumbers = listOf("businessPlaceTaxNumbers"),
                vendorTaxNumbers = listOf("vendorTaxNumbers")
        )

        result = callMethod<MockPurchaseItemManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseItemManager, "buildCriteria", searchModel
        )

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals(listOf("APPROVED"), resultParam[MockPurchaseItem::lifecycle.name])
        assertEquals(listOf("linearIds"), resultParam[MockPurchaseItem::linearId.name])
        assertEquals(listOf("purchaseOrderLinearIds"), resultParam[MockPurchaseItem::purchaseOrderLinearId.name])
        assertEquals(listOf("businessPlaceTaxNumbers"), resultParam[MockPurchaseItem::businessPlaceTaxNumber.name])
        assertEquals(listOf("vendorTaxNumbers"), resultParam[MockPurchaseItem::vendorTaxNumber.name])

        // check operation for all field should be In
        assert(resultOperation.filterNot { it.key == MockPurchaseItem::status.name }.all { it.value == caseIn })

        // check status not invalid
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseItem::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseItem::status.name])

        // Search param for GREATER_THAN_OR_EQUAL case
        val caseGreaterThanOrEqual = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name

        searchModel = PurchaseSearchModel(
                proposedRevisedDeliveryDateFrom = "03/02/2020",
                expectedDeliveryDateFrom = "03/02/2020",
                purchaseUpdatedDateFrom = "03/02/2020"
        )

        result = callMethod<MockPurchaseItemManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseItemManager, "buildCriteria", searchModel
        )

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockPurchaseItem::proposedRevisedDeliveryDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockPurchaseItem::expectedDeliveryDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockPurchaseItem::lastUpdatedDate.name + FROM]?.toString())

        // check operation for all field should be greater than or equal
        assert(resultOperation.filterNot { it.key == MockPurchaseItem::status.name }.all { it.value == caseGreaterThanOrEqual })

        // check status not invalid
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseItem::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseItem::status.name])

        // Search param for LESS_THAN case
        val caseLessThan = SearchCriteriaOperation.LESSTHAN.name

        searchModel = PurchaseSearchModel(
                proposedRevisedDeliveryDateTo = "03/02/2020",
                expectedDeliveryDateTo = "03/02/2020",
                purchaseUpdatedDateTo = "03/02/2020"
        )

        result = callMethod<MockPurchaseItemManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseItemManager, "buildCriteria", searchModel
        )

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockPurchaseItem::proposedRevisedDeliveryDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockPurchaseItem::expectedDeliveryDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockPurchaseItem::lastUpdatedDate.name + TO]?.toString())

        // check operation for all field should be less than
        assert(resultOperation.filterNot { it.key == MockPurchaseItem::status.name }.all { it.value == caseLessThan })

        // check status not invalid
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseItem::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseItem::status.name])

        // Search param for EQUAL case
        searchModel = PurchaseSearchModel(purchaseOrderLinearId = "PO1")

        result = callMethod<MockPurchaseItemManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseItemManager, "buildCriteria", searchModel
        )

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        // check param and its value
        assertEquals("PO1", resultParam[MockPurchaseItem::purchaseOrderLinearId.name])

        // check operation must be empty
        assert(resultOperation.filterNot { it.key == MockPurchaseItem::status.name }.isEmpty())

        // check status not invalid
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseItem::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseItem::status.name])

        // Search param for IS NULL
        searchModel = PurchaseSearchModel(deleteFlag = "IS_NULL")

        result = callMethod<MockPurchaseItemManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseItemManager, "buildCriteria", searchModel
        )

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        assertEquals(NullOperator.IS_NULL.name, resultParam[MockPurchaseItem::deleteFlag.name])
        assertEquals(SearchCriteriaOperation.ISNULL.name, resultOperation[MockPurchaseItem::deleteFlag.name])

        // check status not invalid
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseItem::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseItem::status.name])

        // Search param for NOT NULL
        searchModel = PurchaseSearchModel(deleteFlag = "NOT_NULL")

        result = callMethod<MockPurchaseItemManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                purchaseItemManager, "buildCriteria", searchModel
        )

        assertNotNull(result)

        resultParam = result.first
        resultOperation = result.second

        assertEquals(NullOperator.NOT_NULL.name, resultParam[MockPurchaseItem::deleteFlag.name])
        assertEquals(SearchCriteriaOperation.NOTNULL.name, resultOperation[MockPurchaseItem::deleteFlag.name])

        // check status not invalid
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockPurchaseItem::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockPurchaseItem::status.name])

    }
}