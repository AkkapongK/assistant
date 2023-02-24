package th.co.dv.p2p.usernotify.utility.web

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import th.co.dv.p2p.common.models.UserAuthorization
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

class RequestUtility {

    companion object {

        fun getCurrentPage(): String {
            val request = getCurrentRequest()

            var path = request.servletPath
            if (request.queryString.isNullOrEmpty().not()) {
                path = path + "?" + request.queryString
            }
            return path
        }

        fun getCurrentRequest(): HttpServletRequest {
            return (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes?)?.request
                    ?: throw UnsupportedOperationException("Request is not set.")
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
            return SecurityContextHolder.getContext().authentication.principal as UserAuthorization
        }
    }

}