package th.co.dv.b2p.linebot.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import th.co.dv.b2p.linebot.Application

@Configuration
class LineBotConfigure: WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val downloadedContentUri: String = Application.downloadedContentDir.toUri().toASCIIString()
        registry.addResourceHandler("/downloaded/**")
                .addResourceLocations(downloadedContentUri)
    }
}