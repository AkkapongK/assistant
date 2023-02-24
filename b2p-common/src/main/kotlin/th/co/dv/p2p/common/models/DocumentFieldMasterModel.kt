package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable

/**
 * Model for table document_field_master on master data service
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DocumentFieldMasterModel(
        var id: Int? = null,
        var state: String? = null,
        var fieldName: String? = null,
        var fieldDescription: String? = null
) : Serializable