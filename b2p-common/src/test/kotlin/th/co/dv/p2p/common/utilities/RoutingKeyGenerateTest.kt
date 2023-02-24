package th.co.dv.p2p.common.utilities

import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test
import th.co.dv.p2p.common.enums.RabbitQueue
import kotlin.test.assertEquals

class RoutingKeyGenerateTest {


    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testGenRoutingKeyForExternalQueue() {

        val queue = RabbitQueue.CREDIT_NOTE_ISSUED

        val result = RoutingKeyGenerate.genRoutingKeyForExternalQueue(queue,"MINOR")
        assertEquals("MINOR.${RabbitQueue.CREDIT_NOTE_ISSUED}", result)



    }

}