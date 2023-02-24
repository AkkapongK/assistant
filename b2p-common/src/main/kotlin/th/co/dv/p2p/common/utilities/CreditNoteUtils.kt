package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.corda.base.models.CreditNoteStatus

object CreditNoteUtils {

    internal val logger: Logger = LoggerFactory.getLogger(CreditNoteUtils::class.java)
    internal val className = CreditNoteUtils::class.java.simpleName

    /**
     * Receives a list of statuses that we then parse into backend lifecycles
     * @param [creditStatuses] list of credit note statuses to be translated
     * @param [matchingStatus] list of matching status to be translated
     * @param [isSeller] which map to use for translation
     * @return Pair of backend lifecycle for credit note status and matching status and flag that let us known the target credit note have been submitted to RD or not
     */
    fun parseStatus(creditStatuses: List<String>?,
                    matchingStatus: List<String>?,
                    isSeller: Boolean): Pair<Pair<Set<String>?, Set<String>?>, Boolean?> {

        AuthorizationUtils.logger.info("${AuthorizationUtils.className}.parseStatus : creditStatuses $creditStatuses  matchingStatus $matchingStatus, isSellerStatus: $isSeller")

        var searchRequestToCancel = false
        var searchRequestToResubmit = false

        val translatedMatchingStatus = matchingStatus?.flatMap { status ->
            CreditNoteStatus.Matcher().fromDisplayName(status).map { matchingStatusMapping ->
                when(status) {
                    CreditNoteStatus.REJECTED_AFTER_RD_SUBMITTED -> searchRequestToCancel = true
                    CreditNoteStatus.REJECTED_BEFORE_RD_SUBMITTED_MATCHER -> searchRequestToResubmit = true
                }
                matchingStatusMapping.key
            }
        }?.distinct()?.toSet()

        // To determine which map to use based on restrictedFlag
        val creditNoteStatusTranslator = if (isSeller) {
            CreditNoteStatus.Seller()
        } else {
            CreditNoteStatus.Buyer()
        }

        val translatedCreditNoteStatuses = creditStatuses?.flatMap { status ->
            creditNoteStatusTranslator.fromDisplayName(status).map { creditNoteStatusMapping ->
                when(status) {
                    CreditNoteStatus.REJECTED_AFTER_RD_SUBMITTED -> searchRequestToCancel = true
                    CreditNoteStatus.REJECTED_BEFORE_RD_SUBMITTED -> searchRequestToResubmit = true
                }
                creditNoteStatusMapping.key
            }
        }?.distinct()?.toSet()

        val isRdSubmitted = when {
            searchRequestToCancel && !searchRequestToResubmit -> true
            !searchRequestToCancel && searchRequestToResubmit -> false
            else -> null
        }

        val result = Pair(translatedCreditNoteStatuses, translatedMatchingStatus)
        return result to isRdSubmitted
    }
}
