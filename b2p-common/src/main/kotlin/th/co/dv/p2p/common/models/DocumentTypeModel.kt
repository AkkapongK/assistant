package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DocumentTypeModel(
    var id: Int? = null,
    var documentCode: String? = null,
    var documentTypeEn: String? = null,
    var documentTypeTh: String? = null,
    var state: String? = null
)
