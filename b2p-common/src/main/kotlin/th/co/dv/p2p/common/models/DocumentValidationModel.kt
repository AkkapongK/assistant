package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.models.PostingDetailModel
import java.math.BigDecimal

/**
 * Model for Document Validation
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DocumentValidationModel (
        val invoiceLinearId: String? = null,
        val invalidInvoiceNo: Boolean? = null,
        val invalidInvoiceDate: Boolean? = null,
        val invalidAmount: Boolean? = null,
        val whtNotSpecified: Boolean? = null,
        val invalidWht: Boolean? = null,
        val invalidVendorBranch: Boolean? = null,
        val invalidTaxCode: Boolean? = null,
        val attachmentIsIncorrectType: Boolean? = null,
        val retentionNotSpecified: Boolean? = null,
        val other: String? = null,
        val externalId: String? = null,
        val invoiceDate: String? = null,
        val companyTaxNumber: String? = null,
        val companyCode: String? = null,
        val companyName: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorNumber: String? = null,
        val vendorName: String? = null,
        val subTotal: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val invoiceTotal: BigDecimal? = null,
        val buyerPostingStatus: String? = null,
        val buyerPostingDate: String? = null,
        val buyerPostingDetail: List<PostingDetailModel>? = emptyList()
)
