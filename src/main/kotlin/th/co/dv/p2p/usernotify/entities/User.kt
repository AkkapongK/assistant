package th.co.dv.p2p.usernotify.entities

import javax.persistence.*

@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val username: String? = null,
    val token: String? = null
)
