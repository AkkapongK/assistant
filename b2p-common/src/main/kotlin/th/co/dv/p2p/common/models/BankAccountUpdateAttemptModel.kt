package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BankAccountUpdateAttemptModel(
        var id: Long? = null,
        var buyerTaxId: String? = null,
        var sellerTaxId: String? = null,
        var updateBankAccount: String? = null,
        var attemptedDate: String? = null
)
