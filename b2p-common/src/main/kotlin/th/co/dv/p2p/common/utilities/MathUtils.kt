package th.co.dv.p2p.common.utilities

import th.co.dv.p2p.common.constants.MAX_DECIMAL_PRECISION
import th.co.dv.p2p.common.constants.currencyNotSupport
import th.co.dv.p2p.corda.base.domain.Amount
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import net.corda.core.contracts.Amount as OldAmount

fun <T : Any> amountFrom(amount: Int, token: T): Amount<T> = Amount.fromDecimal(BigDecimal.valueOf(amount.toLong()), token)

fun THB(amount: Int): Amount<Currency> = amountFrom(amount, THB)
fun THB(amount: Double): Amount<Currency> = DVAMOUNT(amount, THB)
fun THB(amount: Long): Amount<Currency> = DVAMOUNT(amount, THB)
// TODO: Stop using Double functions and use Big Decimal for arithmatic functions
fun Double.scaleToPenny(tokenSize: Int = 2): Long = this.toDecimal().movePointRight(tokenSize).setScale(0, RoundingMode.HALF_UP).toLong()

fun Double.toAmount(currency: String): Amount<Currency> = Amount(this.scaleToPenny(currency.CURRENCY.defaultFractionDigits), currency.CURRENCY)
fun Double.scaleUp(newScale: Int): Double = BigDecimal(this.toString()).setScale(newScale, RoundingMode.HALF_UP).toDouble()
fun Double.scaleDown(newScale: Int): Double = BigDecimal(this.toString()).setScale(newScale,RoundingMode.HALF_DOWN).toDouble()
/** Currency helpers. */
fun Long.toDecimal(tokenSize: Int = 2): BigDecimal = BigDecimal(this.toString()).movePointLeft(tokenSize)

fun Double.toDecimal(): BigDecimal = BigDecimal(this.toString())
fun BigDecimal.toNewAmount(currency: String): Amount<Currency> = Amount.fromDecimal(this.scaleUp(currency.CURRENCY.defaultFractionDigits), currency.CURRENCY)
fun BigDecimal.toAmount(currency: String): OldAmount<Currency> = OldAmount.fromDecimal(this.scaleUp(currency.CURRENCY.defaultFractionDigits), currency.CURRENCY)
fun BigDecimal.scaleUp(newScale: Int): BigDecimal = this.setScale(newScale,RoundingMode.HALF_UP)
fun BigDecimal.scaleDown(newScale: Int): BigDecimal = this.setScale(newScale, RoundingMode.HALF_DOWN)
fun BigDecimal.negativeToZero(): BigDecimal = if (this.isLessThan(BigDecimal.ZERO)) BigDecimal.ZERO else this
fun BigDecimal?.isEqual(other: BigDecimal?): Boolean {
    return if (this != null && other != null) {
        this.isEqual(other)
    } else {
        this == other
    }
}

fun BigDecimal?.isNotEqual(other: BigDecimal?): Boolean {
    return if (this != null && other != null) {
        this.isNotEqual(other)
    } else {
        this != other
    }
}

/**
 * Method to convert positive value to negative
 */
fun BigDecimal.toNegative(): BigDecimal = if (this.isGreaterThan(BigDecimal.ZERO)) this.multiply((-1.0).toBigDecimal()) else this



/**
 * Takes a BigDecimal and multiply by its token fraction
 *
 * i.e 40.25 Euro has fraction digits of 2, so the nominal/minimum representable quantity is 4025 cents
 *
 * @param defaultFractionDigits the default number of fraction digits used with this currency.
 * For example, the default number of fraction digits for the Euro is 2,
 * while for the Japanese Yen it's 0.
 * In the case of pseudo-currencies, such as IMF Special Drawing Rights,
 * -1 is returned.
 */
