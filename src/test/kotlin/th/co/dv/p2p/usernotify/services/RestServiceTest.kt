package th.co.dv.p2p.usernotify.services

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import th.co.dv.p2p.common.models.ResponseModel
import th.co.dv.p2p.common.utilities.RestServiceUtilities
import th.co.dv.p2p.common.utilities.SponsorContextHolder
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import th.co.dv.p2p.usernotify.config.AuthenticationProperties
import th.co.dv.p2p.usernotify.utility.web.RequestUtility
import javax.servlet.http.HttpServletRequest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RestServiceTest {

    @MockK
    lateinit var authenticationProperties: AuthenticationProperties

    @InjectMockKs
    lateinit var restService: RestService

    @Before
    fun setup() = MockKAnnotations.init(this)

    private val url = "http://localhost"
    private val mockHttpEntity = HttpEntity("")
    private val expectedResponse = ResponseEntity(
        ResponseModel(data = PurchaseOrderModel()),
        HttpStatus.OK
    )

    @Test
    fun `Test exchange string`() {
        mockkObject(RestService)
        val restService = spyk<RestService>(recordPrivateCalls = true)
        val restTemplate = mockk<RestTemplate>(relaxed = true)

        // Case dont send enforceServiceAccessToken
        every { restService.authenticationProperties } returns authenticationProperties
        every { RestService.buildRestTemplate(authenticationProperties) } returns restTemplate
        every { restService["buildHttpRequestWithToken"](mockHttpEntity, false) } returns mockHttpEntity
        every { restService["addSponsorHeader"](mockHttpEntity) } returns mockHttpEntity
        every {
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                mockHttpEntity,
                object : ParameterizedTypeReference<ResponseModel<PurchaseOrderModel>>() {}
            )
        } returns expectedResponse

        var result = restService.exchange(
            url,
            HttpMethod.GET,
            mockHttpEntity,
            object : ParameterizedTypeReference<ResponseModel<PurchaseOrderModel>>() {}
        )
        assertEquals(expectedResponse, result)
        verify(exactly = 1) { RestService.buildRestTemplate(authenticationProperties) }
        verify(exactly = 1) { restService["buildHttpRequestWithToken"](mockHttpEntity, false) }
        verify(exactly = 1) { restService["addSponsorHeader"](mockHttpEntity) }
        clearMocks(restService, RestService, answers = false)

        // Case send enforceServiceAccessToken
        every { restService["buildHttpRequestWithToken"](mockHttpEntity, true) } returns mockHttpEntity
        result = restService.exchange(
            url,
            HttpMethod.GET,
            mockHttpEntity,
            object : ParameterizedTypeReference<ResponseModel<PurchaseOrderModel>>() {},
            true
        )
        assertEquals(expectedResponse, result)
        verify(exactly = 1) { RestService.buildRestTemplate(authenticationProperties) }
        verify(exactly = 1) { restService["buildHttpRequestWithToken"](mockHttpEntity, true) }
        verify(exactly = 1) { restService["addSponsorHeader"](mockHttpEntity) }
        unmockkAll()
    }

    @Test
    fun `Test exchange uri`() {
        mockkObject(RestService)
        val restService = spyk<RestService>(recordPrivateCalls = true)
        val restTemplate = mockk<RestTemplate>(relaxed = true)
        val uri = UriComponentsBuilder.fromHttpUrl(url).build().toUri()

        // Case dont send enforceServiceAccessToken
        every { restService.authenticationProperties } returns authenticationProperties
        every { RestService.buildRestTemplate(authenticationProperties) } returns restTemplate
        every { restService["buildHttpRequestWithToken"](mockHttpEntity, false) } returns mockHttpEntity
        every { restService["addSponsorHeader"](mockHttpEntity) } returns mockHttpEntity
        every {
            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                mockHttpEntity,
                object : ParameterizedTypeReference<ResponseModel<PurchaseOrderModel>>() {}
            )
        } returns expectedResponse

        var result = restService.exchange(
            uri,
            HttpMethod.GET,
            mockHttpEntity,
            object : ParameterizedTypeReference<ResponseModel<PurchaseOrderModel>>() {}
        )
        assertEquals(expectedResponse, result)
        verify(exactly = 1) { RestService.buildRestTemplate(authenticationProperties) }
        verify(exactly = 1) { restService["buildHttpRequestWithToken"](mockHttpEntity, false) }
        verify(exactly = 1) { restService["addSponsorHeader"](mockHttpEntity) }
        clearMocks(restService, RestService, answers = false)

        // Case send enforceServiceAccessToken
        every { restService["buildHttpRequestWithToken"](mockHttpEntity, true) } returns mockHttpEntity
        result = restService.exchange(
            uri,
            HttpMethod.GET,
            mockHttpEntity,
            object : ParameterizedTypeReference<ResponseModel<PurchaseOrderModel>>() {},
            true
        )
        assertEquals(expectedResponse, result)
        verify(exactly = 1) { RestService.buildRestTemplate(authenticationProperties) }
        verify(exactly = 1) { restService["buildHttpRequestWithToken"](mockHttpEntity, true) }
        verify(exactly = 1) { restService["addSponsorHeader"](mockHttpEntity) }
        unmockkAll()
    }

    @Test
    fun `Test buildHttpRequestWithToken`() {
        val restService = spyk<RestService>(recordPrivateCalls = true)
        mockkObject(RestServiceUtilities)

        // Case authorizationHeader = null
        every { restService["getAccessToken"](any<Boolean>()) } returns null
        every { RestServiceUtilities.buildAuthorizationHeader(null) } returns null

        var result = ReflectionTestUtils.invokeMethod<HttpEntity<*>>(
            restService,
            "buildHttpRequestWithToken",
            HttpEntity(""),
            true
        )!!
        assertEquals("", result.body)
        assertNull(result.headers.getFirst(RestServiceUtilities.HEADER_AUTHORIZATION))


        // Case authorizationHeader != null
        every { restService["getAccessToken"](any<Boolean>()) } returns "token"
        every { RestServiceUtilities.buildAuthorizationHeader("token") } returns "Bearer token"

        result = ReflectionTestUtils.invokeMethod<HttpEntity<*>>(
            restService,
            "buildHttpRequestWithToken",
            HttpEntity(""),
            true
        )!!
        assertEquals("", result.body)
        assertEquals("Bearer token", result.headers.getFirst(RestServiceUtilities.HEADER_AUTHORIZATION))

        unmockkObject(RestServiceUtilities)
    }

    @Test
    fun `Test getAccessToken`() {
        val restService = spyk<RestService>(recordPrivateCalls = true)
        mockkObject(RequestUtility)

        // enforceServiceAccessToken = true
        var token = "token from getServiceAccessToken()"
        every { restService["getServiceAccessToken"]() } returns token

        var result = ReflectionTestUtils.invokeMethod<String>(restService, "getAccessToken", true)
        assertEquals("token from getServiceAccessToken()", result)
        verify(exactly = 0) { RequestUtility.getCurrentRequest() }
        verify(exactly = 1) { restService["getServiceAccessToken"]() }
        clearMocks(restService, RequestUtility, answers = false)

        // Case enforceServiceAccessToken = false and no request
        every { RequestUtility.getCurrentRequest() } throws UnsupportedOperationException("Request is not set.")
        result = ReflectionTestUtils.invokeMethod(restService, "getAccessToken", false)
        assertEquals("token from getServiceAccessToken()", result)
        verify(exactly = 1) { RequestUtility.getCurrentRequest() }
        verify(exactly = 1) { restService["getServiceAccessToken"]() }
        clearMocks(restService, RequestUtility, answers = false)

        // Case enforceServiceAccessToken = false and request header has no authorization
        val httpServletRequest = mockk<HttpServletRequest>()
        every { RequestUtility.getCurrentRequest() } returns httpServletRequest
        every { httpServletRequest.getHeader(RestServiceUtilities.HEADER_AUTHORIZATION) } returns null

        result = ReflectionTestUtils.invokeMethod(restService, "getAccessToken", false)
        assertNull(result)
        verify(exactly = 1) { RequestUtility.getCurrentRequest() }
        verify(exactly = 0) { restService["getServiceAccessToken"]() }
        clearMocks(restService, RequestUtility, answers = false)

        // Case enforceServiceAccessToken = false and request header has authorization
        token = "token"
        val header = HttpHeaders()
        header.add(RestServiceUtilities.HEADER_AUTHORIZATION, "Bearer $token")
        every { httpServletRequest.getHeader(RestServiceUtilities.HEADER_AUTHORIZATION) } returns token

        result = ReflectionTestUtils.invokeMethod(restService, "getAccessToken", false)
        assertEquals("token", result)
        verify(exactly = 1) { RequestUtility.getCurrentRequest() }
        verify(exactly = 0) { restService["getServiceAccessToken"]() }
    }

    @Test
    fun `Test requestAuthorization`() {
        mockkObject(RestService)
        val restService = spyk<RestService>(recordPrivateCalls = true)
        val restTemplate = mockk<RestTemplate>(relaxed = true)

        every { restService.authenticationProperties } returns authenticationProperties
        every { authenticationProperties.clientId } returns "clientId"
        every { authenticationProperties.clientSecret } returns "clientSecret"
        every { authenticationProperties.accessTokenUri } returns url

        every { RestService.buildRestTemplate(authenticationProperties) } returns restTemplate

        val response = mapOf<String, Any?>(RestServiceUtilities.ACCESS_TOKEN to "token")
        val resultFromAuth = ResponseEntity(response, HttpStatus.OK)
        every {
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                any(),
                object : ParameterizedTypeReference<Map<String, Any?>>() {}
            )
        } returns resultFromAuth

        val result = ReflectionTestUtils.invokeMethod<Map<String, Any?>>(restService, "requestAuthorization")
        assertEquals(response, result)
        unmockkAll()
    }

    @Test
    fun `test getServiceAccessToken`() {
        val restService = spyk<RestService>(recordPrivateCalls = true)
        ReflectionTestUtils.setField(
            restService,
            "serviceAuthorization",
            mapOf<String, Any?>(RestServiceUtilities.ACCESS_TOKEN to "aaaaa")
        )

        // Case is require to request
        every { restService["isRequireToRequestAuthorization"]() } returns true
        every { restService["requestAuthorization"]() } returns mapOf<String, Any?>(RestServiceUtilities.ACCESS_TOKEN to 99)
        var result = ReflectionTestUtils.invokeMethod<String>(restService, "getServiceAccessToken")
        assertEquals("99", result)
        assertEquals(
            mutableMapOf<String, Any?>(RestServiceUtilities.ACCESS_TOKEN to 99),
            ReflectionTestUtils.getField(restService, "serviceAuthorization")
        )
        verify(exactly = 1) { restService["requestAuthorization"]() }

        // Case is not require to request && test field serviceAuthorization will be same as case above
        every { restService["isRequireToRequestAuthorization"]() } returns false
        result = ReflectionTestUtils.invokeMethod(restService, "getServiceAccessToken")
        assertEquals("99", result)
        assertEquals(
            mutableMapOf<String, Any?>(RestServiceUtilities.ACCESS_TOKEN to 99),
            ReflectionTestUtils.getField(restService, "serviceAuthorization")
        )
        verify(exactly = 1) { restService["requestAuthorization"]() }
    }

    @Test
    fun `test isRequireToRequestAuthorization`() {
        val restService = spyk<RestService>(recordPrivateCalls = true)
        mockkObject(RestServiceUtilities)

        // Case call first time (token is null)
        var result = ReflectionTestUtils.invokeMethod<Boolean>(restService, "isRequireToRequestAuthorization")
        assertTrue(result!!)

        // Case require
        every { restService.authenticationProperties } returns authenticationProperties
        every { authenticationProperties.expiryThreshold } returns 5
        ReflectionTestUtils.setField(
            restService,
            "serviceAuthorization",
            mapOf<String, Any?>(RestServiceUtilities.ACCESS_TOKEN to "aaa", RestServiceUtilities.EXPIRES_ON to "123456")
        )
        every { RestServiceUtilities.isTokenExpired(123456L, 300) } returns false
        result = ReflectionTestUtils.invokeMethod(restService, "isRequireToRequestAuthorization")
        assertFalse(result!!)

        // Case error can not cast to Long
        ReflectionTestUtils.setField(
            restService,
            "serviceAuthorization",
            mapOf<String, Any?>(RestServiceUtilities.ACCESS_TOKEN to "bbb", RestServiceUtilities.EXPIRES_ON to "aaaa")
        )
        result = ReflectionTestUtils.invokeMethod(restService, "isRequireToRequestAuthorization")
        assertTrue(result!!)

        unmockkObject(RestServiceUtilities)
    }

    @Test
    fun `test addSponsorHeader`(){
        mockkObject(SponsorContextHolder)

        every { SponsorContextHolder.getCurrentSponsor() } returns "currentSponsor"
        val expectHeaders = HttpHeaders()
        expectHeaders.add(RestServiceUtilities.HEADER_SPONSOR, "currentSponsor")
        val expectHttpEntity = HttpEntity(mockHttpEntity.body, expectHeaders)
        var result = ReflectionTestUtils.invokeMethod<HttpEntity<*>>(restService, "addSponsorHeader", mockHttpEntity)
        assertEquals(expectHttpEntity, result)

        every { SponsorContextHolder.getCurrentSponsor() } returns null
        result = ReflectionTestUtils.invokeMethod(restService, "addSponsorHeader", mockHttpEntity)
        assertEquals(mockHttpEntity, result)

        unmockkObject(SponsorContextHolder)
    }
}