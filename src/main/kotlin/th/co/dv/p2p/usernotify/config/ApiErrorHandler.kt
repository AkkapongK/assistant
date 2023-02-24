package th.co.dv.p2p.usernotify.config

import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class ApiErrorHandler : ResponseErrorHandler {

    override fun handleError(response: ClientHttpResponse) {}

    override fun hasError(response: ClientHttpResponse): Boolean {
        return response.statusCode.is2xxSuccessful.not()
    }
}