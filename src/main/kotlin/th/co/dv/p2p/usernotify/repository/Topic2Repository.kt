package th.co.dv.p2p.usernotify.repository

import org.springframework.stereotype.Repository
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.Topic
import th.co.dv.p2p.usernotify.entities.Topic2

@Repository
interface Topic2Repository : CustomJpaRepository<Topic2, String> {
    fun findByName(name: String): Topic2?
}