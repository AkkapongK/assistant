package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude


/**
 * Model for get request configuration
 * @property type : type of request
 * @property subtype : subType of request
 * @property purchaseOrder : flag can create request ref purchase order document
 * @property invoice : flag can create request ref invoice document
 * @property others : flag can create request ref other document
 * @property conversionStep : conversionStep
 * @property customConversion : customConversion
 * @property documentType : document type
 * @property targetLifecycle : targetLifecycle
 * @property initiator : initiator of the request document
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestConfigurationModel(
        val type: String? = null,
        val subtype: String? = null,
        val purchaseOrder: Boolean = false,
        val invoice: Boolean = false,
        val others: Boolean = false,
        val conversionStep: String? = null,
        val customConversion: Boolean = false,
        val documentType: List<RequestDocumentTypeModel>? = emptyList(),
        val targetLifecycle: String? = null,
        val initiator: String? = null
)


/**
 * Mode for request document type
 * @property documentType : documentType
 * @property textfield : flag is text field
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestDocumentTypeModel(
        val documentType: String? = null,
        val textfield: Boolean? = null
)