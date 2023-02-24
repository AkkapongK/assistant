package th.co.dv.p2p.usernotify.repository

import org.springframework.stereotype.Repository
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.Topic
import th.co.dv.p2p.usernotify.entities.UserAnswer

@Repository
interface UserAnswerRepository : CustomJpaRepository<UserAnswer, Long> {
    fun findByUsername(username: String): List<UserAnswer>
}