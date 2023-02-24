package th.co.dv.p2p.common.utilities

import net.corda.core.node.services.vault.CollectionOperator
import th.co.dv.p2p.common.annotations.NoTenant
import th.co.dv.p2p.common.annotations.Tenant
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.enums.*
import th.co.dv.p2p.common.exceptions.AuthorizationException
import th.co.dv.p2p.common.models.Condition
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.AuthorizationUtils.hasInternalPrivileges
import th.co.dv.p2p.common.utilities.AuthorizationUtils.hasTenantAuthorization
import th.co.dv.p2p.common.utilities.Conditions.usingWith
import th.co.dv.p2p.common.utilities.search.AuthorizationByFields
import th.co.dv.p2p.common.utilities.search.AuthorizationByFields.buildSearchCriteriasFromConditions
import th.co.dv.p2p.common.utilities.search.SearchCriterias

object DataTenant {

    /**
     * This function use to add data tenant criteria to query criterias
     *
     * @param criterias : query criterias
     * @param userAuthorization : user authorization
     * @param mapItemWithName : Map of Model item name with field item in database
     * @param clazz : state
     */
    fun <T> addDataTenantsCriteriaFromAuthorizationWithClass(
            criterias: SearchCriterias<*>,
            userAuthorization: UserAuthorization,
            mapItemWithName: Map<String, String>? = null,
            clazz: Class<T>,
            node: String? = null
    ) {
        val dataTenantAnnotation = getDataTenantAnnotation(clazz)

        if (hasInternalPrivileges(userAuthorization) || dataTenantAnnotation is NoTenant) return

        val transactionalAction = when(node) {
            Node.BUYER.name -> TransactionalAction_BUYER
            Node.SELLER.name -> TransactionalAction_SELLER
            else -> TransactionalAction_BUYER_OR_SELLER
        }
        val dataTenantFields = getDataTenantFieldsFromAnnotation(dataTenantAnnotation as Tenant, transactionalAction).distinct()

        // not check size of data tenant fields must equal size of transaction actions (like validate when saving)
        // for support case state has tenant only one field (buyer or seller)
        // e.g. @Tenant(buyer="companyTaxId , seller="") or @Tenant(buyer="" , seller="vendorTaxId")
        // check not empty because state that use tenant annotation should define tenant field
        notFondDataTenantFieldsForApplyToGetTransaction.usingWith<IllegalStateException>(dataTenantFields.isNotEmpty())

        val dataTenantCondition = buildDataTenantCondition(dataTenantFields, userAuthorization.tenants, mapItemWithName)

        // create outer criteria with inner condition criteria
        val allDataTenantCriteria = buildSearchCriteriasFromConditions(
                conditions = dataTenantCondition,
                setAndOuterCriteria = true,
                setAndInnerConditionCriteria = false,
                clazz = clazz
        )

        // add to main criteria
        criterias.getCriterias().add(allDataTenantCriteria)

    }

    /**
     * This function use to get data tenant field from class of state
     *
     *  @param tenantAnnotation : tenant annotation
     *  @param transactionalActions: list of transactional action
     */
    fun getDataTenantFieldsFromAnnotation(tenantAnnotation: Tenant, transactionalActions: List<TransactionalAction>): List<String> {
        return transactionalActions.mapNotNull { transactionalAction ->
            tenantAnnotation.getDataTenantFieldByTransactionalAction(transactionalAction)
        }
    }

    /**
     * This function use to get data tenant field by transactional action
     *
     * @param transactionalAction : transactional action
     */
    private fun Tenant.getDataTenantFieldByTransactionalAction(transactionalAction: TransactionalAction): String? {
        val dataTenantField = when (transactionalAction) {
            TransactionalAction.BUYER -> this.buyer
            TransactionalAction.SELLER -> this.seller
        }
        return when (dataTenantField.isNotBlank()) {
            true -> dataTenantField
            false -> null
        }
    }

