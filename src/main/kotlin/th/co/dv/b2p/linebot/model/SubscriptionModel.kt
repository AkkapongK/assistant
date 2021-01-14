package th.co.dv.b2p.linebot.model

data class SubscriptionModel (
    val type: String? = null,
    val name: String? = null,
    val userIds: List<String> = mutableListOf()
)
