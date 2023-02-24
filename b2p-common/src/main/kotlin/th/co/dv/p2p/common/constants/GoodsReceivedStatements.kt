package th.co.dv.p2p.common.constants

object GoodsReceivedStatements {
    /**
     * Method for return where clause statement which calculate normal item that have quantity left
     * @param quantityField ex. goods_received_item.quantity
     * @param reverseField ex. goods_received_item.reverse_quantity
     */
    fun getResolveNormalItemWhereClause(quantityField: String, reverseField: String) = """
        ISNULL(CAST(JSON_VALUE($quantityField, '$.initial') AS decimal(28,10)),0) - ISNULL(CAST(JSON_VALUE($reverseField, '$.initial') AS decimal(28,10)),0) > 0 
    """
}