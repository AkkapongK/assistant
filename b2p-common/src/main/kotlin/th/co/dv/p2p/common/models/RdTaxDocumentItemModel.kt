package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

/**
 * Model for RdTaxDocumentItemElastic from aggregate service
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RdTaxDocumentItemModel(
        val itemNo: Long? = null,
        val documentItemNo: String? = null,
        val refPONo: String? = null,
        val refInvoiceNo: String? = null,
        val documentNo: String? = null,
        val materialDesc: String? = null,
        val qty: BigDecimal? = null,
        val unit: String? = null,
        val unitPrice: BigDecimal? = null,
        val correctUnitPrice: BigDecimal? = null,
        val taxCode: String? = null,
        val taxRate: BigDecimal? = null,
        val subTotal: BigDecimal? = null,
        val currency: String? = null
)