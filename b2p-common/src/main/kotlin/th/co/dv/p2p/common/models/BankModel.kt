package th.co.dv.p2p.common.models

import java.util.*

data class BankModel (
        var bankCode: String? = null,
        var bankNameThai: String? = null,
        var bankNameEng: String? = null,
        var inITMX: Boolean? = null,
        val createdDate: Date? = null,
        val updatedDate: Date? = null
)