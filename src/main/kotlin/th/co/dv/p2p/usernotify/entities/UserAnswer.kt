package th.co.dv.p2p.usernotify.entities

import javax.persistence.*

//TODO: use redis instead
@Entity
@Table
data class UserAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val username: String? = null,
    val command: String? = null,
    @Column
    val answer: String? = null
)


