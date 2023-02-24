package th.co.dv.p2p.usernotify.config

import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import th.co.dv.p2p.common.config.CachedBodyHttpServletRequest
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest


@Component
class CachingRequestBodyFilter : GenericFilterBean() {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val currentRequest = servletRequest as HttpServletRequest
        val wrappedRequest = CachedBodyHttpServletRequest(currentRequest)
        chain.doFilter(wrappedRequest, servletResponse)
    }
}