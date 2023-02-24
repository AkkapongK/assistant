package th.co.dv.p2p.common.base.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.enums.Command
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.exceptions.InternalRedisException
import th.co.dv.p2p.common.exceptions.LockingRecordException
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.models.BuyerVendorModel
import th.co.dv.p2p.common.models.ContractModel
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.common.utilities.AuthorizationUtils.validateModelAuthorization
import th.co.dv.p2p.common.utilities.MapUtility.eventStateModelMapper
import th.co.dv.p2p.common.utilities.MapUtility.jacksonObjectMapperInstance
import th.co.dv.p2p.common.utilities.getFieldValue
import th.co.dv.p2p.common.utilities.getUsernameFromSecurityContext
import th.co.dv.p2p.common.utilities.setFieldValue
import th.co.dv.p2p.corda.base.IllegalFlowException
import th.co.dv.p2p.corda.base.models.*
import java.time.Duration

val logger: Logger = LoggerFactory.getLogger("RedisUtility")
val LOCK_RECORD_TIMEOUT: Duration = Duration.ofMinutes(120)

/**
 * Method for get data from redis by transaction and service
 * the key that used to find record be in pattern [transactionId]:[serviceCode]:*
 */
fun RedisTemplate<String, String>.findRecord(transactionId: String, service: Services, sponsor: String): List<String?> {
    val redisTemplate = this

    val finalKey = completeRedisKey<Any?>(
            service = service,
            sponsor = sponsor,
            transactionId = transactionId,
            documentKeyFn = { STAR }) ?: throw LockingRecordException(keyIsNull)

    val existingKey = redisTemplate.keys(finalKey)
    if (existingKey.isNotEmpty()) {
        return existingKey.map {
            redisTemplate.opsForValue().get(it)
        }
    } else {
        throw InternalRedisException("$CANNOT_FIND_RECORD key: $finalKey")
    }
}

fun RedisTemplate<String, String>.getDataByKey(key: String): String? {
    return this.opsForValue().get(key)
}


/**
 * Method for get data from redis by transaction and service and map data to actual model
 * the key that used to find record be in pattern [transactionId]:[serviceCode]:*
 */
fun RedisTemplate<String, String>.findAndMapRecord(transactionId: String, service: Services, sponsor: String): List<EventStateModel> {
    val relatedData = this.findRecord(
            transactionId = transactionId,
            sponsor = sponsor,
            service = service)

    return relatedData.mapNotNull { if (it.isNullOrBlank().not()) eventStateModelMapper(it!!) else null }
}

/**
 * Method for get related data
 */
fun RedisTemplate<String, String>.getRelatedData(transactionId: String, service: Services, sponsor: String): Map<String?, EventStateModel> {
    val relatedData = this.findAndMapRecord(
            transactionId = transactionId,
            service = service,
            sponsor = sponsor)

    return relatedData.groupBy { it.command }.mapValues { (_, data) ->
        val nextState = AllStates()
        val previousState = AllStates()
        data.forEach { eventState ->
            nextState.add(eventState.nextState)
            previousState.add(eventState.previousState)
        }
        EventStateModel(nextState = nextState, previousState = previousState, relatedServices = emptyList())
    }
}


/**
 * Method for delete record by transaction and service
 * the key that used to delete record be in pattern [transactionId]:[serviceCode]:*
 */
fun RedisTemplate<String, String>.deleteRecord(transactionId: String, service: Services, sponsor: String) {
    val redisTemplate = this

    val finalKey = completeRedisKey<Any>(
            service = service,
            sponsor = sponsor,
            transactionId = transactionId,
            documentKeyFn = { STAR }) ?: throw LockingRecordException(keyIsNull)

    val existingKey = redisTemplate.keys(finalKey)
    if (existingKey.isNotEmpty()) redisTemplate.delete(existingKey)
}

/**
 * Method to delete record in redis by document key
 *
 * @param models: List of entity that want to lock
 * @param transactionId: The transaction id that used for that request
 * @param service: Service name of the request
 * @param getKey: Function to generate key (return in String)
 */
