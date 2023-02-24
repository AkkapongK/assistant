package th.co.dv.p2p.common.models

import th.co.dv.p2p.corda.base.models.InvoiceModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import java.math.BigDecimal
import java.util.*

/**
 * Model for file monitoring when upload document by .csv, .txt, etc
 */
data class FileMonitoringModel(
        val id: Long? = null,
        val source: String? = null,
        val type: String? = null,
        val filePath: String? = null,
        val fileName: String? = null,
        val companyName: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorName: String? = null,
        val totalRecords: BigDecimal? = null,
        val uploadedDate: Date? = null,
        val uploadedBy: String? = null,
        val status: String? = null,
        val message: String? = null,
        val uuid: String? = null,
        val purchaseOrderModels: List<PurchaseOrderModel>? = null,
        val goodsReceivedModels: List<PurchaseOrderModel>? = null,
        val invoiceModels: List<InvoiceModel>? = null
)