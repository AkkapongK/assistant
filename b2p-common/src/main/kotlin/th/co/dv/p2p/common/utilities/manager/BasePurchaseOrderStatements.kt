package th.co.dv.p2p.common.utilities.manager

object BasePurchaseOrderStatements {

    const val PURCHASE_ORDER_SELECT_SQL = """
        SELECT purchase_order.linear_id, purchase_order.currency
    """
    const val PURCHASE_ITEM_SELECT_SQL = """
        SELECT  purchase_item.linear_id, purchase_item.business_place
    """

    const val PURCHASE_ORDER_SEARCH_SELECT_SQL = """
        SELECT purchase_order.linear_id, purchase_order.currency,
            CAST (SUM(
                CASE
                    WHEN purchase_item.item_category = 'ADVANCE' THEN 0
                    ELSE ROUND(purchase_item.po_item_unit_price * CAST(JSON_VALUE(purchase_item.quantity, '$.initial') AS decimal(28,10)), 2)
                END) AS DECIMAL(28,10)) AS initial_amount,
            CAST (SUM(
                CASE
                    WHEN purchase_item.item_category = 'ADVANCE' THEN 0
                    ELSE ROUND(purchase_item.po_item_unit_price * CAST(JSON_VALUE(purchase_item.quantity, '$.remaining') AS decimal(28,10)), 2)
                END) AS DECIMAL(28,10)) AS remaining_amount
    """

    const val GROUP_BY = """
        purchase_order.linear_id, purchase_order.currency, purchase_order.purchase_order_number
    """
    const val DEFAULT_WHERE_VALUE_PLACEHOLDER = ":value"
    const val DEFAULT_WHERE = """(CAST(JSON_VALUE(purchase_item.quantity, '$.remaining') AS decimal(28,10)) + CAST(JSON_VALUE(purchase_item.over_delivery_quantity, '$.remaining') AS decimal(28,10))) > $DEFAULT_WHERE_VALUE_PLACEHOLDER"""


    const val HAVING_CLAUSE_OPERATION_PLACEHOLDER = ":operation"
    const val HAVING_CLAUSE_VALUE_PLACEHOLDER = ":value"
    const val HAVING_CLAUSE = """
        SUM(
            CASE
                WHEN purchase_item.item_category = 'ADVANCE' THEN 0
                ELSE ROUND(purchase_item.po_item_unit_price * CAST(JSON_VALUE(purchase_item.quantity, '$.initial') AS decimal(28,10)), 2)
            END) $HAVING_CLAUSE_OPERATION_PLACEHOLDER $HAVING_CLAUSE_VALUE_PLACEHOLDER
    """

    const val HAVING_CLAUSE_QUANTITY = """
        SUM(
            CASE
                WHEN purchase_item.item_category = 'ADVANCE' THEN 0
                ELSE CAST(JSON_VALUE(purchase_item.quantity, '$.remaining') AS decimal(28,10)) + CAST(JSON_VALUE(purchase_item.over_delivery_quantity, '$.remaining') AS decimal(28,10))
            END) $HAVING_CLAUSE_OPERATION_PLACEHOLDER $HAVING_CLAUSE_VALUE_PLACEHOLDER
    """

    const val ORDER_BY_PLACEHOLDER = ":group"
    const val ORDER_DIRECTION_PLACEHOLDER = ":sortDirection"
    const val ORDER_BY = """
        $ORDER_BY_PLACEHOLDER $ORDER_DIRECTION_PLACEHOLDER
    """

    const val OFFSET_PLACEHOLDER = ":page"
    const val OFFSET = """
        OFFSET $OFFSET_PLACEHOLDER ROWS
    """

    const val FETCH_PLACEHOLDER = ":size"
    const val FETCH = """
        FETCH FIRST $FETCH_PLACEHOLDER ROWS ONLY
    """

    val defaultField = listOf("linearId", "currency")
    val defaultFieldPurchaseItem = listOf("linearId")
    const val defaultPurchaseItemSortField = "purchase_item.linear_id"
    const val defaultSortField = "purchaseOrderNumber"
    const val defaultSortFieldItem = "poItemNo"

}