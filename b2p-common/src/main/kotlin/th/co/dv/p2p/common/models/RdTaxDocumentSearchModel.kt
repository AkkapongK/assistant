package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RdTaxDocumentSearchModel(
        val id: List<String>? = null,
        val documentLinearId: String? = null,
        val documentNo: String? = null,
        val documentType: String? = null,
        val documentDateFrom: String? = null,
        val documentDateTo: String? = null,
        val rdSubmittedDateFrom: String? = null,
        val rdSubmittedDateTo: String? = null,
        val rdLifecycle: String? = null,
        val companyTaxID: String? = null,
        val vendorTaxID: String? = null,
        val documentStatus: String? = null,
        val taxInvoiceRefNo: String? = null,
        val returnItems: Boolean = false,
        val rdPostingStatus: String? = null,
        val pageNumber: Int = 1,
        val pageSize: Int = 500,
        val sortField: String = "documentNo",
        val sortOrder: Int = 1
)