fun <T> RedisTemplate<String, String>.deleteRecordByKey(
    models: List<T>,
    service: Services,
    sponsor: String,
    transactionId: String,
    getKey: (T) -> String?
) {
    val redisTemplate = this
    models.forEach { entity ->
        val finalKey = completeRedisKey(
                service = service,
                sponsor = sponsor,
                transactionId = transactionId,
                model = entity,
                documentKeyFn = getKey) ?: throw LockingRecordException(keyIsNull)
        val existingKey = redisTemplate.keys(finalKey)
        if (existingKey.isNotEmpty()) redisTemplate.delete(existingKey)
    }
}


/**
 * Method that used to lock target record after locked other request cannot create or update on same document
 * If try to do the system will throw [LockingRecordException]
 *
 * @param models: List of entity that want to lock
 * @param transactionId: The transaction id that used for that request
 * @param sponsor: sponsor of the request
 * @param getKey: Function to generate key (return in String)
 */
fun <T> RedisTemplate<String, String>.lockRecord(
    models: List<T>,
    sponsor: String,
    service: Services,
    transactionId: String,
    getKey: (T) -> String?
) {

    val redisTemplate = this
    val keyGroups = models.groupBy { entity ->
        completeRedisKey(
                sponsor = sponsor,
                service = service,
                model = entity,
                documentKeyFn = getKey)
    }

    keyGroups.forEach { (validationKey, keyModels) ->
        if (validationKey == null) return@forEach
        val existingKey = redisTemplate.keys(validationKey)
        if (existingKey.isEmpty()) {
            val entity = keyModels.first()
            val finalKey = completeRedisKey(
                sponsor = sponsor,
                service = service,
                transactionId = transactionId,
                model = entity,
                documentKeyFn = getKey) ?: throw LockingRecordException(keyIsNull)
            val isSet = redisTemplate.opsForValue().setIfAbsent(finalKey, "", LOCK_RECORD_TIMEOUT) ?: false
            if (isSet.not()) {
                logger.error("$duplicateRecord transactionId: $transactionId, key: $validationKey, locked by: $existingKey")
                throw LockingRecordException(duplicateRecord)
            }
        } else {
            logger.error("$duplicateRecord transactionId: $transactionId, key: $validationKey, locked by: $existingKey")
            throw LockingRecordException(duplicateRecord)
        }
    }
}


/**
 * Method that used to update target record as a temporary database
 * If try to do the system will throw [InternalRedisException]
 *
 * @param models: List of entity that want to update
 * @param command: command of this transaction
 * @param transactionId: The transaction id that used for that request
 * @param service: Service name of the request
 * @param getKey: Function to generate key (return in String)
 */
fun <T: Any> RedisTemplate<String, String>.updateRecord(
    models: List<T>,
    previousModels: List<T>,
    command: Command,
    sponsor: String,
    service: Services,
    transactionId: String,
    isUpdateLockedRecord: Boolean = true,
    getKey: (T) -> String?
) {

    validateModelAuthorization(models + previousModels)
    val redisTemplate = this
    val previousModelMap = previousModels.associateBy {
        completeRedisKey(
            sponsor = sponsor,
            service = service,
            transactionId = transactionId,
            model = it,
            documentKeyFn = getKey) ?: throw LockingRecordException(keyIsNull)
    }.toMutableMap()

    models.forEach { model ->
        val finalKey = completeRedisKey(
            sponsor = sponsor,
            service = service,
            transactionId = transactionId,
            model = model,
            documentKeyFn = getKey) ?: throw LockingRecordException(keyIsNull)

        val previousModel = previousModelMap.remove(finalKey)
        val updatedNextModel = updateModel(previousModel, model)

        updateRedisProcess(
            redisTemplate = redisTemplate,
            finalKey = finalKey,
            previousModel = previousModel,
            nextModel = updatedNextModel,
            command = command,
            isUpdateLockedRecord = isUpdateLockedRecord
        )
    }
    // loop to update previous that not match with new model
    previousModelMap.forEach { (key, previous) ->
        updateRedisProcess(
            redisTemplate = redisTemplate,
            finalKey = key,
            previousModel = previous,
            nextModel = null,
            command = command,
            isUpdateLockedRecord = isUpdateLockedRecord
        )
    }
}


