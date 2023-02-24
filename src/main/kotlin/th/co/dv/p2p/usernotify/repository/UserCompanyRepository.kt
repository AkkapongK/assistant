package th.co.dv.p2p.usernotify.repository

import org.springframework.stereotype.Repository
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.UserCompany

@Repository
interface UserCompanyRepository : CustomJpaRepository<UserCompany, Long> {
    fun findByUserId(userId: Long): List<UserCompany>
    fun findByCompanyTaxNumberIn(companyTaxes: List<String>): List<UserCompany>
}