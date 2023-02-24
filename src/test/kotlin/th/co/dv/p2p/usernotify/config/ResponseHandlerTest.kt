package th.co.dv.p2p.usernotify.config

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.test.util.ReflectionTestUtils
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.models.ErrorObject
import th.co.dv.p2p.common.models.PageModel
import th.co.dv.p2p.common.models.ResponseModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import java.lang.reflect.AnnotatedElement
import kotlin.test.assertEquals

class ResponseHandlerTest {

    @InjectMockKs
    lateinit var responseHandler: ResponseHandler

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testGetService() {
        val result = ReflectionTestUtils.invokeMethod<Services>(responseHandler, "getService")
        assertEquals(Services.CREDIT_NOTE, result)
    }

    @Test
    fun testHandleException() {
        val ex = Exception("TEST")
        val responseHandler = spyk<ResponseHandler>()
        val response = mockk<ResponseModel<ErrorObject>>()
        every { responseHandler["getErrorResponseSpecific"](any<Exception>(), any<String>()) } returns response
        val result = responseHandler.handleException(ex)
        assertEquals(response, result)
    }

    @Test
    fun testSupports() {
        val returnType = mockk<MethodParameter>()
        val converterType = mockk<HttpMessageConverter<*>>()
        val result = ReflectionTestUtils.invokeMethod<Boolean>(responseHandler, "supports", returnType, converterType::class.java)!!
        assertTrue(result)
    }

    @Test
    fun testBeforeBodyWrite() {
        val body = "Test"
        val returnType = mockk<MethodParameter>()
        val selectedContentType =  mockk<MediaType>()
        val selectedConverterType = mockk<HttpMessageConverter<*>>()
        val request = mockk<ServerHttpRequest>()
        val response = mockk<ServerHttpResponse>()
        val annotatedElement = mockk<AnnotatedElement>()

        every { returnType.annotatedElement } returns annotatedElement
        val responseHandler = spyk<ResponseHandler>()
        every { responseHandler["responseModel"](any(), any<AnnotatedElement>(), any<List<String>>()) } returns "MOCK"

        every { responseHandler["getHttpStatusFromResponseModel"]("MOCK") } returns HttpStatus.OK
        every { response.setStatusCode(any()) } just Runs

        var result = ReflectionTestUtils.invokeMethod<Any>(
                responseHandler,
                "beforeBodyWrite",
                body,
                returnType,
                selectedContentType,
                selectedConverterType::class.java,
                request,
                response
        )!!
        assertEquals("MOCK", result)
        verify(exactly = 1) { response.setStatusCode(HttpStatus.OK) }
        verify(exactly = 0) { response.setStatusCode(HttpStatus.NO_CONTENT) }
        clearMocks(response, answers = false)

        every { responseHandler["responseModel"](any(), any<AnnotatedElement>(), any<List<String>>()) } returns emptyList<Any>()
        every { responseHandler["getHttpStatusFromResponseModel"](emptyList<Any>()) } returns HttpStatus.NO_CONTENT
        every { response.setStatusCode(any()) } just Runs

        result = ReflectionTestUtils.invokeMethod<Any>(
                responseHandler,
                "beforeBodyWrite",
                body,
                returnType,
                selectedContentType,
                selectedConverterType::class.java,
                request,
                response
        )!!
        assertEquals(emptyList<Any>(), result)
        verify(exactly = 0) { response.setStatusCode(HttpStatus.OK) }
        verify(exactly = 1) { response.setStatusCode(HttpStatus.NO_CONTENT) }
    }

    @Test
    fun testGetHttpStatusFromResponseModel() {
        // case response body is null
        var responseBody:Any? = null
        var result = ReflectionTestUtils.invokeMethod<HttpStatus>(
                responseHandler,
                "getHttpStatusFromResponseModel",
                responseBody
        )
        assertEquals(HttpStatus.NO_CONTENT, result)

        // case response body is ResponseModel with valid status code
        responseBody = ResponseModel(statusCode = 500, data = ErrorObject())
        result = ReflectionTestUtils.invokeMethod(
                responseHandler,
                "getHttpStatusFromResponseModel",
                responseBody
        )
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result)

        // case response body is ResponseModel with invalid status code
        responseBody = ResponseModel(statusCode = 777, data = ErrorObject())
        result = ReflectionTestUtils.invokeMethod(
                responseHandler,
                "getHttpStatusFromResponseModel",
                responseBody
        )
        assertEquals(HttpStatus.OK, result)

        // case response body is Collection of ResponseModel
        responseBody = listOf(
                ResponseModel(statusCode = 200, data = ErrorObject()),
                ResponseModel(statusCode = 404, data = ErrorObject())
        )
        result = ReflectionTestUtils.invokeMethod(
                responseHandler,
                "getHttpStatusFromResponseModel",
                responseBody
        )
        assertEquals(HttpStatus.NOT_FOUND, result)

        // case response body is Collection of other type
        responseBody = listOf(InvoiceModel(), InvoiceModel(), InvoiceModel())
        result = ReflectionTestUtils.invokeMethod(
                responseHandler,
                "getHttpStatusFromResponseModel",
                responseBody
        )
        assertEquals(HttpStatus.OK, result)

        // case response body is of other type
        responseBody = mockk<PageModel<InvoiceModel>>()
        result = ReflectionTestUtils.invokeMethod(
                responseHandler,
                "getHttpStatusFromResponseModel",
                responseBody
        )
        assertEquals(HttpStatus.OK, result)
    }
}