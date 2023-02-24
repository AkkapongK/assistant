package th.co.dv.p2p.common.utilities

import net.corda.core.node.services.vault.NullOperator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.enums.LEVEL
import th.co.dv.p2p.common.enums.TransactionalAction
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.models.*
import th.co.dv.p2p.common.utilities.Conditions.using
import th.co.dv.p2p.common.utilities.RequestUtils.getUserAuthorizationOrDefault
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.corda.base.IllegalFlowException
import th.co.dv.p2p.corda.base.models.*

object AuthorizationUtils {

    const val ROLE_INTERFACE = "ROLE_INTERFACE"
    const val ROLE_BUYER_CUSTOM_INTERFACE = "ROLE_BUYER_CUSTOM_INTERFACE"
    const val ANONYMOUS_USER = "anonymousUser"

    val INTERFACE_AUTHORIZATION = UserAuthorization(
            username = "N/A",
            companiesCode = emptyList(),
            authorities = listOf(ROLE_INTERFACE),
            userGroups = emptyList())

    val logger: Logger = LoggerFactory.getLogger(AuthorizationUtils::class.java)
    val className: String = AuthorizationUtils::class.java.simpleName

    /**
     * Method for get state name from model class
     *
     * @model: Model class
     */
    fun mapModelToStateName(model: Class<*>): String {

        return when (model) {
            CreditNoteModel::class.java -> "CreditNote"
            CreditNoteItemModel::class.java -> "CreditNoteItem"
            DebitNoteModel::class.java -> "DebitNote"
            DebitNoteItemModel::class.java -> "DebitNoteItem"
            GoodsReceivedModel::class.java -> "GoodsReceived"
            GoodsReceivedItemModel::class.java -> "GoodsReceivedItem"
            InvoiceModel::class.java -> "Invoice"
            InvoiceItemModel::class.java -> "InvoiceItem"
            PaymentModel::class.java -> "Payment"
            PurchaseOrderModel::class.java -> "PurchaseOrder"
            PurchaseItemModel::class.java -> "PurchaseItem"
            RequestModel::class.java -> "Request"
            RequestItemModel::class.java -> "RequestItem"
            FinanceableDocumentModel::class.java -> "FinanceableDocument"
            LoanModel::class.java -> "Loan"
            TaxDocumentModel::class.java -> "TaxDocument"
            else -> throw IllegalArgumentException("Unsupported model.")
        }
    }

    /**
     * Method for get state name from model class
     *
     * @model: Model class
     */
    fun getStateHeader(entityName: String): String {

        return when (entityName) {
            "PurchaseItem", "PurchaseOrderElastic" -> "PurchaseOrder"
            "GoodsReceivedItem", "GoodsReceivedElastic" -> "GoodsReceived"
            "InvoiceItem", "InvoiceElastic" -> "Invoice"
            "DebitNoteItem" -> "DebitNote"
            "CreditNoteItem" -> "CreditNote"
            "RequestItem" -> "Request"
            "PaymentElastic" -> "Payment"
            "TaxDocument", "TaxDocumentElastic" -> "TaxDocument"
            else -> entityName
        }
    }

