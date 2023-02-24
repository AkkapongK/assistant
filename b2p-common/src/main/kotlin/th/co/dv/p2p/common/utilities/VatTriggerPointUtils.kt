package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.enums.VatTriggerPoint
import th.co.dv.p2p.common.models.TaxModel

/**
 * Utility for calculate vat trigger point
 */
object VatTriggerPointUtils {

    internal val logger: Logger = LoggerFactory.getLogger(VatTriggerPointUtils::class.java)
    internal val className = VatTriggerPointUtils::class.java.simpleName

    /**
     * Method for calculate vat trigger point.
     * Considering [VatTriggerPoint.Invoice] is as first priority,
     * [VatTriggerPoint.Payment] as second, and [VatTriggerPoint.None] for the last.
     * @param taxList list of tax involved in the calculation
     */
    fun getVatTriggerPoint(taxList: List<TaxModel>): String {
        val taxTriggerPointList = taxList.map { it.taxTriggerPoint }
        return when {
            // If there are any Invoice or null in list, answer Invoice
            taxTriggerPointList.any { it == VatTriggerPoint.Invoice.name || it.isNullOrEmpty() } -> VatTriggerPoint.Invoice.name
            // If there is Payment in list, answer Payment
            taxTriggerPointList.any { it == VatTriggerPoint.Payment.name } -> VatTriggerPoint.Payment.name
            // If all vat trigger point are None, answer None
            taxTriggerPointList.all { it == VatTriggerPoint.None.name } -> VatTriggerPoint.None.name
            // return Invoice by default
            else -> VatTriggerPoint.Invoice.name
        }
    }

}