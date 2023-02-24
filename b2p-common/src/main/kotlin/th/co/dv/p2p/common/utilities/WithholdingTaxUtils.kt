package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.models.TaxModel
import java.math.BigDecimal

/**
 * Utility for calculate withholding tax rate
 */
object WithholdingTaxUtils {

    internal val logger: Logger = LoggerFactory.getLogger(WithholdingTaxUtils::class.java)
    internal val className = WithholdingTaxUtils::class.java.simpleName

    /**
     * Function transform withholdingTaxRate by 4 case
     * Case 1 "withholdingTaxCode" = NULL, "withholdingTaxRate"= any value
     *       - Rate always null
     *
     * Case 2 "withholdingTaxCode" = NOT NULL, "withholdingTaxRate"= NULL
     *       - Use value of "withholdingTaxRate" from table tax by lookup from "withholdingTaxCode"
     *
     * Case 3 "withholdingTaxCode" = NOT NULL, "withholdingTaxRate"= NOT NULL
     *       - Use value of "withholdingTaxRate" from input
     *
     * @param whtCode withholding tax code
     * @param whtRate withholding tax rate
     * @param whtConfig tax model
     */
     fun transformWithholdingTaxRate(whtCode: String?, whtRate: BigDecimal?, whtConfig: TaxModel?): BigDecimal? {
        return when {
            whtCode.isNullOrBlank() -> null
            whtCode.isNullOrBlank().not() && whtRate == null -> whtConfig?.taxRate
            else -> whtRate
        }
    }

}