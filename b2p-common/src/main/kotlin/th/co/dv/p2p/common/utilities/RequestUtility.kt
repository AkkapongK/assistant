package th.co.dv.p2p.common.utilities

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import th.co.dv.p2p.common.constants.AUTHORIZATION
import th.co.dv.p2p.common.constants.DOT
import th.co.dv.p2p.common.models.UserAuthorization
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

class RequestUtility {

    companion object {

        private val logger: Logger = LoggerFactory.getLogger(RequestUtility::class.java)
        private val className = RequestUtility::class.java.simpleName

        // list of possible AppId key in token, higher priority key should come first.
        private val appIdFields = listOf("appid", "client_id")

        fun getCurrentPage(): String {
            val request = getCurrentRequest()

            var path = request.servletPath
            if (!StringUtility.isEmpty(request.queryString)) {
                path = path + "?" + request.queryString
            }
            return path
        }

        fun getCurrentRequest(): HttpServletRequest {
            return (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes?)?.request
                ?: throw UnsupportedOperationException("Request is not set.")
        }

        /**
         * method for get http headers
         */
        fun getCurrentHTTPHeaders(): Map<String, String> {
            val headers = mutableMapOf<String, String>()
            val currentRequest = getCurrentRequest()
            val headerNames = currentRequest.headerNames
            while (headerNames.hasMoreElements()) {
                val key = headerNames.nextElement()
                val value = currentRequest.getHeader(key)
                headers[key] = value
            }
            logger.info("$className.getCurrentHTTPHeaders.headers : $headers")
            return headers
        }

        /**
         * method for get appId or clientId from token
         *
         * when authenticate with db authentication, token will contain client_id
         * when authenticate with Azure AD, token will contain appid
         */
        fun getTokenAppId(): String? {
            val mapper = jacksonObjectMapper()
            val token = getCurrentHTTPHeaders()[AUTHORIZATION]
            // jwt format is header.payload.signature but we need only payload.
            val encodedStringPayload = token?.split(DOT)?.get(1) ?: return null
            // decode payload
            val decodedStringPayload = decode(encodedStringPayload)
            logger.debug("$className.getTokenAppId decodedStringPayload : $decodedStringPayload")

            return try {
                val dataMap = mapper.readValue(decodedStringPayload, Map::class.java)
                appIdFields.firstOrNull {
                    dataMap[it] != null
                }?.let { dataMap[it] as String }
            } catch (e: Exception) {
                logger.error("$className.getTokenAppId error : ", e)
                null
            }
        }

        fun getCurrentSession(): HttpSession {
            return getCurrentRequest().session
        }

        fun getContextPath(): String {
            val request = getCurrentRequest()
            (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
            return request.contextPath
        }

        fun getContextUrl(path: String): String {
            var contextUrl = path

            val request = getCurrentRequest()
            contextUrl = if (path.startsWith("/")) {
                request.contextPath + contextUrl
            } else {
                request.contextPath + "/" + contextUrl
            }
            return contextUrl
        }

        /**
         * Get authorization detail from current user
         */
        fun getUserAuthorization(): UserAuthorization {
            // Since we already check authorization in API GATEWAY and we want to skip process when get user information
            // so we won't send any token or auth when send request between service
            // we will grant INTERFACE ROLE to ANONYMOUS authentication (no auth)
            return SecurityContextHolder.getContext().authentication.principal.let { principal ->
                if (principal.toString() == AuthorizationUtils.ANONYMOUS_USER) {
                    AuthorizationUtils.INTERFACE_AUTHORIZATION
                } else {
                    principal as UserAuthorization
                }
            }
        }

        /**
         * method for decode
         * @param encodedString encoded string
         */
        fun decode(encodedString: String): String = String(Base64.getUrlDecoder().decode(encodedString))

        fun getSponsorHeaderOrNull(): String? {
            return try {
                getCurrentRequest().getHeader(RestServiceUtilities.HEADER_SPONSOR)
            } catch (e: Exception) {
                null
            }
        }
    }

}