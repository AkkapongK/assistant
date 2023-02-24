package th.co.dv.p2p.common.utilities

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import org.junit.Before
import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.AUTHORIZATION
import java.util.*
import javax.servlet.http.HttpServletRequest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RequestUtilityTest {
    // class for mock headerNames
    private class HeaderNamesEnumeration(headerIterator: Iterator<String>) : Enumeration<String> {
        private var headerIterator: Iterator<String>? = headerIterator

        override fun hasMoreElements(): Boolean {
            return headerIterator!!.hasNext()
        }

        override fun nextElement(): String {
            return headerIterator!!.next()
        }

    }


    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun `Test getCurrentHTTPHeaders`() {
        // mock RequestUtility
        mockkObject(RequestUtility)
        // mock expected header
        val headers = mapOf(
            "Content-Type" to "text/html",
            "Content-Length" to "2"
        )
        val headerNames = HeaderNamesEnumeration(headers.keys.iterator())
        val httpRequest = mockk<HttpServletRequest>()

        every { RequestUtility.getCurrentRequest() } returns httpRequest
        every { httpRequest.headerNames } returns headerNames
        headers.forEach { (key, value) ->
            every { httpRequest.getHeader(key) } returns value
        }

        val result = RequestUtility.getCurrentHTTPHeaders()
        assertEquals(headers, result)
    }

    @Test
    fun `Test getTokenAppId`() {
        // mock RequestUtility
        mockkObject(RequestUtility)

        val token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJleHAiOjE1ODg4MzYyNDAsInVzZXJfbmFtZSI6InRlc3RhZG0xIiwiYXV0aG9yaXRpZXMiOlsiVVNFUiJdLCJqdGkiOiJkMTEyYTg0Mi05YTQ0LTRmYjItODgyMi1hZjBiNzgwMDJhNjMiLCJjbGllbnRfaWQiOiJjb3JkYXVpIiwic2NvcGUiOlsic2VydmVyIl19." +
                "ZREYBt9TXekWBzaGMcDOflm-BR85-4U0Gq4tIOL-neQiV3zEv6exG127kLn_aCFmiBvRiK7GUBLMkQ_3F7n5IcJpN17N-PRidvGx2Y3KBKatsfckX0ogoGZUmiF7Mazao3NoopVSGxH4tEffhi2EANmyVXQjdZe5x_6qDpqSk4lrrqduXDX5kUiuTercPmykp1L52n3g9CId0VFR9_IdXZoS0GS8BVQ5XhQAVT4Q5RLfxnYYb_cnTyEq0Oj_pvpWcxX--vKm12LNPDyvcKJiR0tD8fdf-nxZaaJuYpF2WTUDC08Im4ulc2QCjZ08gi9LFNSeHVB9BiAM10YXCkj7pw"

        // Case null
        every { RequestUtility.getCurrentHTTPHeaders() } returns mapOf()
        var result = Try.on {
            RequestUtility.getTokenAppId()
        }
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())

        // Case Exception
        val mapper = mockk<ObjectMapper>()
        every { mapper.readValue(any<String>(),any<Class<*>>()) } throws Exception("Test Exception")
        result = Try.on {
            RequestUtility.getTokenAppId()
        }
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())

        // Case Success, client_id
        every { RequestUtility.getCurrentHTTPHeaders() } returns mapOf(AUTHORIZATION to token)
        every { RequestUtility.decode(any()) } returns " {\"exp\":1588836240,\"user_name\":\"testadm1\",\"authorities\":[\"USER\"],\"jti\":\"d112a842-9a44-4fb2-8822-af0b78002a63\",\"client_id\":\"cordaui\",\"scope\":[\"server\"]}"

        result = Try.on {
            RequestUtility.getTokenAppId()
        }
        assertTrue(result.isSuccess)
        assertEquals("cordaui", result.getOrThrow())

        // Case Success, app_id
        every { RequestUtility.getCurrentHTTPHeaders() } returns mapOf(AUTHORIZATION to token)
        every { RequestUtility.decode(any()) } returns " {\"exp\":1588836240,\"user_name\":\"testadm1\",\"authorities\":[\"USER\"],\"jti\":\"d112a842-9a44-4fb2-8822-af0b78002a63\",\"client_id\":\"cordaui\",\"scope\":[\"server\"],\"appid\":\"cordaui-appid\"}"

        result = Try.on {
            RequestUtility.getTokenAppId()
        }
        assertTrue(result.isSuccess)
        assertEquals("cordaui-appid", result.getOrThrow())

        // Case not found
        every { RequestUtility.getCurrentHTTPHeaders() } returns mapOf(AUTHORIZATION to token)
        every { RequestUtility.decode(any()) } returns " {\"exp\":1588836240,\"user_name\":\"testadm1\",\"authorities\":[\"USER\"],\"jti\":\"d112a842-9a44-4fb2-8822-af0b78002a63\",\"clientid\":\"cordaui\",\"scope\":[\"server\"],\"appids\":\"cordaui-appid\"}"

        result = Try.on {
            RequestUtility.getTokenAppId()
        }
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())

        unmockkObject(RequestUtility)
    }

    @Test
    fun `Test decode`() {
        // mock RequestUtility
        val encodedString = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9"
        val expectedResult = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}"
        val result = RequestUtility.decode(encodedString)
        assertEquals(expectedResult, result)
    }

}