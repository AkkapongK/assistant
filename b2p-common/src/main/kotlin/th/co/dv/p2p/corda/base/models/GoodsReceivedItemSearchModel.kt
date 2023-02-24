package th.co.dv.p2p.corda.base.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class GoodsReceivedItemSearchModel(
        val goodsReceivedDateFrom: String? = null,
        val goodsReceivedDateTo: String? = null,
        val goodsReceivedLinearId: List<String>? = null,
        val goodsReceivedExternalId: String? = null,
        val externalId: String? = null,
        val purchaseOrderExternalId: String? = null,
        val purchaseItemExternalId: String? = null,
        val purchaseItemLinearId: List<String>? = null,
        val siblingLinearId: String? = null,
        val vendorNumber: String? = null,
        val vendorName: String? = null,
        val initialInvoiceExternalId: String? = null,
        val invoiceExternalId: String? = null,
        val filterReverse: Boolean? = null,
        val initialExternalId: List<String>? = null,
        val initialGoodsReceivedExternalId: List<String>? = null,
        val site: String? = null,
        val statuses: List<String>? = null,
        val movementClass: List<String>? = null,
        val sortField: String = "externalId",
        val sortOrder: Int = 1,
        val pageNumber : Int = 1,
        val pageSize : Int = 500,
        val companyTaxNumber: String? = null,
        val documentEntryYear: String? = null,
        val initialDocumentEntryYear: String? = null
)