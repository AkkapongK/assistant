package th.co.dv.p2p.usernotify.repository

import org.springframework.stereotype.Repository
import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.ReplyToken
import th.co.dv.p2p.usernotify.entities.Topic
import th.co.dv.p2p.usernotify.entities.UserMessages

@Repository
interface ReplyTokenRepository : CustomJpaRepository<ReplyToken, Long> {
    fun findByUsername(username: String): List<ReplyToken>
}