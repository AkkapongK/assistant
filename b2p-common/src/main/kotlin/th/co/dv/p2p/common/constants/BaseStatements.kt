package th.co.dv.p2p.common.constants

object BaseStatements {
    const val SELECT_COUNT = """
        SELECT COUNT(*)
    """
    const val SUB_SQL_KEY = ":subSql"
    const val COUNT_SQL = """
        $SELECT_COUNT FROM ($SUB_SQL_KEY) AS Z
    """
    const val FETCH_PLACEHOLDER = ":size"
    const val FETCH = """
    FETCH FIRST $FETCH_PLACEHOLDER ROWS ONLY
"""

    const val OFFSET_PLACEHOLDER = ":page"
    const val OFFSET = """
        OFFSET $OFFSET_PLACEHOLDER ROWS
    """

    /**
     * Method for create fetch clause
     */
    fun createFetchClause(pageSize: Int): String {
        return FETCH.replace(FETCH_PLACEHOLDER, pageSize.toString())
    }

    /**
     * Method for create offset clause
     */
    fun createOffsetClause(pageNumber: Int, pageSize: Int): String {
        return OFFSET.replace(OFFSET_PLACEHOLDER, ((pageNumber - 1) * pageSize).toString())
    }
}