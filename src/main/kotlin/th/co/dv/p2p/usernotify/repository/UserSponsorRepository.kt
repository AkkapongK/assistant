package th.co.dv.p2p.usernotify.repository

import org.springframework.stereotype.Repository
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.UserSponsor

@Repository
interface UserSponsorRepository: CustomJpaRepository<UserSponsor, Long> {
    fun findByUserId(userId: Long): List<UserSponsor>
}