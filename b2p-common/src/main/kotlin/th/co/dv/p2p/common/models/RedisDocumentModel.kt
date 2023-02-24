package th.co.dv.p2p.common.models

data class RedisDocumentModel<T>  (
    val command: String? = null,
    val documentModel: T? = null
)