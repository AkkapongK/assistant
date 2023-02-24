package th.co.dv.p2p.common.utilities.manager

import io.mockk.*
import io.mockk.impl.annotations.MockK
import net.corda.core.node.services.vault.NullOperator
import org.junit.Before
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.constants.FROM
import th.co.dv.p2p.common.constants.TO
import th.co.dv.p2p.common.enums.Lifecycle
import th.co.dv.p2p.common.enums.RecordStatus
import th.co.dv.p2p.common.models.NativeQueryModel
import th.co.dv.p2p.common.utilities.AuthorizationUtils.INTERFACE_AUTHORIZATION
import th.co.dv.p2p.common.utilities.BaseManagerUtils
import th.co.dv.p2p.common.utilities.determineOperationFromValue
import th.co.dv.p2p.common.utilities.manager.BaseCreditNoteStatements.defaultSortField
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import th.co.dv.p2p.common.utilities.search.paging.PagableList
import th.co.dv.p2p.corda.base.models.CreditNoteModel
import th.co.dv.p2p.corda.base.models.CreditNoteSearchModel
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BaseCreditNoteManagerTest {

    @MockK
    lateinit var creditNoteService: MockCreditNoteService

    @Before
    fun setup() = MockKAnnotations.init(this)


    private val creditNote = MockCreditNote(
            linearId = "linearId",
            externalId = "externalId"
    )

    private val creditNoteModel = CreditNoteModel(
            linearId = "linearId",
            externalId = "externalId"
    )

    private val creditNoteSearchModel = CreditNoteSearchModel(
            linearIds = listOf("linearId"),
            creditNoteExternalId = "externalId",
            pageNumber = 1,
            pageSize = 20,
            sortField = "vendorName",
            sortOrder = 0
    )

    private val authorization = INTERFACE_AUTHORIZATION
    private val param = mapOf("status" to listOf("INVALID"))
    private val operation = mapOf("status" to "NOT_IN")
    private val buildCriteria = Pair(param, operation)
    private val headerClass = MockCreditNote::class.java
    private val itemClass = MockCreditNoteItem::class.java
    private val searchCriterias = SearchCriterias(MockCreditNote::class.java)

    @Test
    fun testSearchQuery() {
        val creditNoteManager = spyk<MockCreditNoteManager>(recordPrivateCalls = true)

        every { creditNoteManager["buildCriteria"](any<CreditNoteSearchModel>()) } returns buildCriteria
        every {
            creditNoteManager["queryByParam"](param, operation, any<CreditNoteSearchModel>(), authorization, searchCriterias)
        } returns PagableList(listOf(creditNoteModel) as MutableList<CreditNoteModel>)
        every {
            creditNoteManager["nativeQuery"](param, operation, any<CreditNoteSearchModel>(), any<List<String>>(), authorization, searchCriterias)
        } returns PagableList(listOf(creditNoteModel.copy(linearId = "linearId2", externalId = "externalId2")) as MutableList<CreditNoteModel>)


        // Case not specific select field
        var selectFields = emptyList<String>()
        var result = creditNoteManager.searchQuery(creditNoteSearchModel, selectFields, authorization, searchCriterias)
        assertEquals(1, result.size)
        assertEquals("linearId", result.first().linearId)
        assertEquals("externalId", result.first().externalId)
        verify(exactly = 1) { creditNoteManager["queryByParam"](param, operation, any<CreditNoteSearchModel>(), authorization, searchCriterias) }
        verify(exactly = 0) { creditNoteManager["nativeQuery"](param, operation, any<CreditNoteSearchModel>(), any<List<String>>(), authorization, searchCriterias) }


        // Case specific select field
        selectFields = listOf("linearID")
        result = creditNoteManager.searchQuery(creditNoteSearchModel.copy(linearIds = listOf("linearId2"), creditNoteExternalId = "externalId2"), selectFields, authorization, searchCriterias)
        assertEquals(1, result.size)
        assertEquals("linearId2", result.first().linearId)
        assertEquals("externalId2", result.first().externalId)
        verify(atMost = 1) { creditNoteManager["queryByParam"](param, operation, any<CreditNoteSearchModel>(), authorization, searchCriterias) }
        verify(exactly = 1) { creditNoteManager["nativeQuery"](param, operation, any<CreditNoteSearchModel>(), any<List<String>>(), authorization, searchCriterias) }

    }

    @Test
    fun testNativeQuery() {
        val nativeQuery = slot<NativeQueryModel>()
        val creditNoteManager = spyk<MockCreditNoteManager>()
        mockkObject(BaseManagerUtils)

        every { BaseManagerUtils.inferSortDirection(1) } returns "asc"
        every { BaseManagerUtils.inferSortDirection(0) } returns "desc"
        every { creditNoteManager.creditNoteService } returns creditNoteService
        every { creditNoteManager["initQueryableService"]() } returns creditNoteService
        //Mock method getCreditNoteColumnName by input
        every { BaseManagerUtils.getColumnName(emptyList(), headerClass = headerClass, itemClass = itemClass) } returns emptyList()
        every { BaseManagerUtils.getColumnName(listOf("vendorName"), headerClass = headerClass, itemClass = itemClass) } returns listOf("credit_note.vendor_name")
        //Mock method filteredEligibleField by input
        every { BaseManagerUtils.filteredEligibleField(emptyList(), headerClass = headerClass, itemClass = itemClass, itemFieldName = InterfaceBaseCreditNote<*>::creditNoteItems.name) } returns emptyList()
        every { BaseManagerUtils.filteredEligibleField(listOf("linearId", "externalId"), headerClass = headerClass, itemClass = itemClass, itemFieldName = InterfaceBaseCreditNote<*>::creditNoteItems.name) } returns listOf("linearId", "externalId")
        every { BaseManagerUtils.filteredEligibleField(listOf("linearId", "externalId", "vendorName"), headerClass = headerClass, itemClass = itemClass, itemFieldName = InterfaceBaseCreditNote<*>::creditNoteItems.name) } returns listOf("linearId", "externalId", "vendorName")


        //Case no select field
        every { creditNoteManager["completeNativeQuery"](capture(nativeQuery)) } answers { nativeQuery.captured }
        every { creditNoteManager["computeGroupByClause"](BaseCreditNoteStatements.DEFAULT_GROUP, creditNoteSearchModel.sortField) } returns Pair(BaseCreditNoteStatements.DEFAULT_GROUP, "credit_note.externalId")
        var selectFields = emptyList<String>()
        every {
            creditNoteService.native(match {
                it.customSelect == BaseCreditNoteStatements.NATIVE_SELECT &&
                        it.fields == BaseCreditNoteStatements.defaultField &&
                        it.groupBy == BaseCreditNoteStatements.DEFAULT_GROUP
            }, any())
        } returns PagableList(listOf(creditNote) as MutableList<MockCreditNote>)
        var result = callMethod<MockCreditNoteManager, PagableList<CreditNoteModel>>(creditNoteManager, "nativeQuery",
                param, operation, creditNoteSearchModel, selectFields, authorization, searchCriterias)
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("linearId", result.first().linearId)
        assertEquals("externalId", result.first().externalId)

        //Case select field equal default field
        selectFields = BaseCreditNoteStatements.defaultField
        result = callMethod<MockCreditNoteManager, PagableList<CreditNoteModel>>(creditNoteManager, "nativeQuery",
                param, operation, creditNoteSearchModel, selectFields, authorization, searchCriterias)
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("linearId", result.first().linearId)
        assertEquals("externalId", result.first().externalId)

        //Case select field has another field which not default
        val groupByCause = BaseCreditNoteStatements.DEFAULT_GROUP + ", credit_note.vendor_name"
        every { creditNoteManager["computeGroupByClause"](groupByCause, creditNoteSearchModel.sortField) } returns Pair(groupByCause, "credit_note.externalId")
        selectFields = BaseCreditNoteStatements.defaultField.plus("vendorName")
        val expectCreditNote = creditNote.copy(linearId = "linearId3", externalId = "externalId3", vendorName = "vendorName")
        every {
            creditNoteService.native(match {
                it.customSelect.contains(BaseCreditNoteStatements.NATIVE_SELECT) &&
                        it.customSelect.contains("credit_note.vendor_name") &&
                        it.fields == BaseCreditNoteStatements.defaultField.plus("vendorName") &&
                        it.groupBy == groupByCause
            }, any())
        } returns PagableList(listOf(expectCreditNote) as MutableList<MockCreditNote>)
        result = callMethod<MockCreditNoteManager, PagableList<CreditNoteModel>>(creditNoteManager, "nativeQuery",
                param, operation, creditNoteSearchModel, selectFields, authorization, searchCriterias)
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("linearId3", result.first().linearId)
        assertEquals("externalId3", result.first().externalId)
        assertEquals("vendorName", result.first().vendorName)

        unmockkObject(BaseManagerUtils)
    }

    @Test
    fun testComputeGroupByClause() {
        val creditNoteManager = spyk<MockCreditNoteManager>()
        val groupByClause = "credit_note.linear_id, credit_note.external_id"
        //Mock method computeSortField by input
        every { creditNoteManager["computeSortField"]("externalId", true) } returns "credit_note.external_id"
        every { creditNoteManager["computeSortField"]("vendorName", true) } returns "credit_note.vendor_name"


        // Case sort field already in group by
        var result = callMethod<MockCreditNoteManager, Pair<String, String>>(creditNoteManager, "computeGroupByClause",
                groupByClause, "externalId")
        assertNotNull(result)
        assertEquals(groupByClause, result.first)
        assertEquals("credit_note.external_id", result.second)


        // Case sort field is not exist in group by
        result = callMethod<MockCreditNoteManager, Pair<String, String>>(creditNoteManager, "computeGroupByClause",
                groupByClause, "vendorName")
        assertNotNull(result)
        assertEquals(groupByClause.plus(", credit_note.vendor_name"), result.first)
        assertEquals("credit_note.vendor_name", result.second)
    }

    @Test
    fun testComputeSortField() {

        val creditNoteManager = spyk<MockCreditNoteManager>()
        mockkObject(BaseManagerUtils)

        every { BaseManagerUtils.computeSortField(
                sortField = any(),
                defaultField = defaultSortField,
                headerClass = headerClass,
                itemClass = itemClass,
                isTableColumn = true) } returns "credit_note.external_id"

        every { BaseManagerUtils.computeSortField(
                sortField = any(),
                defaultField = defaultSortField,
                headerClass = headerClass,
                itemClass = itemClass,
                isTableColumn = false) } returns defaultSortField

        // Case return table column
        var result = callMethod<MockCreditNoteManager, String>(creditNoteManager, "computeSortField", "externalId", true)
        assertNotNull(result)
        assertEquals("credit_note.external_id", result)

        // Case return model column
        result = callMethod<MockCreditNoteManager, String>(creditNoteManager, "computeSortField", "invoice.site", false)
        assertNotNull(result)
        assertEquals(defaultSortField, result)

        verify(exactly = 1) { BaseManagerUtils.computeSortField(
                sortField = any(),
                defaultField = defaultSortField,
                headerClass = headerClass,
                itemClass = itemClass,
                isTableColumn = true) }

        verify(exactly = 1) { BaseManagerUtils.computeSortField(
                sortField = any(),
                defaultField = defaultSortField,
                headerClass = headerClass,
                itemClass = itemClass,
                isTableColumn = false) }

        unmockkObject(BaseManagerUtils)
    }

    @Test
    fun testQueryByParam() {
        val creditNoteManager = spyk<MockCreditNoteManager>()
        mockkObject(BaseManagerUtils)
        every { BaseManagerUtils.inferSortDirection(1) } returns "asc"
        every { BaseManagerUtils.inferSortDirection(0) } returns "desc"

        //Case can not find from native Query
        every {
            creditNoteManager["nativeQuery"](param, operation, CreditNoteSearchModel().copy(returnCreditNoteItems = false), emptyList<String>(), authorization, searchCriterias)
        } returns PagableList(mutableListOf<CreditNoteModel>())
        var result = callMethod<MockCreditNoteManager, PagableList<CreditNoteModel>>(creditNoteManager, "queryByParam",
                param, operation, CreditNoteSearchModel(), authorization, searchCriterias)
        assertEquals(PagableList(mutableListOf()), result)

        //Case find data from Native
        val mockNativeQueryResult = PagableList(mutableListOf(creditNoteModel))
        mockNativeQueryResult.setPage(10)
        mockNativeQueryResult.setPageSize(20)
        mockNativeQueryResult.setTotalSize(200)
        every {
            creditNoteManager["nativeQuery"](param, operation, creditNoteSearchModel.copy(returnCreditNoteItems = false), emptyList<String>(), authorization, searchCriterias)
        } returns mockNativeQueryResult
        every { creditNoteManager["computeSortField"]("vendorName", false) } returns "vendorName"
        val linearIdParam = mapOf(
                "linearId" to listOf("linearId"),
                "pageNumber" to "1",
                "pageSize" to "20",
                "sortField" to "vendorName",
                "sortOrder" to "desc",
                "isPaging" to false
        )
        val linearIdOperation = mapOf("linearId" to "IN")
        every { creditNoteManager.creditNoteService } returns creditNoteService
        every { creditNoteManager["initQueryableService"]() } returns creditNoteService
        every { creditNoteService.findByParam(linearIdParam, linearIdOperation, INTERFACE_AUTHORIZATION) } returns listOf(creditNote)
        val expect = PagableList(mutableListOf(creditNoteModel))
        expect.setPage(10)
        expect.setPageSize(20)
        expect.setTotalSize(200)
        result = callMethod<MockCreditNoteManager, PagableList<CreditNoteModel>>(creditNoteManager, "queryByParam",
                param, operation, creditNoteSearchModel, authorization, searchCriterias)
        assertEquals(expect, result)

        unmockkObject(BaseManagerUtils)
    }

    @Test
    fun testBuildCriteria() {
        mockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")

        val creditNoteManager = spyk<MockCreditNoteManager>()

        //Not specific search param
        var searchModel = CreditNoteSearchModel()
        var result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        var resultParam = result.first
        var resultOperation = result.second
        assert(resultParam.size == 1)
        assert(resultOperation.size == 1)
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockCreditNote::status.name])

        every { determineOperationFromValue("paymentDescription") } returns Pair("paymentDescription", SearchCriteriaOperation.STARTS_WITH.name)

        // Search param for STARTS_WITH case
        val caseInsensitive = SearchCriteriaOperation.STARTS_WITH.name
        searchModel = CreditNoteSearchModel(
                invoiceExternalId = "invoiceExternalId",
                vendorName = "vendorName",
                vendorNumber = "vendorNumber",
                vendorTaxNumber = "vendorTaxNumber",
                companyTaxNumber = "companyTaxNumber",
                companyCode = "companyCode",
                companyName = "companyName",
                creditNoteExternalId = "creditNoteExternalId",
                referenceField1 = "referenceField1",
                taxDocumentNumber = "taxDocumentNumber",
                paymentDescription = "paymentDescription"
        )
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockCreditNote::status.name])
        assert(resultOperation.filterNot { it.key == MockCreditNote::status.name }.all { it.value == caseInsensitive })
        assertEquals("invoiceExternalId", resultParam[MockCreditNote::invoiceExternalId.name])
        assertEquals("vendorName", resultParam[MockCreditNote::vendorName.name])
        assertEquals("vendorNumber", resultParam[MockCreditNote::vendorNumber.name])
        assertEquals("vendorTaxNumber", resultParam[MockCreditNote::vendorTaxNumber.name])
        assertEquals("companyTaxNumber", resultParam[MockCreditNote::companyTaxNumber.name])
        assertEquals("companyCode", resultParam[MockCreditNote::companyCode.name])
        assertEquals("companyName", resultParam[MockCreditNote::companyName.name])
        assertEquals("creditNoteExternalId", resultParam[MockCreditNote::externalId.name])
        assertEquals("referenceField1", resultParam[MockCreditNote::referenceField1.name])
        assertEquals("taxDocumentNumber", resultParam[MockCreditNote::taxDocumentNumber.name])
        assertEquals("paymentDescription", resultParam[MockCreditNote::paymentDescription.name])

        //Search param for IN case
        val caseIn = SearchCriteriaOperation.IN.name
        searchModel = CreditNoteSearchModel(
                statuses = listOf(RecordStatus.VALID.name),
                linearIds = listOf("linearId1", "linearId1", "linearId2"),
                postingStatus = listOf("SUCCESS", "PENDING"),
                invoiceLinearId = listOf("invoiceLinearId1", "invoiceLinearId2"),
                adjustmentType = listOf("PRICE", "QUANTITY"),
                lifecycles = listOf("ISSUED, MATCHED"),
                vatTriggerPoints = listOf("None", "Payment"),
                taxDocumentLinearIds = listOf("01", "02")
        )
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertEquals(listOf(RecordStatus.VALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.IN.name, resultOperation[MockCreditNote::status.name])
        assertTrue(resultOperation.filterNot { it.key == MockCreditNote::status.name }.all { it.value == caseIn })
        assertEquals(listOf("ISSUED, MATCHED"), resultParam[MockCreditNote::lifecycle.name])
        assertEquals(listOf("linearId1", "linearId2"), resultParam[MockCreditNote::linearId.name])
        assertEquals(listOf("invoiceLinearId1", "invoiceLinearId2"), resultParam[MockCreditNote::invoiceLinearId.name])
        assertEquals(listOf("PRICE", "QUANTITY"), resultParam[MockCreditNote::adjustmentType.name])
        assertEquals(listOf("None", "Payment"), resultParam[MockCreditNote::vatTriggerPoint.name])
        assertEquals(listOf("01", "02"), resultParam[MockCreditNote::taxDocumentLinearId.name])
        // Removed from build criteria
        assertNull(resultParam[MockCreditNote::buyerPostingStatus.name])

        //Search param for GREATERTHAN_OR_EQUAL case
        val caseGreaterThanOrEqual = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name
        searchModel = CreditNoteSearchModel(
                documentEntryDateFrom = "03/02/2020",
                creditNoteDateFrom = "03/02/2020",
                creditNotePostingDateFrom = "03/02/2020",
                matchingDateFrom = "03/02/2020",
                paymentDateFrom = "03/02/2020",
                buyerPostingDateFrom = "03/02/2020"
        )
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockCreditNote::status.name])
        assert(resultOperation.filterNot { it.key == MockCreditNote::status.name }.all { it.value == caseGreaterThanOrEqual })
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockCreditNote::documentEntryDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockCreditNote::creditNoteDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockCreditNote::creditPostingUpdatedDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockCreditNote::lastMatchUpdatedDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockCreditNote::paymentDate.name + FROM]?.toString())
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockCreditNote::buyerPostingDate.name + FROM]?.toString())


        //Search param for LESSTHAN case
        val caseLessThan = SearchCriteriaOperation.LESSTHAN.name
        searchModel = CreditNoteSearchModel(
                documentEntryDateTo = "03/02/2020",
                creditNoteDateTo = "03/02/2020",
                creditNotePostingDateTo = "03/02/2020",
                matchingDateTo = "03/02/2020",
                paymentDateTo = "03/02/2020",
                buyerPostingDateTo = "03/02/2020"
        )
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockCreditNote::status.name])
        assert(resultOperation.filterNot { it.key == MockCreditNote::status.name }.all { it.value == caseLessThan })
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockCreditNote::documentEntryDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockCreditNote::creditNoteDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockCreditNote::creditPostingUpdatedDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockCreditNote::lastMatchUpdatedDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockCreditNote::paymentDate.name + TO]?.toString())
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockCreditNote::buyerPostingDate.name + TO]?.toString())


        //Search param for NOT_NULL case
        val caseNotNull = SearchCriteriaOperation.NOTNULL.name
        searchModel = CreditNoteSearchModel(isIssuePayment = true, isNormalSubtype = false)
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockCreditNote::status.name])
        assert(resultOperation.filterNot { it.key == MockCreditNote::status.name }.all { it.value == caseNotNull })
        assertEquals("NOT_NULL", resultParam[MockCreditNote::paymentItemLinearId.name])
        assertEquals(NullOperator.NOT_NULL.name, resultParam[MockCreditNote::creditNoteSubType.name])
        assertEquals(SearchCriteriaOperation.NOTNULL.name, resultOperation[MockCreditNote::creditNoteSubType.name])


        //Search param for NULL case
        val caseNull = SearchCriteriaOperation.ISNULL.name
        searchModel = CreditNoteSearchModel(isIssuePayment = false, isNormalSubtype = true)
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockCreditNote::status.name])
        assert(resultOperation.filterNot { it.key == MockCreditNote::status.name }.all { it.value == caseNull })
        assertEquals("IS_NULL", resultParam[MockCreditNote::paymentItemLinearId.name])
        assertEquals(NullOperator.IS_NULL.name, resultParam[MockCreditNote::creditNoteSubType.name])
        assertEquals(SearchCriteriaOperation.ISNULL.name, resultOperation[MockCreditNote::creditNoteSubType.name])


        //Special case creditPostingUpdatedDate
        searchModel = CreditNoteSearchModel(
                creditPostingUpdatedDate = "03/02/2020"
        )
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockCreditNote::status.name])
        assertEquals("2020-02-03 00:00:00.0", resultParam[MockCreditNote::creditPostingUpdatedDate.name + FROM]?.toString())
        assertEquals(caseGreaterThanOrEqual, resultOperation[MockCreditNote::creditPostingUpdatedDate.name + FROM])
        assertEquals("2020-02-04 00:00:00.0", resultParam[MockCreditNote::creditPostingUpdatedDate.name + TO]?.toString())
        assertEquals(caseLessThan, resultOperation[MockCreditNote::creditPostingUpdatedDate.name + TO])


        //Special case paymentItemLinearIds
        // case paymentItemLinearIds send only 1 element will use searchCriteria EQUAL
        val caseEqual = SearchCriteriaOperation.EQUAL.name
        searchModel = CreditNoteSearchModel(
                paymentItemLinearIds = listOf("paymentItemLinearId1")
        )
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockCreditNote::status.name])
        assertEquals("paymentItemLinearId1", resultParam[MockCreditNote::paymentItemLinearId.name])
        assertEquals(caseEqual, resultOperation[MockCreditNote::paymentItemLinearId.name])
        // case paymentItemLinearIds send more than 1 elements will use searchCriteria IN
        searchModel = CreditNoteSearchModel(
                paymentItemLinearIds = listOf("paymentItemLinearId1", "paymentItemLinearId2")
        )
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
                creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertEquals(listOf(RecordStatus.INVALID.name), resultParam[MockCreditNote::status.name])
        assertEquals(SearchCriteriaOperation.NOT_IN.name, resultOperation[MockCreditNote::status.name])
        assertEquals(listOf("paymentItemLinearId1", "paymentItemLinearId2"), resultParam[MockCreditNote::paymentItemLinearId.name])
        assertEquals(caseIn, resultOperation[MockCreditNote::paymentItemLinearId.name])

        //Case isRdSubmitted is not null && isOnHold
        searchModel = CreditNoteSearchModel(lifecycles = listOf("ISSUED, MATCHED"), isRdSubmitted = false, isOnHold = true)
        result = callMethod<MockCreditNoteManager, Pair<MutableMap<String, Any>, MutableMap<String, String>>>(
            creditNoteManager, "buildCriteria", searchModel)
        assertNotNull(result)
        resultParam = result.first
        resultOperation = result.second
        assertNull(resultParam[MockCreditNote::lifecycle.name])
        assertNull(resultOperation[MockCreditNote::lifecycle.name])
        assertEquals(true, resultParam[MockCreditNote::isOnHold.name])
        assertNull(resultOperation[MockCreditNote::isOnHold.name])

        unmockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")
    }

    @Test
    fun testBuildExtraSearchCriteria() {
        val manager = spyk<MockCreditNoteManager>()
        var result = callMethod<MockCreditNoteManager, SearchCriterias<MockCreditNote>?>(
                manager,
                "buildExtraSearchCriteria",
                CreditNoteSearchModel()
        )
        assertNotNull(result)
        assertEquals(0, result.getCriterias().size)

        var creditNoteSearchModel = CreditNoteSearchModel(
                statuses = listOf(Lifecycle.CreditNoteLifecycle.MATCHED.name),
                documentEntryDateFrom = "20/02/2020",
                documentEntryDateTo = "20/02/2020",
                invoiceLinearId = listOf("invoiceLinearId"),
                linearIds = listOf("linearId1", "linearId2", "linearId3"),
                invoiceExternalId = "invoiceExternalId",
                vendorName = "vendorName",
                vendorNumber = "vendorNumber",
                vendorTaxNumber = "vendorTaxNumber",
                companyTaxNumber = "companyTaxNumber",
                companyCode = "companyCode",
                companyName = "companyName",
                creditNoteDateFrom = "20/02/2020",
                creditNoteDateTo = "20/02/2020",
                creditNoteExternalId = "externalId",
                creditPostingUpdatedDate = "20/02/2020",
                adjustmentType = listOf("QUANTITY"),
                isSellerStatus = false,
                returnCreditNoteItems = false,
                postingStatus = listOf("postingStatus"),
                postingStatusNotIn = listOf("postingStatusNotIn"),
                creditNotePostingDateFrom = "20/02/2020",
                creditNotePostingDateTo = "20/02/2020",
                exactCreditNoteBuyerPostingDate = "2020-07-13T09:35:31.693Z",
                matchingDateFrom = "20/02/2020",
                matchingDateTo = "20/02/2020",
                sortField = "externalId",
                sortOrder = 1,
                pageNumber = 1,
                pageSize = 200,
                isIssuePayment = false,
                referenceField1 = "referenceField1",
                paymentDateFrom = "20/02/2020",
                paymentDateTo = "20/02/2020",
                returnBuyerPostingDetail = false
        )

        result = callMethod(
                manager,
                "buildExtraSearchCriteria",
                creditNoteSearchModel
        )
        assertNotNull(result)
        // 3 for 3 params: exactCreditNoteBuyerPostingDate, postingStatus, postingStatusNotIn
        assertEquals(3, result.getCriterias().size)

        creditNoteSearchModel = CreditNoteSearchModel(
            lifecycles = listOf(Lifecycle.CreditNoteLifecycle.MATCHED.name, Lifecycle.CreditNoteLifecycle.ISSUED.name),
            isRdSubmitted = true
        )

        result = callMethod(manager, "buildExtraSearchCriteria", creditNoteSearchModel)

        assertNotNull(result)
        assertEquals(0, result.getCriterias().size)

        creditNoteSearchModel = CreditNoteSearchModel(lifecycles = listOf(Lifecycle.CreditNoteLifecycle.MATCHED.name, Lifecycle.CreditNoteLifecycle.ISSUED.name))

        result = callMethod(manager, "buildExtraSearchCriteria", creditNoteSearchModel)

        assertNotNull(result)
        assertEquals(0, result.getCriterias().size)

        creditNoteSearchModel = CreditNoteSearchModel(
            lifecycles = listOf(Lifecycle.CreditNoteLifecycle.MATCHED.name, Lifecycle.CreditNoteLifecycle.REJECTED.name),
            isRdSubmitted = true
        )

        result = callMethod(manager, "buildExtraSearchCriteria", creditNoteSearchModel)

        assertNotNull(result)
        assertEquals(1, result.getCriterias().size)
    }
}