package th.co.dv.p2p.common.base.services

import org.apache.commons.logging.LogFactory
import org.springframework.data.redis.core.RedisTemplate
import th.co.dv.p2p.common.base.enums.MessageType
import th.co.dv.p2p.common.base.utilities.deleteRecord
import th.co.dv.p2p.common.base.utilities.findRecord
import th.co.dv.p2p.common.constants.MESSAGE_TYPE_NOT_SUPPORT
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.exceptions.InternalRedisException
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.corda.base.IllegalFlowException

abstract class BaseNotifyService(val service: Services) {

    protected val logger = LogFactory.getLog(BaseNotifyService::class.java)!!
    protected val className = BaseNotifyService::class.java.simpleName
    protected val redisTemplate by lazy { initRedisTemplate() }
    protected val producerService by lazy { initProducerService() }
    protected abstract fun initRedisTemplate(): RedisTemplate<String, String>
    protected abstract fun initProducerService(): BaseProducer

    /**
     * Main method
     */
    fun process(messageEventModel: StreamingModel<Any>) {
        when (messageEventModel.messageType) {
            MessageType.NOTIFY_SUCCESS -> processNotifySuccess(messageEventModel)
            MessageType.NOTIFY_FAILED -> processNotifyFailed(messageEventModel)
            else -> throw IllegalFlowException(MESSAGE_TYPE_NOT_SUPPORT)
        }
    }

    /**
     * Method for stream commit message
     */
    protected fun streamCommitMessage(messageEventModel: StreamingModel<Any>) {
        producerService.streamEventMessage(
            topic = service.commitTopic,
            transactionId = messageEventModel.id,
            command = "NO_COMMAND",
            data = null,
            relatedServices = messageEventModel.relatedServices,
            messageType = MessageType.COMMIT
        )
    }

    /**
     * Method for check that transaction is not finish processing for specific service
     */
    protected fun isServiceNotDone(transactionId: String, service: Services, sponsor: String): Boolean {
        return try {
            val records = redisTemplate.findRecord(transactionId = transactionId, service = service, sponsor = sponsor)
            records.isEmpty() || records.any { it.isNullOrBlank() }
        } catch (e: InternalRedisException) {
            true
        }
    }

    /**
     * Method for handle NOTIFY_FAILED, We will save message and clear all transaction in redis
     */
    protected fun processNotifyFailed(messageEventModel: StreamingModel<Any>) {
        saveErrorMessage(messageEventModel)
        clearTransactionInRedis(messageEventModel)
    }

    /**
     * Method for handle NOTIFY_SUCCESS,
     * We will check if transaction is still available, if not we will clear transaction in redis
     */
    protected fun processNotifySuccess(messageEventModel: StreamingModel<Any>) {
        // check if current transaction still exist
        val isSelfServiceNotDone = isServiceNotDone(transactionId = messageEventModel.id, service = service, sponsor = messageEventModel.sponsor!!)
        if (isSelfServiceNotDone) {
            logger.warn("$className.processNotifySuccess not found $service records for " +
                    "transaction ${messageEventModel.id} service: ${messageEventModel.type} " +
                    "sponsor ${messageEventModel.sponsor} relatedService: ${messageEventModel.relatedServices}")
            return
        }

        // check that is there any service that not finish processing
        val isTransactionNotDone = messageEventModel.relatedServices.any {
            // since our service already done
            if (it == service) false
            else isServiceNotDone(transactionId = messageEventModel.id, service = it, sponsor = messageEventModel.sponsor!!)
        }
        // when some service not finish processing, do nothing
        if (isTransactionNotDone) return

        // stream commit message
        streamCommitMessage(messageEventModel)
    }

    protected fun clearTransactionInRedis(messageEventModel: StreamingModel<Any>) {
        messageEventModel.relatedServices.forEach {
            redisTemplate.deleteRecord(transactionId = messageEventModel.id, service = it, sponsor = messageEventModel.sponsor!!)
        }
    }

    /**
     * Method for save error message
     */
    protected abstract fun saveErrorMessage(messageEventModel: StreamingModel<Any>)
}