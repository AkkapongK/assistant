package th.co.dv.p2p.common.utilities.search


import io.mockk.*
import net.corda.core.node.services.vault.*
import net.corda.core.utilities.Try
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.constants.comma
import th.co.dv.p2p.common.models.Condition
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.models.UserGroup
import th.co.dv.p2p.common.utilities.AuthorizationUtils
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import kotlin.test.*

class AuthorizationByFieldsTest {


    private val condition1 = Condition(
            field = InvoiceModel::companyTaxNumber.name,
            operator = CollectionOperator.IN.name,
            value = "0105556176239,0745550001199"
    )

    private val condition2 = Condition(
            field = InvoiceModel::vendorTaxNumber.name,
            operator = CollectionOperator.IN.name,
            value = "0105556176239,0745550001199"
    )

    private val userGroup1 = UserGroup(
            name = "group1",
            states = mapOf(
                    "Invoice" to listOf(
                            condition1,
                            condition2
                    )
            )

    )

    private val userGroup2 = UserGroup(
            name = "group2",
            states = mapOf("Invoice" to listOf(condition1, condition2))

    )

    private val userAuthorization = UserAuthorization(
            username = "test",
            tenants = listOf("0105556176239", "0745550001199"),
            userGroups = listOf(userGroup1, userGroup2)
    )

    @Test
    fun `Test addCriteriaFromAuthorization`() {
        val authorizationByFields = spyk<AuthorizationByFields>(recordPrivateCalls = true)
        val criterias = SearchCriterias(InvoiceModel::class.java)
        val mapItemWithName = mapOf("InvoiceItem" to "", "" to "invoice")
        val clazz = InvoiceModel::class.java

        every { authorizationByFields["addCriteriaFromAuthorizationWithClass"](criterias, userAuthorization, mapItemWithName, clazz) } returns Unit

        val result = Try.on {
            authorizationByFields.addCriteriaFromAuthorization<InvoiceModel>(criterias, userAuthorization, mapItemWithName)
        }
        assertTrue(result.isSuccess)

        verify(exactly = 1) { authorizationByFields["addCriteriaFromAuthorizationWithClass"](criterias, userAuthorization, mapItemWithName, clazz) }

    }

