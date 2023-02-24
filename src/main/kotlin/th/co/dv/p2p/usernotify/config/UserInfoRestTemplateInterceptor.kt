package th.co.dv.p2p.usernotify.config

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import th.co.dv.p2p.common.utilities.RestServiceUtilities
import th.co.dv.p2p.usernotify.utility.web.RequestUtility

class UserInfoRestTemplateInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        val sponsor = try {
            RequestUtility.getCurrentRequest().getHeader(RestServiceUtilities.HEADER_SPONSOR)
        } catch (e: Exception) {
            null
        }
        sponsor?.let { request.headers.add(RestServiceUtilities.HEADER_SPONSOR, sponsor) }
        return execution.execute(request, body)
    }
}