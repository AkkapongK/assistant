package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

/**
 * Model for RdTaxDocumentElastic from aggregate service
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RdTaxDocumentModel(
        val id: String? = null,
        val documentLinearId: String? = null,
        val documentNo: String? = null,
        val documentType: String? = null,
        val documentDate: String? = null,
        val subTotal: BigDecimal? = null,
        val taxTotal: BigDecimal? = null,
        val totalAmount: BigDecimal? = null,
        val currency: String? = null,
        val companyTaxID: String? = null,
        val companyBranchCode: String? = null,
        val companyName: String? = null,
        val vendorTaxID: String? = null,
        val vendorBranchCode: String? = null,
        val vendorName: String? = null,
        val documentStatus: String? = null,
        val receiptNo: String? = null,
        val documentSubType: String? = null,
        val taxInvoiceRefNo: String? = null,
        val taxInvoiceRefLinearId: String? = null,
        val taxInvoiceRefDate: String? = null,
        val rdSubmittedDate: String? = null,
        val companyCode: String? = null,
        val companyAddress: String? = null,
        val vendorCode: String? = null,
        val vendorAddress: String? = null,
        val advanceDeductionAmount: BigDecimal? = null,
        val totalOriginalValue: BigDecimal? = null,
        val totalCorrectValue: BigDecimal? = null,
        val difference: BigDecimal? = null,
        val issuedBy: String? = null,
        val remark: String? = null,
        val reason: String? = null,
        val cancellationReason: String? = null,
        val cancellationDescription: String? = null,
        val paymentDate: String? = null,
        val rdPostingStatus: String? = null,
        val rdLifecycle: String? = null,
        val item: List<RdTaxDocumentItemModel>? = emptyList(),
        val cnReferToInvoiceElastic: List<CNReferToInvoiceModel>? = emptyList(),
        val dnReferToInvoiceElastic: List<DNReferToInvoiceModel>? = emptyList(),
        val documentCreatedDate: String? = null,
        val reasonCode: String? = null,
        val documentCode: String? = null
)