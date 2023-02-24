package th.co.dv.p2p.usernotify.models

data class UserAnswerModel(
    val key: String? = null,
    val value: String? = null,
    val command: String? = null
)

fun List<UserAnswerModel>.getValueOf(key: String): String? {
    return this.find {
        it.key == key
    }?.value

}
