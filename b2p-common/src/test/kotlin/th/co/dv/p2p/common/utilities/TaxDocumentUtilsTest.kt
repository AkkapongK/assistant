package th.co.dv.p2p.common.utilities

import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TaxDocumentUtilsTest {

    @Test
    fun testParseStatus() {
        val taxDocumentUtils = spyk<TaxDocumentUtils>()

        // list of status is null
        var result = taxDocumentUtils.parseStatus(null)
        assertNull(result)

        // find multiple status
        val listOfStatus = listOf("Submitted", "Rejected")
        val expectedResult = setOf("ISSUED", "REJECTED")
        result = taxDocumentUtils.parseStatus(listOfStatus)

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(expectedResult, result)
    }

}