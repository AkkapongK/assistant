package th.co.dv.p2p.usernotify.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.stereotype.Component

@Configuration
class RedisConfig {
    @Autowired
    lateinit var springRedisProperties: SpringRedisProperties

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        return JedisConnectionFactory(RedisStandaloneConfiguration(
            springRedisProperties.host!!,
            springRedisProperties.port!!
        ))
    }

    @Primary
    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {

        val om = ObjectMapper()
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY)
        // redis serialize
        val jackson2JsonRedisSerializer: Jackson2JsonRedisSerializer<*> = Jackson2JsonRedisSerializer(Any::class.java)
        jackson2JsonRedisSerializer.setObjectMapper(om)
        val template = StringRedisTemplate(jedisConnectionFactory())
        template.keySerializer = jackson2JsonRedisSerializer
        template.hashKeySerializer = jackson2JsonRedisSerializer
        template.hashValueSerializer = jackson2JsonRedisSerializer
        template.valueSerializer = jackson2JsonRedisSerializer

        template.afterPropertiesSet()
        return template
    }
}

@Component
@ConfigurationProperties("spring.redis")
data class SpringRedisProperties (
        var host: String? = null,
        var port: Int? = null
)