/**
 * Method that used to update data into redis
 *
 * @param redisTemplate: Redis service
 * @param finalKey: target key
 * @param previousModel: previous state
 * @param nextModel: next state
 * @param command: Command of this transaction
 */
fun <T: Any> updateRedisProcess(
    redisTemplate: RedisTemplate<String, String>,
    finalKey: String,
    previousModel: T?,
    nextModel: T?,
    command: Command,
    isUpdateLockedRecord: Boolean
) {

    try {
        val data = jacksonObjectMapperInstance.writeValueAsString(
                EventStateModel(
                        previousState = buildAllState(previousModel),
                        nextState = nextModel?.let { buildAllState(it) } ?: AllStates(),
                        command = command.getCommandName(),
                        relatedServices = emptyList()
                )
        )
        val isSet = if (isUpdateLockedRecord) {
            redisTemplate.opsForValue().setIfPresent(finalKey, data, LOCK_RECORD_TIMEOUT)
        } else {
            redisTemplate.opsForValue().setIfAbsent(finalKey, data, LOCK_RECORD_TIMEOUT)
        } ?: false
        if (isSet.not()) {
            throw LockingRecordException("$cannotUpdateRecord key: $finalKey")
        }
    } catch (e: Exception) {
        throw LockingRecordException("$cannotUpdateRecord key: $finalKey")
    }
}

/**
 * Method for update field issuedBy for invoice, creditnote, debitnote
 */
@Suppress("UNCHECKED_CAST")
fun <T: Any> updateModel(previousModel: T?, model: T): T {
    if(previousModel != null) return model
    return when (model) {
        is InvoiceModel, is CreditNoteModel, is DebitNoteModel  -> {
            val targetFieldName = InvoiceModel::issuedBy.name
            val issuedBy = model.getFieldValue<String>(targetFieldName)
            if (issuedBy.isNullOrBlank()) {
                val username = getUsernameFromSecurityContext()
                model.setFieldValue(targetFieldName, username)
            }
            model
        }
        else -> model
    }
}

fun <T> buildAllState(model: T?): AllStates? {
    if (model == null) return null
    return when (model) {
        is InvoiceModel -> AllStates(invoices = listOf(model))
        is InvoiceItemModel -> AllStates(invoiceItems = listOf(model))
        is PurchaseOrderModel -> AllStates(purchaseOrders = listOf(model))
        is PurchaseItemModel -> AllStates(purchaseItems = listOf(model))
        is GoodsReceivedModel -> AllStates(goodsReceiveds = listOf(model))
        is GoodsReceivedItemModel -> AllStates(goodsReceivedItems = listOf(model))
        is CreditNoteModel -> AllStates(creditNotes = listOf(model))
        is CreditNoteItemModel -> AllStates(creditNoteItems = listOf(model))
        is DebitNoteModel -> AllStates(debitNotes = listOf(model))
        is DebitNoteItemModel -> AllStates(debitNoteItems = listOf(model))
        is RequestModel -> AllStates(requests = listOf(model))
        is RequestItemModel -> AllStates(requestItems = listOf(model))
        is PaymentModel -> AllStates(payments = listOf(model))
        is TaxDocumentModel -> AllStates(taxDocuments = listOf(model))
        is BuyerVendorModel -> AllStates(buyerVendors = listOf(model))
        is ContractModel -> AllStates(contracts = listOf(model))
        is FinanceableDocumentModel -> AllStates(financeableDocuments = listOf(model))
        is RepaymentRequestModel -> AllStates(repaymentRequests = listOf(model))
        is RepaymentHistoryModel -> AllStates(repaymentHistories = listOf(model))
        is LoanModel -> AllStates(loans = listOf(model))
        is LoanProfileModel -> AllStates(loanProfiles = listOf(model))
        else -> throw IllegalFlowException(DOCUMENT_NOT_SUPPORT)
    }
}

