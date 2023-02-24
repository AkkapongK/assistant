package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import th.co.dv.p2p.corda.base.models.InvoiceModel

/**
 * Model to send request goods received email
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SendRequestGREmailModel (
        val invoice: InvoiceModel? = null,
        val toEmails: List<String> = emptyList(),
        val ccEmail: String? = null
)