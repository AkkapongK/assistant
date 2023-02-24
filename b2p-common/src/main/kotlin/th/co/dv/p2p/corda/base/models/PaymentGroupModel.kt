package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentGroupModel(
        val linearId: String? = null,
        val invoiceModelList: List<InvoiceModel> = emptyList(),
        val debitNoteModelList: List<DebitNoteModel> = emptyList(),
        val creditNoteModelList: List<CreditNoteModel> = emptyList(),
        val page: Int? = null,
        val pageSize: Int? = null,
        val totalRecords: Int? = null
)