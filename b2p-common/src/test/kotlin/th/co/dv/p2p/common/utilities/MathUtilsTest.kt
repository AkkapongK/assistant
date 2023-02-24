package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertTrue
import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MathUtilsTest {

    @Test
    fun `Test negativeToZero`() {
        // Case negative
        var result = BigDecimal(-100).negativeToZero()
        assertEquals(BigDecimal.ZERO, result)
        // Case positive
        result = BigDecimal(100).negativeToZero()
        assertEquals(BigDecimal(100), result)

    }

    @Test
    fun testToNegative() {
        // Case positive
        var result = BigDecimal(10).toNegative()
        assertTrue((-10).toBigDecimal().compareTo(result) == 0)
        // Case negative
        result = BigDecimal(-10).toNegative()
        assertTrue((-10).toBigDecimal().compareTo(result) == 0)
        // Case zero
        result = BigDecimal.ZERO.toNegative()
        assertEquals(BigDecimal.ZERO, result)
    }

    @Test
    fun `Test BigDecimal isEqual`() {
        val dataNull: BigDecimal? = null
        val data0: BigDecimal? = BigDecimal(0).setScale(7)
        val data1: BigDecimal? = BigDecimal(1).setScale(2)

        // Case both data is not null
        var result = data1.isEqual(BigDecimal(1).setScale(5))
        assertTrue(result)
        result = data0.isEqual(BigDecimal(0).setScale(5))
        assertTrue(result)
        result = data1.isEqual(BigDecimal(2).setScale(5))
        assertFalse(result)
        result = data0.isEqual(BigDecimal(2).setScale(5))
        assertFalse(result)

        // Case both data is null
        result = dataNull.isEqual(null)
        assertTrue(result)

        // Case both data is null and not null
        result = dataNull.isEqual(data0)
        assertFalse(result)
        result = data0.isEqual(dataNull)
        assertFalse(result)
    }

    @Test
    fun `Test setScaleByCurrency String`() {
        mockkStatic("th.co.dv.p2p.common.utilities.MathUtilsKt")
        var value = 10.005.toBigDecimal()
        var currency = "THB"
        val token = currency.CURRENCY

        every { value.setScaleByCurrency(token) } returns 10.01.toBigDecimal()
        val result = value.setScaleByCurrency(currency)
        assertEquals(10.01.toBigDecimal(), result)

        currency = "BTC"
        value = 10.toBigDecimal()
        var failResult = Try.on { value.setScaleByCurrency(currency) }
        assertTrue(failResult.isFailure)
        assertTrue(failResult.toString().contains("Currency not support : $currency"))

        value = 10.0049999.toBigDecimal()
        failResult = Try.on { value.setScaleByCurrency(null) }
        assertTrue(failResult.isFailure)
        assertTrue(failResult.toString().contains("Currency not support : null"))

        unmockkStatic("th.co.dv.p2p.common.utilities.MathUtilsKt")
    }

    @Test
    fun `Test setScaleByCurrency Currency`() {
        var value = 10.005.toBigDecimal()
        val currency = "THB"
        val token = currency.CURRENCY
        var result = value.setScaleByCurrency(token)
        assertEquals(10.01.toBigDecimal(), result)

        value = 10.0049999.toBigDecimal()
        result = value.setScaleByCurrency(token)
        assertEquals(BigDecimal(10).setScale(2), result)

    }

    @Test
    fun `Test setScale`() {
        var value = 10.toBigDecimal()
        var result = value.setScale()
        assertEquals(BigDecimal(10).setScale(10), result)

        value = 10.00000000005.toBigDecimal()
        result = value.setScale()
        assertEquals(10.0000000001.toBigDecimal().setScale(10), result)

        value = 10.00000000004.toBigDecimal()
        result = value.setScale()
        assertEquals(10.toBigDecimal().setScale(10), result)

    }

    @Test
    fun `test quantity sumAll`() {
        var quantityList = listOf(Quantity(initial = -10.0, unit = "unit"), Quantity(initial = -12.2, unit = "unit"))
        var result = quantityList.sumAll()
        assertEquals(Quantity(initial = (-22.2).toBigDecimal(), consumed = BigDecimal.ZERO, unit = "unit", remaining = (-22.2).toBigDecimal()), result)

        quantityList = listOf(Quantity(initial = 10.0, unit = "unit"), Quantity(initial = 12.2, unit = "unit"))
        result = quantityList.sumAll()
        assertEquals(Quantity(initial = 22.2.toBigDecimal(), consumed = BigDecimal.ZERO, unit = "unit", remaining = 22.2.toBigDecimal()), result)

        quantityList = listOf(Quantity(initial = -10.0, unit = "unit"), Quantity(initial = 12.2, unit = "unit"), Quantity(initial = 12.2, unit = "unit"))
        result = quantityList.sumAll()
        assertEquals(Quantity(initial = 14.4.toBigDecimal(), consumed = BigDecimal.ZERO, unit = "unit", remaining = 14.4.toBigDecimal()), result)
    }
}