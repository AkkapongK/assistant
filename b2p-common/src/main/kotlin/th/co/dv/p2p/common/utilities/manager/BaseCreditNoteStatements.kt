package th.co.dv.p2p.common.utilities.manager

object BaseCreditNoteStatements {

    const val OFFSET_PLACEHOLDER = ":page"
    const val OFFSET = """
        OFFSET $OFFSET_PLACEHOLDER ROWS
    """

    const val FETCH_PLACEHOLDER = ":size"
    const val FETCH = """
    FETCH FIRST $FETCH_PLACEHOLDER ROWS ONLY
"""

    val defaultField = listOf("linearId", "externalId", "rdSubmittedDate")
    const val defaultSortField = "externalId"

    const val NATIVE_SELECT = """
    SELECT credit_note.linear_id, credit_note.external_id, credit_note.rd_submitted_date
"""
    const val DEFAULT_GROUP = """
    credit_note.linear_id, credit_note.external_id, credit_note.rd_submitted_date
"""

    const val ORDER_BY_PLACEHOLDER = ":group"
    const val ORDER_DIRECTION_PLACEHOLDER = ":sortDirection"
    const val ORDER_BY = """
    $ORDER_BY_PLACEHOLDER $ORDER_DIRECTION_PLACEHOLDER
"""
}