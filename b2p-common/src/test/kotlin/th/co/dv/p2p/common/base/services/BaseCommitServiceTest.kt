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
import th.co.dv.p2p.common.base.utilities.getRelatedData
import th.co.dv.p2p.common.constants.SPONSOR_CANNOT_BE_NULL
import th.co.dv.p2p.common.enums.InvoiceCommand
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.corda.base.models.InvoiceModel

class BaseCommitServiceTest {

    class TestCommitService: BaseCommitService<InvoiceModel>(InvoiceModel::class.java, Services.INVOICE) {
        override fun initRedisTemplate(): RedisTemplate<String, String> {
            TODO("Not yet implemented")
        }

        override fun transformAndSave(command: String?, eventStateModel: EventStateModel): EventStateModel {
            TODO("Not yet implemented")
        }
    }

    @MockK
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Before
    fun setup() = MockKAnnotations.init(this)

    private val relatedServices = listOf(Services.PURCHASE, Services.INVOICE)
    private val command = InvoiceCommand.Issue.name
    private val command2 = InvoiceCommand.Edit.name
    private val invoiceModel = InvoiceModel(linearId = "001", externalId = "INV_001")
    private val invoiceModel2 = InvoiceModel(linearId = "002", externalId = "INV_002")
    private val eventStateModel1 = EventStateModel(
        nextState = AllStates(invoices = listOf(invoiceModel)),
        relatedServices = listOf()
    )
    private val eventStateModel2 = EventStateModel(
        nextState = AllStates(invoices = listOf(invoiceModel2)),
        relatedServices = listOf()
    )
    private val streamingModel = StreamingModel<Any>(
        sponsor = "DV",
        id = "TX_ID",
        type = Services.INVOICE.name,
        relatedServices = relatedServices,
        command = command,
        messageType = MessageType.COMMIT,
        data = null
    )

    @Test
    fun testProcess() {

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")

        val testCommitService = spyk<TestCommitService>(recordPrivateCalls = true)
        val commandWithData = mapOf<String?, EventStateModel>(command to eventStateModel1)

        every { testCommitService["initRedisTemplate"]() } returns redisTemplate
        every { testCommitService["transformAndSave"](match<String> { it == command }, eventStateModel1) } throws IllegalArgumentException("Failed Message")
        every { testCommitService["processAfterStored"](streamingModel, command, eventStateModel1) } returns eventStateModel1
        every { testCommitService["processAfterStored"](streamingModel, command2, eventStateModel2) } returns eventStateModel2
        every { redisTemplate.getRelatedData(streamingModel.id, Services.INVOICE, streamingModel.sponsor!!) } returns commandWithData

        // Case skip
        var result = Try.on { testCommitService.process(streamingModel.copy(messageType = MessageType.NOTIFY_SUCCESS)) }
        assert(result.isSuccess)

        result = Try.on { testCommitService.process(streamingModel.copy(relatedServices = listOf(Services.PURCHASE))) }
        assert(result.isSuccess)

        // Case failed to for max retry
        result = Try.on { testCommitService.process(streamingModel) }
        assert(result.isFailure)
        assert(result.toString().contains("Failed Message"))

        verify(exactly = 4) { redisTemplate.getRelatedData(streamingModel.id, Services.INVOICE, streamingModel.sponsor!!) }
        verify(exactly = 4) { testCommitService["transformAndSave"](match<String> { it == command }, eventStateModel1) }
        verify(exactly = 0) { testCommitService["processAfterStored"](any<StreamingModel<Any>>(), any<String>(), any<EventStateModel>()) }
        clearAllMocks(answers = false)

        val multipleCommandWithData = mapOf<String?, EventStateModel>(command to eventStateModel1, command2 to eventStateModel2)
        every { redisTemplate.getRelatedData(streamingModel.id, Services.INVOICE, streamingModel.sponsor!!) } returns multipleCommandWithData
        every { testCommitService["transformAndSave"](match<String> { it == command }, eventStateModel1) } returns eventStateModel1
        every { testCommitService["transformAndSave"](match<String> { it == command2 }, eventStateModel2) } returns eventStateModel2

        // Case success and process after store
        result = Try.on { testCommitService.process(streamingModel) }
        assert(result.isSuccess)

        verify(exactly = 1) { redisTemplate.getRelatedData(streamingModel.id, Services.INVOICE, streamingModel.sponsor!!) }
        verify(exactly = 1) { testCommitService["transformAndSave"](match<String> { it == command }, eventStateModel1) }
        verify(exactly = 1) { testCommitService["transformAndSave"](match<String> { it == command2 }, eventStateModel2) }
        verify(exactly = 1) { testCommitService["processAfterStored"](streamingModel, command, eventStateModel1) }
        verify(exactly = 1) { testCommitService["processAfterStored"](streamingModel, command2, eventStateModel2) }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")

    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Test
    fun testProcessAfterStored() {

        val testCommitService = spyk<TestCommitService>(recordPrivateCalls = true)

        every { testCommitService["deleteRecord"](streamingModel.id, streamingModel.sponsor) } returns Unit
        every { testCommitService["broadcastMessage"](streamingModel.id, command, Services.INVOICE, streamingModel.relatedServices, eventStateModel1) } returns Unit
        every { testCommitService["sendToExternalQueue"](streamingModel.id, streamingModel.sponsor, eventStateModel1, command) } returns Unit
        every { testCommitService["sendToInternalQueue"](streamingModel.id, eventStateModel1, command) } returns Unit

        // Case sponsor is not null
        var result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(testCommitService, "processAfterStored", streamingModel, command, eventStateModel1)
        }
        assert(result.isSuccess)

        verify(exactly = 1) { testCommitService["deleteRecord"](streamingModel.id, streamingModel.sponsor) }
        verify(exactly = 1) { testCommitService["broadcastMessage"](streamingModel.id, command, Services.INVOICE, streamingModel.relatedServices, eventStateModel1) }
        verify(exactly = 1) { testCommitService["sendToExternalQueue"](streamingModel.id, streamingModel.sponsor, eventStateModel1, command) }
        verify(exactly = 1) { testCommitService["sendToInternalQueue"](streamingModel.id, eventStateModel1, command) }
        clearMocks(testCommitService, answers = false)

        // Case sponsor is null
        result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(testCommitService, "processAfterStored", streamingModel.copy(sponsor = null), command, eventStateModel1)
        }
        assert(result.isFailure)
        assert(result.toString().contains(SPONSOR_CANNOT_BE_NULL))

        verify(exactly = 0) { testCommitService["deleteRecord"](streamingModel.id, streamingModel.sponsor) }
        verify(exactly = 0) { testCommitService["broadcastMessage"](streamingModel.id, command, Services.INVOICE, streamingModel.relatedServices, eventStateModel1) }
        verify(exactly = 0) { testCommitService["sendToExternalQueue"](streamingModel.id, streamingModel.sponsor, eventStateModel1, command) }
        verify(exactly = 0) { testCommitService["sendToInternalQueue"](streamingModel.id, eventStateModel1, command) }

    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Test
    fun testDeleteRecord() {

        val testCommitService = spyk<TestCommitService>()
        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")

        every { testCommitService["initRedisTemplate"]() } returns redisTemplate
        every { redisTemplate.deleteRecord(streamingModel.id, Services.INVOICE, streamingModel.sponsor!!) } just Runs

        val result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(testCommitService, "deleteRecord", streamingModel.id, streamingModel.sponsor)
        }
        assert(result.isSuccess)
        verify(exactly = 1) { redisTemplate.deleteRecord(streamingModel.id, Services.INVOICE, streamingModel.sponsor!!) }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")

    }

}