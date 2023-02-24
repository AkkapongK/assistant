package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

/**
 * Search Model for Tax Document
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaxDocumentSearchModel (
        val linearIds: List<String>? = null,
        val vendorNumber: String? = null,
        val vendorName: String? = null,
        val vendorTaxNumber: String? = null,
        val companyCode: String? = null,
        val companyName: String? = null,
        val documentType: String? = null,
        val documentNumber: String? = null,
        val documentDateFrom: String?= null,
        val documentDateTo: String?= null,
        val statuses: List<String>? = null,
        val postingStatus: List<String>? = null,
        val postingStatusNotIn: List<String>? = null,
        val buyerPostingDateFrom: String? = null,
        val buyerPostingDateTo: String? = null,
        val referenceField1: String? = null,
        val attachmentPosted: Boolean? = null,
        val pageNumber : Int = 1,
        val pageSize : Int = 1000,
        val sortField: String = "documentNumber",
        val sortOrder: Int = 1,
        val rdSubmitted: Boolean? = null,
        val returnReferenceDocument: Boolean? = null,
        val companyTaxNumbers: List<String>? = null
)