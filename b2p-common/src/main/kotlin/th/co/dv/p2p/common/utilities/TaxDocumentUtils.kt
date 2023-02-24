package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.corda.base.models.TaxDocumentStatusModel

object TaxDocumentUtils {

    internal val logger: Logger = LoggerFactory.getLogger(TaxDocumentUtils::class.java)
    internal val className = TaxDocumentUtils::class.java.simpleName

    /**
     * Receives a list of statuses that we then parse into backend lifecycles
     * @param [taxDocumentStatuses] list of tax document statuses to be translated
     * @return list of backend lifecycle for tax document
     */
    fun parseStatus(taxDocumentStatuses: List<String>?): Set<String>? {
        logger.info("$className.parseStatus taxDocumentStatuses: $taxDocumentStatuses")

        val taxDocumentStatusTranslator = TaxDocumentStatusModel.StatusMapping
        val translatedTaxDocumentStatuses = taxDocumentStatuses?.flatMap { status ->
            taxDocumentStatusTranslator.fromDisplayName(status).map { it.name }
        }?.toSet()

        logger.info("$className.parseStatus translatedStatuses are the following: $translatedTaxDocumentStatuses")

        return translatedTaxDocumentStatuses
    }
}