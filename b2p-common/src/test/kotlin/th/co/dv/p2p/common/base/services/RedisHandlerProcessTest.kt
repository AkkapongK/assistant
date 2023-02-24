package th.co.dv.p2p.common.base.services

import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.junit.Before
import org.junit.Test
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.util.ReflectionTestUtils
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.base.utilities.deleteRecord
import th.co.dv.p2p.common.constants.REQUIRED_TRANSACTION_ID_IN_ARG
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.utilities.SponsorContextHolder
import kotlin.test.assertEquals

class RedisHandlerProcessTest {

    @MockK
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Before
    fun setup() = MockKAnnotations.init(this)

    class RedisHandlerTest: RedisHandlerProcess(Services.INVOICE)
    private val sponsor = "DV"

    @Test
    fun testProcess() {

        SponsorContextHolder.setSponsor(sponsor)

        val redisHandlerProcess = spyk<RedisHandlerTest>(recordPrivateCalls = true)
        val joinPoint = mockk<ProceedingJoinPoint>()
        val methodSignature = mockk<MethodSignature>()
        val parameterNames = listOf("Param1", "Param2").toTypedArray()
        val parameterValues = listOf("value1", "value2").toTypedArray()
        mockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")
        ReflectionTestUtils.setField(redisHandlerProcess, "redisTemplate", redisTemplate)

        every { joinPoint.proceed() } returns "String Result" andThenThrows IllegalArgumentException("Error")
        every { joinPoint.signature } returns methodSignature
        every { joinPoint.args } returns parameterValues
        every { methodSignature.parameterNames } returns parameterNames
        every { redisHandlerProcess["getTransactionId"](parameterNames.toList(), parameterValues.toList()) } returns "TX_ID"
        every { redisTemplate.deleteRecord("TX_ID", Services.INVOICE, sponsor) } just Runs

        // Case success
        var result = Try.on { ReflectionTestUtils.invokeMethod<Any>(redisHandlerProcess, "process", joinPoint) }
        assert(result.isSuccess)
        assertEquals("String Result", result.getOrThrow())

        verify(exactly = 0) { redisHandlerProcess["getTransactionId"](any<List<String>>(), any<List<String>>()) }
        verify(exactly = 0) { redisTemplate.deleteRecord(any(), any(), any()) }

        // Case failed and remove record from redis
        result = Try.on { ReflectionTestUtils.invokeMethod<Any>(redisHandlerProcess, "process", joinPoint) }
        assert(result.isFailure)
        assert(result.toString().contains("Error"))

        verify(exactly = 1) { redisHandlerProcess["getTransactionId"](parameterNames.toList(), parameterValues.toList()) }
        verify(exactly = 1) { redisTemplate.deleteRecord("TX_ID", Services.INVOICE, sponsor) }

        unmockkStatic("th.co.dv.p2p.common.base.utilities.RedisUtilityKt")

        SponsorContextHolder.clear()
    }

    @Test
    fun testGetTransactionId() {

        val redisHandlerProcess = spyk<RedisHandlerTest>()

        val parameterNames = listOf("Param1", "Param2", "transactionId")
        val parameterValues = listOf("value1", "value2", "TX_ID")

        // Case parameter name is not contain transactionId
        var result = Try.on {
            ReflectionTestUtils.invokeMethod<String>(redisHandlerProcess, "getTransactionId", parameterNames.dropLast(1), parameterValues)
        }
        assert(result.isFailure)
        assert(result.toString().contains(REQUIRED_TRANSACTION_ID_IN_ARG))

        // Case parameter name is contain transactionId
        result = Try.on {
            ReflectionTestUtils.invokeMethod<String>(redisHandlerProcess, "getTransactionId", parameterNames, parameterValues)
        }
        assert(result.isSuccess)
        assertEquals("TX_ID", result.getOrThrow())

    }


}