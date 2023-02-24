package th.co.dv.p2p.usernotify.models

import th.co.dv.p2p.usernotify.entities.*

data class SubscribeUserDataModel(
    val userSubscribes: List<UserSubscribe> = emptyList(),
    val userChannels: List<UserChannel> = emptyList(),
    val userCompanies: List<UserCompany> = emptyList(),
    val userSponsors: List<UserSponsor> = emptyList(),
    val user: User? = null
)