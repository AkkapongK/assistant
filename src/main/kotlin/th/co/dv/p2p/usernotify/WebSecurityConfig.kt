package th.co.dv.p2p.usernotify

import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import th.co.dv.p2p.common.utilities.AuthorizationUtils
import th.co.dv.p2p.usernotify.config.UserInfoRestTemplateInterceptor

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableResourceServer
@EnableOAuth2Client
class WebSecurityConfig : ResourceServerConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
//        http.authorizeRequests().anyRequest().authenticated()
//        http.cors().and().csrf().disable()
        http.authorizeRequests()
//            .antMatchers("/callback").permitAll()
//            .anyRequest().authenticated()
            .anyRequest().permitAll()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("*")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    // TODO: Can remove if don't want to build UserAuthorization
    @Bean
    fun myPrincipalExtractor(): PrincipalExtractor {
        return MyPrincipalExtractor()
    }

    @Bean
    fun userInfoRestTemplate(): UserInfoRestTemplateCustomizer {
        return UserInfoRestTemplateCustomizer { template ->
            template.interceptors.add(UserInfoRestTemplateInterceptor())
        }
    }
}

// TODO: Can remove if don't want to build UserAuthorization
class MyPrincipalExtractor : PrincipalExtractor {

    override fun extractPrincipal(map: Map<String, Any>): Any {
        //Extracts the authorities from the map with the a key
        val authoritiesExtracted = FixedAuthoritiesExtractor().extractAuthorities(map).map { it.authority }

        return AuthorizationUtils.buildUserAuthorization(map, authoritiesExtracted)

    }
}
