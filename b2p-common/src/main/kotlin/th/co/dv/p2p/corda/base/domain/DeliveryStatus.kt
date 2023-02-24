package th.co.dv.p2p.corda.base.domain

enum class DeliveryStatus(val code: String?, val description: String?) {
    COMPLETED("X","Yes");

    companion object {
        fun fromCode(code: String?) = values().filter { it.code == code }
        fun fromDescription(description: String?) = values().filter { it.description == description }
    }
}