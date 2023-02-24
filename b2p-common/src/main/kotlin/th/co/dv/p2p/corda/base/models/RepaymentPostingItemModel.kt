package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RepaymentPostingItemModel(
    val documentLinearId: String? = null,
    val fiDocFiscalYear: String? = null,
    val fiDocNumber: String? = null
)
