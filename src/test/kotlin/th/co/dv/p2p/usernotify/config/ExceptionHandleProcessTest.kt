package th.co.dv.p2p.usernotify.config

import io.mockk.*
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExceptionHandleProcessTest {

    @Test
    fun testExceptionHandling() {

        // Mock input
        val joinPoint = mockk<ProceedingJoinPoint>()
        val exceptionHandleProcess = spyk<ExceptionHandleProcess>(recordPrivateCalls = true)

        every { exceptionHandleProcess["process"](joinPoint) } returns "result"
        val result = exceptionHandleProcess.exceptionHandling(joinPoint)
        assertNotNull(result)
        assertEquals("result", result)

        verify(exactly = 1) { exceptionHandleProcess["process"](joinPoint) }

    }

}