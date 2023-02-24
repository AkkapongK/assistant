package th.co.dv.p2p.common.models

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jgroups.util.Util.assertTrue
import org.junit.Test
import th.co.dv.p2p.common.utilities.setScaleByCurrency
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AggregateResponseModelTest {

    @Test
    fun testSetScaleForAggregateResponseModel() {
        mockkStatic("th.co.dv.p2p.common.models.AggregateResponseModelKt")
        val aggregateResponseModel = AggregateResponseModel(
                countTotal = 2,
                amountTotal = 10.1234.toBigDecimal(),
                currency = "THB"
        )
        every { setAmountTotal(10.1234.toBigDecimal(), "THB")} returns 10.12.toBigDecimal()
        val result = aggregateResponseModel.setScale()
        assertTrue(10.12.toBigDecimal().compareTo(result.amountTotal!!) == 0)
        unmockkStatic("th.co.dv.p2p.common.models.AggregateResponseModelKt")
    }

    @Test
    fun testSetScaleForAggregateMetadataModel() {
        mockkStatic("th.co.dv.p2p.common.models.AggregateResponseModelKt")
        val data = listOf(AggregateResponseModel(currency = "THB"))
        val aggregateMetadataModel = AggregateMetadataModel(
                countTotal = 2,
                amountTotal = 10.1454.toBigDecimal(),
                data = data
        )
        every { setAmountTotal(10.1454.toBigDecimal(), "THB")} returns 10.15.toBigDecimal()
        val result = aggregateMetadataModel.setScale()
        assertTrue(10.15.toBigDecimal().compareTo(result.amountTotal!!) == 0)
        unmockkStatic("th.co.dv.p2p.common.models.AggregateResponseModelKt")
    }

    @Test
    fun `test setAmountTotal`(){
        mockkStatic("th.co.dv.p2p.common.utilities.MathUtilsKt")

        every { 10.2356.toBigDecimal().setScaleByCurrency("THB") } returns 99.99.toBigDecimal()
        var result = setAmountTotal(10.2356.toBigDecimal(), "THB")
        assertNotNull(result)
        assertEquals(99.99.toBigDecimal(), result)

        result = setAmountTotal(22.toBigDecimal(), null)
        assertNotNull(result)
        assertEquals(22.toBigDecimal(), result)

        unmockkStatic("th.co.dv.p2p.common.utilities.MathUtilsKt")
    }
}