    /**
     * This function use to get data tenant annotation for get transaction
     *
     * @param clazz : class of state
     */
    fun <T> getDataTenantAnnotation(clazz: Class<T>): Annotation {
        val dataTenantAnnotations = clazz.annotations.filter { it is Tenant || it is NoTenant }
        // must have data tenant annotation and cannot apply annotation Tenant along with NoTenant
        stateShouldHaveOnlyOneDataTenantAnnotation.format(clazz.simpleName).usingWith<IllegalStateException>(dataTenantAnnotations.size == 1)

        return dataTenantAnnotations.single()
    }

    /**
     * This function use to get data tenant annotation for save transaction
     *
     * @param state : State that we considering
     */
    fun <T> getDataTenantAnnotation(state: T): Annotation {
        // JPA parameter save method is generic type , it must cast to Any class to get Annotation.
        val classOfState = (state as Any)::class.java

        return getDataTenantAnnotation(classOfState)
    }

    /**
     * This function use to convert data tenant field with data tenant value to condition model
     *
     * @param dataTenantFields: field that use for create data tenant criteria
     * @param tenants: data tenant value (companyTaxId or vendorTaxId)
     * @param mapItemWithName: Map of Model item name with field item in database
     */
    fun buildDataTenantCondition(dataTenantFields: List<String>, tenants: List<String>, mapItemWithName: Map<String, String>? = null): List<Condition> {

        noAuthorizationOnTenants.usingWith<AuthorizationException>(tenants.isNotEmpty())

        val dataTenantCondition = dataTenantFields.map { field ->
            Condition(field = field, operator = CollectionOperator.IN.name, value = tenants.joinToString(comma))
        }

        return AuthorizationByFields.completeUserAuthorization(dataTenantCondition, mapItemWithName)
    }

    /**
     * Method for validate data tenant of state that apply data tenant when saving transaction
     * (use for new save method)
     *
     * @param state : State that we considering
     * @param userAuthorization : User authorization
     * @param transactionalActions : list of transactional action
     */
    fun <T> validateTenantState(state: T, userAuthorization: UserAuthorization, transactionalActions: List<TransactionalAction>) {

        noAuthorizationOnTenants.usingWith<AuthorizationException>(hasTenantAuthorization(userAuthorization))

        if (hasInternalPrivileges(userAuthorization)) return

        val dataTenantAnnotation = getDataTenantAnnotation(state)

        // Can apply for no tenant state with internal privilege only
        noAuthorizationOnTenants.usingWith<AuthorizationException>(dataTenantAnnotation is Tenant)

        val dataTenantFields = getDataTenantFieldsFromAnnotation(dataTenantAnnotation as Tenant, transactionalActions)

        // data tenant fields must equal transactional actions
        // e.g. state maintain @Tenant(buyer="companyTaxId, seller="") but want to save transaction with seller action
        // in this case state not maintain seller field . So should not be able to save transaction.
        notFoundDataTenantFieldsForApplyToSaveTransaction.usingWith<IllegalStateException>(dataTenantFields.size == transactionalActions.size)

        val tenants = when (userAuthorization.userType == TransactionalAction.BUYER.name) {
            true -> userAuthorization.sponsors
            false -> userAuthorization.tenants
        }
        val dataTenantConditions = buildDataTenantCondition(dataTenantFields.distinct(), tenants)
        val isValidDataTenant = dataTenantConditions.any { condition -> AuthorizationUtils.validateCondition(condition, state) }

        noAuthorizationOnTenants.usingWith<AuthorizationException>(isValidDataTenant)
    }

    /**
     * Method for validate data tenant of state that not apply data tenant when saving transaction
     * (use for old save method (from jpa))
     *
     * @param state : State that we considering
     */
    fun <T> validateNoTenantState(state: T) {

        val dataTenantAnnotation = getDataTenantAnnotation(state)

        unsupportedTenantState.usingWith<UnsupportedOperationException>(dataTenantAnnotation is NoTenant)
    }

}