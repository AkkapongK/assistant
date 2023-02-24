package th.co.dv.p2p.common.utilities.search

import net.corda.core.node.services.vault.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.DOT
import th.co.dv.p2p.common.models.Condition
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.AuthorizationUtils.getStateHeader
import th.co.dv.p2p.common.utilities.isNullOrEmpty
import th.co.dv.p2p.common.utilities.splitAndTrim

object AuthorizationByFields {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * This function is to add criteria using user authorization to existing criteria
     *
     * @param criterias: Base criteria
     * @param userAuthorization: User authorization
     * @param mapItemWithName: Map of Model item name with field item in database
     */
    inline fun <reified T : Any> addCriteriaFromAuthorization(
            criterias: SearchCriterias<*>,
            userAuthorization: UserAuthorization,
            mapItemWithName: Map<String, String>? = null) {
        addCriteriaFromAuthorizationWithClass(criterias, userAuthorization, mapItemWithName, T::class.java)
    }


    /**
     * This function use to add auth by field criteria to query criterias
     *
     * @param criterias : query criterias
     * @param userAuthorization : user authorization
     * @param mapItemWithName : Map of Model item name with field item in database
     * @param clazz : state
     */
    fun <T : Any> addCriteriaFromAuthorizationWithClass(
            criterias: SearchCriterias<*>,
            userAuthorization: UserAuthorization,
            mapItemWithName: Map<String, String>? = null,
            clazz: Class<T>) {

        if (userAuthorization.userGroups.isNullOrEmpty()) return

        // Get mapping between State and Schema (used for create criteria)
        val stateName = getStateHeader(clazz.simpleName)

        // Loop each group of user to create query criteria
        val allGroupCriteria = SearchCriterias(clazz)

        userAuthorization.userGroups.forEach { group ->
            // Loop each group of user to create list of query criteria
            val conditionsForState = completeUserAuthorization(
                    conditionsForState = group.states.getOrDefault(stateName, emptyList()),
                    mapItemWithName = mapItemWithName)

            // In case conditionsForState is empty we need to skip this round
            if (conditionsForState.isEmpty()) return@forEach
            // create group outer criteria with inner condition criteria
            val searchGroupCriteria = buildSearchCriteriasFromConditions(
                    conditions = conditionsForState,
                    setAndOuterCriteria = false,
                    setAndInnerConditionCriteria = true,
                    clazz = clazz
            )
            // add each group criteria to all group criteria
            allGroupCriteria.getCriterias().add(searchGroupCriteria)
        }

        // add to main criteria if sub criteria is not empty
        if (allGroupCriteria.getCriterias().size > 0) criterias.getCriterias().add(allGroupCriteria)
    }


    /**
     * Method for complete condition in user authorization
     * we need to convert state name to item name
     *
     * @param conditionsForState: List of condition
     * @param mapItemWithName: Map of Model item name with field item in database
     */
    fun completeUserAuthorization(conditionsForState: List<Condition>, mapItemWithName: Map<String, String>? = null): List<Condition> {
        mapItemWithName ?: return conditionsForState

        return conditionsForState.map { condition ->
            val fields = condition.field.splitAndTrim(DOT, true)
            // In case item size must more than 1
            if (fields.size > 1) {
                val itemFieldName = mapItemWithName[fields.first()] ?: return@map condition

                val newList = when (itemFieldName.isNotEmpty()) {
                    true -> listOf(itemFieldName) + fields.drop(1)
                    false -> fields.drop(1)
                }
                condition.copy(field = newList.joinToString(DOT))
            } else {
                // Now we support auth by field in case get data from item table
                // We need to convert field in header table to headerLinkedField.fieldName
                when (mapItemWithName[""] == null) {
                    true -> condition
                    false -> {
                        condition.copy(field = mapItemWithName[""] + DOT + condition.field)
                    }
                }

            }
        }
    }


