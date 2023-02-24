package th.co.dv.p2p.usernotify.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import th.co.dv.p2p.usernotify.config.ApiErrorHandler
import th.co.dv.p2p.usernotify.config.AuthenticationProperties
import th.co.dv.p2p.usernotify.utility.RestServiceUtilities.ACCESS_TOKEN
import th.co.dv.p2p.usernotify.utility.RestServiceUtilities.CLIENT_CREDENTIAL
import th.co.dv.p2p.usernotify.utility.RestServiceUtilities.EXPIRES_ON
import th.co.dv.p2p.usernotify.utility.RestServiceUtilities.HEADER_AUTHORIZATION
import th.co.dv.p2p.usernotify.utility.RestServiceUtilities.buildAuthorizationHeader
import th.co.dv.p2p.usernotify.utility.RestServiceUtilities.isTokenExpired
import java.net.URI
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * A base service to make api calls with authorization
 */
@Service
class RestService {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RestService::class.java)
        private val className: String = RestService::class.java.simpleName

        /**
         * Method to build rest template
         */
        fun buildRestTemplate(authenticationProperties: AuthenticationProperties): RestTemplate {
            return RestTemplateBuilder()
                .interceptors(RequestResponseLoggingInterceptor())
                .errorHandler(ApiErrorHandler())
                .setReadTimeout(Duration.ofMinutes(authenticationProperties.timeout.toLong()))
                .setConnectTimeout(Duration.ofMinutes(authenticationProperties.timeout.toLong()))
                .build()
        }

        /**
         * Method to get access token from authorization service
         */
        fun requestServiceToken(authenticationProperties: AuthenticationProperties): HttpHeaders {
            val restTemplate = buildRestTemplate(authenticationProperties)
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            headers.setBasicAuth(authenticationProperties.clientId!!, authenticationProperties.clientSecret!!)
            val httpEntity = HttpEntity(CLIENT_CREDENTIAL, headers)
            val response = restTemplate.exchange(
                authenticationProperties.accessTokenUri!!,
                HttpMethod.POST,
                httpEntity,
                object : ParameterizedTypeReference<Map<String, Any?>>() {}
            ).body!!
            logger.info("$className.requestAuthorization response: $response")

            val toSendHeaders = HttpHeaders()
            toSendHeaders[HEADER_AUTHORIZATION] = response[ACCESS_TOKEN]?.toString()?.let { buildAuthorizationHeader(it) }
            return toSendHeaders
        }
    }

    @Autowired
    lateinit var authenticationProperties: AuthenticationProperties

    private var serviceAuthorization = mapOf<String, Any?>()

    fun <T> exchange(
        url: String,
        method: HttpMethod,
        requestEntity: HttpEntity<*>?,
        responseType: ParameterizedTypeReference<T>
    ): ResponseEntity<T> {

        val restTemplate = buildRestTemplate(authenticationProperties)

        return restTemplate.exchange(
            url,
            method,
            requestEntity,
            responseType
        )
    }

    /**
     * Method to do RestTemplate.exchange with url type string
     */
    fun <T> exchange(
        url: String,
        method: HttpMethod,
        requestEntity: HttpEntity<*>?,
        responseType: ParameterizedTypeReference<T>,
        enforceServiceAccessToken: Boolean = false
    ): ResponseEntity<T> {

        val restTemplate = buildRestTemplate(authenticationProperties)
        val finalHttpEntity = buildHttpRequestWithToken(requestEntity, enforceServiceAccessToken)

        return restTemplate.exchange(
            url,
            method,
            finalHttpEntity,
            responseType
        )
    }

    /**
     * Method to do RestTemplate.exchange with url type URI
     */
    fun <T> exchange(
        url: URI,
        method: HttpMethod,
        requestEntity: HttpEntity<*>?,
        responseType: ParameterizedTypeReference<T>,
        enforceServiceAccessToken: Boolean = false
    ): ResponseEntity<T> {

        val restTemplate = buildRestTemplate(authenticationProperties)
        val finalHttpEntity = buildHttpRequestWithToken(requestEntity, enforceServiceAccessToken)

        return restTemplate.exchange(
            url,
            method,
            finalHttpEntity,
            responseType
        )
    }

    /**
     * Method to build http request with token
     * First get authorization from HttpServletRequest and build http header with authorization
     */
    private fun <T> buildHttpRequestWithToken(
        requestEntity: HttpEntity<T>?,
        enforceServiceAccessToken: Boolean
    ): HttpEntity<T?> {
        val accessToken = null
        val authorizationHeader = buildAuthorizationHeader(accessToken)
        val headers = HttpHeaders.writableHttpHeaders(requestEntity?.headers ?: HttpHeaders())
        authorizationHeader?.let { headers.add(HEADER_AUTHORIZATION, it) }

        return HttpEntity(requestEntity?.body, headers)
    }


    /**
     * Method for get service access token and save new authorization for check expire
     */
    private fun getServiceAccessToken(): String {
        if (isRequireToRequestAuthorization()) serviceAuthorization = requestAuthorization()
        return serviceAuthorization[ACCESS_TOKEN].toString()
    }

    /**
     * Method for check is require to request authorization from authorization service
     * by checking token expired and if have any exception will return true
     */
    private fun isRequireToRequestAuthorization(): Boolean {
        return try {
            if (serviceAuthorization[ACCESS_TOKEN] == null) return true
            val expiresOn = serviceAuthorization[EXPIRES_ON].toString().toLong()
            isTokenExpired(expiresOn, TimeUnit.MINUTES.toSeconds(authenticationProperties.expiryThreshold.toLong()))
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Method to get access token from authorization service
     */
    private fun requestAuthorization(): Map<String, Any?> {
        val restTemplate = buildRestTemplate(authenticationProperties)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.setBasicAuth(authenticationProperties.clientId!!, authenticationProperties.clientSecret!!)
        val httpEntity = HttpEntity(CLIENT_CREDENTIAL, headers)
        val response = restTemplate.exchange(
            authenticationProperties.accessTokenUri!!,
            HttpMethod.POST,
            httpEntity,
            object : ParameterizedTypeReference<Map<String, Any?>>() {}
        ).body!!
        logger.debug("$className.requestAuthorization response: $response")
        return response
    }

}