package th.co.dv.p2p.usernotify.repository

import org.springframework.stereotype.Repository
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.UserChannel

@Repository
interface UserChannelRepository : CustomJpaRepository<UserChannel, Long> {
    fun findByUserId(userId: Long): List<UserChannel>
    fun findByUserIdAndChannel(userId: Long, channel: String): List<UserChannel>
    fun findByUserIdIn(userIds: List<Long>): List<UserChannel>
    fun findByRefIdAndChannel(refId: String, channel: String): List<UserChannel>
}