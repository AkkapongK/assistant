package th.co.dv.b2p.linebot.model

data class FriendModel (
    val userId: String? = null,
    val name: String? = null,
    val squad: String? = null,
    val isMaster: Boolean = false
) {
    override fun toString(): String {
        return "Name: $name, Squad: $squad, isMaster: $isMaster"
    }
}

