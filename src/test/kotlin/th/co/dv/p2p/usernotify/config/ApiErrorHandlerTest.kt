package th.co.dv.p2p.usernotify.config

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.springframework.http.client.ClientHttpResponse

class ApiErrorHandlerTest {

    @Test
    fun testHasError() {
        val apiErrorHandler = spyk<ApiErrorHandler>()
        val response = mockk<ClientHttpResponse>()

        // Case have error
        every { response.statusCode.is2xxSuccessful } returns false
        var result = apiErrorHandler.hasError(response)
        assertTrue(result)

        // Case no error
        every { response.statusCode.is2xxSuccessful } returns true
        result = apiErrorHandler.hasError(response)
        assertFalse(result)
    }

}