package th.co.dv.p2p.common.models

import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.MAX_DECIMAL_PRECISION
import th.co.dv.p2p.common.utilities.setScale
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal
import kotlin.test.assertEquals

class QuantityTest {

    @Test
    fun`Test initial Quantity`() {

        //constructor(initial: BigDecimal, consumed: BigDecimal, unit: String)
        var result = Try.on {  Quantity(initial = BigDecimal("15.943"), consumed = BigDecimal.ZERO, unit = "BAG") }
        assert(result.isSuccess)
        assertEquals(BigDecimal("15.943").setScale(),  result.getOrThrow().initial)
        assertEquals(BigDecimal("0.000").setScale(),   result.getOrThrow().consumed)
        assertEquals("BAG",                 result.getOrThrow().unit)
        assertEquals(BigDecimal("15.943").setScale(),  result.getOrThrow().remaining)

        result = Try.on {  Quantity(initial = BigDecimal("15"), consumed = BigDecimal.ZERO, unit = "BAG") }
        assert(result.isSuccess)
        assertEquals(BigDecimal("15.000").setScale(),  result.getOrThrow().initial)
        assertEquals(BigDecimal("0.000").setScale(),   result.getOrThrow().consumed)
        assertEquals("BAG",                 result.getOrThrow().unit)
        assertEquals(BigDecimal("15.000").setScale(),  result.getOrThrow().remaining)

        result = Try.on {  Quantity(initial = BigDecimal("15.25"), consumed = BigDecimal.ZERO, unit = "BAG") }
        assert(result.isSuccess)
        assertEquals(BigDecimal("15.250").setScale(),  result.getOrThrow().initial)
        assertEquals(BigDecimal("0.000").setScale(),   result.getOrThrow().consumed)
        assertEquals("BAG",                 result.getOrThrow().unit)
        assertEquals(BigDecimal("15.250").setScale(),  result.getOrThrow().remaining)

        result = Try.on {  Quantity(initial = BigDecimal("15.94399333333"), consumed = BigDecimal.ZERO, unit = "BAG") }
        assert(result.isFailure)
        assert(result.toString().contains("Scale of quantity should less than or equal $MAX_DECIMAL_PRECISION digits."))

        //constructor(initial: BigDecimal, unit: String)
        result = Try.on {  Quantity(initial = BigDecimal("15.943"), unit = "BAG") }
        assert(result.isSuccess)
        assertEquals(BigDecimal("15.943").setScale(),  result.getOrThrow().initial)
        assertEquals(BigDecimal("0.000").setScale(),   result.getOrThrow().consumed)
        assertEquals("BAG",                 result.getOrThrow().unit)
        assertEquals(BigDecimal("15.943").setScale(),  result.getOrThrow().remaining)

        result = Try.on {  Quantity(initial = BigDecimal("15.94355555555"), unit = "BAG") }
        assert(result.isFailure)
        assert(result.toString().contains("Scale of quantity should less than or equal $MAX_DECIMAL_PRECISION digits."))

        //constructor(initial: Double, unit: String)
        result = Try.on {  Quantity(initial = "15.943".toDouble(), unit = "BAG") }
        assert(result.isSuccess)
        assertEquals(BigDecimal("15.943").setScale(),  result.getOrThrow().initial)
        assertEquals(BigDecimal("0.000").setScale(),   result.getOrThrow().consumed)
        assertEquals("BAG",                 result.getOrThrow().unit)
        assertEquals(BigDecimal("15.943").setScale(),  result.getOrThrow().remaining)

        result = Try.on {  Quantity(initial = "15.94355555555".toDouble(), unit = "BAG") }
        assert(result.isFailure)
        assert(result.toString().contains("Scale of quantity should less than or equal $MAX_DECIMAL_PRECISION digits."))

        //constructor(initial: Double, consumed: Double, unit: String)
        result = Try.on {  Quantity(initial = "15.943".toDouble(), consumed = "0".toDouble(), unit = "BAG") }
        assert(result.isSuccess)
        assertEquals(BigDecimal("15.943").setScale(),  result.getOrThrow().initial)
        assertEquals(BigDecimal("0.000").setScale(),   result.getOrThrow().consumed)
        assertEquals("BAG",                 result.getOrThrow().unit)
        assertEquals(BigDecimal("15.943").setScale(),  result.getOrThrow().remaining)

        result = Try.on {  Quantity(initial = "15.94355555555".toDouble(), consumed = "0".toDouble(), unit = "BAG") }
        assert(result.isFailure)
        assert(result.toString().contains("Scale of quantity should less than or equal $MAX_DECIMAL_PRECISION digits."))


    }

}