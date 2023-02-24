package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.enums.LoanStatus

object LoanUtils {
    internal val logger: Logger = LoggerFactory.getLogger(LoanUtils::class.java)
    internal val className = LoanUtils::class.java.simpleName

    /**
     * Receives a list of statuses that we then parse into backend lifecycles
     * @param [loanStatuses] list of loan statuses to be translated
     * @return list of backend lifecycle for loan
     */
    fun parseStatus(loanStatuses: List<String>?): Set<String>? {
        logger.info("$className.parseStatus loanStatuses: $loanStatuses")

        val translatedTaxDocumentStatuses = loanStatuses?.map { LoanStatus.findByDisplayName(it) }?.toSet()

        logger.info("$className.parseStatus translatedStatuses are the following: $translatedTaxDocumentStatuses")

        return translatedTaxDocumentStatuses
    }
}