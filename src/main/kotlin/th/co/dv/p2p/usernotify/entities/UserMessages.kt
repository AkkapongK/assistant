package th.co.dv.p2p.usernotify.entities

import org.hibernate.annotations.CreationTimestamp
import java.util.*
import javax.persistence.*

@Entity
@Table
data class UserMessages(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val fromUser: String? = null,
    val toUser: String? = null,
    val message: String? = null,
    @CreationTimestamp
    @Column(updatable = false)
    val createdDate: Date? = null
)