package th.co.dv.p2p.usernotify.entities

import javax.persistence.*

@Entity
@Table
data class UserSubscribe(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val userId: Long? = null,
    val topicId: Long? = null
)