fun AllStates.add(newState: AllStates?) {
    if (newState == null) return
    this.invoices = (this.invoices ?: emptyList()) + (newState.invoices ?: emptyList())
    this.invoiceItems = (this.invoiceItems ?: emptyList()) + (newState.invoiceItems ?: emptyList())
    this.purchaseOrders = (this.purchaseOrders ?: emptyList()) + (newState.purchaseOrders ?: emptyList())
    this.purchaseItems = (this.purchaseItems ?: emptyList()) + (newState.purchaseItems ?: emptyList())
    this.goodsReceiveds = (this.goodsReceiveds ?: emptyList()) + (newState.goodsReceiveds ?: emptyList())
    this.goodsReceivedItems = (this.goodsReceivedItems ?: emptyList()) + (newState.goodsReceivedItems ?: emptyList())
    this.creditNotes = (this.creditNotes ?: emptyList()) + (newState.creditNotes ?: emptyList())
    this.creditNoteItems = (this.creditNoteItems ?: emptyList()) + (newState.creditNoteItems ?: emptyList())
    this.debitNotes = (this.debitNotes ?: emptyList()) + (newState.debitNotes ?: emptyList())
    this.debitNoteItems = (this.debitNoteItems ?: emptyList()) + (newState.debitNoteItems ?: emptyList())
    this.requests = (this.requests ?: emptyList()) + (newState.requests ?: emptyList())
    this.requestItems = (this.requestItems ?: emptyList()) + (newState.requestItems ?: emptyList())
    this.payments = (this.payments ?: emptyList()) + (newState.payments ?: emptyList())
    this.taxDocuments = (this.taxDocuments ?: emptyList()) + (newState.taxDocuments ?: emptyList())
    this.buyerVendors = (this.buyerVendors ?: emptyList()) + (newState.buyerVendors ?: emptyList())
    this.contracts = (this.contracts ?: emptyList()) + (newState.contracts ?: emptyList())
    this.financeableDocuments = (this.financeableDocuments ?: emptyList()) + (newState.financeableDocuments ?: emptyList())
    this.repaymentRequests = (this.repaymentRequests ?: emptyList()) + (newState.repaymentRequests ?: emptyList())
    this.repaymentHistories = (this.repaymentHistories ?: emptyList()) + (newState.repaymentHistories ?: emptyList())
    this.loans = (this.loans ?: emptyList()) + (newState.loans ?: emptyList())
    this.loanProfiles = (this.loanProfiles ?: emptyList()) + (newState.loanProfiles ?: emptyList())
}


/**
 * Method for complete key
 * the complete key be in pattern [transactionId]:[sponsor]:[serviceCode]:[documentKey]
 */
fun <T: Any?> completeRedisKey(
    sponsor: String,
    service: Services,
    transactionId: String? = null,
    model: T? = null,
    documentKeyFn: (T) -> String?
): String? {

    val finalTransactionId = transactionId ?: STAR
    val finalService = service.code

    val finalDocumentKey = when (model == null) {
        true -> STAR
        false -> documentKeyFn(model)
    } ?: return null
    return finalTransactionId + COLON + sponsor.lowercase() + COLON + finalService + COLON + finalDocumentKey
}


/**
 * This method will return data from steaming model or redis
 */
fun RedisTemplate<String, String>.getData(streamingModel: StreamingModel<EventStateModel>): EventStateModel? {
    val redisKey = streamingModel.redisKey
    val data = streamingModel.data
    return when (redisKey != null) {
        true -> this.getDataByKey(redisKey)?.let { eventStateModelMapper(it) }
        false -> data
    }
}


