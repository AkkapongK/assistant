package th.co.dv.p2p.usernotify.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.nio.charset.Charset

class RequestResponseLoggingInterceptor: ClientHttpRequestInterceptor {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RequestResponseLoggingInterceptor::class.java)
    }

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        logRequest(request, body)
        val response = execution.execute(request, body)
        logResponse(response)
        return response
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        logger.info("Request URL : ${request.uri}")
        logger.info("Request Method : ${request.method}")
        logger.info("Request Header : ${request.headers}")

        val contentTypes = request.headers.contentType.toString()
        if (contentTypes.startsWith("multipart/form-data", true).not()) {
            logger.info("Request Body : ${String(body, Charset.forName("UTF-8"))}")
        }

    }

    private fun logResponse(response: ClientHttpResponse) {
        logger.info("Response Raw Status Code : ${response.rawStatusCode}")
        logger.info("Response Status Code : ${response.statusCode}")
        logger.info("Response Status Text: ${response.statusText}")
        logger.info("Response Headers: ${response.headers}")
    }

}