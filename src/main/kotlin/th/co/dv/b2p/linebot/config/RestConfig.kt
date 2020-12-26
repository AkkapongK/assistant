package th.co.dv.b2p.linebot.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import th.co.dv.b2p.linebot.services.RequestResponseLoggingInterceptor

@Configuration
class RestConfig {
    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return restTemplateBuilder
                .interceptors(RequestResponseLoggingInterceptor())
                .errorHandler(ApiErrorHandler())
                .build()

    }
}