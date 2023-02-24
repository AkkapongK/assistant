package th.co.dv.p2p.common.constants

object ReconcileStatements {
    const val TABLE_NAME_STATEMENT = ":tableName"
    const val SCHEMA_STATEMENT = ":schema"
    const val WHERE_CONDITION_STATEMENT = ":where"
    const val ADDITIONAL_JOIN_STATEMENT = ":additionalJoin"
    const val ADDITIONAL_FIELDS_STATEMENT = ":additionalFields"
    const val LINEAR_ID_LIST_STATEMENT = ":linearIdList"
    const val SELECT_FIELDS_STATEMENT = "t.*"

    val QUERY_LINEAR_ID_MS_SQL = """
        $SELECT_CLAUSE t.linear_id
        $FROM_CLAUSE $SCHEMA_STATEMENT$DOT$TABLE_NAME_STATEMENT as t
        $ADDITIONAL_JOIN_STATEMENT
        $WHERE_CONDITION_STATEMENT
    """.trimIndent()

    val QUERY_DATA_MS_SQL = """
        $SELECT_CLAUSE $SELECT_FIELDS_STATEMENT $ADDITIONAL_FIELDS_STATEMENT
        $FROM_CLAUSE $SCHEMA_STATEMENT$DOT$TABLE_NAME_STATEMENT as t 
        $ADDITIONAL_JOIN_STATEMENT
        $WHERE_CLAUSE t.linear_id IN ($LINEAR_ID_LIST_STATEMENT)
    """.trimIndent()
}