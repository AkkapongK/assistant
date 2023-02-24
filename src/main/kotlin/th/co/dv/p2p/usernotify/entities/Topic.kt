package th.co.dv.p2p.usernotify.entities

import com.fasterxml.jackson.annotation.JsonFormat
import th.co.dv.p2p.common.utilities.DATE_TIME_FORMAT
import java.util.*
import javax.persistence.*

@Entity
@Table
data class Topic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT, timezone = "Asia/Bangkok")
    @Temporal(TemporalType.TIMESTAMP)
    val updatedDate: Date? = null
)
