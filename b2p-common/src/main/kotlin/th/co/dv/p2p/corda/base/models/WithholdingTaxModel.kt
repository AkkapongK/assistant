package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

/**
 *
 * The model use for mapping the WithholdingTax data to json object and return in the response (API)
 * This model is for release 6 onward
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class WithholdingTaxModel(
        val whtFormType: String? = null,
        val whtTaxRunningNo: String? = null,
        val whtAttachNo: String? = null,
        val totalWhtAmount: BigDecimal? = null,
        val whtPayType: String? = null,
        val whtRemark: String? = null,
        val whtDeductDate: String? = null,
        val whtSignatory: String? = null,
        val withholdingTaxItem: List<WithholdingTaxItemModel>? = null
)