fun BigDecimal.scaleToPenny(defaultFractionDigits: Int, roundingMode: Int = BigDecimal.ROUND_HALF_UP) : Long {
    val scale = RoundingMode.valueOf(roundingMode)
    return this.movePointRight(defaultFractionDigits).setScale(0, scale).toLong()
}
fun BigDecimal.scaleToPenny(currency: Currency, roundingMode: Int = BigDecimal.ROUND_HALF_UP) : Long = this.scaleToPenny(currency.defaultFractionDigits, roundingMode)

/**
 * Returns the sum of all values produced by [selector] function applied to each element in
 * the collection.
 */
inline fun <T> Iterable<T>.sumByDecimal(selector: (T) -> BigDecimal): BigDecimal {
    var sum: BigDecimal = BigDecimal.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * A function to divide Amount<Currency> by the BigDecimal
 * Rounds Half Up to token size
 * @return the calculation result with currency
 */
operator fun Amount<Currency>.div(other: BigDecimal): Amount<Currency> {
    val scale = this.token.defaultFractionDigits
    val value = (this.toDecimal().divide(other, scale, RoundingMode.HALF_UP))
    return Amount.fromDecimal(value, this.token)
}
/**
 * This function use to deplete a Quantity object by increasing the Quantity.consumed
 * this will auto calculate the Quantity.remaining
 *
 * @param amount is the amount to increase the consumed field. This must be non-negative value.
 */
fun Quantity.deplete(amount: BigDecimal): Quantity {
    when {
        amount.isLessThan(BigDecimal.ZERO) -> throw IllegalArgumentException("Quantity.deplete amount must be non-negative value, amount: $amount")
        amount.isGreaterThan(remaining) -> throw IllegalArgumentException("Quantity.deplete amount must be less than or equal to remaining, remaining: ${this.remaining.setScale().toPlainString()} amount: $amount")
    }

    return plus(Quantity(BigDecimal.ZERO, amount, unit))
}

/**
 * This function use to restore a Quantity object by decreasing the Quantity.consumed
 * this will auto calculate the Quantity.remaining
 *
 * @param amount is the amount to decrease the consumed field. This must be non-negative value.
 */
fun Quantity.restore(amount: BigDecimal): Quantity {
    when {
        amount.isLessThan(BigDecimal.ZERO) -> throw IllegalArgumentException("Quantity.restore amount must be non-negative value, amount: $amount")
        amount.isGreaterThan(consumed) -> throw IllegalArgumentException("Quantity.restore amount must be less than or equal to consumed, consumed: $consumed  amount: $amount")
    }
    return plus(Quantity(BigDecimal.ZERO, amount.negate(), unit))
}

/**
 * Method for sum list of Quantity
 */
fun List<Quantity>.sumAll(): Quantity {
    return this.reduce { acc, quantity -> acc.plus(quantity) }
}

/**
 * This function use to check whether amount contain negative value or not
 */
fun Amount<Currency>.isNegative(): Boolean {
    return this.quantity < 0
}

/**
 * This function use to check whether amount contain negative value or not
 */
fun Amount<Currency>.isPositive(): Boolean {
    return this.quantity > 0
}

/**
 * This function for get currency by currency code
 * @param currency: currency
 */
fun BigDecimal.setScaleByCurrency(currency: String?): BigDecimal {

    val token = try {
        currency!!.CURRENCY
    } catch (e: Exception) {
        throw IllegalArgumentException("$currencyNotSupport : $currency")
    }

    return this.setScaleByCurrency(token)
}

/**
 * This function for set scale according to currency
 * @param currency: currency
 */
fun BigDecimal.setScaleByCurrency(currency: Currency): BigDecimal {
    return this.setScale(currency.defaultFractionDigits, RoundingMode.HALF_UP)
}

/**
 * This function for set scale of BigDecimal to 10
 */
fun BigDecimal.setScale():BigDecimal{
    return this.setScale(MAX_DECIMAL_PRECISION, RoundingMode.HALF_UP)
}
