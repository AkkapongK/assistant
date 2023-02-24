package th.co.dv.p2p.common.models

import java.util.*

data class BankBranchModel (
        var bankCode: String? = null,
        var bankBranchCode: String? = null,
        var bankBranchNameThai: String? = null,
        var bankBranchNameEng: String? = null,
        val createdDate: Date? = null,
        val updatedDate: Date? = null
)