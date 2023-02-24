package th.co.dv.p2p.usernotify.kafka

import org.apache.commons.logging.LogFactory
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import th.co.dv.p2p.common.base.services.BaseProducer
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.kafka.JsonSerializer
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.kafka.models.NotifyEventModel
import th.co.dv.p2p.common.models.RetryPropertiesModel
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.common.utilities.copyPropsFrom
import th.co.dv.p2p.usernotify.config.RetryProperties

@Service
// TODO: Change service here
class ProducerServices : BaseProducer(Services.CREDIT_NOTE) {

    companion object {
        val logger = LogFactory.getLog(ProducerServices::class.java)!!
        val className = ProducerServices::class.java.simpleName
    }

    @Autowired
    lateinit var kafkaProperties: KafkaProperties

    @Autowired
    lateinit var retryProperties: RetryProperties

    val serviceName = Services.CREDIT_NOTE.name

    /**
     * init() function call when StreamingService is initiate by corda.
     * This function use to get configuration for connect to kafka server.
     */
    override fun init(): Producer<String, Any>? {
        var streamingHostValue: String? = null
        return try {
            val streamingEnabled = kafkaProperties.enabled
            logger.info("$className.init streaming enabled: $streamingEnabled")
            if (streamingEnabled) {
                streamingHostValue = kafkaProperties.streamingHost
                logger.info("$className.init streaming host: $streamingHostValue")
                val configureProducer = mutableMapOf(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to streamingHostValue,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
                )
                kafkaProperties.maxMessageSize?.let {
                    configureProducer.put(
                        ProducerConfig.MAX_REQUEST_SIZE_CONFIG,
                        kafkaProperties.maxMessageSize
                    )
                }
                kafkaProperties.maxBlockMs?.let {
                    configureProducer.put(
                        ProducerConfig.MAX_BLOCK_MS_CONFIG,
                        kafkaProperties.maxBlockMs
                    )
                }
                kafkaProperties.bufferMemory?.let {
                    configureProducer.put(
                        ProducerConfig.BUFFER_MEMORY_CONFIG,
                        kafkaProperties.bufferMemory
                    )
                }
                kafkaProperties.lingerMs?.let {
                    configureProducer.put(
                        ProducerConfig.LINGER_MS_CONFIG,
                        kafkaProperties.lingerMs
                    )
                }
                kafkaProperties.batchSize?.let {
                    configureProducer.put(
                        ProducerConfig.BATCH_SIZE_CONFIG,
                        kafkaProperties.batchSize
                    )
                }
                kafkaProperties.compressionType?.let {
                    configureProducer.put(
                        ProducerConfig.COMPRESSION_TYPE_CONFIG,
                        kafkaProperties.compressionType
                    )
                }
                kafkaProperties.enableIdempotence?.let {
                    configureProducer.put(
                        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                        kafkaProperties.enableIdempotence
                    )
                }
                kafkaProperties.ack?.let { configureProducer.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.ack) }
                kafkaProperties.retries?.let {
                    configureProducer.put(
                        ProducerConfig.RETRIES_CONFIG,
                        kafkaProperties.retries
                    )
                } ?: configureProducer.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE)
                kafkaProperties.requestTimeout?.let {
                    configureProducer.put(
                        ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                        kafkaProperties.requestTimeout
                    )
                }
                kafkaProperties.maxInFlightRequestsPerConnection?.let {
                    configureProducer.put(
                        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                        kafkaProperties.maxInFlightRequestsPerConnection
                    )
                }

                KafkaProducer(configureProducer.toMap())

            } else null
        } catch (e: Exception) {
            logger.error("$className.init Failed to connect to Streaming Service $streamingHostValue, Caused By: $e")
            // this will trigger server to restart corda services, exit code could be any positive number.
            null
        }
    }

    override fun initRetryPropertiesModel(): RetryPropertiesModel {
        val retryPropertiesModel = RetryPropertiesModel()
        retryPropertiesModel.copyPropsFrom(retryProperties)
        return retryPropertiesModel
    }

    /**
     * Method for send notify event back
     */
    fun publishNotify(
        topic: String,
        transactionId: String,
        command: String,
        notifyEventModel: NotifyEventModel
    ) {

        logger.info("$className.publishNotify Streaming notify event: $notifyEventModel")
        val streamingModel = StreamingModel(
            id = transactionId,
            command = command,
            type = serviceName,
            data = notifyEventModel
        )

        streamToKafka(topic, streamingModel)
    }

    /**
     * Method for stream event state model to queue
     *
     * @param transactionId : transaction id
     * @param relatedService : List of service that involved
     * @param topic : topic for streaming data
     * @param allStatesIn : all input state
     * @param allStatesOut : all output state
     * @param command : command for this transaction
     *
     */
    fun streamToKafkaWithTopic(
        transactionId: String,
        relatedService: List<String>,
        topic: String,
        allStatesIn: AllStates? = null,
        allStatesOut: AllStates,
        command: String
    ) {
        val streamingModel = generateStreamingModel(
            allStatesIn = allStatesIn,
            allStatesOut = allStatesOut,
            transactionId = transactionId,
            relatedService = relatedService,
            command = command
        )
        streamToKafka(topic, streamingModel)
    }

    /**
     * Method for generate streaming model
     *
     * @param transactionId : transaction id
     * @param relatedService : List of service that involved
     * @param command : command for this transaction
     * @param allStatesOut : all output state
     * @param allStatesIn : all input state
     */
    private fun generateStreamingModel(
        transactionId: String,
        relatedService: List<String>,
        command: String,
        allStatesOut: AllStates,
        allStatesIn: AllStates? = null
    ): StreamingModel<EventStateModel> {
        return StreamingModel(
            id = transactionId,
            command = command,
            type = serviceName,
            data = EventStateModel(
                previousState = allStatesIn,
                nextState = allStatesOut,
                relatedServices = relatedService
            )
        )
    }

}