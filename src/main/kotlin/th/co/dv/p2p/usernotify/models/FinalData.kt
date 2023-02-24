package th.co.dv.p2p.usernotify.models

import th.co.dv.p2p.usernotify.entities.*

data class FinalData(
    val userSubscribe: Pair<List<UserSubscribe>, List<UserSubscribe>>,
    val userChannel: Pair<List<UserChannel>, List<UserChannel>>,
    val userCompany: Pair<List<UserCompany>, List<UserCompany>>,
    val userSponsor: Pair<List<UserSponsor>, List<UserSponsor>>,
    val user: User
)
