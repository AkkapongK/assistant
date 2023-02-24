package th.co.dv.p2p.usernotify.entities

import javax.persistence.*

@Entity
@Table
data class LineCommandRelation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val command: String? = null,
    val description: String? = null
)
