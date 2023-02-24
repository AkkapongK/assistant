package th.co.dv.p2p.common.utilities

import io.mockk.*
import net.corda.core.node.services.vault.CollectionOperator
import net.corda.core.utilities.Try
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.annotations.NoTenant
import th.co.dv.p2p.common.annotations.Tenant
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.enums.*
import th.co.dv.p2p.common.models.Condition
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.models.UserGroup
import th.co.dv.p2p.common.utilities.search.AuthorizationByFields
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.common.utilities.search.SearchCriterias
import th.co.dv.p2p.corda.base.models.InvoiceModel
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DataTenantTest {


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
            sponsors = listOf("DV"),
            userGroups = listOf(userGroup1, userGroup2)
    )

    @Tenant(buyer = "companyTaxNumber", seller = "vendorTaxNumber")
    private data class DataClassWithTenantAnnotation(
            val companyTaxNumber: String? = null,
            val vendorTaxNumber: String? = null
    )

    @Tenant(buyer = "", seller = "")
    @NoTenant
    private data class DataClassWithTenantNoTenantAnnotation(
            val comTax: String? = null,
            val vendorTax: String? = null
    )

    @NoTenant
    private data class DataClassWithNoTenantAnnotation(
            val companyTaxNumber: String? = null,
            val vendorTaxNumber: String? = null
    )

    @Test
    fun `Test addDataTenantsCriteriaFromAuthorizationWithClass`() {
        val dataTenant = spyk<DataTenant>(recordPrivateCalls = true)
        mockkObject(AuthorizationUtils, AuthorizationByFields)

        // mock input
        val criterias = SearchCriterias(InvoiceModel::class.java)
        val dataTenantFields = listOf(InvoiceModel::companyTaxNumber.name, InvoiceModel::vendorTaxNumber.name)
        val mapItemWithName = mapOf("InvoiceItem" to "", "" to "invoice")
        val dataTenantCondition = listOf(condition1, condition2)
        val clazz = InvoiceModel::class.java
        val mockScWithInnerConditionCriteria = SearchCriterias(InvoiceModel::class.java)
        val tenantAnnotation = mockk<Tenant>()
        val notTenantAnnotation = mockk<NoTenant>()

        // Case has internal privileges
        every { dataTenant.getDataTenantAnnotation(clazz) } returns notTenantAnnotation
        every { AuthorizationUtils.hasInternalPrivileges(userAuthorization) } returns true
        var result = Try.on {
            dataTenant.addDataTenantsCriteriaFromAuthorizationWithClass(criterias, userAuthorization, mapItemWithName, clazz)
        }
        assertTrue(result.isSuccess)
        assertEquals(0, criterias.getCriterias().size)
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_BUYER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_SELLER) }
        verify(exactly = 0) { dataTenant.buildDataTenantCondition(any(), any(), any()) }
        verify(exactly = 0) { AuthorizationByFields.buildSearchCriteriasFromConditions(any(), any(), any(), any<Class<Any>>()) }

        // Case annotation is NoTenant
        every { AuthorizationUtils.hasInternalPrivileges(userAuthorization) } returns false
        result = Try.on {
            dataTenant.addDataTenantsCriteriaFromAuthorizationWithClass(criterias, userAuthorization, mapItemWithName, clazz)
        }
        assertTrue(result.isSuccess)
        assertEquals(0, criterias.getCriterias().size)
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_BUYER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_SELLER) }
        verify(exactly = 0) { dataTenant.buildDataTenantCondition(any(), any(), any()) }
        verify(exactly = 0) { AuthorizationByFields.buildSearchCriteriasFromConditions(any(), any(), any(), any<Class<Any>>()) }

        // Case Not found data tenant fields for apply to get transaction.
        every { dataTenant.getDataTenantAnnotation(clazz) } returns tenantAnnotation
        every { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) } returns emptyList()
        result = Try.on {
            dataTenant.addDataTenantsCriteriaFromAuthorizationWithClass(criterias, userAuthorization, mapItemWithName, clazz)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(notFondDataTenantFieldsForApplyToGetTransaction))
        assertEquals(0, criterias.getCriterias().size)
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_BUYER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_SELLER) }
        verify(exactly = 0) { dataTenant.buildDataTenantCondition(any(), any(), any()) }
        verify(exactly = 0) { AuthorizationByFields.buildSearchCriteriasFromConditions(any(), any(), any(), any<Class<Any>>()) }
        clearMocks(dataTenant, answers = false)

        // Case success
        every { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) } returns dataTenantFields
        every { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants, mapItemWithName) } returns dataTenantCondition
        every {
            AuthorizationByFields.buildSearchCriteriasFromConditions(
                    dataTenantCondition,
                setAndOuterCriteria = true,
                setAndInnerConditionCriteria = false,
                clazz = clazz
            )
        } returns mockScWithInnerConditionCriteria

        result = Try.on {
            dataTenant.addDataTenantsCriteriaFromAuthorizationWithClass(criterias, userAuthorization, mapItemWithName, clazz)
        }
        assertTrue(result.isSuccess)
        assertEquals(1, criterias.getCriterias().size)
        assertEquals(mockScWithInnerConditionCriteria, criterias.getCriterias().single())
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_BUYER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_SELLER) }
        verify(exactly = 1) { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants, mapItemWithName) }
        verify(exactly = 1) { AuthorizationByFields.buildSearchCriteriasFromConditions(dataTenantCondition, setAndOuterCriteria = true, setAndInnerConditionCriteria = false, clazz = clazz) }
        clearMocks(dataTenant, AuthorizationByFields, answers = false)
        criterias.getCriterias().clear()

        // Case success node = BUYER
        every { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER) } returns dataTenantFields

        result = Try.on {
            dataTenant.addDataTenantsCriteriaFromAuthorizationWithClass(criterias, userAuthorization, mapItemWithName, clazz, Node.BUYER.name)
        }
        assertTrue(result.isSuccess)
        assertEquals(1, criterias.getCriterias().size)
        assertEquals(mockScWithInnerConditionCriteria, criterias.getCriterias().single())
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_BUYER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_SELLER) }
        verify(exactly = 1) { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants, mapItemWithName) }
        verify(exactly = 1) { AuthorizationByFields.buildSearchCriteriasFromConditions(dataTenantCondition, true, false, clazz) }
        clearMocks(dataTenant, AuthorizationByFields, answers = false)
        criterias.getCriterias().clear()

        // Case success node = SELLER
        every { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_SELLER) } returns dataTenantFields

        result = Try.on {
            dataTenant.addDataTenantsCriteriaFromAuthorizationWithClass(criterias, userAuthorization, mapItemWithName, clazz, Node.SELLER.name)
        }
        assertTrue(result.isSuccess)
        assertEquals(1, criterias.getCriterias().size)
        assertEquals(mockScWithInnerConditionCriteria, criterias.getCriterias().single())
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_BUYER) }
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(any(), TransactionalAction_SELLER) }
        verify(exactly = 1) { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants, mapItemWithName) }
        verify(exactly = 1) { AuthorizationByFields.buildSearchCriteriasFromConditions(dataTenantCondition, true, false, clazz) }
        clearMocks(dataTenant, answers = false)

        unmockkObject(AuthorizationUtils, AuthorizationByFields)

    }


    @Test
    fun `Test integration getDataTenantFieldsFromAnnotation`() {
        val dataTenant = spyk<DataTenant>()
        // input
        val tenantAnnotation = mockk<Tenant>()

        // Case buyer field is not blank , seller field is not blank
        every { tenantAnnotation.buyer } returns DataClassWithTenantAnnotation::companyTaxNumber.name
        every { tenantAnnotation.seller } returns DataClassWithTenantAnnotation::vendorTaxNumber.name
        var result = dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER)
        assertEquals(listOf(DataClassWithTenantAnnotation::companyTaxNumber.name, DataClassWithTenantAnnotation::vendorTaxNumber.name), result)

        // Case buyer field is not blank but seller field is blank
        every { tenantAnnotation.seller } returns ""
        result = dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER)
        assertEquals(listOf(DataClassWithTenantAnnotation::companyTaxNumber.name), result)

        // Case buyer field is blank but seller field is not blank
        every { tenantAnnotation.buyer } returns ""
        every { tenantAnnotation.seller } returns DataClassWithTenantAnnotation::vendorTaxNumber.name
        result = dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER)
        assertEquals(listOf(DataClassWithTenantAnnotation::vendorTaxNumber.name), result)

        // Case buyer field is blank and seller field is blank
        every { tenantAnnotation.seller } returns ""
        result = dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Test Tenant getDataTenantFieldByTransactionalAction`() {
        val dataTenant = spyk<DataTenant>()
        val tenantAnnotation = mockk<Tenant>()

        // Case TransactionalAction is BUYER
        every { tenantAnnotation.buyer } returns DataClassWithTenantAnnotation::companyTaxNumber.name
        var result = callMethod<DataTenant, String>(dataTenant, "getDataTenantFieldByTransactionalAction", tenantAnnotation, TransactionalAction.BUYER)
        assertEquals(DataClassWithTenantAnnotation::companyTaxNumber.name, result)

        // Case TransactionalAction is BUYER but field in annotation is Blank
        every { tenantAnnotation.buyer } returns ""
        result = callMethod<DataTenant, String>(dataTenant, "getDataTenantFieldByTransactionalAction", tenantAnnotation, TransactionalAction.BUYER)
        assertNull(result)

        // Case TransactionalAction is Seller
        every { tenantAnnotation.seller } returns DataClassWithTenantAnnotation::vendorTaxNumber.name
        result = callMethod<DataTenant, String>(dataTenant, "getDataTenantFieldByTransactionalAction", tenantAnnotation, TransactionalAction.SELLER)
        assertEquals(DataClassWithTenantAnnotation::vendorTaxNumber.name, result)

        // Case TransactionalAction is SELLER but field in annotation is Blank
        every { tenantAnnotation.seller } returns ""
        result = callMethod<DataTenant, String>(dataTenant, "getDataTenantFieldByTransactionalAction", tenantAnnotation, TransactionalAction.BUYER)
        assertNull(result)

    }

    @Test
    fun `Test getDataTenantAnnotation By Class`() {
        // Case state contain Tenant and NoTenant annotation.
        var result = Try.on {
            DataTenant.getDataTenantAnnotation(DataClassWithTenantNoTenantAnnotation::class.java)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("DataClassWithTenantNoTenantAnnotation state should have one data tenant annotation [Tenant , NoTenant]."))

        // Case no data tenant annotation
        result = Try.on {
            DataTenant.getDataTenantAnnotation(InvoiceModel::class.java)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("InvoiceModel state should have one data tenant annotation [Tenant , NoTenant]."))

        // Case state has only Tenant annotation
        result = Try.on {
            DataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java)
        }
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow() is Tenant)

        // Case state has only NoTenant annotation
        result = Try.on {
            DataTenant.getDataTenantAnnotation(DataClassWithNoTenantAnnotation::class.java)
        }
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow() is NoTenant)
    }

    @Test
    fun `Test integration getDataTenantAnnotation By Generic`() {
        val dataClassWithTenantAnnotation = DataClassWithTenantAnnotation()
        val dataClassWithNoTenantAnnotation = DataClassWithNoTenantAnnotation()
        val dataClassWithTenantNoTenantAnnotation = DataClassWithTenantNoTenantAnnotation()
        val invoiceModel = InvoiceModel()

        // Case state contain Tenant and NoTenant annotation.
        var result = Try.on {
            DataTenant.getDataTenantAnnotation(dataClassWithTenantNoTenantAnnotation)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("DataClassWithTenantNoTenantAnnotation state should have one data tenant annotation [Tenant , NoTenant]."))

        // Case no data tenant annotation
        result = Try.on {
            DataTenant.getDataTenantAnnotation(invoiceModel)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("InvoiceModel state should have one data tenant annotation [Tenant , NoTenant]."))

        // Case state has only Tenant annotation
        result = Try.on {
            DataTenant.getDataTenantAnnotation(dataClassWithTenantAnnotation)
        }
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow() is Tenant)

        // Case state has only NoTenant annotation
        result = Try.on {
            DataTenant.getDataTenantAnnotation(dataClassWithNoTenantAnnotation)
        }
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow() is NoTenant)

    }


    @Test
    fun `Test buildDataTenantCondition`() {
        val dataTenant = spyk<DataTenant>()

        val dataTenantFields = listOf(InvoiceModel::companyTaxNumber.name, InvoiceModel::vendorTaxNumber.name)
        val tenants = listOf("0105556176239", "0745550001199")
        val mapItemWithName = mapOf("invoiceItem" to "", "" to "invoice")
        val dataTenantCondition = listOf(condition1, condition2)

        mockkObject(AuthorizationByFields)

        // Case tenants is empty
        var result = Try.on {
            dataTenant.buildDataTenantCondition(dataTenantFields, emptyList(), mapItemWithName)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(noAuthorizationOnTenants))

        // Case success
        every { AuthorizationByFields.completeUserAuthorization(dataTenantCondition, mapItemWithName) } returns dataTenantCondition.map { it.copy(field = "invoice." + it.field) }
        result = Try.on {
            dataTenant.buildDataTenantCondition(dataTenantFields, tenants, mapItemWithName)
        }
        assertTrue(result.isSuccess)
        result.getOrThrow().forEachIndexed { index, condition ->
            assertEquals("invoice." + dataTenantCondition[index].field, condition.field)
            assertEquals(CollectionOperator.IN.name, condition.operator)
            assertEquals("0105556176239,0745550001199", condition.value)
        }

        unmockkObject(AuthorizationByFields)
    }

    @Test
    fun `Test validateTenantState`() {
        val dataTenant = spyk<DataTenant>()
        val tenantAnnotation = mockk<Tenant>()
        val noTenantAnnotation = mockk<NoTenant>()
        val dataTenantFields = listOf(DataClassWithTenantAnnotation::companyTaxNumber.name, DataClassWithTenantAnnotation::vendorTaxNumber.name)
        val dataTenantFieldsBuyer = listOf(DataClassWithTenantAnnotation::companyTaxNumber.name)
        val dataClassWithTenantAnnotation = DataClassWithTenantAnnotation(companyTaxNumber = "A", vendorTaxNumber = "B")
        val condition1 = Condition(
                field = DataClassWithTenantAnnotation::companyTaxNumber.name,
                operator = SearchCriteriaOperation.IN.name,
                value = "A,B"
        )
        val condition2 = Condition(
                field = DataClassWithTenantAnnotation::vendorTaxNumber.name,
                operator = SearchCriteriaOperation.IN.name,
                value = "A,B"
        )
        val dataTenantConditions = listOf(condition1, condition2)

        mockkObject(AuthorizationUtils)

        // Case hasn't tenant authorization
        every { AuthorizationUtils.hasTenantAuthorization(userAuthorization) } returns false
        var result = Try.on {
            dataTenant.validateTenantState(dataClassWithTenantAnnotation, userAuthorization, TransactionalAction_BUYER)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(noAuthorizationOnTenants))
        verify(exactly = 1) { AuthorizationUtils.hasTenantAuthorization(userAuthorization) }
        verify(exactly = 0) { AuthorizationUtils.hasInternalPrivileges(any()) }
        verify(exactly = 0) { dataTenant.getDataTenantAnnotation(any<Class<Any>>()) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), any()) }
        verify(exactly = 0) { dataTenant.buildDataTenantCondition(any(), any(), any()) }
        clearMocks(dataTenant, AuthorizationUtils, answers = false)

        // Case has internal privileges
        every { AuthorizationUtils.hasTenantAuthorization(userAuthorization) } returns true
        every { AuthorizationUtils.hasInternalPrivileges(userAuthorization) } returns true
        result = Try.on {
            dataTenant.validateTenantState(dataClassWithTenantAnnotation, userAuthorization, TransactionalAction_BUYER)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { AuthorizationUtils.hasTenantAuthorization(userAuthorization) }
        verify(exactly = 1) { AuthorizationUtils.hasInternalPrivileges(userAuthorization) }
        verify(exactly = 0) { dataTenant.getDataTenantAnnotation(any<Class<Any>>()) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), any()) }
        verify(exactly = 0) { dataTenant.buildDataTenantCondition(any(), any(), any()) }
        clearMocks(dataTenant, AuthorizationUtils, answers = false)

        // Case data tenant annotation isn't Tenant
        every { AuthorizationUtils.hasInternalPrivileges(userAuthorization) } returns false
        every { dataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java) } returns noTenantAnnotation
        result = Try.on {
            dataTenant.validateTenantState(dataClassWithTenantAnnotation, userAuthorization, TransactionalAction_BUYER)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(noAuthorizationOnTenants))
        verify(exactly = 1) { AuthorizationUtils.hasTenantAuthorization(userAuthorization) }
        verify(exactly = 1) { AuthorizationUtils.hasInternalPrivileges(userAuthorization) }
        verify(exactly = 1) { dataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java) }
        verify(exactly = 0) { dataTenant.getDataTenantFieldsFromAnnotation(any(), any()) }
        verify(exactly = 0) { dataTenant.buildDataTenantCondition(any(), any(), any()) }
        clearMocks(dataTenant, AuthorizationUtils, answers = false)

        // Case Not found data tenant fields for apply to save transaction.
        every { dataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java) } returns tenantAnnotation
        every { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, listOf(TransactionalAction.BUYER)) } returns emptyList()
        result = Try.on {
            dataTenant.validateTenantState(dataClassWithTenantAnnotation, userAuthorization, TransactionalAction_BUYER)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(notFoundDataTenantFieldsForApplyToSaveTransaction))
        verify(exactly = 1) { AuthorizationUtils.hasTenantAuthorization(userAuthorization) }
        verify(exactly = 1) { AuthorizationUtils.hasInternalPrivileges(userAuthorization) }
        verify(exactly = 1) { dataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java) }
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER) }
        verify(exactly = 0) { dataTenant.buildDataTenantCondition(any(), any(), any()) }
        clearMocks(dataTenant, AuthorizationUtils, answers = false)

        every { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) } returns dataTenantFields

        // Case companyTaxNumber and vendorTaxNumber in tenant
        every { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants) } returns dataTenantConditions
        result = Try.on {
            dataTenant.validateTenantState(dataClassWithTenantAnnotation, userAuthorization, TransactionalAction_BUYER_OR_SELLER)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { AuthorizationUtils.hasTenantAuthorization(userAuthorization) }
        verify(exactly = 1) { AuthorizationUtils.hasInternalPrivileges(userAuthorization) }
        verify(exactly = 1) { dataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java) }
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 1) { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants) }
        clearMocks(dataTenant, AuthorizationUtils, answers = false)

        // Case companyTaxNumber in tenants vendorTaxNumber not in tenant
        every { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants) } returns dataTenantConditions
        result = Try.on {
            dataTenant.validateTenantState(dataClassWithTenantAnnotation.copy(vendorTaxNumber = "C"), userAuthorization, TransactionalAction_BUYER_OR_SELLER)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { AuthorizationUtils.hasInternalPrivileges(userAuthorization) }
        verify(exactly = 1) { dataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java) }
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 1) { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants) }
        clearMocks(dataTenant, AuthorizationUtils, answers = false)

        // Case companyTaxNumber not in tenants vendorTaxNumber in tenant
        every { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants) } returns dataTenantConditions
        result = Try.on {
            dataTenant.validateTenantState(dataClassWithTenantAnnotation.copy(companyTaxNumber = "C"), userAuthorization, TransactionalAction_BUYER_OR_SELLER)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { AuthorizationUtils.hasTenantAuthorization(userAuthorization) }
        verify(exactly = 1) { AuthorizationUtils.hasInternalPrivileges(userAuthorization) }
        verify(exactly = 1) { dataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java) }
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 1) { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants) }
        clearMocks(dataTenant, AuthorizationUtils, answers = false)


        // Case companyTaxNumber and vendorTaxNumber not in tenant
        every { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants) } returns dataTenantConditions
        result = Try.on {
            dataTenant.validateTenantState(dataClassWithTenantAnnotation.copy(companyTaxNumber = "C", vendorTaxNumber = "C"), userAuthorization, TransactionalAction_BUYER_OR_SELLER)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(noAuthorizationOnTenants))
        verify(exactly = 1) { AuthorizationUtils.hasTenantAuthorization(userAuthorization) }
        verify(exactly = 1) { AuthorizationUtils.hasInternalPrivileges(userAuthorization) }
        verify(exactly = 1) { dataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java) }
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER_OR_SELLER) }
        verify(exactly = 1) { dataTenant.buildDataTenantCondition(dataTenantFields, userAuthorization.tenants) }
        clearMocks(dataTenant, AuthorizationUtils, answers = false)

        // Case user type is buyer
        every { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, listOf(TransactionalAction.BUYER)) } returns dataTenantFieldsBuyer
        every { dataTenant.buildDataTenantCondition(dataTenantFieldsBuyer, userAuthorization.sponsors) } returns dataTenantConditions
        val userAuthorizationBuyer = userAuthorization.copy(userType = TransactionalAction.BUYER.name)
        result = Try.on {
            dataTenant.validateTenantState(dataClassWithTenantAnnotation, userAuthorizationBuyer, TransactionalAction_BUYER)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { AuthorizationUtils.hasTenantAuthorization(userAuthorizationBuyer) }
        verify(exactly = 1) { AuthorizationUtils.hasInternalPrivileges(userAuthorizationBuyer) }
        verify(exactly = 1) { dataTenant.getDataTenantAnnotation(DataClassWithTenantAnnotation::class.java) }
        verify(exactly = 1) { dataTenant.getDataTenantFieldsFromAnnotation(tenantAnnotation, TransactionalAction_BUYER) }
        verify(exactly = 1) { dataTenant.buildDataTenantCondition(dataTenantFieldsBuyer, userAuthorizationBuyer.sponsors) }

        unmockkObject(AuthorizationUtils)
    }

    @Test
    fun `Test validateNoTenantState`() {
        val dataTenant = spyk<DataTenant>()
        val tenantAnnotation = mockk<Tenant>()
        val noTenantAnnotation = mockk<NoTenant>()
        val dataClassWithTenantAnnotation = DataClassWithTenantAnnotation()
        val dataClassWithTenantNoTenantAnnotation = DataClassWithTenantNoTenantAnnotation()

        // Unsupported tenant state.
        every { dataTenant.getDataTenantAnnotation(dataClassWithTenantAnnotation) } returns tenantAnnotation
        var result = Try.on {
            dataTenant.validateNoTenantState(dataClassWithTenantAnnotation)

        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(unsupportedTenantState))

        // No authorization on tenant.
        every { dataTenant.getDataTenantAnnotation(dataClassWithTenantNoTenantAnnotation) } returns noTenantAnnotation
        result = Try.on {
            dataTenant.validateNoTenantState(dataClassWithTenantNoTenantAnnotation)
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun testHasTenantAuthorization() {

        val userAuthBuyer = UserAuthorization(username = "user1", sponsors = listOf("DV"), userType = TransactionalAction.BUYER.name)
        val userAuthSeller = UserAuthorization(username = "user2", tenants = listOf("001"), userType = TransactionalAction.SELLER.name)
        val userAuthNone = UserAuthorization(username = "user3", tenants = listOf("002"))

        // Case user type is buyer and sponsor is not empty
        var result = AuthorizationUtils.hasTenantAuthorization(userAuthBuyer)
        assertTrue(result)

        // Case user type is buyer and sponsor is empty
        result = AuthorizationUtils.hasTenantAuthorization(userAuthBuyer.copy(sponsors = emptyList()))
        assertFalse(result)

        // Case user type is seller and tenant is not empty
        result = AuthorizationUtils.hasTenantAuthorization(userAuthSeller)
        assertTrue(result)

        // Case user type is seller and tenant is empty
        result = AuthorizationUtils.hasTenantAuthorization(userAuthSeller.copy(tenants = emptyList()))
        assertFalse(result)

        // Case user type is none and tenant is not empty
        result = AuthorizationUtils.hasTenantAuthorization(userAuthNone)
        assertTrue(result)

        // Case user type is none and tenant is empty
        result = AuthorizationUtils.hasTenantAuthorization(userAuthNone.copy(tenants = emptyList()))
        assertFalse(result)

    }
}