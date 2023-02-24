package th.co.dv.p2p.common.utilities

import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.*
import junit.framework.TestCase.assertTrue
import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.failedRequirement
import th.co.dv.p2p.common.enums.BaseMessageError
import th.co.dv.p2p.common.exceptions.AuthorizationException
import th.co.dv.p2p.common.models.CompanyBranchModel
import th.co.dv.p2p.common.models.CompanyModel
import th.co.dv.p2p.common.models.SearchInput
import th.co.dv.p2p.common.utilities.Conditions.using
import th.co.dv.p2p.common.utilities.Conditions.usingWith
import th.co.dv.p2p.common.utilities.Conditions.usingWithout
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.corda.base.IllegalFlowException
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.FileAttachmentModel
import th.co.dv.p2p.corda.base.models.PartyModel
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.jvm.javaField
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BaseHelperTest {

    @Test
    fun testUsingWithout() {
        var result = Try.on { "test." usingWithout true }
        assertTrue(result.isSuccess)

        result = Try.on { "test." usingWithout false }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("test."))
        assertFalse(result.toString().contains(failedRequirement))

        result = Try.on {
            TestError.V001 usingWithout (false)
        }
        assertNotNull(result.isFailure)
        assertTrue(result.toString().contains("Error (Error Code: V001)"))
        assertFalse(result.toString().contains(failedRequirement))
    }

    @Test
    fun testDeepCopy() {
        val company = CompanyModel(taxId = "taxId", companyBranches = mutableListOf(CompanyBranchModel(branchCode = "branchCode")))
        val shallowCopyCompany = company.copy()
        val deepCopyCompany = company.deepCopy()
        company.companyBranches.clear()
        assertEquals(0, company.companyBranches.size)
        assertEquals(0, shallowCopyCompany.companyBranches.size)
        assertEquals(shallowCopyCompany, company)

        val expectDeepCopyResult = CompanyModel(taxId = "taxId", companyBranches = mutableListOf(CompanyBranchModel(branchCode = "branchCode")))
        assertEquals(1, deepCopyCompany.companyBranches.size)
        assertEquals(expectDeepCopyResult, deepCopyCompany)
    }

    @Test
    fun testValidateEditableFields() {
        val data = Data(fieldA = "a", fieldB = "b", fieldC = BigDecimal("12.1234"), fieldD = BigDecimal("13.1234"))
        val uneditableFieldNames = listOf("fieldB", "fieldD")

        //Edit editable field (string)
        var newData = Data(fieldA = "a_new", fieldB = "b", fieldC = BigDecimal("12.1234"), fieldD = BigDecimal("13.1234"))
        var result = data.validateEditableFields(uneditableFieldNames, newData, "{0}")
        assertTrue(result.isEmpty())

        //Edit uneditable field (string)
        newData = Data(fieldA = "a", fieldB = "b_new", fieldC = BigDecimal("12.1234"), fieldD = BigDecimal("13.1234"))
        result = data.validateEditableFields(uneditableFieldNames, newData, "{0}")
        assertTrue(result.isNotEmpty())
        assertEquals(listOf("fieldB"), result)

        //Edit editable field (big decimal)
        newData = Data(fieldA = "a", fieldB = "b", fieldC = BigDecimal("11.1111"), fieldD = BigDecimal("13.1234"))
        result = data.validateEditableFields(uneditableFieldNames, newData, "{0}")
        assertTrue(result.isEmpty())

        //Edit uneditable field (big decimal)
        newData = Data(fieldA = "a_new", fieldB = "b", fieldC = BigDecimal("12.1234"), fieldD = BigDecimal("11.1111"))
        result = data.validateEditableFields(uneditableFieldNames, newData, "{0}")
        assertTrue(result.isNotEmpty())
        assertEquals(listOf("fieldD"), result)

    }

    fun testFieldCustomSet() {
        val purchaseOrderModel = PurchaseOrderModel(linearId = "123")

        // case null
        val linearIdField = purchaseOrderModel::linearId.javaField!!

        linearIdField.customSet(purchaseOrderModel, null)
        assertNull(purchaseOrderModel.linearId)

        // case set BigInteger to Long
        mockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")

        val paymentTermDaysField = purchaseOrderModel::paymentTermDays.javaField!!
        every { changeValueTypeToFieldType(paymentTermDaysField, BigInteger.TEN, any()) } returns 12L
        paymentTermDaysField.customSet(purchaseOrderModel, BigInteger.TEN)
        assertEquals(12L, purchaseOrderModel.paymentTermDays)

        // case set others
        val purchaseOrderNumberField = purchaseOrderModel::purchaseOrderNumber.javaField!!
        purchaseOrderNumberField.customSet(purchaseOrderModel, "ext")
        assertEquals("ext", purchaseOrderModel.purchaseOrderNumber)

        verify(exactly = 1) { changeValueTypeToFieldType(paymentTermDaysField, BigInteger.TEN, any()) }

        unmockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")
    }

    @Test
    fun testChangeValueTypeToFieldType() {
        val purchaseOrderModel = PurchaseOrderModel()
        // mock input
        val targetField = purchaseOrderModel::paymentTermDays.javaField!!
        val value = BigInteger.TEN
        val rootException = Exception("CONVERT FAILED")

        // Case : Big int to long
        val result = changeValueTypeToFieldType(targetField, value, rootException)
        assertEquals(10L, result)

        // Case : not support
        var expectedResult = Try.on { changeValueTypeToFieldType(targetField, 10, rootException) }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("CONVERT FAILED"))

        expectedResult = Try.on { changeValueTypeToFieldType(purchaseOrderModel::purchaseOrderNumber.javaField!!, value, rootException) }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("CONVERT FAILED"))

    }

    @Test
    fun testCastSearchInput() {

        val searchInputEqual = SearchInput(value = "Test", oper = SearchCriteriaOperation.EQUAL.name)
        val searchInputContain = SearchInput(value = "Test", oper = SearchCriteriaOperation.CONTAIN.name)
        val searchInputStartWith = SearchInput(value = "Test", oper = SearchCriteriaOperation.STARTS_WITH.name)

        mockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")
        every { extractSearchInput(any()) } returnsMany listOf(searchInputEqual, searchInputContain, searchInputStartWith) andThen null

        // Case send search model and operation EQUAL
        var result = castSearchInput<String>("Test")
        assertEquals("Test", result)

        // Case send search model and operation not EQUAL
        result = castSearchInput<String>("Test")
        assertEquals("%Test%", result)

        // Case send search model and operation START_WITH
        result = castSearchInput<String>("Test")
        assertEquals("Test%", result)

        // Case send string and not send flag
        result = castSearchInput<String>("Test")
        assertEquals("Test", result)

        // Case send string and not send flag = false
        result = castSearchInput<String>("Test", false)
        assertEquals("Test%", result)

        // Case input is null
        result = castSearchInput<String>(null)
        assertNull(result)

        verify(exactly = 5) { extractSearchInput(any()) }
        unmockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")
    }

    @Test
    fun testExtractSearchInput() {

        val searchInputEqual = SearchInput(value = "Test", oper = SearchCriteriaOperation.EQUAL.name)
        val searchInputContain = SearchInput(value = "Test", oper = SearchCriteriaOperation.CONTAIN.name)

        val searchInputEqualString = jacksonObjectMapper().writeValueAsString(searchInputEqual)
        val searchInputContainString = jacksonObjectMapper().writeValueAsString(searchInputContain)

        // Case search input with equal operation
        var result = extractSearchInput(searchInputEqualString)
        assertEquals(searchInputEqual, result)

        // Case search input with other operation
        result = extractSearchInput(searchInputContainString)
        assertEquals(searchInputContain, result)

        // Case cannot extract to search input
        result = extractSearchInput("TEST")
        assertNull(result)

        // Case input is null
        result = extractSearchInput(null)
        assertNull(result)

    }

    @Test
    fun testDetermineOperationFromValue() {

        // Case value have percent surrounding
        var result = determineOperationFromValue("%Test%")
        assertEquals("Test", result.first)
        assertEquals(SearchCriteriaOperation.CONTAIN.name, result.second)

        // Case value have percent only prefix
        result = determineOperationFromValue("%Test")
        assertEquals("Test", result.first)
        assertEquals(SearchCriteriaOperation.ENDS_WITH.name, result.second)

        // Case value have percent only suffix
        result = determineOperationFromValue("Test%")
        assertEquals("Test", result.first)
        assertEquals(SearchCriteriaOperation.STARTS_WITH.name, result.second)

        // Case value don't have percent
        result = determineOperationFromValue("Test")
        assertEquals("Test", result.first)
        assertNull(result.second)

    }

    @Test
    fun `test isListEqualsByFieldName`() {
        val purchaseOrderModels = listOf(PurchaseOrderModel(linearId = "abc"), PurchaseOrderModel(linearId = "abc"))
        var purchaseItemModels = listOf(PurchaseItemModel(companyCode = "abc"), PurchaseItemModel(companyCode = "abc"))

        //empty headerList with header fieldName
        var result = isListEqualsByFieldName(emptyList<PurchaseOrderModel>(), emptyList<PurchaseItemModel>(), "linearId")
        assertNull(result)

        //empty itemList with item fieldName
        result = isListEqualsByFieldName(emptyList<PurchaseOrderModel>(), emptyList<PurchaseItemModel>(), "item.linearId")
        assertNull(result)

        //test all type
        val fieldNames = listOf("accounting", "businessPlaceAddress1", "customisedFields", "paymentTermDays",
                "paymentTermMonths", "fileAttachments", "initialTotal", "requiredAcknowledgement")
        val po = PurchaseOrderModel(
                accounting = PartyModel(legalName = "legalName", organisation = "organ", organisationUnit = "unit"),
                businessPlaceAddress1 = "businessPlaceAddress1",
                customisedFields = mapOf("a" to 1),
                paymentTermDays = 2L,
                paymentTermMonths = 0,
                fileAttachments = listOf(FileAttachmentModel(attachmentId = 1)),
                initialTotal = 5.toBigDecimal(),
                requiredAcknowledgement = true
        )
        fieldNames.forEach {
            result = isListEqualsByFieldName(listOf(po, po), emptyList<PurchaseItemModel>(), it)
            assertNotNull(result)
            assertTrue(result!!)
        }

        val po2 = PurchaseOrderModel(
                accounting = PartyModel(legalName = "legalName2", organisation = "organ", organisationUnit = "unit"),
                businessPlaceAddress1 = "businessPlaceAddress",
                customisedFields = mapOf("a" to 2),
                paymentTermDays = 3L,
                paymentTermMonths = 2,
                fileAttachments = listOf(FileAttachmentModel(attachmentId = 3)),
                initialTotal = 5.0.toBigDecimal(),
                requiredAcknowledgement = false
        )
        fieldNames.forEach {
            result = isListEqualsByFieldName(listOf(po, po2), emptyList<PurchaseItemModel>(), it)
            assertNotNull(result)
            assertFalse(result!!)
        }

        //test item field equals
        result = isListEqualsByFieldName(purchaseOrderModels, purchaseItemModels, "item.companyCode")
        assertNotNull(result)
        assertTrue(result!!)

        //test item field not equals
        purchaseItemModels = listOf(PurchaseItemModel(companyCode = "abc"), PurchaseItemModel(companyCode = "abc"), PurchaseItemModel(companyCode = "456"))
        result = isListEqualsByFieldName(purchaseOrderModels, purchaseItemModels, "item.companyCode")
        assertNotNull(result)
        assertFalse(result!!)
    }

    @Test
    fun `Test using`(){
        // Case throw exception
        var result = trySilently {
            "Test AuthorizationException" using (false)
        }
        assertNotNull(result.exception)
        assertTrue(result.exception is IllegalFlowException)
        assertEquals("Failed requirement: Test AuthorizationException", result.exception!!.message)

        // Case not throw
        result = trySilently {
            "Test AuthorizationException" using (true)
        }
        assertTrue(result.body is Unit)
        assertNull(result.exception)

        result = trySilently {
            TestError.V001 using (false)
        }
        assertNotNull(result.exception)
        assertTrue(result.exception is IllegalFlowException)
        assertEquals("Failed requirement: Error (Error Code: V001)", result.exception!!.message)

    }

    @Test
    fun `Test usingWith`() {
        // Case throw exception
        var result = trySilently {
            "Test AuthorizationException".usingWith<AuthorizationException>(false)
        }
        assertNotNull(result.exception)
        assertTrue(result.exception is AuthorizationException)
        assertEquals("Failed requirement: Test AuthorizationException", result.exception!!.message)

        // Case not throw
        result = trySilently {
            "Test AuthorizationException".usingWith<AuthorizationException>(true)
        }
        assertTrue(result.body is Unit)
        assertNull(result.exception)
    }

    @Test
    fun `Test convertToMapSpecific`() {
        val quantity = Quantity(BigDecimal.TEN.setScale(), "EA")
        val result = quantity.convertToMapSpecific()
        val expectedResult = mapOf(
                "initial" to "10.0000000000",
                "consumed" to "0.0000000000",
                "remaining" to "10.0000000000",
                "unit" to "EA"
        )
        assertEquals(expectedResult, result)
    }

    @Test
    fun `Test QuantityConverter convertToDatabaseColumn`() {
        mockkConstructor(ObjectMapper::class)
        mockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")
        val quantityConverter = spyk<QuantityConverter>()
        val quantity = Quantity(BigDecimal.TEN.setScale(), "EA")
        val quantityMap = mapOf(
                "initial" to "10.0000000000",
                "consumed" to "0.0000000000",
                "remaining" to "10.0000000000",
                "unit" to "EA"
        )
        val expectedResult = "{\"initial\":\"10.0000000000\",\"consumed\":\"0.0000000000\",\"unit\":\"EA\",\"remaining\":\"10.0000000000\"}"

        every { quantity.convertToMapSpecific() } returns quantityMap
        every { anyConstructed<ObjectMapper>().writeValueAsString(quantityMap) } returns expectedResult andThenThrows
                JsonGenerationException("ERROR", null as JsonGenerator?)

        // success case
        var result = Try.on { quantityConverter.convertToDatabaseColumn(quantity) }
        assert(result.isSuccess)
        assertEquals(expectedResult, result.getOrThrow())

        // error case
        result = Try.on { quantityConverter.convertToDatabaseColumn(quantity) }
        assert(result.isFailure)
        assert(result.toString().contains("QuantityConverter writing error"))

        unmockkStatic("th.co.dv.p2p.common.utilities.BaseHelperKt")
        unmockkConstructor(ObjectMapper::class)
    }

    @Test
    fun `test determineParamOperation`(){
        var testData = listOf<Any>("1")
        var result = testData.determineParamOperation()
        assertEquals("1", result.first)
        assertEquals(SearchCriteriaOperation.EQUAL.name, result.second)

        testData = listOf(1, 2)
        result = testData.determineParamOperation()
        assertEquals(testData, result.first)
        assertEquals(SearchCriteriaOperation.IN.name, result.second)
    }
}

class Data(
        var fieldA: String? = null,
        var fieldB: String? = null,
        var fieldC: BigDecimal? = null,
        var fieldD: BigDecimal? = null
)

enum class TestError(val msg: String): BaseMessageError {
    V001("Error");
    override fun getCode() = name
    override fun getMessage() = msg
}