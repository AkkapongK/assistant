package th.co.dv.p2p.usernotify.repository

import org.springframework.stereotype.Repository
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.User
import th.co.dv.p2p.usernotify.entities.UserCompany

@Repository
interface UserRepository: CustomJpaRepository<User, Long> {
    fun findByUsername(username: String): User?
}