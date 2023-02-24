package th.co.dv.p2p.common.base.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.TRANSACTION_ID
import th.co.dv.p2p.common.utilities.MapContextHolder
import th.co.dv.p2p.common.utilities.SponsorContextHolder

class CustomLogger(clazz: Class<*>) {

    val logger: Logger

    init {
        logger = LoggerFactory.getLogger(clazz)
    }

    val isDebugEnabled: Boolean
        get() = logger.isDebugEnabled

    val isInfoEnabled: Boolean
        get() = logger.isInfoEnabled

    val isWarnEnabled: Boolean
        get() = logger.isWarnEnabled

    val isErrorEnabled: Boolean
        get() = logger.isErrorEnabled

    val isTraceEnabled: Boolean
        get() = logger.isTraceEnabled

    fun trace(msg: String, t: Throwable? = null) {
        val finalMsg = getPrefix() + msg
        if (t != null) logger.trace(finalMsg, t) else logger.trace(finalMsg)
    }

    fun debug(msg: String, t: Throwable? = null) {
        val finalMsg = getPrefix() + msg
        if (t != null) logger.debug(finalMsg, t) else logger.debug(finalMsg)
    }

    fun info(msg: String, t: Throwable? = null) {
        val finalMsg = getPrefix() + msg
        if (t != null) logger.info(finalMsg, t) else logger.info(finalMsg)
    }

    fun warn(msg: String, t: Throwable? = null) {
        val finalMsg = getPrefix() + msg
        if (t != null) logger.warn(finalMsg, t) else logger.warn(finalMsg)
    }

    fun error(msg: String, t: Throwable? = null) {
        val finalMsg = getPrefix() + msg
        if (t != null) logger.error(finalMsg, t) else logger.error(finalMsg)
    }

    private fun getPrefix(): String {
        return "${MapContextHolder.getValue(TRANSACTION_ID)}:${SponsorContextHolder.getCurrentSponsor()}:"
    }
}