package th.co.dv.p2p.common.base.services

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.Before
import org.junit.Test
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.util.ReflectionTestUtils
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.base.enums.MessageType
import th.co.dv.p2p.common.constants.REQUEST_IN_COMMAND
import th.co.dv.p2p.common.constants.REQUEST_OUT_COMMAND
import th.co.dv.p2p.common.constants.UNDERSCORE
import th.co.dv.p2p.common.enums.InvoiceCommand
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.kafka.KafkaTopicConstant
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.models.LogRequestInModel
import th.co.dv.p2p.common.models.LogRequestOutModel
import th.co.dv.p2p.common.models.RetryPropertiesModel
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.common.utilities.MapUtility
import th.co.dv.p2p.common.utilities.SponsorContextHolder
import th.co.dv.p2p.corda.base.models.InvoiceModel
import java.time.Duration
import java.util.*
import java.util.concurrent.Future
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BaseProducerTest {

    class TestProducerService: BaseProducer(Services.INVOICE) {

        override fun getMaxSize(): Int {
            return 2 * 1024 * 1024
        }

        override fun init(): Producer<String, Any> {
            TODO("Not yet implemented")
        }

        override fun initRetryPropertiesModel(): RetryPropertiesModel {
            return RetryPropertiesModel(retryPeriod = 5L , maxRetryCount = 3)
        }

    }

    @MockK
    lateinit var producer: Producer<String, Any>

    @MockK
    lateinit var redisTemplate: RedisTemplate<String, String>

    @InjectMockKs
    lateinit var testProducerService: TestProducerService

    @Before
    fun setup() = MockKAnnotations.init(this)

    private val date = Date()
    private val topic = "TOPIC_NAME"
    private val message = "test"
    private val redisKey = "key"
    private val relatedServices = listOf(Services.PURCHASE, Services.INVOICE)
    private val invoiceModel = InvoiceModel(linearId = "001", externalId = "INV_001")
    private val eventStateModel = EventStateModel(
        nextState = AllStates(invoices = listOf(invoiceModel)),
        relatedServices = listOf()
    )

    private val streamingModel = StreamingModel<Any>(
        sponsor = "DV",
        id = "TX_ID",
        type = Services.INVOICE.name,
        relatedServices = relatedServices,
        command = InvoiceCommand.Issue.name,
        messageType = MessageType.COMMIT,
        message = message,
        redisKey = redisKey,
        data = eventStateModel
    )

    @Test
    fun testCreateMessageEventModel() {

        // Case sent all parameter
        var result = testProducerService.createMessageEventModel("TX_ID", InvoiceCommand.Issue.name, MessageType.COMMIT, eventStateModel, message, redisKey, "DV", relatedServices)
        assertNotNull(result)
        assertEquals(streamingModel, result)

        // Case sent only required parameter
        result = testProducerService.createMessageEventModel("TX_ID", InvoiceCommand.Issue.name, MessageType.COMMIT, null, null, null, null, relatedServices + relatedServices)
        assertNotNull(result)
        assertEquals(streamingModel.copy(sponsor = null, data = null, message = null, redisKey = null), result)

    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Test
    fun testStreamToKafka() {

        SponsorContextHolder.setSponsor("DV")

        val producerService = spyk<TestProducerService>(recordPrivateCalls = true)
        val response = mockk<Future<RecordMetadata>>()
        val dataToSend = ProducerRecord<String, Any>(topic, streamingModel.id, streamingModel)

        every { producerService["init"]() } returns producer
        every { producer.send(dataToSend) } returns response andThen response andThenThrows IllegalArgumentException("Error")

        // Case success with sponsor
        var result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(producerService, "streamToKafka", topic, streamingModel)
        }
        assert(result.isSuccess)

        verify(exactly = 1) { producer.send(dataToSend) }
        clearMocks(producer, answers = false)

        // Case success with no sponsor
        result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(producerService, "streamToKafka", topic, streamingModel.copy(sponsor = null))
        }
        assert(result.isSuccess)

        verify(exactly = 1) { producer.send(dataToSend) }
        clearMocks(producer, answers = false)

        // Case failed to produce data
        result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(producerService, "streamToKafka", topic, streamingModel)
        }
        assert(result.isFailure)
        assert(result.toString().contains("Error"))

        verify(exactly = 3) { producer.send(dataToSend) }

        SponsorContextHolder.clear()
    }

    @Test
    fun testStreamEventMessage() {

        val producerService = spyk<TestProducerService>(recordPrivateCalls = true)
        val redisKeyAndData = Pair<String?, EventStateModel?>(redisKey, eventStateModel)

        every { producerService["handleBroadcastData"](eventStateModel) } returns redisKeyAndData
        every { producerService["createMessageEventModel"](
            streamingModel.id,
            streamingModel.command,
            any<MessageType>(),
            any<EventStateModel>(),
            message,
            any<String>(),
            any<String>(),
            relatedServices
        ) } returns streamingModel

        every { producerService["streamToKafka"](topic, streamingModel) } returns Unit

        // Case message type is not broadcast
        var result = Try.on {
            producerService.streamEventMessage(
                topic = topic,
                transactionId = streamingModel.id,
                command = streamingModel.command,
                data = eventStateModel,
                messageType = MessageType.COMMIT,
                relatedServices = relatedServices,
                message = message)
        }
        assert(result.isSuccess)

        verify(exactly = 0) { producerService["handleBroadcastData"](any<EventStateModel>()) }
        verify(exactly = 1) { producerService["createMessageEventModel"](
            streamingModel.id,
            streamingModel.command,
            MessageType.COMMIT,
            any<EventStateModel>(),
            message,
            any<String>(),
            any<String>(),
            relatedServices
        ) }
        verify(exactly = 1) { producerService["streamToKafka"](topic, streamingModel) }
        clearAllMocks(answers = false)

        // Case message type is broadcast
         result = Try.on {
            producerService.streamEventMessage(
                topic = topic,
                transactionId = streamingModel.id,
                command = streamingModel.command,
                data = eventStateModel,
                messageType = MessageType.BROADCAST,
                relatedServices = relatedServices,
                message = streamingModel.message)
        }
        assert(result.isSuccess)

        verify(exactly = 1) { producerService["handleBroadcastData"](eventStateModel) }
        verify(exactly = 1) { producerService["createMessageEventModel"](
            streamingModel.id,
            streamingModel.command,
            MessageType.BROADCAST,
            any<EventStateModel>(),
            message,
            any<String>(),
            any<String>(),
            relatedServices
        ) }
        verify(exactly = 1) { producerService["streamToKafka"](topic, streamingModel) }

    }

    @Test
    fun testStreamEventMessageForRequestIn() {

        val producerService = spyk<TestProducerService>(recordPrivateCalls = true)
        val url = "test-url"
        val method = "method"
        val headers = "headers"
        val queryString = "queryString"
        val body = "body"
        val topicForRequest = Services.INVOICE.name.uppercase() + UNDERSCORE + KafkaTopicConstant.REQUEST_INPUT

        val streaming = StreamingModel(
            sponsor = streamingModel.sponsor,
            id = streamingModel.id,
            type = streamingModel.type,
            command = REQUEST_IN_COMMAND,
            data = LogRequestInModel(
                url = url,
                method = method,
                headers = headers,
                queryString = queryString,
                body = body,
                request_timestamp = "2022-04-25"
            )
        )

        every { producerService["streamToKafka"](topicForRequest , streaming) } returns Unit

        val result = Try.on {
            producerService.streamEventMessage(
                transactionId = streamingModel.id,
                sponsor = streamingModel.sponsor!!,
                service = streamingModel.type,
                topic = topicForRequest,
                command = REQUEST_IN_COMMAND,
                data = LogRequestInModel(
                            url = url,
                            method = method,
                            headers = headers,
                            queryString = queryString,
                            body = body,
                            request_timestamp = "2022-04-25"
                )
            )
        }
        assert(result.isSuccess)

        verify(exactly = 1) { producerService["streamToKafka"](topicForRequest , streaming) }

    }

    @Test
    fun testStreamEventMessageForRequestOut() {

        val producerService = spyk<TestProducerService>(recordPrivateCalls = true)
        val url = "test-url"
        val method = "method"
        val headers = "headers"
        val queryString = "queryString"
        val body = "body"
        val topicForRequest = Services.INVOICE.name.uppercase() + UNDERSCORE + KafkaTopicConstant.REQUEST_OUTPUT

        val streaming = StreamingModel(
                sponsor = streamingModel.sponsor,
                id = streamingModel.id,
                type = streamingModel.type,
                command = REQUEST_OUT_COMMAND,
                data = LogRequestOutModel(
                        status= "Failed",
                        message = listOf("Internal error"),
                        response_timestamp = "2022-04-25"
                )
        )

        every { producerService["streamToKafka"](topicForRequest , streaming) } returns Unit

        val result = Try.on {
            producerService.streamEventMessage(
                    transactionId = streamingModel.id,
                    sponsor = streamingModel.sponsor!!,
                    service = streamingModel.type,
                    topic = topicForRequest,
                    command = REQUEST_OUT_COMMAND,
                    data = LogRequestOutModel(
                            status= "Failed",
                            message = listOf("Internal error"),
                            response_timestamp = "2022-04-25"
                    )
            )
        }
        assert(result.isSuccess)

        verify(exactly = 1) { producerService["streamToKafka"](topicForRequest , streaming) }

    }

    @Test
    fun testHandleBroadcastData() {

        val dataString = MapUtility.jacksonObjectMapperInstance.writeValueAsString(eventStateModel)
        val producerService = spyk<TestProducerService>(recordPrivateCalls = true)

        every { redisTemplate.opsForValue().set(any(), dataString, Duration.ofDays(1L)) } returns Unit
        ReflectionTestUtils.setField(producerService, "redisTemplate", redisTemplate)

        // Case data is less than max data size
        var result = ReflectionTestUtils.invokeMethod<Pair<String?, EventStateModel?>>(producerService, "handleBroadcastData", eventStateModel)
        assertNotNull(result)
        assertNull(result.first)
        assertEquals(eventStateModel, result.second)

        every { producerService["getMaxSize"]() } returns 2

        // Case data is more than max data size
        result = ReflectionTestUtils.invokeMethod(producerService, "handleBroadcastData", eventStateModel)
        assertNotNull(result)
        assertNotNull(result.first)
        assertNull(result.second)

    }

}