package th.co.dv.p2p.common.base.services

import org.apache.commons.logging.LogFactory
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import th.co.dv.p2p.common.base.enums.MessageType
import th.co.dv.p2p.common.constants.REQUEST_IN_COMMAND
import th.co.dv.p2p.common.constants.UNDERSCORE
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.exceptions.SendToKafkaException
import th.co.dv.p2p.common.kafka.KafkaTopicConstant.Companion.REQUEST_INPUT
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.models.LogRequestInModel
import th.co.dv.p2p.common.models.RetryPropertiesModel
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.common.utilities.LoggerUtils.END_STAGE
import th.co.dv.p2p.common.utilities.LoggerUtils.LOG_STREAMING_SERVER_COMPLETE
import th.co.dv.p2p.common.utilities.MapUtility.jacksonObjectMapperInstance
import th.co.dv.p2p.common.utilities.SponsorContextHolder
import th.co.dv.p2p.common.utilities.retry
import java.time.Duration
import java.util.*

abstract class BaseProducer(val service: Services) {

    protected val logger = LogFactory.getLog(BaseProducer::class.java)!!
    protected val className = BaseProducer::class.java.simpleName
    protected val producer by lazy { init() }
    protected val retryPropertiesModel by lazy { initRetryPropertiesModel() }

    @Value("\${kafka.maxDataSize:2}")
    private val maxDataSize: Int = 2
    private val maxDataSizeByte
        get() = getMaxSize()

    open fun getMaxSize(): Int { return maxDataSize * 1024 * 1024 }

    // TODO: Need to override
    protected abstract fun init(): Producer<String, Any>?
    // TODO: Need to override
    protected abstract fun initRetryPropertiesModel(): RetryPropertiesModel

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, String>

    /**
     * Method for create [MessageEventModel]
     *
     *  @param transactionId: Transaction id that used to refer the request
     * @param command: Command that used to create/update data
     * @param data: Updated data that we want to stream to let other service known the change
     */
    fun createMessageEventModel(transactionId: String,
                                command: String,
                                messageType: MessageType,
                                data: EventStateModel? = null,
                                message: String? = null,
                                redisKey: String? = null,
                                sponsor: String? = null,
                                relatedServices: List<Services>): StreamingModel<Any> {

        val finalRelatedServices = relatedServices.plus(service).distinct()
        return StreamingModel(
                id = transactionId,
                type = service.name,
                relatedServices = finalRelatedServices,
                command = command,
                messageType = messageType,
                message = message,
                data = data,
                sponsor = sponsor,
                redisKey = redisKey
        )
    }

    /**
     * Kafka producer send the data to kafka server.
     * @param topic: Kafka topic.
     * @param message: Data that we sent to the kafka server.
     */
    protected fun streamToKafka(topic: String, message: StreamingModel<*>) {
        logger.info("$className.streamToKafka topic $topic model: ${message.id}")
        if (message.sponsor.isNullOrBlank()) message.sponsor = SponsorContextHolder.getCurrentSponsor()
        try {
            retry(retryPropertiesModel.maxRetryCount!!, retryPropertiesModel.retryPeriod!!) {
                producer?.send(ProducerRecord<String, Any>(topic, message.id, message))
            }
        } catch (e: Exception) {
            throw SendToKafkaException(e)
        }

        logger.info(END_STAGE(LOG_STREAMING_SERVER_COMPLETE))
    }


    /**
     * Method for produce message
     */
    fun streamEventMessage(topic: String,
                           transactionId: String,
                           command: String,
                           data: EventStateModel?,
                           messageType: MessageType,
                           relatedServices: List<Services>,
                           message: String? = null
    ) {

        val (redisKey, documentData) = when (messageType) {
            MessageType.BROADCAST -> handleBroadcastData(data!!)
            else -> null to null
        }

        val messageEventModel = createMessageEventModel(
            transactionId = transactionId,
            command = command,
            messageType = messageType,
            data = documentData,
            message = message,
            relatedServices = relatedServices,
            redisKey = redisKey
        )

        streamToKafka(topic, messageEventModel)
    }

    /**
     * Method for produce message
     */
    fun streamEventMessage(transactionId: String,
                           sponsor: String,
                           service: String,
                           command: String,
                           topic: String,
                           data: Any
    ) {

        streamToKafka(
                topic = topic,
                message = StreamingModel(
                        sponsor = sponsor,
                        id = transactionId,
                        type = service,
                        command = command,
                        data = data
                )
        )
    }

    /**
     * This method will return pair of redisKey and list of data
     * if size of data is larger than threshold [maxDataSizeByte]
     * we will put data to redis and new generated key and return new key with data as null
     * If size of data is less than threshold we will return key as null and original data
     */
    protected fun handleBroadcastData(data: EventStateModel): Pair<String?, EventStateModel?> {
        val dataSize = jacksonObjectMapperInstance.writeValueAsString(data).toByteArray().size
        return if (dataSize > maxDataSizeByte) {
            val key = UUID.randomUUID().toString()
            redisTemplate.opsForValue().set(key, jacksonObjectMapperInstance.writeValueAsString(data), Duration.ofDays(1L))
            key to null
        } else null to data
    }

}