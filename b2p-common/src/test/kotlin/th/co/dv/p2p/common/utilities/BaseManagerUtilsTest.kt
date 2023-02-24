package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import org.junit.Test
import th.co.dv.p2p.common.TestHelper
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.FROM
import th.co.dv.p2p.common.constants.TO
import th.co.dv.p2p.common.utilities.BaseManagerUtils.generateCriteriaMap
import th.co.dv.p2p.common.utilities.BaseManagerUtils.getColumnName
import th.co.dv.p2p.common.utilities.manager.*
import th.co.dv.p2p.common.utilities.search.SearchCriteria
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BaseManagerUtilsTest {

    private val headerClass = MockCreditNote::class.java
    private val itemClass = MockCreditNoteItem::class.java

    @Test
    fun testFilteredEligibleField() {
        val listOfInputField = listOf("externalId", "item.referenceField1")

        // not include item field
        var result = BaseManagerUtils.filteredEligibleField(listOfInputField, headerClass = headerClass, itemClass = itemClass)
        assertEquals(1, result.size)
        assert(result.containsAll(listOf("externalId")))

        // include item field
        result = BaseManagerUtils.filteredEligibleField(listOfInputField, true, headerClass = headerClass, itemClass = itemClass, itemFieldName = "testItemName")
        assertEquals(2, result.size)
        assert(result.containsAll(listOf("externalId", "testItemName.referenceField1")))
    }

    @Test
    fun testComputeSortField() {

        mockkObject(BaseManagerUtils)
        every { getColumnName(
                fields = listOf("businessPlaceTaxNumber"),
                headerClass = MockPurchaseOrder::class.java,
                itemClass = MockPurchaseItem::class.java) } returns listOf("purchase_order.business_place_tax_number")
        every { getColumnName(
                fields = listOf("item.site"),
                headerClass = MockPurchaseOrder::class.java,
                itemClass = MockPurchaseItem::class.java) } returns listOf()
        every { getColumnName(
                fields = listOf(BasePurchaseOrderStatements.defaultSortField),
                headerClass = MockPurchaseOrder::class.java,
                itemClass = MockPurchaseItem::class.java) } returns listOf("purchase_order.purchase_order_number")

        // Case return header field with table column
        var result = BaseManagerUtils.computeSortField(
                sortField = "businessPlaceTaxNumber",
                defaultField = BasePurchaseOrderStatements.defaultSortField,
                headerClass = MockPurchaseOrder::class.java,
                itemClass = MockPurchaseItem::class.java,
                isTableColumn = true)

        assertNotNull(result)
        assertEquals("purchase_order.business_place_tax_number", result)

        // Case return header field with model column
        result = BaseManagerUtils.computeSortField(
                sortField = "businessPlaceTaxNumber",
                defaultField = BasePurchaseOrderStatements.defaultSortField,
                headerClass = MockPurchaseOrder::class.java,
                itemClass = MockPurchaseItem::class.java,
                isTableColumn = false)

        assertNotNull(result)
        assertEquals("businessPlaceTaxNumber", result)

        // Case return empty list use default sort field with table column
        result = BaseManagerUtils.computeSortField(
                sortField = "item.site",
                defaultField = BasePurchaseOrderStatements.defaultSortField,
                headerClass = MockPurchaseOrder::class.java,
                itemClass = MockPurchaseItem::class.java,
                isTableColumn = true)

        assertNotNull(result)
        assertEquals("purchase_order.purchase_order_number", result)

        // Case return empty list use default sort field with model column
        result = BaseManagerUtils.computeSortField(
                sortField = "item.site",
                defaultField = BasePurchaseOrderStatements.defaultSortField,
                headerClass = MockPurchaseOrder::class.java,
                itemClass = MockPurchaseItem::class.java,
                isTableColumn = false)

        assertNotNull(result)
        assertEquals(BasePurchaseOrderStatements.defaultSortField, result)

        unmockkObject(BaseManagerUtils)
    }

    @Test
    fun testGetColumnName() {
        val listOfInputField = listOf("externalId", "item.referenceField1")

        // not include item field
        var result = getColumnName(listOfInputField, false, headerClass = headerClass, itemClass = itemClass)
        assertEquals(1, result.size)
        assert(result.containsAll(listOf("mock_credit_note.external_id")))

        // include item field
        result = getColumnName(listOfInputField, true, headerClass = headerClass, itemClass = itemClass)
        assertEquals(2, result.size)
        assert(result.containsAll(listOf("mock_credit_note.external_id", "mock_credit_note_item.reference_field1")))

        // Case input invalid
        result = getColumnName(listOf("mock", "input.mock"), headerClass = headerClass, itemClass = itemClass)
        assertEquals(0, result.size)
    }

    @Test
    fun testGenerateCriteriaMap() {
        val field = "field"
        val value = "value"
        val searchOperation = SearchCriteriaOperation.NOT_EQUAL

        // Do not mock method addCriteria to make sure criteria has been added
        val result = generateCriteriaMap(headerClass, field, value, searchOperation)
        assertEquals(1, result.getCriterias().size)
        val criteriaResult = result.getCriterias().find {
            val searchCriteria = it as SearchCriteria
            searchCriteria.getField() == field
        } as SearchCriteria

        assertNotNull(criteriaResult)
        assertEquals(value, criteriaResult.getValue())
        assertEquals(SearchCriteriaOperation.NOT_EQUAL, criteriaResult.getOp())
    }

    @Test
    fun testAddCriteria() {
        val baseManagerUtils = spyk<BaseManagerUtils>()
        val sc = SearchCriterias(MockCreditNote::class.java)
        val date1 = Date()
        val date2 = Date.from(Instant.now().plusDays(1))
        val mockParam = mapOf(
                InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerApprovedDate.name + FROM to date1,
                InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerApprovedDate.name + TO to date2
        )

        val mockOperations = mapOf(
                InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerApprovedDate.name + FROM to SearchCriteriaOperation.GREATERTHAN_OR_EQUAL,
                InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerApprovedDate.name + TO to SearchCriteriaOperation.LESSTHAN
        )

        val result = Try.on {
            TestHelper.callMethod<BaseManagerUtils, Unit>(
                    baseManagerUtils, "addCriteria", sc, mockParam, mockOperations)
        }

        assertTrue(result.isSuccess)
        //assert criteria in SearchCriteias
        // buyerApprovedDateFrom
        val buyerApprovedDateFromCriteria = sc.getCriterias().find {
            val searchCriteria = it as SearchCriteria
            searchCriteria.getField() == InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerApprovedDate.name + FROM
        } as SearchCriteria
        assertNotNull(buyerApprovedDateFromCriteria)
        assertEquals(date1, buyerApprovedDateFromCriteria.getValue())
        assertEquals(SearchCriteriaOperation.GREATERTHAN_OR_EQUAL, buyerApprovedDateFromCriteria.getOp())

        // buyerApprovedDateTo
        val buyerApprovedDateToCriteria = sc.getCriterias().find {
            val searchCriteria = it as SearchCriteria
            searchCriteria.getField() == InterfaceBaseCreditNote<InterfaceBaseCreditNoteItem>::buyerApprovedDate.name + TO
        } as SearchCriteria
        assertNotNull(buyerApprovedDateToCriteria)
        assertEquals(date2, buyerApprovedDateToCriteria.getValue())
        assertEquals(SearchCriteriaOperation.LESSTHAN, buyerApprovedDateToCriteria.getOp())

    }
}