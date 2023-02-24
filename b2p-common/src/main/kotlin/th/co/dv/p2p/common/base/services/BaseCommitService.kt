package th.co.dv.p2p.common.base.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.logging.LogFactory
import org.springframework.data.redis.core.RedisTemplate
import th.co.dv.p2p.common.base.enums.MessageType
import th.co.dv.p2p.common.base.utilities.deleteRecord
import th.co.dv.p2p.common.base.utilities.getRelatedData
import th.co.dv.p2p.common.constants.CANNOT_FIND_RECORD
import th.co.dv.p2p.common.constants.SPONSOR_CANNOT_BE_NULL
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.corda.base.IllegalFlowException

/**
 * abstract class for handle commit event
 *
 * 1. Get related data from redis by transaction id and service
 * 2. transform data and store related data to database
 * 3. delete record from redis
 * 4. produce broadcast event
 */
abstract class BaseCommitService<T: Any>(
        val modelClass: Class<T>,
        val service: Services
) {

    protected val logger = LogFactory.getLog(BaseCommitService::class.java)!!
    protected val className = BaseCommitService::class.java.simpleName

    protected val redisTemplate by lazy { initRedisTemplate() }

    // TODO: Need to override
    protected abstract fun initRedisTemplate(): RedisTemplate<String, String>

    private val mapper = jacksonObjectMapper()
    private val maxRetry = 3

    /**
     * Main method
     *
     * @return flag that let us known process is finish or not, [null] if skip message
     */
    open fun process(streamingModel: StreamingModel<Any>, retry: Int = 0, callbackFn: (StreamingModel<Any>) -> Unit = {}) {

        if (streamingModel.messageType != MessageType.COMMIT || streamingModel.relatedServices.contains(service).not()) return

        val commandWithData: Map<String?, EventStateModel>

        if (streamingModel.sponsor == null) throw IllegalArgumentException(SPONSOR_CANNOT_BE_NULL)

        val updatedEventStateModel = mutableMapOf<String?, EventStateModel>()

        try {
            // 1. Get related data
            commandWithData = redisTemplate.getRelatedData(streamingModel.id, service, streamingModel.sponsor!!)

            if (commandWithData.isEmpty()) throw IllegalFlowException(CANNOT_FIND_RECORD)

            // 2. Transform model to entity and save
            commandWithData.forEach { (eachCommand, data) ->
                updatedEventStateModel[eachCommand] = transformAndSave(eachCommand, data)
            }

        } catch (e: Exception) {
            if (retry < maxRetry) return process(streamingModel, (retry + 1), callbackFn)
            else throw e
        }

        callbackFn(streamingModel)
        updatedEventStateModel.forEach { (command, data) ->
            processAfterStored(
                    streamingModel = streamingModel,
                    command = command,
                    eventStateModel = data)
        }
    }

    /**
     * Method for process after stored data to database
     */
    open protected fun processAfterStored(
        streamingModel: StreamingModel<Any>,
        command: String?,
        eventStateModel: EventStateModel
    ) {
        if (streamingModel.sponsor == null) throw IllegalArgumentException(SPONSOR_CANNOT_BE_NULL)
        // Delete data from redis
        deleteRecord(streamingModel.id, streamingModel.sponsor!!)

        // broadcast
        broadcastMessage(
                transactionId = streamingModel.id,
                command = command,
                service = service,
                relatedService = streamingModel.relatedServices,
                eventStateModel = eventStateModel
        )

        logger.info("$className.processAfterStored done: ${streamingModel.id}")
        sendToExternalQueue(streamingModel.id, streamingModel.sponsor!!, eventStateModel, command)
        sendToInternalQueue(streamingModel.id, eventStateModel, command)
    }


    /**
     * Method to delete record from redid by transaction and service
     */
    open protected fun deleteRecord(transactionId: String, sponsor: String) {
        redisTemplate.deleteRecord(transactionId, service, sponsor)
    }

    /**
     * Method for publish broadcast message
     */
    open protected fun broadcastMessage(transactionId: String, command: String?, service: Services, relatedService: List<Services>, eventStateModel: EventStateModel) {
        /**
         * Not implement here
         * The service that inherit from this must override
         */
    }

    /**
     * Method for transform data and save to database
     */
    abstract fun transformAndSave(command: String?, eventStateModel: EventStateModel): EventStateModel

    /**
     * Method for publish message to external queue
     */
    open protected fun sendToExternalQueue(transactionId: String, sponsor: String, eventStateModel: EventStateModel, command: String?) {
        /**
         * Not implement here
         * The service that inherit from this must override
         */
    }

    /**
     * Method for publish message to internal queue
     */
    open protected fun sendToInternalQueue(transactionId: String, eventStateModel: EventStateModel, command: String?) {
        /**
         * Not implement here
         * The service that inherit from this must override
         */
    }

}