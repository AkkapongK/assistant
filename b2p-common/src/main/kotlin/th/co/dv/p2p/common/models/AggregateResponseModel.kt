package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonInclude
import th.co.dv.p2p.common.utilities.setScaleByCurrency
import java.math.BigDecimal

/**
 * Data class for response aggregate result
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AggregateResponseModel(
        val key: Any? = null,
        val countTotal: Int? = null,
        val vendorTotal: Int? = null,
        val amountTotal: BigDecimal? = null,
        val currency: String? = null,
)

/**
 * Model for Header of aggregate response
 *
 * @property countTotal Total records
 * @property amountTotal Total Sum amount records
 * @property groupBy Group by field
 * @property data list of AggregateDataModel
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AggregateMetadataModel(
        val countTotal: Int? = null,
        val amountTotal: BigDecimal? = null,
        val groupBy: String? = null,
        val data: List<AggregateResponseModel>? = null
)

/**
 * Method to set scale big decimal using round haft up for AggregateResponseModel
 */
fun AggregateResponseModel.setScale(): AggregateResponseModel {
    val amountTotal = setAmountTotal(amountTotal, this.currency)
    return AggregateResponseModel(
            key = this.key,
            countTotal = this.countTotal,
            vendorTotal = this.vendorTotal,
            amountTotal = amountTotal,
            currency = this.currency
    )
}

/**
 * Method to set scale big decimal using round haft up for AggregateMetadataModel
 */
fun AggregateMetadataModel.setScale(): AggregateMetadataModel {
    val currency = this.data?.firstOrNull()?.currency
    val amountTotal = setAmountTotal(amountTotal, currency)
    return AggregateMetadataModel(
            countTotal = this.countTotal,
            amountTotal = amountTotal,
            groupBy = this.groupBy,
            data = this.data
    )
}

/**
 * Method to set scale for amountTotal when currency is not null
 */
fun setAmountTotal(amountTotal: BigDecimal?, currency: String?): BigDecimal? {
    return if (currency == null) amountTotal else amountTotal?.setScaleByCurrency(currency)
}