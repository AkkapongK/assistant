package th.co.dv.p2p.common.utilities

class SponsorContextHolder {
    companion object {
        private val contextHolder = ThreadLocal<String?>()

        fun setSponsor(sponsor: String?) {
            contextHolder.set(sponsor)
        }

        fun getCurrentSponsor(): String? {
            return contextHolder.get()
        }

        fun clear() {
            contextHolder.remove()
        }
    }
}