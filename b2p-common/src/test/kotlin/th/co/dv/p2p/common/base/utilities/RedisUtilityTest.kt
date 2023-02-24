package th.co.dv.p2p.common.base.utilities

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.springframework.data.redis.core.RedisTemplate
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.enums.InvoiceCommand
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.models.BuyerVendorModel
import th.co.dv.p2p.common.models.ContractModel
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.common.utilities.*
import th.co.dv.p2p.common.utilities.RedisUtils.getInvoiceHeaderKeyDefault
import th.co.dv.p2p.corda.base.models.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RedisUtilityTest {

    @MockK
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Before
    fun setup() = MockKAnnotations.init(this)

    private val defaultAllState = AllStates(
        invoices = emptyList(),
        invoiceItems = emptyList(),
        purchaseOrders = emptyList(),
        purchaseItems = emptyList(),
        goodsReceiveds = emptyList(),
        goodsReceivedItems = emptyList(),
        creditNotes = emptyList(),
        creditNoteItems = emptyList(),
        debitNotes = emptyList(),
        debitNoteItems = emptyList(),
        requests = emptyList(),
        requestItems = emptyList(),
        payments = emptyList(),
        taxDocuments = emptyList(),
        buyerVendors = emptyList(),
        contracts = emptyList(),
        financeableDocuments = emptyList(),
        repaymentRequests = emptyList(),
        repaymentHistories = emptyList(),
        loans = emptyList(),
        loanProfiles = emptyList()
    )

    @Test
    fun testFindRecord() {

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        val invoice = InvoiceModel(
            linearId = "ID01",
            externalId = "INV01",
            vendorTaxNumber = "VEN01",
            companyTaxNumber = "BUYER"
        )
        val invoiceString = jacksonObjectMapper().writeValueAsString(invoice)
        val completeKey = "TX_ID:DV:INV:$STAR"
        val completeKey2 = "TX_ID:DV:INV2:$STAR"

        every { completeRedisKey<Any?>("DV", Services.INVOICE, "TX_ID", any(), any()) } returns completeKey
        every { redisTemplate.keys(completeKey) } returns mutableSetOf() andThen mutableSetOf(completeKey, completeKey2)
        every { redisTemplate.opsForValue().get(completeKey) } returns invoiceString
        every { redisTemplate.opsForValue().get(completeKey2) } returns invoiceString

        // Case 1 key is empty
        var result = Try.on {
            redisTemplate.findRecord("TX_ID", Services.INVOICE, "DV")
        }
        assert(result.isFailure)
        assert(result.toString().contains("$CANNOT_FIND_RECORD key: $completeKey"))

        verify(exactly = 1) { completeRedisKey<Any?>("DV", Services.INVOICE, "TX_ID", any(), any()) }
        verify(exactly = 1) { redisTemplate.keys(any()) }
        clearAllMocks(answers = false)

        // Case 2 success
        result = Try.on {
            redisTemplate.findRecord("TX_ID", Services.INVOICE, "DV")
        }
        assert(result.isSuccess)
        assertNotNull(result.getOrThrow())
        assertEquals(2, result.getOrThrow().size)
        assertEquals(listOf(invoiceString, invoiceString), result.getOrThrow())

        verify(exactly = 1) { completeRedisKey<Any?>("DV", Services.INVOICE, "TX_ID", any(), any()) }
        verify(exactly = 1) { redisTemplate.keys(any()) }
        clearAllMocks(answers = false)

        // Case complete key return null
        every { completeRedisKey<Any?>("DV", Services.INVOICE, "TX_ID", any(), any()) } returns null
        result = Try.on {
            redisTemplate.findRecord("TX_ID", Services.INVOICE, "DV")
        }
        assert(result.isFailure)
        assert(result.toString().contains(keyIsNull))

        verify(exactly = 1) { completeRedisKey<Any?>("DV", Services.INVOICE, "TX_ID", any(), any()) }
        verify(exactly = 0) { redisTemplate.keys(any()) }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")

    }

    @Test
    fun testGetDataByKey() {

        val value = "test"
        val key = "TX_ID:DV:INV:$STAR"
        every { redisTemplate.opsForValue().get(key) } returns value andThen null

        // Case find data
        var result = redisTemplate.getDataByKey(key)
        assertNotNull(result)
        assertEquals(value, result)

        verify(exactly = 1) { redisTemplate.opsForValue().get(key) }

        // Case not find data
        result = redisTemplate.getDataByKey(key)
        assertNull(result)

        verify(exactly = 2) { redisTemplate.opsForValue().get(key) }

    }

    @Test
    fun testFindAndMapRecord() {

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        mockkObject(MapUtility)
        val eventStateModel = EventStateModel(nextState = AllStates(), relatedServices = listOf())

        every { redisTemplate.findRecord("TX_ID", Services.INVOICE, "DV") } returns listOf("TransactionDetail")
        every { MapUtility.eventStateModelMapper("TransactionDetail") } returns eventStateModel

        val result = redisTemplate.findAndMapRecord("TX_ID", Services.INVOICE, "DV")
        assertEquals(1, result.size)
        assertEquals(listOf(eventStateModel), result)

        verify(exactly = 1) { redisTemplate.findRecord("TX_ID", Services.INVOICE, "DV") }
        verify(exactly = 1) { MapUtility.eventStateModelMapper("TransactionDetail") }

        unmockkObject(MapUtility)
        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")

    }

    @Test
    fun testGetRelatedData() {

        val invoice1 = InvoiceModel(linearId = "001")
        val invoice2 = InvoiceModel(linearId = "002")
        val invoice3 = InvoiceModel(linearId = "003")

        val eventState1 = EventStateModel(
            nextState = AllStates(invoices = listOf(invoice1)),
            relatedServices = listOf(),
            command = "Edit"
        )
        val eventState2 = EventStateModel(
            nextState = AllStates(invoices = listOf(invoice2)),
            previousState = AllStates(invoices = listOf(invoice2)),
            relatedServices = listOf(),
            command = "Edit"
        )
        val eventState3 = EventStateModel(
            nextState = AllStates(invoices = listOf(invoice3)),
            relatedServices = listOf(),
            command = "Issue"
        )

        val expectedEventStateIssueCommand = EventStateModel(
            nextState = defaultAllState.copy(invoices = listOf(invoice3)),
            previousState = AllStates(),
            relatedServices = emptyList()
        )

        val expectedEventStateEditCommand = EventStateModel(
            nextState = defaultAllState.copy(invoices = listOf(invoice1, invoice2)),
            previousState = defaultAllState.copy(invoices = listOf(invoice2)),
            relatedServices = emptyList()
        )

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        every { redisTemplate.findAndMapRecord("TX_ID", Services.INVOICE, "DV") } returns listOf(
            eventState1,
            eventState2,
            eventState3
        )

        val result = redisTemplate.getRelatedData("TX_ID", Services.INVOICE, "DV")
        assertNotNull(result)
        assertEquals(2, result.size)

        val eventStateIssueCommand = result["Issue"]
        assertNotNull(eventStateIssueCommand)
        assertEquals(expectedEventStateIssueCommand, eventStateIssueCommand)

        val eventStateEditCommand = result["Edit"]
        assertNotNull(eventStateEditCommand)
        assertEquals(expectedEventStateEditCommand, eventStateEditCommand)

        verify(exactly = 1) { redisTemplate.findAndMapRecord("TX_ID", Services.INVOICE, "DV") }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
    }

    @Test
    fun testDeleteRecord() {

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        val completeKey = "TX_ID:INV:$STAR"

        every { completeRedisKey<Any?>("DV", Services.INVOICE, "TX_ID", any(), any()) } returns completeKey
        every { redisTemplate.keys(completeKey) } returns mutableSetOf() andThen mutableSetOf(completeKey)
        every { redisTemplate.delete(mutableSetOf(completeKey)) } returns 0L

        // Case 1 not find any record to delete
        var result = Try.on {
            redisTemplate.deleteRecord("TX_ID", Services.INVOICE, "DV")
        }
        assert(result.isSuccess)

        verify(exactly = 1) {
            completeRedisKey<Any?>(
                "DV",
                Services.INVOICE,
                "TX_ID",
                any(),
                any()
            )
        }
        verify(exactly = 1) { redisTemplate.keys(any()) }
        verify(exactly = 0) { redisTemplate.delete(any<Collection<String>>()) }
        clearAllMocks(answers = false)

        // Case 1 find record to delete
        result = Try.on {
            redisTemplate.deleteRecord("TX_ID", Services.INVOICE, "DV")
        }
        assert(result.isSuccess)

        verify(exactly = 1) { completeRedisKey<Any?>("DV", Services.INVOICE, "TX_ID", any(), any()) }
        verify(exactly = 1) { redisTemplate.keys(any()) }
        verify(exactly = 1) { redisTemplate.delete(mutableSetOf(completeKey)) }
        clearAllMocks(answers = false)

        // Case complete key return null
        every { completeRedisKey<Any?>("DV", Services.INVOICE, "TX_ID", any(), any()) } returns null
        result = Try.on {
            redisTemplate.deleteRecord("TX_ID", Services.INVOICE, "DV")
        }
        assert(result.isFailure)
        assert(result.toString().contains(keyIsNull))

        verify(exactly = 1) { completeRedisKey<Any?>("DV", Services.INVOICE, "TX_ID", any(), any()) }
        verify(exactly = 0) { redisTemplate.keys(any()) }
        verify(exactly = 0) { redisTemplate.delete(mutableSetOf(completeKey)) }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")

    }

    @Test
    fun testDeleteRecordByKey() {

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        val date = DateUtility.convertStringToDate("2022-03-17 00:00:00", DateUtility.WS_DATE_TIME_FORMAT)?.toInstant()?.stringify()
        val invoice1 = InvoiceModel(
            linearId = "ID01",
            externalId = "INV01",
            vendorTaxNumber = "VEN01",
            companyTaxNumber = "BUYER",
            invoiceDate = date
        )
        val invoice2 = InvoiceModel(
            linearId = "ID02",
            externalId = "INV02",
            vendorTaxNumber = "VEN01",
            companyTaxNumber = "BUYER",
            invoiceDate = date
        )
        val expectedKey1 = getInvoiceHeaderKeyDefault.invoke(invoice1)
        val expectedKey2 = getInvoiceHeaderKeyDefault.invoke(invoice2)

        assertNotNull(expectedKey1)
        assertNotNull(expectedKey2)

        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, getInvoiceHeaderKeyDefault) } returns expectedKey1
        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, getInvoiceHeaderKeyDefault) } returns expectedKey2
        every { redisTemplate.keys(expectedKey1) } returns mutableSetOf(expectedKey1)
        every { redisTemplate.keys(expectedKey2) } returns mutableSetOf<String>()
        every { redisTemplate.delete(mutableSetOf(expectedKey1)) } returns 0L

        var result = Try.on {
            redisTemplate.deleteRecordByKey(
                listOf(invoice1, invoice2),
                Services.INVOICE,
                "DV",
                "TX_ID",
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isSuccess)

        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, getInvoiceHeaderKeyDefault) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, getInvoiceHeaderKeyDefault) }
        verify(exactly = 1) { redisTemplate.keys(expectedKey1) }
        verify(exactly = 1) { redisTemplate.keys(expectedKey2) }
        verify(exactly = 1) { redisTemplate.delete(mutableSetOf(expectedKey1)) }
        clearAllMocks(answers = false)

        // Case complete key is null
        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, getInvoiceHeaderKeyDefault) } returns null

        result = Try.on {
            redisTemplate.deleteRecordByKey(
                listOf(invoice1, invoice2),
                Services.INVOICE,
                "DV",
                "TX_ID",
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains(keyIsNull))

        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, getInvoiceHeaderKeyDefault) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, getInvoiceHeaderKeyDefault) }
        verify(exactly = 0) { redisTemplate.keys(expectedKey1) }
        verify(exactly = 0) { redisTemplate.keys(expectedKey2) }
        verify(exactly = 0) { redisTemplate.delete(mutableSetOf(expectedKey1)) }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
    }

    @Test
    fun testLockRecord() {

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        val invoice1 = InvoiceModel(
            linearId = "ID01",
            externalId = "INV01",
            vendorTaxNumber = "VEN01",
            companyTaxNumber = "BUYER"
        )
        val invoice2 = InvoiceModel(
            linearId = "ID02",
            externalId = "INV02",
            vendorTaxNumber = "VEN01",
            companyTaxNumber = "BUYER"
        )

        val expectedKey1 = "keyINV1"
        val expectedKey2 = "keyINV2"
        val completeKey1 = "TX_ID:DV:INV:$expectedKey1"
        val completeKey2 = "TX_ID:DV:INV:$expectedKey2"

        every { completeRedisKey("DV", Services.INVOICE, null, invoice1, any()) } returns expectedKey1
        every { completeRedisKey("DV", Services.INVOICE, null, invoice2, any()) } returns expectedKey2
        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) } returns completeKey1
        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) } returns completeKey2

        every { redisTemplate.keys(expectedKey1) } returns setOf(expectedKey1) andThen setOf()
        every { redisTemplate.keys(expectedKey2) } returns setOf()
        every { redisTemplate.opsForValue().setIfAbsent(completeKey1, "", LOCK_RECORD_TIMEOUT) } returns false andThen true
        every { redisTemplate.opsForValue().setIfAbsent(completeKey2, "", LOCK_RECORD_TIMEOUT) } returns true

        // Case send key is exist
        var result = Try.on {
            redisTemplate.lockRecord(listOf(invoice1), "DV", Services.INVOICE, "TX_ID", getInvoiceHeaderKeyDefault)
        }
        assert(result.isFailure)
        assert(result.toString().contains(duplicateRecord))

        verify(exactly = 1) { redisTemplate.keys(expectedKey1) }
        verify(exactly = 0) { redisTemplate.keys(expectedKey2) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, null, invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, null, invoice2, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        clearAllMocks(answers = false)

        // Case send 1 models and failed when lock
        result = Try.on {
            redisTemplate.lockRecord(listOf(invoice1), "DV", Services.INVOICE, "TX_ID", getInvoiceHeaderKeyDefault)
        }
        assert(result.isFailure)
        assert(result.toString().contains(duplicateRecord))

        verify(exactly = 1) { redisTemplate.keys(expectedKey1) }
        verify(exactly = 0) { redisTemplate.keys(expectedKey2) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, null, invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, null, invoice2, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        clearAllMocks(answers = false)

        // Case send 1 models and success
        result = Try.on {
            redisTemplate.lockRecord(listOf(invoice1), "DV", Services.INVOICE, "TX_ID", getInvoiceHeaderKeyDefault)
        }
        assert(result.isSuccess)

        verify(exactly = 1) { redisTemplate.keys(expectedKey1) }
        verify(exactly = 0) { redisTemplate.keys(expectedKey2) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, null, invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, null, invoice2, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        clearAllMocks(answers = false)

        // Case send 2 models but the same key
        result = Try.on {
            redisTemplate.lockRecord(
                listOf(invoice1, invoice1),
                "DV",
                Services.INVOICE,
                "TX_ID",
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isSuccess)

        verify(exactly = 1) { redisTemplate.keys(expectedKey1) }
        verify(exactly = 0) { redisTemplate.keys(expectedKey2) }
        verify(exactly = 2) { completeRedisKey("DV", Services.INVOICE, null, invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, null, invoice2, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        clearAllMocks(answers = false)

        // Case send 2 models with different key
        result = Try.on {
            redisTemplate.lockRecord(
                listOf(invoice1, invoice2),
                "DV",
                Services.INVOICE,
                "TX_ID",
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isSuccess)

        verify(exactly = 1) { redisTemplate.keys(expectedKey1) }
        verify(exactly = 1) { redisTemplate.keys(expectedKey2) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, null, invoice1, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, null, invoice2, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        clearAllMocks(answers = false)

        // Case send 2 model but 1 complete key is null
        every { completeRedisKey("DV", Services.INVOICE, null, invoice2, any()) } returns null

        result = Try.on {
            redisTemplate.lockRecord(
                listOf(invoice1, invoice2),
                "DV",
                Services.INVOICE,
                "TX_ID",
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isSuccess)

        verify(exactly = 1) { redisTemplate.keys(expectedKey1) }
        verify(exactly = 0) { redisTemplate.keys(expectedKey2) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, null, invoice1, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, null, invoice2, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
    }

    @Test
    fun testUpdateRecord() {

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        mockkObject(AuthorizationUtils)
        val invoice1 = InvoiceModel(
            linearId = "ID01",
            externalId = "INV01",
            vendorTaxNumber = "VEN01",
            companyTaxNumber = "BUYER"
        )
        val invoice2 = InvoiceModel(
            linearId = "ID02",
            externalId = "INV02",
            vendorTaxNumber = "VEN01",
            companyTaxNumber = "BUYER"
        )

        val completeKey1 = "TX_ID:DV:INV1"
        val completeKey2 = "TX_ID:DV:INV2"

        every { AuthorizationUtils.validateModelAuthorization(listOf(invoice1)) } returns Unit
        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) } returns completeKey1
        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) } returns completeKey2
        every { updateRedisProcess<InvoiceModel>(redisTemplate, any(), any(), any(), InvoiceCommand.Edit, true) } returns Unit

        // Case no previous state
        var result = Try.on {
            redisTemplate.updateRecord(
                listOf(invoice1),
                emptyList(),
                InvoiceCommand.Edit,
                "DV",
                Services.INVOICE,
                "TX_ID",
                    true,
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isSuccess)

        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        verify(exactly = 1) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), null, any(), InvoiceCommand.Edit, true) }
        verify(exactly = 0) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), any(), null, InvoiceCommand.Edit, true) }
        clearAllMocks(answers = false)

        // Case have previous state with same key
        every { AuthorizationUtils.validateModelAuthorization(listOf(invoice1, invoice1)) } returns Unit
        result = Try.on {
            redisTemplate.updateRecord(
                listOf(invoice1),
                listOf(invoice1),
                InvoiceCommand.Edit,
                "DV",
                Services.INVOICE,
                "TX_ID",
                    true,
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isSuccess)

        verify(exactly = 2) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        verify(exactly = 1) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), any(), any(), InvoiceCommand.Edit, true) }
        verify(exactly = 0) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), any(), null, InvoiceCommand.Edit, true) }
        clearAllMocks(answers = false)

        // Case have previous state with different key
        every { AuthorizationUtils.validateModelAuthorization(listOf(invoice1, invoice2)) } returns Unit
        result = Try.on {
            redisTemplate.updateRecord(
                listOf(invoice1),
                listOf(invoice2),
                InvoiceCommand.Edit,
                "DV",
                Services.INVOICE,
                "TX_ID",
                    true,
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isSuccess)

        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        verify(exactly = 1) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), null, any(), InvoiceCommand.Edit, true) }
        verify(exactly = 1) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), any(), null, InvoiceCommand.Edit, true) }
        clearAllMocks(answers = false)

        // Case previous state complete key return null
        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) } returns null

        result = Try.on {
            redisTemplate.updateRecord(
                listOf(invoice1),
                listOf(invoice2),
                InvoiceCommand.Edit,
                "DV",
                Services.INVOICE,
                "TX_ID",
                    true,
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains(keyIsNull))

        verify(exactly = 0) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        verify(exactly = 0) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), null, any(), InvoiceCommand.Edit, true) }
        verify(exactly = 0) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), any(), null, InvoiceCommand.Edit, true) }
        clearAllMocks(answers = false)

        // Case next state complete key return null
        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) } returns null
        every { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) } returns completeKey2

        result = Try.on {
            redisTemplate.updateRecord(
                listOf(invoice1),
                listOf(invoice2),
                InvoiceCommand.Edit,
                "DV",
                Services.INVOICE,
                "TX_ID",
                true,
                getInvoiceHeaderKeyDefault
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains(keyIsNull))

        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice1, any()) }
        verify(exactly = 1) { completeRedisKey("DV", Services.INVOICE, "TX_ID", invoice2, any()) }
        verify(exactly = 0) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), null, any(), InvoiceCommand.Edit, true) }
        verify(exactly = 0) { updateRedisProcess<InvoiceModel>(redisTemplate, any(), any(), null, InvoiceCommand.Edit, true) }
        clearAllMocks(answers = false)

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
    }

    @Test
    fun testUpdateRedisProcess() {

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        val prevModel = InvoiceModel(linearId = "001")
        val nextModel = InvoiceModel(linearId = "002")

        val prevAllStates = AllStates(invoices = listOf(prevModel))
        val nextAllStates = AllStates(invoices = listOf(nextModel))

        val finalKey = "Key"
        every { buildAllState(prevModel) } returns prevAllStates
        every { buildAllState(nextModel) } returns nextAllStates
        every { redisTemplate.opsForValue().setIfPresent(finalKey, any(), match { it.seconds == LOCK_RECORD_TIMEOUT.seconds }) } throws IllegalArgumentException("Error") andThen false andThen null andThen true

        // Case send both next and prev model and get error when update data
        var result = Try.on {
            updateRedisProcess(redisTemplate, finalKey, prevModel, nextModel, InvoiceCommand.Edit, true)
        }
        assert(result.isFailure)
        assert(result.toString().contains("$cannotUpdateRecord key: $finalKey"))

        verify(exactly = 1) { buildAllState(prevModel) }
        verify(exactly = 1) { buildAllState(nextModel) }
        clearAllMocks(answers = false)

        // Case send both next and prev model and setIfPresent return false
        result = Try.on {
            updateRedisProcess(redisTemplate, finalKey, prevModel, nextModel, InvoiceCommand.Edit, true)
        }
        assert(result.isFailure)
        assert(result.toString().contains("$cannotUpdateRecord key: $finalKey"))

        verify(exactly = 1) { buildAllState(prevModel) }
        verify(exactly = 1) { buildAllState(nextModel) }
        clearAllMocks(answers = false)

        // Case send only prev model and setIfPresent return null
        result = Try.on {
            updateRedisProcess(redisTemplate, finalKey, prevModel, null, InvoiceCommand.Edit, true)
        }
        assert(result.isFailure)
        assert(result.toString().contains("$cannotUpdateRecord key: $finalKey"))

        verify(exactly = 1) { buildAllState(prevModel) }
        verify(exactly = 0) { buildAllState(nextModel) }
        clearAllMocks(answers = false)

        // Case sent both next and prev model and update successful
        result = Try.on {
            updateRedisProcess(redisTemplate, finalKey, prevModel, nextModel, InvoiceCommand.Edit, true)
        }
        assert(result.isSuccess)

        verify(exactly = 1) { buildAllState(prevModel) }
        verify(exactly = 1) { buildAllState(nextModel) }
        clearAllMocks(answers = false)

        // Case sent both next and prev model and setIfAbsent successful
        every { redisTemplate.opsForValue().setIfAbsent(finalKey, any(), match { it.seconds == LOCK_RECORD_TIMEOUT.seconds }) } returns true andThen false
        result = Try.on {
            updateRedisProcess(redisTemplate, finalKey, prevModel, nextModel, InvoiceCommand.Edit, false)
        }
        assert(result.isSuccess)
        verify(exactly = 1) { buildAllState(prevModel) }
        verify(exactly = 1) { buildAllState(nextModel) }
        clearAllMocks(answers = false)

        // Case send only prev model and setIfAbsent return false
        result = Try.on {
            updateRedisProcess(redisTemplate, finalKey, prevModel, nextModel, InvoiceCommand.Edit, false)
        }
        assert(result.isFailure)
        assert(result.toString().contains("$cannotUpdateRecord key: $finalKey"))
        verify(exactly = 1) { buildAllState(prevModel) }
        verify(exactly = 1) { buildAllState(nextModel) }
        clearAllMocks(answers = false)

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
    }

    @Test
    fun testBuildAllState() {

        val inputWithExpectedOutput = mapOf(
            InvoiceModel(linearId = "001") to AllStates(invoices = listOf(InvoiceModel(linearId = "001"))),
            InvoiceItemModel(linearId = "001") to AllStates(invoiceItems = listOf(InvoiceItemModel(linearId = "001"))),
            PurchaseOrderModel(linearId = "001") to AllStates(purchaseOrders = listOf(PurchaseOrderModel(linearId = "001"))),
            PurchaseItemModel(linearId = "001") to AllStates(purchaseItems = listOf(PurchaseItemModel(linearId = "001"))),
            GoodsReceivedModel(linearId = "001") to AllStates(goodsReceiveds = listOf(GoodsReceivedModel(linearId = "001"))),
            GoodsReceivedItemModel(linearId = "001") to AllStates(goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "001"))),
            CreditNoteModel(linearId = "001") to AllStates(creditNotes = listOf(CreditNoteModel(linearId = "001"))),
            CreditNoteItemModel(linearId = "001") to AllStates(creditNoteItems = listOf(CreditNoteItemModel(linearId = "001"))),
            DebitNoteModel(linearId = "001") to AllStates(debitNotes = listOf(DebitNoteModel(linearId = "001"))),
            DebitNoteItemModel(linearId = "001") to AllStates(debitNoteItems = listOf(DebitNoteItemModel(linearId = "001"))),
            RequestModel(linearId = "001") to AllStates(requests = listOf(RequestModel(linearId = "001"))),
            RequestItemModel(linearId = "001") to AllStates(requestItems = listOf(RequestItemModel(linearId = "001"))),
            PaymentModel(linearId = "001") to AllStates(payments = listOf(PaymentModel(linearId = "001"))),
            TaxDocumentModel(linearId = "001") to AllStates(taxDocuments = listOf(TaxDocumentModel(linearId = "001"))),
            BuyerVendorModel(vendorTaxId = "001") to AllStates(buyerVendors = listOf(BuyerVendorModel(vendorTaxId = "001"))),
            ContractModel(linearId = "001") to AllStates(contracts = listOf(ContractModel(linearId = "001"))),
            null to null
        )

        inputWithExpectedOutput.forEach { (input, expectedOutput) ->
            val result = buildAllState(input)
            assertEquals(expectedOutput, result)
        }

    }

    @Test
    fun testAdd() {

        // Case existing state is empty list and new state is null
        var targetState = defaultAllState.copy()
        var result = Try.on { targetState.add(null) }
        assert(result.isSuccess)
        assertEquals(defaultAllState, targetState)

        // Case existing is null and new state is empty list
        targetState = AllStates()
        result = Try.on { targetState.add(defaultAllState) }
        assert(result.isSuccess)
        assertEquals(defaultAllState, targetState)

        // Case existing is null and new state is null
        targetState = AllStates()
        result = Try.on { targetState.add(null) }
        assert(result.isSuccess)
        assertEquals(AllStates(), targetState)

        val newState = AllStates(
            invoices = listOf(InvoiceModel(linearId = "002")),
            invoiceItems = listOf(InvoiceItemModel(linearId = "002")),
            purchaseOrders = listOf(PurchaseOrderModel(linearId = "002")),
            purchaseItems = listOf(PurchaseItemModel(linearId = "002")),
            goodsReceiveds = listOf(GoodsReceivedModel(linearId = "002")),
            goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "002")),
            creditNotes = listOf(CreditNoteModel(linearId = "002")),
            creditNoteItems = listOf(CreditNoteItemModel(linearId = "002")),
            debitNotes = listOf(DebitNoteModel(linearId = "002")),
            debitNoteItems = listOf(DebitNoteItemModel(linearId = "002")),
            requests = listOf(RequestModel(linearId = "002")),
            requestItems = listOf(RequestItemModel(linearId = "002")),
            payments = listOf(PaymentModel(linearId = "002")),
            taxDocuments = listOf(TaxDocumentModel(linearId = "002")),
            buyerVendors = listOf(BuyerVendorModel(vendorTaxId = "002")),
            contracts = listOf(ContractModel(linearId = "002")),
            financeableDocuments = listOf(FinanceableDocumentModel(linearId = "002")),
            repaymentRequests = listOf(RepaymentRequestModel(repaymentRequestId = "002")),
            repaymentHistories = listOf(RepaymentHistoryModel(repaymentRequestId = "002")),
            loans = listOf(LoanModel(linearId = "002")),
            loanProfiles = listOf(LoanProfileModel(borrowerTaxId = "002"))
        )

        // Case existing state is nul and new state is have value
        targetState = AllStates()
        result = Try.on { targetState.add(newState) }
        assert(result.isSuccess)
        assertEquals(newState, targetState)

        val existingState = AllStates(
            invoices = listOf(InvoiceModel(linearId = "001")),
            invoiceItems = listOf(InvoiceItemModel(linearId = "001")),
            purchaseOrders = listOf(PurchaseOrderModel(linearId = "001")),
            purchaseItems = listOf(PurchaseItemModel(linearId = "001")),
            goodsReceiveds = listOf(GoodsReceivedModel(linearId = "001")),
            goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "001")),
            creditNotes = listOf(CreditNoteModel(linearId = "001")),
            creditNoteItems = listOf(CreditNoteItemModel(linearId = "001")),
            debitNotes = listOf(DebitNoteModel(linearId = "001")),
            debitNoteItems = listOf(DebitNoteItemModel(linearId = "001")),
            requests = listOf(RequestModel(linearId = "001")),
            requestItems = listOf(RequestItemModel(linearId = "001")),
            payments = listOf(PaymentModel(linearId = "001")),
            taxDocuments = listOf(TaxDocumentModel(linearId = "001")),
            buyerVendors = listOf(BuyerVendorModel(vendorTaxId = "001")),
            contracts = listOf(ContractModel(linearId = "001")),
            financeableDocuments = listOf(FinanceableDocumentModel(linearId = "001")),
            repaymentRequests = listOf(RepaymentRequestModel(repaymentRequestId = "001")),
            repaymentHistories = listOf(RepaymentHistoryModel(repaymentRequestId = "001")),
            loans = listOf(LoanModel(linearId = "001")),
            loanProfiles = listOf(LoanProfileModel(borrowerTaxId = "001"))
        )

        // Case existing state have value but new state is empty
        targetState = existingState
        result = Try.on { targetState.add(defaultAllState) }
        assert(result.isSuccess)
        assertEquals(existingState, targetState)

        val combineState = AllStates(
            invoices = listOf(InvoiceModel(linearId = "001"), InvoiceModel(linearId = "002")),
            invoiceItems = listOf(InvoiceItemModel(linearId = "001"), InvoiceItemModel(linearId = "002")),
            purchaseOrders = listOf(PurchaseOrderModel(linearId = "001"), PurchaseOrderModel(linearId = "002")),
            purchaseItems = listOf(PurchaseItemModel(linearId = "001"), PurchaseItemModel(linearId = "002")),
            goodsReceiveds = listOf(GoodsReceivedModel(linearId = "001"), GoodsReceivedModel(linearId = "002")),
            goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "001"), GoodsReceivedItemModel(linearId = "002")),
            creditNotes = listOf(CreditNoteModel(linearId = "001"), CreditNoteModel(linearId = "002")),
            creditNoteItems = listOf(CreditNoteItemModel(linearId = "001"), CreditNoteItemModel(linearId = "002")),
            debitNotes = listOf(DebitNoteModel(linearId = "001"), DebitNoteModel(linearId = "002")),
            debitNoteItems = listOf(DebitNoteItemModel(linearId = "001"), DebitNoteItemModel(linearId = "002")),
            requests = listOf(RequestModel(linearId = "001"), RequestModel(linearId = "002")),
            requestItems = listOf(RequestItemModel(linearId = "001"), RequestItemModel(linearId = "002")),
            payments = listOf(PaymentModel(linearId = "001"), PaymentModel(linearId = "002")),
            taxDocuments = listOf(TaxDocumentModel(linearId = "001"), TaxDocumentModel(linearId = "002")),
            buyerVendors = listOf(BuyerVendorModel(vendorTaxId = "001"), BuyerVendorModel(vendorTaxId = "002")),
            contracts = listOf(ContractModel(linearId = "001"), ContractModel(linearId = "002")),
            financeableDocuments = listOf(FinanceableDocumentModel(linearId = "001"), FinanceableDocumentModel(linearId = "002")),
            repaymentRequests = listOf(RepaymentRequestModel(repaymentRequestId = "001"), RepaymentRequestModel(repaymentRequestId = "002")),
            repaymentHistories = listOf(RepaymentHistoryModel(repaymentRequestId = "001"), RepaymentHistoryModel(repaymentRequestId = "002")),
            loans = listOf(LoanModel(linearId = "001"), LoanModel(linearId = "002")),
            loanProfiles = listOf(LoanProfileModel(borrowerTaxId = "001"), LoanProfileModel(borrowerTaxId = "002"))
        )

        // Case both existing state and new state have value
        targetState = existingState
        result = Try.on { targetState.add(newState) }
        assert(result.isSuccess)
        assertEquals(combineState, targetState)

    }

    @Test
    fun testCompleteRedisKey() {

        val sponsor = "DV"
        val sponsorLowercase = sponsor.lowercase()
        val transactionId = "TX_ID"
        val date = DateUtility.convertStringToDate("2022-03-17 00:00:00", DateUtility.WS_DATE_TIME_FORMAT)?.toInstant()?.stringify()
        val invoice = InvoiceModel(
            linearId = "ID01",
            externalId = "INV01",
            vendorTaxNumber = "VEN01",
            companyTaxNumber = "BUYER",
            invoiceDate = date
        )

        // Case model is null and transaction is null
        var result = completeRedisKey<InvoiceModel>(sponsor, Services.INVOICE, null, null, getInvoiceHeaderKeyDefault)
        assertNotNull(result)
        assertEquals("$STAR$COLON$sponsorLowercase$COLON${Services.INVOICE.code}$COLON$STAR", result)

        // Case model and transaction is not null
        val invoiceKey = getInvoiceHeaderKeyDefault.invoke(invoice)
        result = completeRedisKey(sponsor, Services.INVOICE, transactionId, invoice, getInvoiceHeaderKeyDefault)
        assertNotNull(result)
        assertEquals("$transactionId$COLON$sponsorLowercase$COLON${Services.INVOICE.code}$COLON$invoiceKey", result)

        // Case complete key return null
        result = completeRedisKey(sponsor, Services.INVOICE, transactionId, invoice.copy(invoiceDate = null), getInvoiceHeaderKeyDefault)
        assertNull(result)

    }

    @Test
    fun testGetData() {

        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        mockkObject(MapUtility)

        val eventStateFromRedis = EventStateModel(nextState = AllStates(invoices = listOf(InvoiceModel(linearId = "001"))), relatedServices = listOf())
        val eventStateFromInput = EventStateModel(nextState = AllStates(invoices = listOf(InvoiceModel(linearId = "002"))), relatedServices = listOf())
        val streamingModel = StreamingModel(
            id = "TX_ID",
            command = "command",
            type = Services.INVOICE.name,
            redisKey = "key",
            data = eventStateFromInput
        )

        every { redisTemplate.getDataByKey("key") } returns null andThen "data"
        every { MapUtility.eventStateModelMapper("data") } returns eventStateFromRedis

        // Case redis key is not null and data from redis is null
        var result = redisTemplate.getData(streamingModel)
        assertNull(result)

        verify(exactly = 1) { redisTemplate.getDataByKey("key") }
        verify(exactly = 0) { MapUtility.eventStateModelMapper("data") }
        clearAllMocks(answers = false)

        // Case redis key is not null and found data in redis
        result = redisTemplate.getData(streamingModel)
        assertNotNull(result)
        assertEquals(eventStateFromRedis, result)

        verify(exactly = 1) { redisTemplate.getDataByKey("key") }
        verify(exactly = 1) { MapUtility.eventStateModelMapper("data") }
        clearAllMocks(answers = false)

        // Case redis key is null and data is not null
        result = redisTemplate.getData(streamingModel.copy(redisKey = null))
        assertNotNull(result)
        assertEquals(eventStateFromInput, result)

        verify(exactly = 0) { redisTemplate.getDataByKey("key") }
        verify(exactly = 0) { MapUtility.eventStateModelMapper("data") }

        // Case redis key is null and data is null
        result = redisTemplate.getData(streamingModel.copy(redisKey = null, data = null))
        assertNull(result)

        verify(exactly = 0) { redisTemplate.getDataByKey("key") }
        verify(exactly = 0) { MapUtility.eventStateModelMapper("data") }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        unmockkObject(MapUtility)

    }

    @Test
    fun testUpdateModel() {
        mockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")
        val userNameFromAuth = "userNameFromAuth"
        every { getUsernameFromSecurityContext() } returns userNameFromAuth
        val invoice = InvoiceModel()
        var updatedInvoice = updateModel(null, invoice)
        assertEquals(userNameFromAuth, updatedInvoice.issuedBy)

        val existingUserName = "existingUserName"
        updatedInvoice = updateModel(null, invoice.copy(issuedBy = existingUserName))
        assertEquals(existingUserName, updatedInvoice.issuedBy)

        updatedInvoice = updateModel(invoice, invoice.copy(issuedBy = existingUserName, externalId = "externalId"))
        assertEquals(existingUserName, updatedInvoice.issuedBy)
        assertEquals("externalId", updatedInvoice.externalId)

        val creditNote = CreditNoteModel()
        val updatedCreditNote = updateModel(null, creditNote)
        assertEquals(userNameFromAuth, updatedCreditNote.issuedBy)

        val debitNote = DebitNoteModel()
        val updatedDebitNote = updateModel(null, debitNote)
        assertEquals(userNameFromAuth, updatedDebitNote.issuedBy)

        val other = RequestModel()
        val updatedOther = updateModel(null, other)
        assertEquals(other, updatedOther)
        unmockkAll()
    }

}