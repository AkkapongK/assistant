package th.co.dv.p2p.common.constants

/**
 * enum for map microService with schema name
 */
enum class MicroServiceSchema (val schema : String){
    PURCHASE("purchaseorder"),
    GOODS_RECEIVED("goodsreceived"),
    INVOICE("invoice"),
    CREDIT_NOTE("creditnote"),
    DEBIT_NOTE("debitnote"),
    AGGREGATE("aggregate"),
    PAYMENT("payment"),
    REQUEST("request"),
    TAX_DOCUMENT("taxdoc")
}