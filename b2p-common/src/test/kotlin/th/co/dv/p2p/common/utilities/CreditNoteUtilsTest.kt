package th.co.dv.p2p.common.utilities

import junit.framework.TestCase
import org.junit.Test
import th.co.dv.p2p.corda.base.models.CreditNoteStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CreditNoteUtilsTest {

    @Test
    fun `Test parseStatus`() {

        // All status is null
        var result = CreditNoteUtils.parseStatus(null, null, true)

        var translatedStatuses = result.first
        var isRdSubmitted = result.second
        var translatedCreditNoteStatuses = translatedStatuses.first
        var translatedMatchingStatus = translatedStatuses.second

        TestCase.assertNull(translatedCreditNoteStatuses)
        TestCase.assertNull(translatedMatchingStatus)
        TestCase.assertNull(isRdSubmitted)

        // Send status to convert for seller status
        result = CreditNoteUtils.parseStatus(listOf("Verifying"), listOf("Submitted"), true)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedCreditNoteStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedCreditNoteStatuses)
        assertNotNull(translatedMatchingStatus)
        TestCase.assertNull(isRdSubmitted)

        assertEquals(3, translatedCreditNoteStatuses.size)
        TestCase.assertTrue(translatedCreditNoteStatuses.containsAll(listOf("PARTIAL", "MISSING", "UNMATCHED")))

        assertEquals(1, translatedMatchingStatus.size)
        assertNotNull(translatedMatchingStatus.find { it == "ISSUED" })

        // Send status to convert for buyer status
        result = CreditNoteUtils.parseStatus(listOf("Payment Failed"), null, false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedCreditNoteStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedCreditNoteStatuses)
        TestCase.assertNull(translatedMatchingStatus)
        TestCase.assertNull(isRdSubmitted)

        assertEquals(1, translatedCreditNoteStatuses.size)
        assertNotNull(translatedCreditNoteStatuses.find { it == "DECLINED" })

        // Send status to convert for not found status
        result = CreditNoteUtils.parseStatus(listOf("Payment Not Failed"), null, true)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedCreditNoteStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedCreditNoteStatuses)
        TestCase.assertNull(translatedMatchingStatus)
        TestCase.assertNull(isRdSubmitted)

        assertEquals(0, translatedCreditNoteStatuses.size)

        // Send status Request to Cancel for search credit note that have been submitted to RD
        result = CreditNoteUtils.parseStatus(listOf(CreditNoteStatus.REJECTED_AFTER_RD_SUBMITTED), null, false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedCreditNoteStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedCreditNoteStatuses)
        TestCase.assertNull(translatedMatchingStatus)
        TestCase.assertTrue(isRdSubmitted!!)

        assertEquals(1, translatedCreditNoteStatuses.size)
        assertNotNull(translatedCreditNoteStatuses.find { it == "REJECTED" })

        // Send status Request to Cancel and Request to Resubmit
        result = CreditNoteUtils.parseStatus(listOf(
            CreditNoteStatus.REJECTED_AFTER_RD_SUBMITTED,
            CreditNoteStatus.REJECTED_BEFORE_RD_SUBMITTED
        ), null, false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedCreditNoteStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedCreditNoteStatuses)
        TestCase.assertNull(translatedMatchingStatus)
        TestCase.assertNull(isRdSubmitted)

        assertEquals(1, translatedCreditNoteStatuses.size)
        assertNotNull(translatedCreditNoteStatuses.find { it == "REJECTED" })
    }
}