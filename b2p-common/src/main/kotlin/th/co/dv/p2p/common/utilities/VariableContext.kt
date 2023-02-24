package th.co.dv.p2p.common.utilities

/**
 * Object for keep key that used to store in redis in each thread
 */
object VariableContext {
    private val currentVariable: ThreadLocal<List<String>> = object : ThreadLocal<List<String>>() {
        override fun initialValue(): List<String> {
            return emptyList()
        }
    }

    fun setCurrentVariable(tenant: List<String>) {
        currentVariable.set(tenant)
    }

    fun getCurrentVariable(): List<String> {
        return currentVariable.get()
    }

    fun clear() {
        currentVariable.remove()
    }
}