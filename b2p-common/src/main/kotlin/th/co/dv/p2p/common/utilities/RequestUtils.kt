package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import th.co.dv.p2p.common.exceptions.AuthorizationException
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.corda.base.models.RequestModel
import java.util.*
import javax.servlet.http.HttpServletRequest


object RequestUtils {

    internal val logger: Logger = LoggerFactory.getLogger(RequestUtils::class.java)
    internal val className = RequestUtils::class.java.simpleName

    /**
     * Method to update request status for front-end
     * it will map lifecycle to front-end status and put it in field status instead of status from micro-service
     */
    fun updateRequestStatus(requests: List<RequestModel>): List<RequestModel> {
        return requests.map { request ->
            val item = request.requestItems.map { it.display() }
            request.display().updateItem(item)
        }
    }

    /**
     * Get header
     */
    fun getHeaders(request: HttpServletRequest): String {
        val headers = request.headerNames
        val outputs = mutableMapOf<String, String>()

        headers.asIterator().forEach {
            outputs.put(it, request.getHeader(it))
        }
        return outputs.toString()
    }

    /**
     * Get Body
     */
    fun getBody(request: HttpServletRequest): String {
        val buffer = StringBuilder()
        val reader = request.reader
        var line: String?
        while ((reader.readLine().also { line = it }) != null) {
            buffer.append(line);
            buffer.append(System.lineSeparator());
        }
        val data = buffer.toString()
        return data
    }

    private fun Enumeration<String>.asIterator(): Iterator<String> {
        return object : MutableIterator<String> {
            override fun hasNext(): Boolean {
                return this@asIterator.hasMoreElements()
            }

            override fun next(): String {
                return this@asIterator.nextElement()
            }

            override fun remove() {
                TODO("Not yet implemented")
            }
        }
    }

    fun getUserAuthorizationOrDefault(default: UserAuthorization): UserAuthorization {
        return SecurityContextHolder.getContext().authentication?.principal?.let { principal ->
            if (principal.toString() == AuthorizationUtils.ANONYMOUS_USER) {
                throw AuthorizationException(AuthorizationUtils.ANONYMOUS_USER)
            } else {
                principal as UserAuthorization
            }
        } ?: default
    }

}