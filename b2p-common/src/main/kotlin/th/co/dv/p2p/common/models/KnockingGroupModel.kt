package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * Model that collect document information in step knocking and issue payment
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class KnockingGroupModel(
        val linearId: String? = null,
        val type: String? = null,
        val amount: BigDecimal? = null,
        val groupDueDate: String? = null,
        @get:JsonProperty("isFinancing")
        val isFinancing: Boolean? = null,
        @get:JsonProperty("isScbBank")
        val isScbBank: Boolean? = null,
        val taggedDocument: List<KnockingTaggedDocumentModel>? = mutableListOf(),
        val companyTaxNumber: String? = null,
        val vendorTaxNumber: String? = null,
        val status: String? = null,
        val remark: String? = null,
        val issuedPaymentDate: String? = null
)