    /**
     * Method for add condition to inner criteria
     *
     * @param conditions : list of conditions
     * @param setAndOuterCriteria: flag to build AND criteria (true build AND / false build OR) for outer criteria
     * @param setAndInnerConditionCriteria: flag to build AND criteria (true build AND / false build OR) for inner criteria
     */
    fun <T> buildSearchCriteriasFromConditions(conditions: List<Condition>,
                                                       setAndOuterCriteria: Boolean,
                                                       setAndInnerConditionCriteria: Boolean,
                                                       clazz: Class<T>): SearchCriterias<T> {

        val searchCriterias = SearchCriterias(clazz)
        searchCriterias.setAnd(setAndOuterCriteria)

        val conditionsCriteria = conditions.createCriteria(setAndInnerConditionCriteria)

        if (conditionsCriteria.isNotEmpty()) searchCriterias.getCriterias().addAll(conditionsCriteria)

        return searchCriterias
    }

    /**
     * Method for create criteria from list of condition model
     *
     * @param setAnd: flag to build AND criteria (true build AND / false build OR)
     */
    private fun List<Condition>.createCriteria(setAnd: Boolean): List<SearchCriteria> {
        return this.map { condition ->
            val (field, parent) = validateField(condition.field)
            createSearchCriteria(
                    condition = condition,
                    field = field,
                    parent = parent,
                    setAnd = setAnd
            )
        }
    }


    /**
     * Method for create search criteria
     * we need to convert each [Condition] to the SearchCriteria, in case parent is not null
     * we will set parent to path in [SearchCriteria]
     *
     * @param condition: Condition model
     * @param field: Field name
     * @param parent: Parent field
     * @param setAnd flag to build AND criteria (true build AND / false build OR)
     */
    private fun createSearchCriteria(condition: Condition, field: String, parent: String? = null, setAnd: Boolean): SearchCriteria {
        val criteria = SearchCriteria()
        criteria.setAnd(setAnd)

        if (parent != null) criteria.setPath(parent)

        criteria.setField(field)
        val (operation, value) = getSearchCriteriaOperationAndValue(condition)
        criteria.setOp(operation)
        criteria.setValue(value)
        return criteria
    }

    /**
     * Method for get SearchCriteriaOperation and final value
     *
     * @param condition: Condition model
     */
    fun getSearchCriteriaOperationAndValue(condition: Condition): Pair<SearchCriteriaOperation, Any> {
        return when (condition.operator) {
            EqualityComparisonOperator.EQUAL.name -> SearchCriteriaOperation.EQUAL to condition.value
            EqualityComparisonOperator.NOT_EQUAL.name -> SearchCriteriaOperation.NOT_EQUAL to condition.value
            NullOperator.IS_NULL.name -> SearchCriteriaOperation.ISNULL to condition.value
            NullOperator.NOT_NULL.name -> SearchCriteriaOperation.NOTNULL to condition.value
            LikenessOperator.LIKE.name -> SearchCriteriaOperation.LIKE to condition.value
            LikenessOperator.NOT_LIKE.name -> SearchCriteriaOperation.NOT_LIKE to condition.value
            CollectionOperator.IN.name -> SearchCriteriaOperation.IN to condition.value.splitAndTrim(separator = ",", trim = true)
            CollectionOperator.NOT_IN.name -> SearchCriteriaOperation.NOT_IN to condition.value.splitAndTrim(separator = ",", trim = true)

            BinaryComparisonOperator.LESS_THAN.name -> SearchCriteriaOperation.LESSTHAN to condition.value
            BinaryComparisonOperator.LESS_THAN_OR_EQUAL.name -> SearchCriteriaOperation.LESSTHAN_OR_EQUAL to condition.value
            BinaryComparisonOperator.GREATER_THAN.name -> SearchCriteriaOperation.GREATERTHAN to condition.value
            BinaryComparisonOperator.GREATER_THAN_OR_EQUAL.name -> SearchCriteriaOperation.GREATERTHAN_OR_EQUAL to condition.value
            else -> throw IllegalArgumentException("Unsupported operator.")
        }
    }

    /**
     * Method for split field in to parent and field
     *
     * @param field: Field name
     *
     * @return map of field and parent field
     */
    fun validateField(field: String): Pair<String, String?> {
        return if (field.contains(DOT)) {
            val fields = field.splitAndTrim(DOT, true)
            fields[1] to fields[0]
        } else {
            field to null
        }
    }
}