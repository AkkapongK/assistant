package th.co.dv.p2p.usernotify.kafka

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.Before
import org.junit.Test
import th.co.dv.p2p.common.kafka.JsonDeserializer
import th.co.dv.p2p.usernotify.config.RetryProperties
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KafkaConfigTest {

    @MockK
    lateinit var properties: KafkaProperties

    @MockK
    lateinit var retryProperties: RetryProperties

    @MockK
    lateinit var springKafka: org.springframework.boot.autoconfigure.kafka.KafkaProperties

    @InjectMockKs
    lateinit var kafkaConfig: KafkaConfig

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testConsumer() {

        val kafkaConfig = spyk<KafkaConfig>()

        val consumerConfig: Map<String, Any> = mutableMapOf(
                ConsumerConfig.GROUP_ID_CONFIG to "GROUP",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java)

        every { kafkaConfig.consumerConfigs(any()) } returns consumerConfig

        val result1 = kafkaConfig.consumerFactoryNotifyAnyContainerFactory("GROUP")
        assertNotNull(result1.valueDeserializer)
        assertNotNull(result1.keyDeserializer)
        assertEquals(StringDeserializer::class.java, result1.keyDeserializer!!::class.java)

        val result2 = kafkaConfig.consumerFactoryEventStateModel("GROUP")
        assertNotNull(result2.valueDeserializer)
        assertNotNull(result2.keyDeserializer)
        assertEquals(StringDeserializer::class.java, result2.keyDeserializer!!::class.java)

    }

    @Test
    fun testConsumerConfigs() {

        every { springKafka.consumer.buildProperties() } returns mapOf<String, JvmType.Object>()

        every { properties.streamingHost } returns "localhost:9092"
        every { properties.fetchMaxBytes } returns 52428800
        every { properties.fetchMinBytes } returns 1
        every { properties.requestTimeout } returns 300000
        every { properties.sessionTimeOut } returns 10000
        every { properties.maxPartitionFetchByteConfig } returns 1048576
        every { properties.maxPollRecord } returns 30
        every { properties.maxPollInterval } returns 1200000
        every { retryProperties.maxRetryCount } returns 3
        every { retryProperties.retryPeriod } returns 3000

        val result = kafkaConfig.consumerConfigs("GROUP")

        assertEquals("localhost:9092", result[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG])
        assertEquals("GROUP", result[ConsumerConfig.GROUP_ID_CONFIG])
        assertEquals(StringDeserializer::class.java, result[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG])
        assertEquals(JsonDeserializer::class.java, result[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG])
        assertEquals(52428800, result[ConsumerConfig.FETCH_MAX_BYTES_CONFIG])
        assertEquals(1, result[ConsumerConfig.FETCH_MIN_BYTES_CONFIG])
        assertEquals(300000, result[ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG])
        assertEquals(10000, result[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG])
        assertEquals(1048576, result[ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG])
        assertEquals(30, result[ConsumerConfig.MAX_POLL_RECORDS_CONFIG])
        assertEquals(1200000, result[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG])
        assertEquals(3000L, result[ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG])
        assertEquals(9000, result[ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG])

    }

}