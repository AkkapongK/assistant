package th.co.dv.p2p.usernotify.repository

import org.springframework.stereotype.Repository
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.UserSubscribe

@Repository
interface UserSubscribeRepository: CustomJpaRepository<UserSubscribe, Long> {
    fun findByUserId(userId: Long): List<UserSubscribe>
    fun findByTopicIdIn(topicIds: List<Long>): List<UserSubscribe>
    fun findByUserIdAndTopicId(userId: Long, topicId: Long): List<UserSubscribe>
}