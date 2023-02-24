package th.co.dv.p2p.common.utilities.search

enum class SearchCriteriaOperation constructor(val realOp: String) {

    EQUAL(" = "),
    NOT_EQUAL(" <> "),
    LESSTHAN(" < "),
    LESSTHAN_OR_EQUAL(" <= "),
    GREATERTHAN(" > "),
    GREATERTHAN_OR_EQUAL(" >= "),
    STARTS_WITH(" START "),
    ENDS_WITH(" END"),
    CONTAIN(" CONTAIN "),
    IN(" IN "),
    NOT_IN(" NOT IN "),
    BETWEEN(" BETWEEN "),
    ISNULL(" IS NULL "),
    NOTNULL(" IS NOT NULL "),
    LIKE(" LIKE "),
    NOT_LIKE(" NOT LIKE "),
    IN_SUBQUERY(" IN SUBQUERY "),
    NOT_IN_SUBQUERY(" NOT IN SUBQUERY "),
    AND_CUSTOM_CRITERIA("AND CUSTOM");


    companion object {

        operator fun get(name: String): SearchCriteriaOperation {
            return this[name]
        }
    }
}