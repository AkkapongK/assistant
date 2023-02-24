package th.co.dv.p2p.usernotify.models

import th.co.dv.p2p.common.models.UserAuthorization

data class SubScribeRequestModel(
    val username: String,
    val subscribeModel: SubscribeModel,
    val token: String
)
