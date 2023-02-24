package th.co.dv.p2p.usernotify.kafka

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertTrue
import net.corda.core.contracts.UniqueIdentifier
import org.apache.kafka.clients.producer.Producer
import org.junit.Before
import org.junit.Test
import org.springframework.test.util.ReflectionTestUtils
import th.co.dv.p2p.common.enums.CreditNoteCommand
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.kafka.KafkaTopicConstant
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.kafka.models.NotifyEventModel
import th.co.dv.p2p.common.kafka.models.NotifyStatus
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.corda.base.models.CreditNoteModel
import th.co.dv.p2p.usernotify.Try
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProducerServicesTest {

    @MockK
    lateinit var kafkaProperties: KafkaProperties

    @InjectMockKs
    lateinit var producerServices: ProducerServices

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testInit() {

        // Case : Kafka disable
        // Mock kafkaProperties
        every { kafkaProperties.enabled } returns false

        // Call method
        val result = ReflectionTestUtils.invokeMethod<Producer<String, Any>>(producerServices, "init")
        assertNull(result)

        // Case : Kafka enable but fail because cannot connect kafka
        // Mock kafkaProperties
        every { kafkaProperties.enabled } returns true
        every { kafkaProperties.streamingHost } returns "kafka-test:9092"
        every { kafkaProperties.maxMessageSize } returns 15728640

        // Call method
        val expectedResult = ReflectionTestUtils.invokeMethod<Producer<String, Any>>(producerServices, "init")
        assertNull(expectedResult)

        // TODO: Success case cannot test
    }

    @Test
    fun testStreamToKafkaWithTopic() {
        val producerServices = spyk<ProducerServices>()

        val transactionId = UUID.randomUUID().toString()
        val relatedServices = listOf(Services.CREDIT_NOTE.name)

        val allStatesIn = AllStates(creditNotes = listOf(CreditNoteModel()))
        val allStatesOut = AllStates(creditNotes = listOf(CreditNoteModel()))

        val eventStateModel = EventStateModel(
                previousState = allStatesIn,
                nextState = allStatesOut,
                relatedServices = relatedServices)
        val streamingModel = StreamingModel(
                id = transactionId,
                command = CreditNoteCommand.SimplyIssueCreditNote.name,
                type = Services.CREDIT_NOTE.name,
                data = eventStateModel
        )

        every { producerServices["generateStreamingModel"](transactionId, relatedServices, CreditNoteCommand.SimplyIssueCreditNote.name,
                allStatesOut, allStatesIn) } returns streamingModel
        every { producerServices["streamToKafka"](any<String>(), any<StreamingModel<EventStateModel>>()) } returns Unit

        val result = Try.on {
            producerServices.streamToKafkaWithTopic(
                    transactionId,
                    relatedServices,
                    KafkaTopicConstant.CREDIT_NOTE_ISSUED,
                    allStatesIn,
                    allStatesOut,
                    CreditNoteCommand.SimplyIssueCreditNote.name)
        }
        assert(result.isSuccess)
    }

    @Test
    fun testGenerateStreamingModel() {
        val transactionId = UniqueIdentifier().id.toString()
        val allStatesIn = AllStates(creditNotes = listOf(CreditNoteModel()))
        val allStatesOut = AllStates(creditNotes = listOf(CreditNoteModel()))
        val relatedService = listOf(Services.CREDIT_NOTE.name)

        val result = ReflectionTestUtils.invokeMethod<StreamingModel<EventStateModel>>(producerServices, "generateStreamingModel",
                transactionId, relatedService, CreditNoteCommand.SimplyIssueCreditNote.name, allStatesOut, allStatesIn)!!
        assertEquals(result.id, transactionId)
        assertEquals(result.command, CreditNoteCommand.SimplyIssueCreditNote.name)
        assertEquals(result.data!!.previousState, allStatesIn)
        assertEquals(result.data!!.nextState, allStatesOut)
        assertEquals(result.data!!.relatedServices, relatedService)
    }

    @Test
    fun testPublishNotify() {
        // Mock input
        val topic = KafkaTopicConstant.CREDIT_NOTE_ISSUED_NOTIFY
        val transactionId = UUID.randomUUID().toString()
        val referenceId = UUID.randomUUID().toString()
        val command = CreditNoteCommand.SimplyIssueCreditNote.name
        val status = NotifyStatus.SUCCESS
        val message = ""
        val notifyEventModel = NotifyEventModel(
                id = referenceId,
                externalId = "CN-001",
                relatedServices = listOf(Services.CREDIT_NOTE.name),
                status = status,
                message = message
        )

        val producerServices = spyk<ProducerServices>()

        every { producerServices["streamToKafka"](any<String>(), match<StreamingModel<NotifyEventModel>>{
            it.data == notifyEventModel
        }) } returns Unit

        // Call method
        val result = Try.on {
            producerServices.publishNotify(topic, transactionId, command, notifyEventModel)
        }

        assertTrue(result.isSuccess)
    }

}