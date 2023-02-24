package th.co.dv.p2p.usernotify.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.stereotype.Component
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.kafka.JsonDeserializer
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.common.utilities.DataSourceContextHolder
import th.co.dv.p2p.common.utilities.SponsorContextHolder
import th.co.dv.p2p.common.utilities.VariableContext
import th.co.dv.p2p.usernotify.config.RetryProperties

@Component
@Configuration
@EnableKafka
@ConditionalOnProperty(name = ["spring.batch.job.enabled"], havingValue = "false")
class KafkaConfig {

    companion object {
        var SYNC_GROUP = Services.USER_NOTIFY.name
        var GROUP_ACTION_HISTORY = "GROUP_ACTION_HISTORY"
    }

    @Autowired
    lateinit var properties: KafkaProperties

    @Autowired
    lateinit var springKafka: org.springframework.boot.autoconfigure.kafka.KafkaProperties

    @Autowired
    lateinit var kafkaConfig: KafkaConfig

    @Autowired
    lateinit var retryProperties: RetryProperties

    /**
     * Define consumer group Notify
     */
    @Bean
    fun notifyListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, StreamingModel<Any>> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, StreamingModel<Any>>()
        factory.consumerFactory = kafkaConfig.consumerFactoryNotifyAnyContainerFactory(SYNC_GROUP)
        factory.setRecordInterceptor { record -> listenerFactoryInterceptor(record) }
        return factory
    }

    @Bean
    fun commitListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, StreamingModel<Any>> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, StreamingModel<Any>>()
        factory.consumerFactory = kafkaConfig.consumerFactoryNotifyAnyContainerFactory(SYNC_GROUP)
        factory.setRecordInterceptor { record -> listenerFactoryInterceptor(record) }
        return factory
    }

    @Bean
    fun invoiceBroadcastListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, StreamingModel<EventStateModel>> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, StreamingModel<EventStateModel>>()
        factory.consumerFactory = kafkaConfig.consumerFactoryEventStateModel(SYNC_GROUP)
        factory.setRecordInterceptor { record -> listenerFactoryInterceptor(record) }
        return factory
    }

    /**
     * Define consumer group broadcast
     */
    @Bean
    fun broadcastListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, StreamingModel<EventStateModel>> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, StreamingModel<EventStateModel>>()
        factory.consumerFactory = kafkaConfig.consumerFactoryEventStateModel(SYNC_GROUP)
        factory.setRecordInterceptor { record -> listenerFactoryInterceptor(record) }
        return factory
    }

    /**
     * Define consumer factory (deserialize)
     */
    fun consumerFactoryEventStateModel(group: String): ConsumerFactory<String, StreamingModel<EventStateModel>> {
        return DefaultKafkaConsumerFactory(consumerConfigs(group), StringDeserializer(),
                JsonDeserializer(EventStateModel::class.java))
    }

    fun consumerFactoryNotifyAnyContainerFactory(group: String): ConsumerFactory<String, StreamingModel<Any>> {
        return DefaultKafkaConsumerFactory(consumerConfigs(group), StringDeserializer(),
            JsonDeserializer(Any::class.java))
    }

    fun consumerConfigs(group: String): Map<String, Any?> {
        val configureConsumer = mutableMapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to properties.streamingHost,
                ConsumerConfig.GROUP_ID_CONFIG to group,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java
        )
        properties.fetchMaxBytes?.let { configureConsumer.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, properties.fetchMaxBytes)}
        properties.fetchMinBytes?.let { configureConsumer.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, properties.fetchMinBytes)}
        properties.requestTimeout?.let { configureConsumer.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, properties.requestTimeout)}
        properties.sessionTimeOut?.let { configureConsumer.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, properties.sessionTimeOut)}
        properties.maxPartitionFetchByteConfig?.let { configureConsumer.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, properties.maxPartitionFetchByteConfig)}
        properties.maxPollRecord?.let { configureConsumer.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.maxPollRecord!!) }
        properties.maxPollInterval?.let { configureConsumer.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, properties.maxPollInterval!!) }
        retryProperties.retryPeriod?.let { configureConsumer.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, it) }
        retryProperties.maxRetryCount?.let { configureConsumer.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, it.times(retryProperties.retryPeriod ?: 60000 ).toInt()) }


        return springKafka.consumer.buildProperties().plus(configureConsumer)
    }

    /**
     * This method for set dataSource from StreamingModel.sponsor
     */
    private fun <K, V> listenerFactoryInterceptor(record: ConsumerRecord<K, V>): ConsumerRecord<K, V> {
        SponsorContextHolder.clear()
        DataSourceContextHolder.clear()
        VariableContext.clear()

        when (val value = record.value()) {
            is StreamingModel<*> -> {
                try {
                    val sponsor = value.sponsor!!
                    DataSourceContextHolder.setCurrentDb(sponsor.lowercase())
                    SponsorContextHolder.setSponsor(sponsor)
                } catch (e: Exception) {
                    throw SerializationException(e)
                }
            }
        }
        return record
    }
}

@Component
@ConfigurationProperties("kafka")
data class KafkaProperties(
        var streamingHost: String? = null,
        var enabled: Boolean = false,
        var maxMessageSize: Int? = null,
        var maxBlockMs: Long? = null,
        var bufferMemory: Long?  = null,
        var lingerMs: Long? = null,
        var batchSize: Int? = null,
        var compressionType: String? = null,
        var enableIdempotence: Boolean? = null,
        var ack: String? = null,
        var retries: Int? = null,
        var fetchMaxBytes: Int? = null,
        var fetchMinBytes: Int? = null,
        var requestTimeout: Int? = null,
        var maxInFlightRequestsPerConnection: Int? = null,
        var sessionTimeOut: Int? = null,
        var maxPartitionFetchByteConfig: Int? = null,
        var maxPollRecord: Int? = null,
        var maxPollInterval: Int? = null
)