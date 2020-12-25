package th.co.dv.b2p.linebot

sealed class Try<out A> {
    companion object {
        /**
         * Executes the given block of code and returns a [Success] capturing the result, or a [Failure] if an exception
         * is thrown.
         */
        @JvmStatic
        inline fun <T> on(body: () -> T): Try<T> {
            return try {
                Success(body())
            } catch (t: Throwable) {
                Failure(t)
            }
        }
    }

    /** Returns `true` iff the [Try] is a [Success]. */
    abstract val isFailure: Boolean

    /** Returns `true` iff the [Try] is a [Failure]. */
    abstract val isSuccess: Boolean

    /** Returns the value if a [Success] otherwise throws the exception if a [Failure]. */
    abstract fun getOrThrow(): A

    data class Success<out A>(val value: A) : Try<A>() {
        override val isSuccess: Boolean get() = true
        override val isFailure: Boolean get() = false
        override fun getOrThrow(): A = value
        override fun toString(): String = "Success($value)"
    }

    data class Failure<out A>(val exception: Throwable) : Try<A>() {
        override val isSuccess: Boolean get() = false
        override val isFailure: Boolean get() = true
        override fun getOrThrow(): A = throw exception
        override fun toString(): String = "Failure($exception)"
    }
}
