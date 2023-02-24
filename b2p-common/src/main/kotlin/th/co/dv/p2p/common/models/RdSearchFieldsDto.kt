package th.co.dv.p2p.common.models

data class RdSearchFieldsDto(
        val fieldDate: String,
        val fieldVendorTaxNumber: String,
        val fieldIsETax: String?,
        val fieldRdSubmittedDate: String
)