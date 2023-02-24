package th.co.dv.p2p.usernotify.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import th.co.dv.p2p.common.config.BaseInboundRequestInterceptor
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.usernotify.kafka.ProducerServices
import javax.servlet.http.HttpServletRequest

@Configuration
class WebMvcConfig : WebMvcConfigurer {

    @Autowired
    lateinit var producerServices: ProducerServices

    override fun addInterceptors(registry: InterceptorRegistry) {
        // TODO: Change service here
        registry.addInterceptor(BaseInboundRequestInterceptor(Services.USER, producerServices))
    }
}