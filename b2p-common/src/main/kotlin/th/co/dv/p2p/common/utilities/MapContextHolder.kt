package th.co.dv.p2p.common.utilities

class MapContextHolder {
    companion object {
        private val contextHolder = ThreadLocal<MutableMap<String, String>>()

        fun setMap(key: String, value: String) {
            val currentMap = contextHolder.get() ?: mutableMapOf()
            currentMap[key] = value
            contextHolder.set(currentMap)
        }

        fun getValue(key: String): String? {
            return contextHolder.get()?.let { it[key] }
        }

        fun clear() {
            contextHolder.remove()
        }
    }
}