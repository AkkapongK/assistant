package th.co.dv.p2p.usernotify.repository

import org.springframework.stereotype.Repository
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.Topic

@Repository
interface TopicRepository : CustomJpaRepository<Topic, Long> {
    fun findByIdIn(ids: List<Long>): List<Topic>
    fun findByName(name: String): List<Topic>
}