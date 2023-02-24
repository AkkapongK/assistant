package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import th.co.dv.p2p.common.Try.Companion.on
import th.co.dv.p2p.common.models.ErrorObject
import th.co.dv.p2p.common.models.ResponseModel
import th.co.dv.p2p.corda.base.models.PageModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RestServiceUtilitiesTest {

    @Test
    fun buildAuthorizationHeader() {

        // case null
        var result = RestServiceUtilities.buildAuthorizationHeader(null)
        assertNull(result)

        // Case not start with Bearer
        var token = "token"
        result = RestServiceUtilities.buildAuthorizationHeader(token)
        assertEquals("${RestServiceUtilities.BEARER} $token", result)

        // Case start with Bearer
        token = "Bearer token2"
        result = RestServiceUtilities.buildAuthorizationHeader(token)
        assertEquals(token, result)
    }

    @Test
    fun isTokenExpired() {
        val input = Instant.now()
        // case token expired, input <= current time + 10 sec.
        var result = RestServiceUtilities.isTokenExpired(input.epochSecond, 10)
        assertTrue(result)

        // case token not expires, input > current time
        result = RestServiceUtilities.isTokenExpired(input.plusSeconds(20).epochSecond, 0)
        assertFalse(result)
    }

    @Test
    fun testHandleRequestResult() {
        val responseEntity = mockk<ResponseEntity<ResponseModel<PageModel<PurchaseOrderModel>>>>()
        every { responseEntity.body } returns ResponseModel(
            statusCode =  HttpStatus.OK.value(),
            data = PageModel(listOf(
                PurchaseOrderModel(linearId = "001")
            ))
        )

        val sendRequest = { responseEntity }

        // Case success
        val result = RestServiceUtilities.handleRequestResult(sendRequest)
        assertEquals(responseEntity.body, result)

        // Case throw error
        every { responseEntity.body } throws Exception("TEST ERROR")
        var expectedResult = on {RestServiceUtilities.handleRequestResult(sendRequest) }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("TEST ERROR"))

        // Case response failed
        every { responseEntity.body } returns ResponseModel(
            statusCode = HttpStatus.BAD_GATEWAY.value(),
            error = ErrorObject(
                code = "PO-E0001",
                message = "PO FAILED"
            )
        )
        expectedResult = on {RestServiceUtilities.handleRequestResult(sendRequest) }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("[PO-E0001] PO FAILED"))
    }

}