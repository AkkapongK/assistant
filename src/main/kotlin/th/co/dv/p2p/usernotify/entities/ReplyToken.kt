package th.co.dv.p2p.usernotify.entities

import org.hibernate.annotations.CreationTimestamp
import java.util.*
import javax.persistence.*

@Entity
@Table
data class ReplyToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val username: String? = null,
    val replyToken: String? = null,
    @CreationTimestamp
    @Column(updatable = false)
    val createdDate: Date? = null
)