    @Test
    fun `Test addCriteriaFromAuthorizationWithClass`() {
        mockkObject(AuthorizationByFields, AuthorizationUtils)
        val userAuthorization = UserAuthorization(
                username = "test",
                tenants = listOf("0105556176239", "0745550001199"),
                userGroups = listOf(userGroup1, userGroup2)
        )
        val mapItemWithName = mapOf("InvoiceItem" to "", "" to "invoice")
        val clazz = InvoiceModel::class.java
        val headerState = "Invoice"

        // Case1 userGroups is null or empty
        val criteriasCase1 = SearchCriterias(InvoiceModel::class.java)
        var result = Try.on {
            AuthorizationByFields.addCriteriaFromAuthorizationWithClass(criteriasCase1, userAuthorization.copy(userGroups = emptyList()), mapItemWithName, clazz)
        }
        assertTrue(result.isSuccess)
        assertTrue(criteriasCase1.getCriterias().isEmpty())
        verify(exactly = 0) { AuthorizationUtils.getStateHeader(any()) }
        verify(exactly = 0) { AuthorizationByFields.completeUserAuthorization(any(), any()) }
        verify(exactly = 0) { AuthorizationByFields["buildSearchCriteriasFromConditions"](any<List<Condition>>(), any<Boolean>(), any<Boolean>(), any<Class<Any>>()) }

        clearMocks(AuthorizationByFields, AuthorizationUtils, answers = false)

        // Case2 completeUserAuthorization return empty
        val criteriasCase2 = SearchCriterias(InvoiceModel::class.java)
        every { AuthorizationUtils.getStateHeader(clazz.simpleName) } returns headerState
        every { AuthorizationByFields.completeUserAuthorization(any(), mapItemWithName) } returns emptyList()
        result = Try.on {
            AuthorizationByFields.addCriteriaFromAuthorizationWithClass(criteriasCase2, userAuthorization, mapItemWithName, clazz)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { AuthorizationUtils.getStateHeader(clazz.simpleName) }
        // 2 because have two user group
        verify(exactly = 2) { AuthorizationByFields.completeUserAuthorization(any(), mapItemWithName) }
        verify(exactly = 0) { AuthorizationByFields["buildSearchCriteriasFromConditions"](any<List<Condition>>(), any<Boolean>(), any<Boolean>(), any<Class<Any>>()) }
        // don't have condition criteria
        assertTrue(criteriasCase2.getCriterias().isEmpty())

        clearMocks(AuthorizationByFields, AuthorizationUtils, answers = false)

        // Case completeUserAuthorization return condition
        val criteriasCase3 = SearchCriterias(InvoiceModel::class.java)
        val mockSearchGroupCriteria = SearchCriterias(InvoiceModel::class.java)
        mockSearchGroupCriteria.setAnd(false)

        userAuthorization.userGroups.forEachIndexed { index, group ->
            val conditionsForState = group.states[headerState]!!
            every {
                AuthorizationByFields.completeUserAuthorization(
                        conditionsForState = conditionsForState,
                        mapItemWithName = mapItemWithName
                )
            } returns conditionsForState

            assertEquals("group${index + 1}", group.name)


            every {
                AuthorizationByFields["buildSearchCriteriasFromConditions"](
                        conditionsForState,
                        false,
                        true,
                        clazz
                )
            } returns mockSearchGroupCriteria

        }

        result = Try.on {
            AuthorizationByFields.addCriteriaFromAuthorizationWithClass(criteriasCase3, userAuthorization, mapItemWithName, clazz)
        }
        assertTrue(result.isSuccess)
        val allGroupCriteria = criteriasCase3.getCriterias().firstOrNull() as SearchCriterias<*>
        assertNotNull(allGroupCriteria)
        assertEquals(allGroupCriteria.getRootObject(), clazz)
        // include criteria userGroup1 , userGroup2
        assertEquals(2, allGroupCriteria.getCriteriaSize())
        assertTrue(allGroupCriteria.getCriterias().all { it == mockSearchGroupCriteria })
        unmockkObject(AuthorizationByFields, AuthorizationUtils)
    }

    @Test
    fun `Test completeUserAuthorization`() {
        val authorizationByFields = spyk<AuthorizationByFields>()
        var condition = condition1.copy(field = "Invoice." + InvoiceModel::companyTaxNumber.name)
        val headerMappingTable = mapOf("InvoiceItem" to "invoiceItems")
        val itemMappingTable = mapOf("InvoiceItem" to "", "" to "invoice")

        // Case mapItemWithName is null
        var result = callMethod<AuthorizationByFields, List<Condition>>(authorizationByFields, "completeUserAuthorization",
                listOf(condition), null
        )!!
        assertEquals(1, result.size)
        assertEquals(listOf(condition), result)

        //========================== split field > 1 ==============================
        // Case query header and auth by field header
        result = callMethod<AuthorizationByFields, List<Condition>>(authorizationByFields, "completeUserAuthorization",
                listOf(condition), headerMappingTable
        )!!
        assertEquals(1, result.size)
        assertEquals(listOf(condition), result)

        // Case query item and auth by field header
        result = callMethod<AuthorizationByFields, List<Condition>>(authorizationByFields, "completeUserAuthorization",
                listOf(condition), itemMappingTable
        )!!
        assertEquals(1, result.size)
        assertEquals(listOf(condition), result)

        // Case query header and auth by field item
        condition = condition.copy(
                field = "InvoiceItem." + InvoiceItemModel::externalId.name
        )
        result = callMethod<AuthorizationByFields, List<Condition>>(authorizationByFields, "completeUserAuthorization",
                listOf(condition), headerMappingTable
        )!!
        assertEquals(1, result.size)
        assertEquals(listOf(condition.copy("invoiceItems.externalId")), result)

        // Case query item and auth by field item
        result = callMethod<AuthorizationByFields, List<Condition>>(authorizationByFields, "completeUserAuthorization",
                listOf(condition), itemMappingTable
        )!!
        assertEquals(1, result.size)
        assertEquals(listOf(condition.copy("externalId")), result)

        //========================== split field == 1 ==============================

        // Case query header and auth by field header
        condition = condition.copy(
                field = InvoiceModel::companyTaxNumber.name
        )
        result = callMethod<AuthorizationByFields, List<Condition>>(authorizationByFields, "completeUserAuthorization",
                listOf(condition), headerMappingTable
        )!!
        assertEquals(1, result.size)
        assertEquals(listOf(condition), result)

        // Case query item and auth by field header
        result = callMethod<AuthorizationByFields, List<Condition>>(authorizationByFields, "completeUserAuthorization",
                listOf(condition), itemMappingTable
        )!!
        assertEquals(1, result.size)
        assertEquals(listOf(condition.copy("invoice.companyTaxNumber")), result)

    }

    @Test
    fun `Test buildSearchCriteriasFromConditions`() {
        mockkObject(AuthorizationByFields)
        val conditions = listOf(condition1, condition2)
        val conditionSC = SearchCriteria()
        conditionSC.setField(InvoiceModel::companyTaxNumber.name)
        val mockConditionSC = listOf(conditionSC)


        every { AuthorizationByFields["createCriteria"](conditions, any<Boolean>()) } returns mockConditionSC

        // Case setAndMainCriteria = true and conditionsCriteria not empty
        var resultMainCriterias = callMethod<AuthorizationByFields, SearchCriterias<InvoiceModel>>(AuthorizationByFields, "buildSearchCriteriasFromConditions",
                conditions, true, false, InvoiceModel::class.java)!!

        assertTrue(resultMainCriterias.isAnd())
        assertEquals(1, resultMainCriterias.getCriteriaSize())
        assertEquals(conditionSC, resultMainCriterias.getCriterias().single())

        // Case setAndMainCriteria = false and conditionsCriteria is empty
        every { AuthorizationByFields["createCriteria"](conditions, any<Boolean>()) } returns emptyList<SearchCriteria>()
        resultMainCriterias = callMethod<AuthorizationByFields, SearchCriterias<InvoiceModel>>(AuthorizationByFields, "buildSearchCriteriasFromConditions",
                conditions, false, false, InvoiceModel::class.java)!!

        assertFalse(resultMainCriterias.isAnd())
        assertTrue(resultMainCriterias.getCriterias().isEmpty())

        unmockkObject(AuthorizationByFields)
    }

    @Test
    fun `Test List Condition createCriteria`() {
        mockkObject(AuthorizationByFields)
        val conditions = listOf(condition1, condition2)

        // mock method
        conditions.forEach { condition ->
            val mockSearchCriteria = SearchCriteria()
            mockSearchCriteria.setAnd(false)
            mockSearchCriteria.setField(condition.field)
            mockSearchCriteria.setOp(SearchCriteriaOperation.IN)
            mockSearchCriteria.setValue(condition.value.split(comma))

            every { AuthorizationByFields.validateField(condition.field) } returns Pair(condition.field, "parent")
            every { AuthorizationByFields["createSearchCriteria"](condition, condition.field, "parent", any<Boolean>()) } returns mockSearchCriteria
        }

        // call method
        val result = callMethod<AuthorizationByFields, List<SearchCriteria>>(AuthorizationByFields, "createCriteria", conditions, false)!!

        result.forEachIndexed { index, resultSearchCriteria ->
            assertFalse(resultSearchCriteria.isAnd())
            assertEquals(conditions[index].field, resultSearchCriteria.getField())
            assertEquals(SearchCriteriaOperation.IN, resultSearchCriteria.getOp())
            assertEquals(listOf("0105556176239", "0745550001199"), resultSearchCriteria.getValue())
        }

        unmockkObject(AuthorizationByFields)

    }

    @Test
    fun `Test createSearchCriteria`() {
        val authorizationByFields = spyk<AuthorizationByFields>()

        every { authorizationByFields["getSearchCriteriaOperationAndValue"](condition1) } returns Pair(SearchCriteriaOperation.IN, listOf("0105556176239", "0745550001199"))

        // Case parent = null ,setAnd = true
        var result = callMethod<AuthorizationByFields, SearchCriteria>(authorizationByFields, "createSearchCriteria",
                condition1, "companyTaxNumber", null, true)!!

        assertTrue(result.isAnd())
        assertNull(result.getPath())
        assertEquals("companyTaxNumber", result.getField())
        assertEquals(SearchCriteriaOperation.IN, result.getOp())
        assertEquals(listOf("0105556176239", "0745550001199"), result.getValue())

        // Case parent = null ,setAnd = false
        result = callMethod<AuthorizationByFields, SearchCriteria>(authorizationByFields, "createSearchCriteria",
                condition1, "companyTaxNumber", null, false)!!

        assertFalse(result.isAnd())
        assertNull(result.getPath())
        assertEquals("companyTaxNumber", result.getField())
        assertEquals(SearchCriteriaOperation.IN, result.getOp())
        assertEquals(listOf("0105556176239", "0745550001199"), result.getValue())

        // Case parent != null ,setAnd = false
        result = callMethod<AuthorizationByFields, SearchCriteria>(authorizationByFields, "createSearchCriteria",
                condition1, "companyTaxNumber", "invoice", false)!!

        assertFalse(result.isAnd())
        assertEquals("invoice", result.getPath())
        assertEquals("companyTaxNumber", result.getField())
        assertEquals(SearchCriteriaOperation.IN, result.getOp())
        assertEquals(listOf("0105556176239", "0745550001199"), result.getValue())

        // Case parent != null ,setAnd = true
        result = callMethod<AuthorizationByFields, SearchCriteria>(authorizationByFields, "createSearchCriteria",
                condition1, "companyTaxNumber", "invoice", true)!!

        assertTrue(result.isAnd())
        assertEquals("invoice", result.getPath())
        assertEquals("companyTaxNumber", result.getField())
        assertEquals(SearchCriteriaOperation.IN, result.getOp())
        assertEquals(listOf("0105556176239", "0745550001199"), result.getValue())
    }

    @Test
    fun `Test getSearchCriteriaOperationAndValue`() {
        val authorizationByFields = spyk<AuthorizationByFields>()

        // EqualityComparisonOperator.EQUAL
        var result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = EqualityComparisonOperator.EQUAL.name))
        assertEquals(SearchCriteriaOperation.EQUAL, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // EqualityComparisonOperator.NOT_EQUAL
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = EqualityComparisonOperator.NOT_EQUAL.name))
        assertEquals(SearchCriteriaOperation.NOT_EQUAL, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // NullOperator.IS_NULL
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = NullOperator.IS_NULL.name))
        assertEquals(SearchCriteriaOperation.ISNULL, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // NullOperator.NOT_NULL
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = NullOperator.NOT_NULL.name))
        assertEquals(SearchCriteriaOperation.NOTNULL, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // LikenessOperator.LIKE
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = LikenessOperator.LIKE.name))
        assertEquals(SearchCriteriaOperation.LIKE, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // LikenessOperator.NOT_LIKE
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = LikenessOperator.NOT_LIKE.name))
        assertEquals(SearchCriteriaOperation.NOT_LIKE, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // CollectionOperator.IN
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = CollectionOperator.IN.name))
        assertEquals(SearchCriteriaOperation.IN, result.first)
        assertEquals(listOf("0105556176239", "0745550001199"), result.second)

        // CollectionOperator.NOT_IN
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = CollectionOperator.NOT_IN.name))
        assertEquals(SearchCriteriaOperation.NOT_IN, result.first)
        assertEquals(listOf("0105556176239", "0745550001199"), result.second)

        // BinaryComparisonOperator.LESS_THAN
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = BinaryComparisonOperator.LESS_THAN.name))
        assertEquals(SearchCriteriaOperation.LESSTHAN, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // BinaryComparisonOperator.LESS_THAN_OR_EQUAL
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = BinaryComparisonOperator.LESS_THAN_OR_EQUAL.name))
        assertEquals(SearchCriteriaOperation.LESSTHAN_OR_EQUAL, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // BinaryComparisonOperator.GREATER_THAN
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = BinaryComparisonOperator.GREATER_THAN.name))
        assertEquals(SearchCriteriaOperation.GREATERTHAN, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // BinaryComparisonOperator.GREATER_THAN_OR_EQUAL
        result = authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = BinaryComparisonOperator.GREATER_THAN_OR_EQUAL.name))
        assertEquals(SearchCriteriaOperation.GREATERTHAN_OR_EQUAL, result.first)
        assertEquals("0105556176239,0745550001199", result.second)

        // Unsupported operator.
        val invalidResult = Try.on {
            authorizationByFields.getSearchCriteriaOperationAndValue(condition1.copy(operator = "Invalid"))
        }
        assertTrue(invalidResult.isFailure)
        assertTrue(invalidResult.toString().contains("Unsupported operator."))
    }


    @Test
    fun `Test validateField`() {
        val authorizationByFields = spyk<AuthorizationByFields>()

        // Case field contain DOT
        var result = authorizationByFields.validateField("invoice.companyTaxNumber")
        assertEquals("companyTaxNumber", result.first)
        assertEquals("invoice", result.second)

        result = authorizationByFields.validateField("companyTaxNumber")
        assertEquals("companyTaxNumber", result.first)
        assertNull(result.second)
    }


}
