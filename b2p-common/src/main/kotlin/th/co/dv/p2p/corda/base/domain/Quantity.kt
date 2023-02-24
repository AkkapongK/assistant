package th.co.dv.p2p.corda.base.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.annotations.NoArgConstructor
import th.co.dv.p2p.common.constants.MAX_DECIMAL_PRECISION
import th.co.dv.p2p.common.utilities.isGreaterThan
import th.co.dv.p2p.common.utilities.isLessThan
import th.co.dv.p2p.common.utilities.setScale
import th.co.dv.p2p.common.utilities.toDecimal
import java.math.BigDecimal

/**
 * In the future we might want to enforce the allowable unit type in Quantity
 * for now its just of type string of any
 */
@CordaSerializable
enum class Unit {
    KILOGRAM,
    GRAM,
    TON,
    BOX,
    PC,
    EA
}

/**
 * Quantity represents a positive quantity of some unit (kilograms, boxes, etc.)
 * The nominal quantity represented by each individual Quantity is equal to the 3

 *
 * @property initial the number of units promised during parent state issuance as a bigdecimal value.
 * @property consumed the number of units issued during children state issuance as a bigdecimal value.
 * @property unit the type of the token, for example [Kilograms, grams].
 * @property remaining the balance by taking initial minus consumed.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgConstructor
data class Quantity(var initial: BigDecimal,
                    var consumed: BigDecimal = BigDecimal.ZERO.setScale(),
                    val unit: String,
                    var remaining: BigDecimal = initial) {

    init {
        require( (initial.stripTrailingZeros().scale() <= MAX_DECIMAL_PRECISION) && (consumed.stripTrailingZeros().scale() <= MAX_DECIMAL_PRECISION) ) { "Scale of quantity should less than or equal $MAX_DECIMAL_PRECISION digits." }
        initial = initial.setScale()
        consumed = consumed.setScale()
        remaining = remaining.setScale()
    }
    
    constructor(initial: BigDecimal, consumed: BigDecimal, unit: String) : this(
            initial = initial,
            consumed = consumed,
            unit = unit,
            remaining = initial.minus(consumed))

    constructor(initial: BigDecimal, unit: String) : this(
            initial = initial,
            consumed = BigDecimal.ZERO,
            unit = unit,
            remaining = initial)

    constructor(initial: Double, unit: String) : this(
            initial = initial.toDecimal(),
            consumed = BigDecimal.ZERO,
            unit = unit,
            remaining = initial.toDecimal())

    constructor(initial: Double, consumed: Double, unit: String) : this(
            initial = initial.toDecimal(),
            consumed = consumed.toDecimal(),
            unit = unit,
            remaining = initial.toDecimal().minus(BigDecimal(consumed)))

    operator fun plus(quantity: Quantity): Quantity {
        if (this.unit.trim().uppercase() != quantity.unit.trim().uppercase()) {
            throw IllegalArgumentException("Units does not match")
        } else {
            val initial = this.initial + quantity.initial
            val consumed = this.consumed + quantity.consumed
            return Quantity(initial = initial, consumed = consumed, unit = quantity.unit)
        }
    }

    operator fun minus(quantity: Quantity): Quantity {
        if (this.unit.trim().uppercase() != quantity.unit.trim().uppercase()) {
            throw IllegalArgumentException("Units does not match")
        } else {
            val initial = this.initial - quantity.initial
            val consumed = this.consumed - quantity.consumed
            return Quantity(initial = initial, consumed = consumed, unit = quantity.unit)
        }
    }

    /**
     * Checks value equality of Quantity objects
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Quantity

        if (unit != other.unit) return false
        if (initial.compareTo(other.initial) != 0) return false
        if (consumed.compareTo(other.consumed) != 0) return false
        if (remaining.compareTo(other.remaining) != 0) return false

        return true
    }
    override fun hashCode(): Int {
        var result = initial.abs().hashCode()
        result = 31 * result + consumed.abs().hashCode()
        result = 31 * result + remaining.abs().hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }
}

/**
 * This function use to deplete a Quantity object by increasing the Quantity.consumed
 * this will auto calculate the Quantity.remaining. Amount to deplete must be less than remaining so we don't over-deplete
 *
 * @param amount is the amount to increase the consumed field. This must be non-negative value.
 */

fun Quantity.deplete(amount: BigDecimal): Quantity {

    when {
        amount.isLessThan(BigDecimal.ZERO) -> throw IllegalArgumentException("Quantity.deplete amount must be non-negative value, amount: $amount")
        amount.isGreaterThan(this.remaining) -> throw IllegalArgumentException("Quantity.deplete amount must be less than or equal to remaining, remaining: ${this.remaining.setScale().toPlainString()}  amount: $amount")
    }

    return plus(Quantity(BigDecimal.ZERO, amount, unit))
}
