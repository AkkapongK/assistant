package th.co.dv.p2p.common.models

import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation

/**
 * Model for search data with value and operation
 * @property value value of the field to search
 * @property oper operation that will be used to search
 */
data class SearchInput(
        val value: String,
        val oper: String = SearchCriteriaOperation.EQUAL.name
)