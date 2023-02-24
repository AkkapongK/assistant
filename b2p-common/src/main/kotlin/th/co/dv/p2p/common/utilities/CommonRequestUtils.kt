package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.corda.base.models.RequestItemModel
import java.math.BigDecimal

object CommonRequestUtils {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    /**
     * Method for calculate with holding tax total
     * filter [`withholdingTaxRate`] is not null and group by [`withholdingTaxRate`]
     *
     * @param requestItems : list of requestItems
     * @return BigDecimal
     */
    fun calculateWithholdingTaxTotal(requestItems: List<RequestItemModel>): BigDecimal {
            logger.info("CommonRequestUtils.calculateWithholdingTaxTotal requestItems: $requestItems")

            val sumOfWithholdingTax = requestItems.filter { it.withholdingTaxRate != null }
                    .groupBy { it.withholdingTaxRate }
                    .mapValues { ( whtRate, groupRequestItems ) ->
                        logger.info("CommonRequestUtils.calculateWithholdingTaxTotal whtRate: $whtRate, groupInvoiceItems: $groupRequestItems")

                        val sumOfSubtotal = groupRequestItems.sumByDecimal { requestItem -> requestItem.subTotal ?: BigDecimal.ZERO }
                        logger.info("CommonRequestUtils.calculateWithholdingTaxTotal sumOfSubtotal: $sumOfSubtotal")
                        val currency = requestItems.first().currency
                        val whtTotal = sumOfSubtotal.times(whtRate!!.movePointLeft(2)).setScaleByCurrency(currency)
                        logger.info("CommonRequestUtils.calculateWithholdingTaxTotal whtTotal: $whtTotal")

                        whtTotal
                    }.let { eachRateWithWhtTotal ->
                        eachRateWithWhtTotal.values.sumByDecimal{ it }
                    }

            logger.info("CommonRequestUtils.calculateWithholdingTaxTotal sumOfWithholdingTax: $sumOfWithholdingTax")

            return sumOfWithholdingTax
    }
}