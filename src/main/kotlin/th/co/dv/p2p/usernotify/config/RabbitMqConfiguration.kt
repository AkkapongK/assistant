package th.co.dv.p2p.usernotify.config

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory

abstract class RabbitMqConfiguration {
    var host: String = "localhost"
    var port: Int = 5672
    lateinit var username: String
    lateinit var password: String

    fun getConnectionFactory(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.host = host
        connectionFactory.port = port
        connectionFactory.username = username
        connectionFactory.setPassword(password)
        return connectionFactory
    }
}