    /**
     * Method for build [UserAuthorization] object
     */
    @Suppress("UNCHECKED_CAST")
    fun buildUserAuthorization(map: Map<String, Any>, authorities: List<String>): UserAuthorization {

        val username = map["name"] as String
        val userAuthentication = map["userAuthentication"] as Map<*, *>?
        val userAuthenticationDetails = userAuthentication?.let { userAuthentication["details"] as Map<*, *>? }

        // Company codes from this token (to be removed and use from authorization instead)
        val companies = userAuthenticationDetails?.getOrDefault("companies", emptyList<String>()) as List<String>?

        val authorization = userAuthenticationDetails?.let { userAuthenticationDetails["authorizations"] as List<Map<String, Any>>? }
        val groups = authorization?.map { group ->
            UserGroup(
                    name = group["name"]!!.toString(),
                    states = (group["states"] as Map<String, Any>).mapValues { (_, value) ->
                        (value as List<Map<String, String>>).map {
                            Condition(
                                field = it["field"]!!,
                                operator = it["operator"] ?: it["operation"]!!,
                                value = it["value"]!!
                            )
                        }
                    })
        }
        val tenants = userAuthenticationDetails?.getOrDefault("tenants", emptyList<String>()) as List<String>?

        val mapInActiveList = userAuthenticationDetails?.getOrDefault("inActiveList", null) as List<Map<String, String>>?
        val inActiveList = mapInActiveList?.map {
            BuyerVendorPKModel(
                    buyerTaxId = it["buyerTaxId"]!!,
                    buyerCode =  it["buyerCode"]!!,
                    vendorTaxId = it["vendorTaxId"]!!,
                    vendorCode = it["vendorCode"]!!
            )
        }

        val userAuthorization = UserAuthorization(
            username = username,
            companiesCode = companies ?: emptyList(),
            authorities = authorities,
            userGroups = groups ?: emptyList(),
            tenants = tenants ?: emptyList(),
            inActiveList = inActiveList
        )

        logger.debug("$className.buildUserAuthorization UserAuthorization: $userAuthorization")

        return userAuthorization

    }

    /**
     * Method for check the user have authorization on the state or not
     */
    inline fun <reified T : Any> hasAuthorization(userAuthorization: UserAuthorization): Boolean {
        return hasAuthorization(userAuthorization, T::class.java)
    }

    /**
     * Method to check the user have internal privileges for access data or not
     */
    fun hasInternalPrivileges(userAuthorization: UserAuthorization): Boolean {
        return (userAuthorization.tenants.size == 1 && userAuthorization.tenants.single() == internalAppPrivileges)
    }

    /**
     * Method for check the user have authorization on tenant or not
     */
    fun hasTenantAuthorization(userAuthorization: UserAuthorization): Boolean {
        return when (userAuthorization.userType == TransactionalAction.BUYER.name) {
            true -> userAuthorization.sponsors.isNotEmpty()
            false -> userAuthorization.tenants.isNotEmpty()
        }
    }

