package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PurposeModel(

        var id:Long? = null,
        var type: String? = null,
        var subType: String? = null,
        var purposeCode: String? = null,
        var purposeDescription: String? = null,
        var purposeDescriptionEng: String? = null

)

enum class PurposeType {
    INVOICE,
    CREDIT_NOTE,
    RECEIPT
}