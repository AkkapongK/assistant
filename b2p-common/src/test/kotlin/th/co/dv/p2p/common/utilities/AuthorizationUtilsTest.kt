package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import net.corda.core.node.services.vault.EqualityComparisonOperator
import net.corda.core.utilities.Try
import org.junit.Test
import th.co.dv.p2p.common.constants.DOCUMENT_NOT_SUPPORT
import th.co.dv.p2p.common.constants.internalAppPrivileges
import th.co.dv.p2p.common.enums.LEVEL
import th.co.dv.p2p.common.models.*
import th.co.dv.p2p.common.utilities.AuthorizationUtils.INTERFACE_AUTHORIZATION
import th.co.dv.p2p.common.utilities.AuthorizationUtils.validateModelAuthorization
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.corda.base.models.*
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthorizationUtilsTest {

    private val conditionsModel = listOf(
            Condition(
                    field = "companyTaxNumber",
                    operator = "EQUAL",
                    value = "0100"
            ),
            Condition(
                    field = "externalId",
                    operator = "EQUAL",
                    value = "INV001"
            ),
            Condition(
                    field = "InvoiceItemModel.linearId",
                    operator = "NOT_NULL",
                    value = ""
            ),
            Condition(
                    field = "InvoiceItemModel.site",
                    operator = "EQUAL",
                    value = "B02"
            )
    )

    private val conditions = listOf(
            Condition(
                    field = "companyTaxNumber",
                    operator = "EQUAL",
                    value = "0100"
            ),
            Condition(
                    field = "externalId",
                    operator = "EQUAL",
                    value = "INV001"
            ),
            Condition(
                    field = "InvoiceItem.linearId",
                    operator = "NOT_NULL",
                    value = ""
            ),
            Condition(
                    field = "InvoiceItem.site",
                    operator = "EQUAL",
                    value = "B02"
            )
    )


    private val conditionRequest = listOf(
            Condition(
                    field = "linearId",
                    operator = "EQUAL",
                    value = "001"
            ),
            Condition(
                    field = "externalId",
                    operator = "EQUAL",
                    value = "REQ001"
            ),
            Condition(
                    field = "RequestItem.site",
                    operator = "EQUAL",
                    value = "B02"
            )

    )

    private val conditionRequestModel = listOf(
            Condition(
                    field = "companyTaxNumber",
                    operator = "EQUAL",
                    value = "0100"
            ),
            Condition(
                    field = "externalId",
                    operator = "EQUAL",
                    value = "REQ001"
            ),
            Condition(
                    field = "RequestItemModel.site",
                    operator = "EQUAL",
                    value = "B02"
            )
    )

    private val defaultAuthorization = UserAuthorization(
            username = "test",
            companiesCode = listOf("0100", "5260", "1234"),
            authorities = listOf("ROLE_USER"),
            userGroups = listOf(UserGroup(
                    name = "ALL",
                    states = mapOf(
                            Pair("InvoiceModel", conditionsModel),
                            Pair("Invoice", conditions),
                            Pair("Request", conditionRequest),
                            Pair("RequestModel", conditionRequestModel)
                    )
            ))
    )

    @Test
    fun `Test buildUserAuthorization`() {
        // Case result companiesCode and userGroups and tenants not empty
        var map = mapOf(
                "name" to "admin",
                "userAuthentication" to mapOf("details" to mapOf(
                        "companies" to listOf("comTax-1"),
                        "authorizations" to listOf(mapOf(
                            "name" to "groupA",
                            "states" to mapOf(
                                "Invoice" to listOf(
                                    mapOf(
                                        "field" to InvoiceModel::linearId.name,
                                        "operator" to SearchCriteriaOperation.EQUAL.name,
                                        "operation" to SearchCriteriaOperation.EQUAL.name,
                                        "value" to "linear-xxxx"
                                    )
                                )
                            )
                        )
                        ),
                    "tenants" to listOf("comTax-1", "vendorTax-1"),
                    "inActiveList" to listOf(
                            mapOf(
                                    "buyerTaxId" to "companyTaxId",
                                    "buyerCode" to "buyerCode",
                                    "vendorTaxId" to "vendorTaxId",
                                    "vendorCode" to "vendorCode"
                            )
                    )
                ))
        )
        val authorities = listOf("authorities")

        var expectedResult = UserAuthorization(
            username = "admin",
            companiesCode = listOf("comTax-1"),
            authorities = authorities,
            userGroups = listOf(
                UserGroup(
                    name = "groupA",
                    states = mapOf(
                        "Invoice" to listOf(
                            Condition(
                                field = InvoiceModel::linearId.name,
                                operator = SearchCriteriaOperation.EQUAL.name,
                                value = "linear-xxxx"
                            )
                        )
                    )
                )
            ),
            tenants = listOf("comTax-1", "vendorTax-1"),
            inActiveList = listOf(
                BuyerVendorPKModel(
                    buyerTaxId = "companyTaxId",
                    buyerCode = "buyerCode",
                    vendorTaxId = "vendorTaxId",
                    vendorCode = "vendorCode"
                )
            )
        )
        var result = AuthorizationUtils.buildUserAuthorization(map, authorities)
        assertEquals(expectedResult, result)

        // Case result companiesCode and userGroups and tenants empty
        map = mapOf("name" to "admin")
        expectedResult = UserAuthorization(
            username = "admin",
            companiesCode = emptyList(),
            authorities = authorities,
            userGroups = emptyList(),
            tenants = emptyList(),
            inActiveList = null
        )
        result = AuthorizationUtils.buildUserAuthorization(map, authorities)
        assertEquals(expectedResult, result)

    }

    @Test
    fun testValidateWithAuthorization() {
        val authorizationUtils = spyk<AuthorizationUtils>()

        val items = InvoiceItemModel(site = "B02", linearId = "item001")
        val invoiceModel = InvoiceModel(
                linearId = "header1",
                externalId = "INV001",
                companyTaxNumber = "0100",
                invoiceItems = listOf(items)
        )

        val requestItems = RequestItemModel(site = "B02")

        val requestModel = RequestModel(
                companyTaxNumber = "0100",
                externalId = "REQ001",
                requestItems = listOf(requestItems)
        )

        // Case many Invoice groups
        val conditions2 = listOf(
                Condition(
                        field = "companyTaxNumber",
                        operator = "EQUAL",
                        value = "0200"
                ),
                Condition(
                        field = "InvoiceItemModel.linearId",
                        operator = "NOT_NULL",
                        value = ""
                ),
                Condition(
                        field = "InvoiceItemModel.site",
                        operator = "EQUAL",
                        value = "B02"
                )
        )
        val group2 = UserGroup(name = "G2", states = mapOf(Pair("InvoiceModel", conditions2)))
        var userAuthorizationInvoice = defaultAuthorization.copy(userGroups = defaultAuthorization.userGroups.plus(group2))

        every { authorizationUtils.needFilterAuthorization(any()) } returns true

        // Validate header field success
        var result = Try.on {
            authorizationUtils.validateWithAuthorization<InvoiceModel, InvoiceModel, InvoiceModel>(
                    invoiceModel,
                    userAuthorizationInvoice
            )
        }
        assert(result.isSuccess)

        // Validate header field multi group success
        result = Try.on {
            authorizationUtils.validateWithAuthorization<InvoiceModel, InvoiceModel, InvoiceModel>(
                    invoiceModel.copy(companyTaxNumber = "0200"),
                    userAuthorizationInvoice
            )
        }
        assert(result.isSuccess)

        // Validate item field success
        result = Try.on {
            authorizationUtils.validateWithAuthorization<InvoiceItemModel, InvoiceModel, InvoiceModel>(
                    items,
                    userAuthorizationInvoice
            )
        }
        assert(result.isSuccess)

        // Validate header field
        result = Try.on {
            authorizationUtils.validateWithAuthorization<InvoiceModel, InvoiceModel, InvoiceModel>(
                    invoiceModel.copy(companyTaxNumber = "test"),
                    userAuthorizationInvoice
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains("No authorization on [companyTaxNumber] : [test]"))

        // Validate item field
        result = Try.on {
            authorizationUtils.validateWithAuthorization<InvoiceItemModel, InvoiceModel, InvoiceModel>(
                    items.copy(site = "001"),
                    userAuthorizationInvoice
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains("No authorization on [site] : [001]"))

        // Validate Request header field success
        result = Try.on {
            authorizationUtils.validateWithAuthorization<RequestModel, RequestModel, RequestModel>(
                    requestModel,
                    defaultAuthorization
            )
        }
        assert(result.isSuccess)

        result = Try.on {
            authorizationUtils.validateWithAuthorization<RequestModel, RequestModel, RequestModel>(
                    requestModel.copy(linearId = "001", companyTaxNumber = ""),
                    defaultAuthorization,
                    true
            )
        }
        assert(result.isSuccess)

        // Validate Request item field success
        result = Try.on {
            authorizationUtils.validateWithAuthorization<RequestItemModel, RequestModel, RequestModel>(
                    requestItems,
                    defaultAuthorization
            )
        }
        assert(result.isSuccess)

        result = Try.on {
            authorizationUtils.validateWithAuthorization<RequestItemModel, RequestModel, RequestModel>(
                    requestItems,
                    defaultAuthorization,
                    true
            )
        }
        assert(result.isSuccess)

        //Validate Request header failed
        result = Try.on {
            authorizationUtils.validateWithAuthorization<RequestModel, RequestModel, RequestModel>(
                    requestModel.copy(externalId = "Test"),
                    defaultAuthorization
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains("No authorization on [externalId] : [Test]"))

        //Validate Request item failed
        result = Try.on {
            authorizationUtils.validateWithAuthorization<RequestItemModel, RequestModel, RequestModel>(
                    requestItems.copy(site = "lol"),
                    defaultAuthorization
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains("No authorization on [site] : [lol]"))


        // User have has no authorization for this state
        userAuthorizationInvoice = UserAuthorization(
                username = "test",
                companiesCode = listOf("0100", "5260", "1234"),
                authorities = listOf("ROLE_USER"),
                userGroups = listOf(UserGroup(
                        name = "ALL",
                        states = mapOf(
                                Pair("PurchaseOrder", listOf(
                                        Condition(
                                                field = "companyTaxNumber",
                                                operator = "EQUAL",
                                                value = "0100"
                                        )
                                ))
                        )
                ))
        )

        result = Try.on {
            authorizationUtils.validateWithAuthorization<InvoiceItemModel, InvoiceModel, InvoiceModel>(
                    items,
                    userAuthorizationInvoice
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains("User doesn't have any authorization for state."))

        // user don't need authorization
        every { authorizationUtils.needFilterAuthorization(any()) } returns false

        result = Try.on {
            authorizationUtils.validateWithAuthorization<InvoiceItemModel, InvoiceModel, InvoiceModel>(
                    items,
                    userAuthorizationInvoice
            )
        }
        assert(result.isSuccess)
    }

    @Test
    fun testValidateCondition() {

        val authorizationUtils = spyk<AuthorizationUtils>()

        var condition = Condition(
                field = "companyTaxNumber",
                operator = "EQUAL",
                value = "0100"
        )

        val invoiceModel = InvoiceModel(
                linearId = "header1",
                externalId = "INV001",
                companyTaxNumber = "0100",
                invoiceTotal = BigDecimal.TEN
        )

        // Operation EQUAL true
        var result = Try.on { authorizationUtils.validateCondition(condition, invoiceModel) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation EQUAL false
        result = Try.on { authorizationUtils.validateCondition(condition, invoiceModel.copy(companyTaxNumber = "test")) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        // Operation NOT_EQUAL true
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "NOT_EQUAL"), invoiceModel.copy(companyTaxNumber = "test")) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation NOT_EQUAL false
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "NOT_EQUAL"), invoiceModel) }

        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        // Operation NOT_NULL true
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "NOT_NULL"), invoiceModel) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation NOT_NULL false
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "NOT_NULL"), invoiceModel.copy(companyTaxNumber = null)) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        // Operation IS_NULL true
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "IS_NULL"), invoiceModel.copy(companyTaxNumber = null)) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation IS_NULL false
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "IS_NULL"), invoiceModel) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        condition = Condition(
                field = "externalId",
                operator = "LIKE",
                value = "INV%")

        // Operation LIKE true
        result = Try.on { authorizationUtils.validateCondition(condition, invoiceModel) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation LIKE false
        result = Try.on { authorizationUtils.validateCondition(condition, invoiceModel.copy(externalId = "test")) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        // Operation NOT_LIKE true
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "NOT_LIKE"), invoiceModel.copy(externalId = "test")) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation NOT_LIKE false
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "NOT_LIKE"), invoiceModel) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        condition = Condition(
                field = "externalId",
                operator = "IN",
                value = "INV001")

        // Operation IN true
        result = Try.on { authorizationUtils.validateCondition(condition, invoiceModel) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation IN false
        result = Try.on { authorizationUtils.validateCondition(condition, invoiceModel.copy(externalId = "test")) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        // Operation NOT_IN true
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "NOT_IN"), invoiceModel.copy(externalId = "test")) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation NOT_IN false
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "NOT_IN"), invoiceModel) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        condition = Condition(
                field = "invoiceTotal",
                operator = "LESSTHAN",
                value = BigDecimal.ONE.toString())

        // Operation LESSTHAN true
        result = Try.on { authorizationUtils.validateCondition(condition, invoiceModel) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation LESSTHAN false
        result = Try.on { authorizationUtils.validateCondition(condition, invoiceModel.copy(invoiceTotal = BigDecimal.ZERO)) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        // Operation LESSTHAN_OR_EQUAL true
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "LESSTHAN_OR_EQUAL", value = BigDecimal.TEN.toString()), invoiceModel) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation LESSTHAN_OR_EQUAL false
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "LESSTHAN_OR_EQUAL", value = BigDecimal.TEN.toString()), invoiceModel.copy(invoiceTotal = BigDecimal.ONE)) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        // Operation GREATERTHAN true
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "GREATERTHAN", value = BigDecimal(20).toString()), invoiceModel) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation GREATERTHAN false
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "GREATERTHAN", value = BigDecimal.TEN.toString()), invoiceModel) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        // Operation GREATERTHAN_OR_EQUAL true
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "GREATERTHAN_OR_EQUAL", value = BigDecimal.TEN.toString()), invoiceModel) }
        assert(result.isSuccess)
        assertTrue(result.getOrThrow())

        // Operation GREATERTHAN_OR_EQUAL false
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "GREATERTHAN_OR_EQUAL", value = BigDecimal.ONE.toString()), invoiceModel) }
        assert(result.isSuccess)
        assertFalse(result.getOrThrow())

        // Case Unsupported operator
        result = Try.on { authorizationUtils.validateCondition(condition.copy(operator = "MOCK", value = BigDecimal.ONE.toString()), invoiceModel) }
        assert(result.isFailure)
        assertTrue(result.toString().contains("Unsupported operator"))


    }

    @Test
    fun testNeedFilterAuthorization() {

        val authorizationUtils = spyk<AuthorizationUtils>()

        var userAuthorization = UserAuthorization(
                username = "test",
                companiesCode = listOf("0100", "5260", "1234"),
                authorities = listOf("ROLE_USER")
        )

        // User need authorization
        var result = authorizationUtils.needFilterAuthorization(userAuthorization)
        assertTrue(result)

        // User don't need authorization
        userAuthorization = UserAuthorization(
                username = "test",
                companiesCode = listOf(),
                authorities = listOf("ROLE_INTERFACE")
        )

        result = authorizationUtils.needFilterAuthorization(userAuthorization)
        assertFalse(result)

        // User don't need authorization
        userAuthorization = UserAuthorization(
                username = "test",
                companiesCode = listOf(),
                authorities = listOf("ROLE_BUYER_CUSTOM_INTERFACE")
        )

        result = authorizationUtils.needFilterAuthorization(userAuthorization)
        assertFalse(result)

    }

    @Test
    fun testExtractUserAuthorizeCondition() {
        val authorizationUtils = spyk<AuthorizationUtils>()

        // Case 1 user group
        var result = authorizationUtils.extractUserAuthorizeCondition(defaultAuthorization, "Invoice")
        assertEquals(conditions, result)

        // Case many Invoice groups
        val conditions2 = listOf(
                Condition(
                        field = "companyTaxNumber",
                        operator = "EQUAL",
                        value = "0200"
                ))
        val group2 = UserGroup(
                name = "G2",
                states = mapOf(Pair("Invoice", conditions2))
        )
        var userAuthorization = defaultAuthorization.copy(userGroups = defaultAuthorization.userGroups.plus(group2))
        result = authorizationUtils.extractUserAuthorizeCondition(userAuthorization, "Invoice")
        assertEquals(conditions.plus(conditions2), result)

        // Case many groups, but Invoice in only one group
        val group3 = UserGroup(
                name = "G3",
                states = mapOf(Pair("CreditNote", conditions2))
        )
        userAuthorization = defaultAuthorization.copy(userGroups = defaultAuthorization.userGroups.plus(group3))
        result = authorizationUtils.extractUserAuthorizeCondition(userAuthorization, "Invoice")
        assertEquals(conditions, result)

        // Case no Invoice in user group
        userAuthorization = defaultAuthorization.copy(userGroups = listOf(group3))
        result = authorizationUtils.extractUserAuthorizeCondition(userAuthorization, "Invoice")
        assertEquals(emptyList(), result)
    }

    @Test
    fun testExtractConditionField() {
        val authorizationUtils = spyk<AuthorizationUtils>()
        val testConditions = conditions.plus(listOf(
                // Add duplication for test distinction
                Condition(field = "companyTaxNumber", operator = "EQUAL", value = "0100"),
                // Add comma for operator EQUAL, this must not be split
                Condition(field = "companyTaxNumber", operator = "EQUAL", value = "2,000"),
                // Add space to trim
                Condition(field = "companyTaxNumber", operator = "IN", value = "0300, 0400"),
                // Add some duplications for test distinction
                Condition(field = "companyTaxNumber", operator = "IN", value = "0300,0400, 0500"),
                // Add unused operator for case empty operation
                Condition(field = "companyTaxNumber", operator = "NOT_IN", value = "0600,0700")))

        // Normal case EQUAL
        var result = authorizationUtils.extractConditionField(testConditions, "companyTaxNumber", listOf(EqualityComparisonOperator.EQUAL.name))
        assertEquals(listOf("0100", "2,000"), result)

        // Normal case IN
        result = authorizationUtils.extractConditionField(testConditions, "companyTaxNumber", listOf(SearchCriteriaOperation.IN.name))
        assertEquals(listOf("0300", "0400", "0500"), result)

        // Case field not found
        result = authorizationUtils.extractConditionField(testConditions, "noExistingField", listOf(EqualityComparisonOperator.EQUAL.name))
        assertEquals(emptyList(), result)

        // Case operation not found
        result = authorizationUtils.extractConditionField(testConditions, "companyTaxNumber", listOf(EqualityComparisonOperator.NOT_EQUAL.name))
        assertEquals(emptyList(), result)

        // Case empty operation
        result = authorizationUtils.extractConditionField(testConditions, "companyTaxNumber", emptyList())
        assertEquals(listOf("0100", "2,000", "0300", "0400", "0500", "0600", "0700"), result)

        // case error
        val failedResult = Try.on { authorizationUtils.extractConditionField(testConditions, "noExistingField", emptyList(), true) }
        assertTrue(failedResult.isFailure)
        assertTrue(failedResult.toString().contains("Could not find condition for noExistingField."))
    }

    @Test
    fun `Test hasInternalPrivileges`() {

        // Case tenant have more than
        var userAuthorization = UserAuthorization(username = "User", tenants = listOf("001", "002"))
        var result = AuthorizationUtils.hasInternalPrivileges(userAuthorization)
        assertFalse(result)

        // Case tenant is empty
        userAuthorization = UserAuthorization(username = "User", tenants = listOf())
        result = AuthorizationUtils.hasInternalPrivileges(userAuthorization)
        assertFalse(result)

        // Case tenant have only 1 but value is not ANY
        userAuthorization = UserAuthorization(username = "User", tenants = listOf("001"))
        result = AuthorizationUtils.hasInternalPrivileges(userAuthorization)
        assertFalse(result)

        // Case tenant have only 1 with value ANY
        userAuthorization = UserAuthorization(username = "User", tenants = listOf(internalAppPrivileges))
        result = AuthorizationUtils.hasInternalPrivileges(userAuthorization)
        assertTrue(result)

    }

    @Test
    fun `Test hasTenantAuthorization`() {
        val userAuthorization = UserAuthorization(username = "User", tenants = listOf("001"))

        // Case has authorization on tenant
        var result = AuthorizationUtils.hasTenantAuthorization(userAuthorization)
        assertTrue(result)

        // Case hasn't authorization on tenant
        result = AuthorizationUtils.hasTenantAuthorization(userAuthorization.copy(tenants = emptyList()))
        assertFalse(result)
    }

    @Test
    fun testHasAuthorizationByLevels() {
        val authorizationUtils = spyk<AuthorizationUtils>()
        var userAuthorization = UserAuthorization(username = "User", authorities = listOf(AuthorizationUtils.ROLE_INTERFACE))

        // Case has authorization by has role ROLE_INTERFACE and userGroups isNullOrEmpty
        var result = authorizationUtils.hasAuthorizationByLevels(
                userAuthorization,
                InvoiceModel::class.java,
                listOf(LEVEL.HEADER))
        assertTrue(result)

        // Case has authorization by have authorization on the state
        userAuthorization = UserAuthorization(username = "User")
        every { authorizationUtils.haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER), InvoiceModel::class.java) } returns true

        result = authorizationUtils.hasAuthorizationByLevels(
                userAuthorization,
                InvoiceModel::class.java,
                listOf(LEVEL.HEADER))
        assertTrue(result)

        // Case has no authorization
        every { authorizationUtils.haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER), InvoiceModel::class.java) } returns false

        result = authorizationUtils.hasAuthorizationByLevels(
                userAuthorization,
                InvoiceModel::class.java,
                listOf(LEVEL.HEADER))
        assertFalse(result)
    }

    @Test
    fun validateModelAuthorizationTest() {
        mockkObject(RequestUtils)
        every { RequestUtils.getUserAuthorizationOrDefault(INTERFACE_AUTHORIZATION) } returns INTERFACE_AUTHORIZATION

        var result = Try.on { validateModelAuthorization(listOf(InvoiceModel(invoiceItems = listOf(InvoiceItemModel())))) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(InvoiceItemModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(PurchaseOrderModel(purchaseItems = listOf(PurchaseItemModel())))) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(PurchaseItemModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(GoodsReceivedModel(goodsReceivedItems = listOf(GoodsReceivedItemModel())))) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(GoodsReceivedItemModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(CreditNoteModel(creditNoteItems = listOf(CreditNoteItemModel())))) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(CreditNoteItemModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(DebitNoteModel(debitNoteItems = listOf(DebitNoteItemModel())))) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(DebitNoteItemModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(RequestModel(requestItems = listOf(RequestItemModel())))) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(RequestItemModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(PaymentModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(TaxDocumentModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(FinanceableDocumentModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(LoanModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(BuyerVendorModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(ContractModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(LoanProfileModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(RepaymentRequestModel())) }
        assertTrue(result.isSuccess)
        result = Try.on { validateModelAuthorization(listOf(RepaymentHistoryModel())) }
        assertTrue(result.isSuccess)

        result = Try.on { validateModelAuthorization(listOf(CompanyModel())) }
        assertTrue(result.isFailure)
        assert(result.toString().contains(DOCUMENT_NOT_SUPPORT))

        unmockkObject(RequestUtils)
    }

}