    /**
     * Method for check the user have authorization on the state or not
     */
    fun <T : Any> hasAuthorization(userAuthorization: UserAuthorization, clazz: Class<T>): Boolean {
        // For INTERFACE, we can get all data without any condition
        if ((userAuthorization.authorities.contains(ROLE_INTERFACE)) &&
                (userAuthorization.userGroups.isNullOrEmpty())) return true

        return when (clazz) {
            InvoiceModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER, LEVEL.ITEM), clazz)
            PurchaseOrderModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER, LEVEL.ITEM), clazz)
            GoodsReceivedModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER, LEVEL.ITEM), clazz)
            CreditNoteModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER, LEVEL.ITEM), clazz)
            DebitNoteModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER, LEVEL.ITEM), clazz)
            RequestModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER), clazz)
            PaymentModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER), clazz)
            FinanceableDocumentModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER), clazz)
            LoanModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER), clazz)
            TaxDocumentModel::class.java -> haveAuthorization(userAuthorization.userGroups, listOf(LEVEL.HEADER), clazz)
            else -> false
        }
    }

    /**
     * Method for checking the invoice have both authority in header and item
     * we checking by levels
     * If we put header and item level in argument we expected the condition have both of header and item
     * (checking by ".", after we split by "." if size of result is 2 it's mean have item)
     *
     * If we not put any LEVEL it's mean not checking
     *
     * Not: we consider each group.
     */
    inline fun <reified T : Any> haveAuthorization(userGroups: List<UserGroup>, levels: List<LEVEL>): Boolean {
        return haveAuthorization(userGroups, levels, T::class.java)
    }

    fun <T : Any> haveAuthorization(userGroups: List<UserGroup>, levels: List<LEVEL>, clazz: Class<T>): Boolean {

        val stateName = mapModelToStateName(clazz)
        userGroups.forEach { userGroup ->
            val conditions = userGroup.states[stateName]
            val haveHeader = when (LEVEL.HEADER in levels) {
                true -> conditions?.any { it.field.splitAndTrim(DOT, true).size == 1 } ?: false
                false -> true
            }
            val haveItem = when (LEVEL.ITEM in levels) {
                true -> conditions?.any { it.field.splitAndTrim(DOT, true).size == 2 } ?: false
                false -> true
            }

            if (haveHeader && haveItem) return true
        }

        return false
    }

    /**
     * To check this user need to add filter by companyCode during query data or not
     * */
    fun needFilterAuthorization(userAuthorization: UserAuthorization): Boolean {
        logger.info("$className.needFilterAuthorization Username: ${userAuthorization.username}, Authorities: ${userAuthorization.authorities}, Companies: ${userAuthorization.companiesCode}")
        return (!userAuthorization.authorities.contains(ROLE_INTERFACE) && !userAuthorization.authorities.contains(ROLE_BUYER_CUSTOM_INTERFACE))
    }

    /**
     * Validate user authorization with input state
     * T is entity that we want to check
     * P is Parent entity of T
     * M is model of the entity
     *
     * If user doesn't required for authorize, we skip
     */
    inline fun <reified T : Any, reified P, reified M : Any> validateWithAuthorization(
            state: T,
            userAuthorization: UserAuthorization,
            isModel: Boolean = false) {

        if (needFilterAuthorization(userAuthorization)) {
            requireAll {
                "User doesn't have any authorization for state." using hasAuthorization<M>(userAuthorization)
                val criterias = validateState<T, P>(state, userAuthorization, isModel)
                val emptyCriterias = criterias.filter { it.isEmpty() }
                if (emptyCriterias.isEmpty()) {
                    val field = criterias.first { it.isNotEmpty() }.first()
                    val value = state.getFieldValue<Any?>(field)
                    "No authorization on [$field] : [$value]" using (emptyCriterias.size == userAuthorization.userGroups.size)
                }
            }
        }

    }

    /**
     * Method for validate authorization of user
     * if user have authorization on the state we return true
     *
     * Note: T is Contract state of the state
     *       P is Contract state of the parent
     *
     * @param state: State that we considering
     * @param userAuthorization: User authorization
     */
    inline fun <reified T, reified P> validateState(state: T, userAuthorization: UserAuthorization, isModel: Boolean = false): MutableList<MutableList<String>> {

        val (mapping, mappingItem) = if (isModel) {
            mapModelToStateName(P::class.java) to mapModelToStateName(T::class.java)
        } else {
            P::class.java.simpleName to T::class.java.simpleName
        }

        val criteriaList = mutableListOf<MutableList<String>>()
        userAuthorization.userGroups.forEach { group ->
            logger.info("$className.validateState Group: $group")

            val criteria = mutableListOf<String>()
            // Filter only criteria for header or item
            val conditionsForState = group.states.getOrDefault(mapping, emptyList()).mapNotNull { condition ->
                // XOR will return true if the right and left side of operation not be the same
                // ie. true xor false ==> true
                //     false xor true ==> true
                // If we consider the child state the first condition will be false so we need to consider only condition that start with child state (the second must be true)
                // In other hand if we consider the parent state the first condition will be true so we consider only condition that not start with any prefix (the second must be false)
                if ((T::class.java == P::class.java) xor (condition.field.contains(DOT))) {
                    //Remove the prefix state
                    condition.copy(field = condition.field.replace("$mappingItem.", ""))
                } else {
                    null
                }
            }

            logger.info("$className.validateState  Condition for state: $conditionsForState")

            // Loop each field in state and create or set query criteria
            conditionsForState.forEach { condition ->
                logger.info("$className.validateState  Condition: $condition")
                if (!validateCondition(condition, state)) criteria.add(condition.field)
            }
            criteriaList.add(criteria)
        }
        logger.info("$className.validateState criteria list: $criteriaList")

        return criteriaList
    }

    /**
     * Method for validate condition that match with value for specific field
     */
    fun <T> validateCondition(condition: Condition, state: T): Boolean {
        val actualValue = state!!.getFieldValue<Any?>(condition.field)
        logger.info("$className.validateCondition Actual Value: $actualValue, ${condition.operator}, ${condition.value}")

        return when (condition.operator) {

            SearchCriteriaOperation.EQUAL.name -> actualValue == condition.value
            SearchCriteriaOperation.NOT_EQUAL.name -> actualValue != condition.value

            NullOperator.IS_NULL.name -> actualValue == null
            NullOperator.NOT_NULL.name -> actualValue != null

            SearchCriteriaOperation.LIKE.name -> "$actualValue".validateLikeValue(condition.value)
            SearchCriteriaOperation.NOT_LIKE.name -> !"$actualValue".validateLikeValue(condition.value)

            SearchCriteriaOperation.IN.name -> condition.value.splitAndTrim(separator = ",", trim = true).contains("$actualValue")
            SearchCriteriaOperation.NOT_IN.name -> condition.value.splitAndTrim(separator = ",", trim = true).contains("$actualValue").not()

            SearchCriteriaOperation.LESSTHAN.name -> actualValue?.let { condition.value < "$it" } ?: false
            SearchCriteriaOperation.LESSTHAN_OR_EQUAL.name -> actualValue?.let { condition.value <= "$it" } ?: false
            SearchCriteriaOperation.GREATERTHAN.name -> actualValue?.let { condition.value > "$it" } ?: false
            SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name -> actualValue?.let { condition.value >= "$it" } ?: false

            else -> throw IllegalArgumentException("Unsupported operator.")
        }
    }

    /**
     * Method for validate like condition with regular expression
     */
    fun String?.validateLikeValue(conditionValue: String): Boolean {
        if (this == null) return false

        val regexConditionValue = Regex(regularStartWith + conditionValue.replace(percent, "(.*)") + regularEndWith)

        return this.matches(regexConditionValue)
    }

    /**
     * Get auth per field conditions from every groups of user authorization
     * @param userAuthorization user authorization
     * @param state target document state
     */
    fun extractUserAuthorizeCondition(userAuthorization: UserAuthorization, state: String): List<Condition> {
        logger.info("$className.extractUserAuthorizeCondition userAuthorization: $userAuthorization, state: $state")
        return userAuthorization.userGroups.mapNotNull { group -> group.states[state] }.flatten()
    }

    /**
     * Get values of target field, and target operations of auth per field
     * @param conditions authorization by field condition
     * @param fieldName target field name
     * @param targetOperations target operations
     * @param failWhenNotFound allow this method to throw error if condition not found
     */
    fun extractConditionField(conditions: List<Condition>,
                              fieldName: String,
                              targetOperations: List<String> = emptyList(),
                              failWhenNotFound: Boolean = false): List<String> {
        logger.info("$className.extractConditionField for $conditions, field: $fieldName, operations: $targetOperations, failWhenNotFound: $failWhenNotFound")
        val operationToSplit = listOf(SearchCriteriaOperation.IN.name, SearchCriteriaOperation.NOT_IN.name)
        val result = mutableListOf<String>()

        conditions.forEach {
            if (it.field == fieldName && (targetOperations.isEmpty() || it.operator in targetOperations)) {
                when (it.operator in operationToSplit) {
                    true -> result.addAll(it.value.splitAndTrim(separator = comma, trim = true))
                    false -> result.add(it.value)
                }
            }
        }

        if (failWhenNotFound) {
            "Could not find condition for $fieldName." using result.isNotEmpty()
        }

        return result.distinct()
    }

    /**
     * Method for check the user have authorization on the state or not
     * check userGroups have this state and checking by levels
     *
     * If user is interface, has role ROLE_INTERFACE and userGroups isNullOrEmpty return true
     *
     * @param userAuthorization: UserAuthorization
     * @param levels: list of LEVEL constant
     * @return Boolean: has authorization or not
     */
    fun hasAuthorizationByLevels(userAuthorization: UserAuthorization, clazz: Class<*>, levels: List<LEVEL>): Boolean {
        if ((userAuthorization.authorities.contains(ROLE_INTERFACE)) &&
                (userAuthorization.userGroups.isNullOrEmpty())) return true

        return haveAuthorization(userAuthorization.userGroups, levels, clazz)
    }

    /**
     * Method for validate auth for model
     */
    fun <T> validateModelAuthorization(models: List<T>) {
        val userAuthorization = getUserAuthorizationOrDefault(INTERFACE_AUTHORIZATION)
        if (models.isEmpty()) return
        models.forEach { model ->
            when (model) {
                is InvoiceModel -> {
                    validateWithAuthorization<InvoiceModel, InvoiceModel, InvoiceModel>(model, userAuthorization, isModel = true)
                    model.invoiceItems.forEach {
                        validateWithAuthorization<InvoiceItemModel, InvoiceModel, InvoiceModel>(it, userAuthorization, isModel = true)
                    }
                }
                is InvoiceItemModel -> validateWithAuthorization<InvoiceItemModel, InvoiceModel, InvoiceModel>(model, userAuthorization, isModel = true)
                is PurchaseOrderModel -> {
                    validateWithAuthorization<PurchaseOrderModel, PurchaseOrderModel, PurchaseOrderModel>(model, userAuthorization, isModel = true)
                    model.purchaseItems.forEach {
                        validateWithAuthorization<PurchaseItemModel, PurchaseOrderModel, PurchaseOrderModel>(it, userAuthorization, isModel = true)
                    }
                }
                is PurchaseItemModel -> validateWithAuthorization<PurchaseItemModel, PurchaseOrderModel, PurchaseOrderModel>(model, userAuthorization, isModel = true)
                is GoodsReceivedModel -> {
                    validateWithAuthorization<GoodsReceivedModel, GoodsReceivedModel, GoodsReceivedModel>(model, userAuthorization, isModel = true)
                    model.goodsReceivedItems.forEach {
                        validateWithAuthorization<GoodsReceivedItemModel, GoodsReceivedModel, GoodsReceivedModel>(it, userAuthorization, isModel = true)
                    }
                }
                is GoodsReceivedItemModel -> validateWithAuthorization<GoodsReceivedItemModel, GoodsReceivedModel, GoodsReceivedModel>(model, userAuthorization, isModel = true)
                is CreditNoteModel -> {
                    validateWithAuthorization<CreditNoteModel, CreditNoteModel, CreditNoteModel>(model, userAuthorization, isModel = true)
                    model.creditNoteItems.forEach {
                        validateWithAuthorization<CreditNoteItemModel, CreditNoteModel, CreditNoteModel>(it, userAuthorization, isModel = true)
                    }
                }
                is CreditNoteItemModel -> validateWithAuthorization<CreditNoteItemModel, CreditNoteModel, CreditNoteModel>(model, userAuthorization, isModel = true)
                is DebitNoteModel -> {
                    validateWithAuthorization<DebitNoteModel, DebitNoteModel, DebitNoteModel>(model, userAuthorization, isModel = true)
                    model.debitNoteItems.forEach {
                        validateWithAuthorization<DebitNoteItemModel, DebitNoteModel, DebitNoteModel>(it, userAuthorization, isModel = true)
                    }
                }
                is DebitNoteItemModel -> validateWithAuthorization<DebitNoteItemModel, DebitNoteModel, DebitNoteModel>(model, userAuthorization, isModel = true)
                is RequestModel -> {
                    validateWithAuthorization<RequestModel, RequestModel, RequestModel>(model, userAuthorization, isModel = true)
                    model.requestItems.forEach {
                        validateWithAuthorization<RequestItemModel, RequestModel, RequestModel>(it, userAuthorization, isModel = true)
                    }
                }
                is RequestItemModel -> validateWithAuthorization<RequestItemModel, RequestModel, RequestModel>(model, userAuthorization, isModel = true)
                is PaymentModel -> validateWithAuthorization<PaymentModel, PaymentModel, PaymentModel>(model, userAuthorization, isModel = true)
                is TaxDocumentModel -> validateWithAuthorization<TaxDocumentModel, TaxDocumentModel, TaxDocumentModel>(model, userAuthorization, isModel = true)
                is BuyerVendorModel, is ContractModel, is LoanProfileModel, is RepaymentRequestModel, is RepaymentHistoryModel, is LoanModel, is FinanceableDocumentModel -> return@forEach
                else -> throw IllegalFlowException(DOCUMENT_NOT_SUPPORT)
            }

        }

    }
}