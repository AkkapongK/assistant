package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import th.co.dv.p2p.corda.base.models.PaymentModel

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentServiceResponseModel(
    val transactionId: String? = null,
    val paymentModels: List<PaymentModel>? = null
)