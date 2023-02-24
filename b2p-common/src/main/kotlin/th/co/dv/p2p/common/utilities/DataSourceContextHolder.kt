package th.co.dv.p2p.common.utilities

import th.co.dv.p2p.common.models.StreamingModel

class DataSourceContextHolder {
    companion object {
        private val contextHolder = ThreadLocal<String>()

        fun setCurrentDb(dbType: String) {
            contextHolder.set(dbType)
        }

        fun getCurrentDb(): String? {
            return contextHolder.get()
        }

        fun clear() {
            contextHolder.remove()
        }

        /**
         * Method for set Sponsor before stream
         * @param modelInput: Data that we sent to the kafka server.
         */
        fun prepareSponsor(modelInput: StreamingModel<*>): StreamingModel<out Any?> {
            return modelInput.copy(
                sponsor = SponsorContextHolder.getCurrentSponsor()
            )
        }
    }
}