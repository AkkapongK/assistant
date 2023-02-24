package th.co.dv.p2p.usernotify.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.swagger.web.SecurityConfigurationBuilder
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfiguration {

    @Configuration
    @EnableSwagger2
    class SwaggerConfiguration {

        @Value("\${swagger.enabled:false}")
        val isSwaggerEnable = false

        @Bean
        fun selectApi(): Docket {
            return Docket(DocumentationType.SWAGGER_2)
                .enable(isSwaggerEnable)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .securityContexts(listOf(securityContext()))
                .securitySchemes(listOf(apiKey()))
        }

        @Bean
        fun security(): SecurityConfiguration {
            return SecurityConfigurationBuilder.builder()
                .scopeSeparator(" ")
                .build()
        }

        private fun apiKey(): ApiKey {
            return ApiKey("JWT", "Authorization", "header")
        }

        private fun securityContext(): SecurityContext {
            return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .build()
        }

        fun defaultAuth(): List<SecurityReference> {
            val authorizationScope = AuthorizationScope("global", "accessEverything")
            val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
            authorizationScopes[0] = authorizationScope
            return listOf(SecurityReference("JWT", authorizationScopes))
        }


        /**
         * Service information for swagger
         */
        private fun apiInfo(): ApiInfo {
            return ApiInfoBuilder()
                .title("Credit note service")
                .description("Swagger for Credit note")
                .build()
        }

    }

    /**
    Swagger no config for completely disable itself.

    even we disable in
    fun selectApi(): Docket {
    return Docket(DocumentationType.SWAGGER_2)
    .enable(isSwaggerEnable)
    ...

    but it still show up swagger page with empty api list.

    We don't want user to see swagger page on production and don't want to create more rule in WebSecurityConfig
    so we create controller to override with not found page in here.

     */
    @RestController
    @ConditionalOnExpression("!\${swagger.enabled:false}")
    class SwaggerController {
        @GetMapping("/swagger-ui.html")
        fun getSwagger(): ResponseEntity<Any> {
            return ResponseEntity.notFound().build()
        }
    }
}