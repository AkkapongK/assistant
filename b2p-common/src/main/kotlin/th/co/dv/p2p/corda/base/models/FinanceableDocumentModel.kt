package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FinanceableDocumentModel(
        val linearId: String? = null,
        val externalId: String? = null,
        val documentType: String? = null,
        val documentIssuedDate: String? = null,
        val issuedDate: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorName: String? = null,
        val buyerTaxId: String? = null,
        val buyerName: String? = null,
        val amount: BigDecimal? = BigDecimal.ZERO,
        val currency: String? = null,
        val status: String? = null,
        val documentDueDate: String? = null,
        val paymentTermDesc: String? = null,
        val expectedDeliveryDate: String? = null
)