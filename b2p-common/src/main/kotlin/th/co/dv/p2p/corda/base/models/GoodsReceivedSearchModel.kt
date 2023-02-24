package th.co.dv.p2p.corda.base.models

import net.corda.core.serialization.CordaSerializable

/**
 * @property goodsPendingInvoicePeriods is the length of days where goods received is pending invoice i.e "7,15,30,200000"
 * @property exactIssuedDateFrom will get gr that have exact input issue date from (include time)
 * @property exactIssuedDateTo will get gr that have exact input issue date from (include time)
 */
@CordaSerializable
data class GoodsReceivedSearchModel(
        val companyCode: String? = null,
        val companyTaxNumber: List<String>? = null,
        val companyName: String? = null,
        val externalId: String? = null,
        val exactExternalId: String? = null,
        val initialInvoiceExternalId: List<String>? = null,
        val invoiceExternalId: String? = null,
        val linearId: String? = null,
        val linearIds: List<String>? = null,
        val pageNumber: Int = 1,
        val pageSize: Int = 500,
        val postingDateFrom: String? = null,
        val postingDateTo: String? = null,
        val site: String? = null,
        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        val returnGoodsReceivedItem: Boolean = false,
        val sortField: String = "externalId",
        val sortOrder: Int = 1,
        val vendorName: String? = null,
        val vendorNumber: String? = null,
        val purchaseOrderExternalId: String? = null,
        val movementClass: List<String>? = null,
        val groupBy: String? = null,
        val dashboardType: String? = null,
        val goodsPendingInvoicePeriods: String? = null,
        val isAutoMatched: Boolean? = null,
        val hasSiblingLinearId: Boolean? = null,
        val documentEntryDateFrom: String? = null,
        val documentEntryDateTo: String? = null,
        val vendorTaxNumber: List<String>? = null,
        val issuedDateFrom: String? = null,
        val issuedDateTo: String? = null,
        val exactIssuedDateFrom: String? = null,
        val exactIssuedDateTo: String? = null,
        val invoiceItemLinearIds: List<String>? = null,
        val purchaseItemLinearIds: List<String>? = null,
        val isCalculateReverseQuantity: Boolean? = null,
        val notInLinearIds: List<String>? = null,
        val grPendingInvoice: Boolean = false,
        val paymentDueDateFrom: String? = null,
        val paymentDueDateTo: String? = null
)
