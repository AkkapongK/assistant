package th.co.dv.p2p.common.base.services

import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.util.ReflectionTestUtils
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.base.enums.MessageType
import th.co.dv.p2p.common.base.utilities.deleteRecord
import th.co.dv.p2p.common.base.utilities.findRecord
import th.co.dv.p2p.common.constants.MESSAGE_TYPE_NOT_SUPPORT
import th.co.dv.p2p.common.enums.InvoiceCommand
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.exceptions.InternalRedisException
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BaseNotifyServiceTest {

    class TestNotifyService: BaseNotifyService(Services.INVOICE) {
        override fun initRedisTemplate(): RedisTemplate<String, String> {
            TODO("Not yet implemented")
        }

        override fun initProducerService(): BaseProducer {
            TODO("Not yet implemented")
        }

        override fun saveErrorMessage(messageEventModel: StreamingModel<Any>) {
            println(messageEventModel)
        }
    }

    @MockK
    lateinit var redisTemplate: RedisTemplate<String, String>

    @MockK
    lateinit var testProducerService: BaseProducerTest.TestProducerService

    @Before
    fun setup() = MockKAnnotations.init(this)

    private val relatedServices = listOf(Services.PURCHASE, Services.INVOICE)
    private val invoiceModel = InvoiceModel(linearId = "002", externalId = "INV_001")
    private val streamingModelSuccess = StreamingModel<Any>(
        sponsor = "DV",
        id = "TX_ID",
        type = Services.INVOICE.name,
        relatedServices = relatedServices,
        command = InvoiceCommand.Issue.name,
        messageType = MessageType.NOTIFY_SUCCESS,
        data = null
    )
    private val streamingModelFailed = streamingModelSuccess.copy(
        messageType = MessageType.NOTIFY_FAILED
    )
    private val streamingModelOther = streamingModelSuccess.copy(
        messageType = MessageType.COMMIT
    )

    @Test
    fun testProcess() {

        val notifyService = spyk<TestNotifyService>(recordPrivateCalls = true)

        every { notifyService["processNotifySuccess"](streamingModelSuccess) } returns Unit
        every { notifyService["processNotifyFailed"](streamingModelFailed) } returns Unit

        // Case notify success
        var result = Try.on { notifyService.process(streamingModelSuccess) }
        assert(result.isSuccess)

        verify(exactly = 1) { notifyService["processNotifySuccess"](streamingModelSuccess) }
        verify(exactly = 0) { notifyService["processNotifyFailed"](any<StreamingModel<Any>>()) }
        clearMocks(notifyService, answers = false)

        // Case notify failed
        result = Try.on { notifyService.process(streamingModelFailed) }
        assert(result.isSuccess)

        verify(exactly = 0) { notifyService["processNotifySuccess"](any<StreamingModel<Any>>()) }
        verify(exactly = 1) { notifyService["processNotifyFailed"](streamingModelFailed) }
        clearMocks(notifyService, answers = false)

        // Case type is other
        result = Try.on { notifyService.process(streamingModelOther) }
        assert(result.isFailure)
        assert(result.toString().contains(MESSAGE_TYPE_NOT_SUPPORT))

        verify(exactly = 0) { notifyService["processNotifySuccess"](any<StreamingModel<Any>>()) }
        verify(exactly = 0) { notifyService["processNotifyFailed"](any<StreamingModel<Any>>()) }

    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Test
    fun testStreamCommitMessage() {

        val notifyService = spyk<TestNotifyService>()

        every { notifyService["initProducerService"]() } returns testProducerService
        every { testProducerService.streamEventMessage(
            topic = Services.INVOICE.commitTopic,
            transactionId = streamingModelSuccess.id,
            command = "NO_COMMAND",
            data = null,
            relatedServices = relatedServices,
            messageType = MessageType.COMMIT
        ) } just Runs

        val result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(notifyService, "streamCommitMessage", streamingModelSuccess)
        }
        assert(result.isSuccess)

        verify(exactly = 1) { testProducerService.streamEventMessage(
            topic = Services.INVOICE.commitTopic,
            transactionId = streamingModelSuccess.id,
            command = "NO_COMMAND",
            data = null,
            relatedServices = relatedServices,
            messageType = MessageType.COMMIT
        ) }
    }

    @Test
    fun testIsServiceNotDone() {

        val notifyService = spyk<TestNotifyService>()
        every { notifyService["initRedisTemplate"]() } returns redisTemplate

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        every { redisTemplate.findRecord(
            streamingModelSuccess.id,
            Services.INVOICE,
            streamingModelSuccess.sponsor!!) } returnsMany listOf(listOf("Data"), listOf(), listOf("1","")) andThenThrows InternalRedisException("Failed in redis.")

        // Case service is done
        var result = ReflectionTestUtils.invokeMethod<Boolean>(notifyService, "isServiceNotDone", streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor)
        assertNotNull(result)
        assertFalse(result)
        verify(exactly = 1) { redisTemplate.findRecord(streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor!!) }

        // Case service is not process
        clearAllMocks(answers = false)
        result = ReflectionTestUtils.invokeMethod(notifyService, "isServiceNotDone", streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor)
        assertNotNull(result)
        assertTrue(result)
        verify(exactly = 1) { redisTemplate.findRecord(streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor!!) }

        // Case service is partial process
        clearAllMocks(answers = false)
        result = ReflectionTestUtils.invokeMethod(notifyService, "isServiceNotDone", streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor)
        assertNotNull(result)
        assertTrue(result)
        verify(exactly = 1) { redisTemplate.findRecord(streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor!!) }

        // Case failed when get data in redis
        clearAllMocks(answers = false)
        result = ReflectionTestUtils.invokeMethod(notifyService, "isServiceNotDone", streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor)
        assertNotNull(result)
        assertTrue(result)
        verify(exactly = 1) { redisTemplate.findRecord(streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor!!) }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Test
    fun testProcessNotifyFailed() {

        val notifyService = spyk<TestNotifyService>(recordPrivateCalls = true)

        every { notifyService["saveErrorMessage"](streamingModelFailed) } returns Unit
        every { notifyService["clearTransactionInRedis"](streamingModelFailed) } returns Unit

        val result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(notifyService, "processNotifyFailed", streamingModelFailed)
        }
        assert(result.isSuccess)

        verify(exactly = 1) { notifyService["saveErrorMessage"](streamingModelFailed) }
        verify(exactly = 1) { notifyService["clearTransactionInRedis"](streamingModelFailed) }

    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Test
    fun testProcessNotifySuccess() {

        val notifyService = spyk<TestNotifyService>(recordPrivateCalls = true)

        every { notifyService["initRedisTemplate"]() } returns redisTemplate
        every { notifyService["isServiceNotDone"](streamingModelSuccess.id, match<Services> { it != Services.INVOICE }, streamingModelSuccess.sponsor) } returnsMany listOf(true, false)
        every { notifyService["isServiceNotDone"](streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor) } returnsMany listOf(true, false)
        every { notifyService["streamCommitMessage"](streamingModelSuccess) } returns Unit

        // Case cannot get current transaction in redis
        var result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(notifyService, "processNotifySuccess", streamingModelSuccess)
        }
        assert(result.isSuccess)
        verify(exactly = 1) { notifyService["isServiceNotDone"](streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor) }
        verify(exactly = 0) { notifyService["isServiceNotDone"](streamingModelSuccess.id, match<Services> { it != Services.INVOICE }, streamingModelSuccess.sponsor) }
        verify(exactly = 0) { notifyService["streamCommitMessage"](any<StreamingModel<Any>>()) }
        clearAllMocks(answers = false)

        // Case find current transaction in redis but all service is not processed
        result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(notifyService, "processNotifySuccess", streamingModelSuccess)
        }
        assert(result.isSuccess)
        verify(exactly = 1) { notifyService["isServiceNotDone"](streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor) }
        verify(exactly = 1) { notifyService["isServiceNotDone"](streamingModelSuccess.id, match<Services> { it != Services.INVOICE }, streamingModelSuccess.sponsor) }
        verify(exactly = 0) { notifyService["streamCommitMessage"](any<StreamingModel<Any>>()) }
        clearAllMocks(answers = false)

        // Case find current transaction in redis and all service is processed
        result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(notifyService, "processNotifySuccess", streamingModelSuccess)
        }
        assert(result.isSuccess)
        verify(exactly = 1) { notifyService["isServiceNotDone"](streamingModelSuccess.id, Services.INVOICE, streamingModelSuccess.sponsor) }
        verify(exactly = 1) { notifyService["isServiceNotDone"](streamingModelSuccess.id, match<Services> { it != Services.INVOICE }, streamingModelSuccess.sponsor) }
        verify(exactly = 1) { notifyService["streamCommitMessage"](any<StreamingModel<Any>>()) }

    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Test
    fun testClearTransactionInRedis() {

        val notifyService = spyk<TestNotifyService>()
        every { notifyService["initRedisTemplate"]() } returns redisTemplate

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        every { redisTemplate.deleteRecord(streamingModelSuccess.id, match { it in relatedServices }, streamingModelSuccess.sponsor!!) } just Runs

        val result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(notifyService, "clearTransactionInRedis", streamingModelSuccess)
        }
        assert(result.isSuccess)
        verify(exactly = 2) { redisTemplate.deleteRecord(streamingModelSuccess.id, match { it in relatedServices }, streamingModelSuccess.sponsor!!) }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
    }

}