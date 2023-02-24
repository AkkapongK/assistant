package th.co.dv.p2p.common.utilities

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.base.Enums
import com.google.gson.Gson
import net.corda.core.contracts.Amount.Companion.sumOrNull
import net.corda.core.contracts.Amount.Companion.sumOrThrow
import net.corda.core.contracts.Amount.Companion.sumOrZero
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.Crypto
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.NodeInfo
import net.corda.core.node.ServiceHub
import org.slf4j.Logger
import th.co.dv.p2p.common.constants.DOT
import th.co.dv.p2p.common.constants.failedRequirement
import th.co.dv.p2p.common.constants.percent
import th.co.dv.p2p.common.enums.BaseMessageError
import th.co.dv.p2p.common.models.ConfigurationByTaxIdModel
import th.co.dv.p2p.common.models.SearchInput
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.corda.base.IllegalFlowException
import th.co.dv.p2p.corda.base.domain.Amount
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.DelegationOfAuthorityModel
import th.co.dv.p2p.corda.base.models.PartyModel
import java.io.IOException
import java.lang.reflect.Field
import java.math.BigDecimal
import java.math.BigInteger
import java.security.PublicKey
import java.text.MessageFormat
import java.util.*
import java.util.function.Predicate
import javax.persistence.AttributeConverter
import net.corda.core.contracts.Amount as CordaAmount

fun String.toX500() = CordaX500Name.parse(this)

object Conditions {
    /** Throws [IllegalFlowException] if the given expression evaluates to false. */
    @Suppress("NOTHING_TO_INLINE")   // Inlining this takes it out of our committed ABI.
    inline infix fun String.using(expr: Boolean) {
        if (!expr) throw IllegalFlowException(failedRequirement.format(this))
    }

    /** Throws [IllegalFlowException] without [failedRequirement] if the given expression evaluates to false. */
    @Suppress("NOTHING_TO_INLINE")   // Inlining this takes it out of our committed ABI.
    inline infix fun String.usingWithout(expr: Boolean) {
        if (!expr) throw IllegalFlowException(this)
    }

    /** Throws [custom exception] if the given expression evaluates to false. */
    @Suppress("NOTHING_TO_INLINE")   // Inlining this takes it out of our committed ABI.
    inline infix fun <reified E : Throwable> String.usingWith(expr: Boolean) {
        val customException = E::class.java.getConstructor(String::class.java).newInstance(failedRequirement.format(this))
        if (!expr) throw customException
    }

    /** Throws [IllegalFlowException] if the given expression evaluates to false. */
    @Suppress("NOTHING_TO_INLINE")   // Inlining this takes it out of our committed ABI.
    inline infix fun BaseMessageError.using(expr: Boolean) {
        if (!expr) throw IllegalFlowException(failedRequirement, this)
    }

    /** Throws [IllegalFlowException] without [failedRequirement] if the given expression evaluates to false. */
    @Suppress("NOTHING_TO_INLINE")   // Inlining this takes it out of our committed ABI.
    inline infix fun BaseMessageError.usingWithout(expr: Boolean) {
        if (!expr) throw IllegalFlowException(this)
    }
}

inline fun <R> requireAll(body: Conditions.() -> R) = Conditions.body()

/**
 * Method to do object deep copy, since method (data class).copy() do shallow copy
 */
inline fun <reified T> T.deepCopy(): T = Gson().run {
    fromJson(toJson(this@deepCopy), T::class.java) as T
}

/**
 * Serializers helpers
 */
fun buildFromJson(jsonPayload: String, fieldName: String = "value"): String? {
    val jsonNode = ObjectMapper().readTree(jsonPayload)
    return jsonNode.findValue(fieldName)?.asText()
}

// If T is provided, we try to convert json string to T object
// i.e buildFromJson<List<NonWorkingDay>>(url)
inline fun <reified T : Any> buildFromJson(jsonPayload: String, fieldName: String = "value"): T {
    val jsonString = buildFromJson(jsonPayload, fieldName)
    return jacksonObjectMapper().readValue(jsonString.toString())
}

/**
 * To convert json string from offchain into List<ConfigurationByTaxIdModel>
 * @param jsonString
 */
fun jsonToConfigurationByTaxIdModels(jsonString: String): List<ConfigurationByTaxIdModel> {
    val mapper = jacksonObjectMapper()
    val node = mapper.readTree(jsonString).toList()
    return node.map {
        ConfigurationByTaxIdModel(id = it["id"].asInt(),
                companyTaxId = it["companyTaxId"].asText(),
                counterPartyTaxId = it["counterPartyTaxId"]?.asText(),
                value = it["value"].asText())
    }
}

/**
 * Helpers to resolve string to an object
 */
@Suspendable
fun String.toUUID(externalId: String? = null): UniqueIdentifier {
    return if (externalId == null) {
        UniqueIdentifier.fromString(this)
    } else {
        UniqueIdentifier(externalId, UUID.fromString(this))
    }
}

fun String?.toBoolean(exactMatch: Boolean = false): Boolean {
    return if (exactMatch) {
        when {
            this != null && this.equals("true", ignoreCase = true) -> true
            this != null && this.equals("false", ignoreCase = true) -> false
            else -> throw IllegalArgumentException("Illegal format or argument to parse.")
        }
    } else {
        java.lang.Boolean.parseBoolean(this)
    }
}

fun String.splitAndTrim(separator: String, trim: Boolean, ignoreCase: Boolean = true): List<String> {
    val list = this.split(delimiters = arrayOf(separator), ignoreCase = ignoreCase)
    return if (trim) {
        list.map { it.trim() }
    } else {
        list
    }
}

/**
 * Returns an enum entry with the specified name or `null` if no such entry was found.
 */
inline fun <reified E : Enum<E>> enumValueOrNull(name: String?, ignoreCase: Boolean = false): E? {
    if (name.isNullOrBlank()) return null

    return if (ignoreCase) {
        getEnumIgnoreCase<E>(name)
    } else {
        Enums.getIfPresent(E::class.java, name).orNull()
    }
}

/**
 * Method for map field value from list
 * @param [targetFieldName] : key of target to set value
 * @param [value] : value to set
 */
fun Any.setFieldValue(targetFieldName: String, value: Any?): Any {
    val clazz = this::class.java
    val targetField = clazz.declaredFields.singleOrNull { it.name.equals(targetFieldName, ignoreCase = false) }
    return if (targetField != null) {
        targetField.customSet(this, value)
    } else {
        throw IllegalArgumentException("Field: $targetFieldName not exist in class ${clazz.name}")
    }
}

/**
 * Method for set value to field with some specific type
 * @param obj: object of the field that we want to set value
 * @param value: new value
 */
fun Field.customSet(obj: Any, value: Any?) {
    this.isAccessible = true

    if (value == null) {
        this.set(obj, null)
    } else {
        try {
            this.set(obj, value)
        } catch (e: Exception) {
            val convertedValue = changeValueTypeToFieldType(this, value, e)
            this.customSet(obj, convertedValue)
        }
    }
}

/**
 * Method for covert type of value to be the same type of targetField
 *
 * Some value cannot add to the field because type different so we need to convert type
 * now we support BigInteger cast to Long when we got other case add in here
 *
 * @param targetField: Destination field
 * @param value: value that we want to add to [targetField]
 * @param rootException: Exception that we want to throw in case not support
 */
fun changeValueTypeToFieldType(targetField: Field, value: Any, rootException: Exception): Any {
    return when {
        targetField.type == Long::class.javaObjectType && value::class == BigInteger::class -> (value as BigInteger).toLong()
        // TODO: add other case here
        else -> throw rootException
    }
}


/**
 * Method check the field can read or not
 * @param [targetFieldName] : key of target to check readable
 */
fun Class<*>.isReadableProperty(targetFieldName: String): Boolean {
    return this.declaredFields.any { it.name.equals(targetFieldName, ignoreCase = false) }
}


/**
 *
 * Gets the enum for the class, returning `null` if not found.
 *
 *
 * This method differs from Enum.valueOf in that it does not throw an exception
 * for an invalid enum name and performs case insensitive matching of the name.
 *
 * @param <E>         the type of the enumeration
 * @param enumName    the enum name, null returns null
 * @return the enum, null if not found
 * @since 3.8
</E> */
inline fun <reified E : Enum<E>> getEnumIgnoreCase(enumName: String?): E? {
    if (enumName == null || !E::class.java.isEnum) {
        return null
    }
    for (each in E::class.java.enumConstants) {
        if (each.name.equals(enumName, ignoreCase = true)) {
            return each
        }
    }
    return null
}

/**
 * Returns a silent try on the action that catch and mute the exception in log.
 * WARNING:
 * 1. MUST be inlined, else calling subflow in the lambda will throw error
 * 2. MUST be non-@Suspendable, else quasar will throw verifyError due to SilentEvent data class
 */
inline fun <T> trySilently(logger: Logger? = null, body: () -> T): SilentEvent<T> {

    return try {
        val event = body()
        SilentEvent(body = event)
    } catch (ex: Exception) {
        println("trySilently terminated by unexpected exception ${ex.message}")
        logger?.error("trySilently exception", ex)
        SilentEvent(exception = ex)
    }
}


/** PublicKey and Party helpers. */
fun ByteArray.toPublicKey(): PublicKey = Crypto.decodePublicKey(this)

fun PublicKey.toParty(services: ServiceHub) = services.identityService.partyFromKey(this)
        ?: throw IllegalArgumentException("Unknown Party.")

fun PublicKey.toParty(services: CordaRPCOps) = services.partyFromKey(this)
        ?: throw IllegalArgumentException("Unknown Party.")

/** Helpers for filtering the network map cache using serviceHub */
fun NodeInfo.isNotary(services: ServiceHub) = services.networkMapCache.notaryIdentities.any { this.isLegalIdentity(it) }

fun NodeInfo.isMe(me: NodeInfo) = this.legalIdentities.first().name == me.legalIdentities.first().name
fun NodeInfo.isNetworkMap() = this.legalIdentities.first().name == NETWORKMAP_X500
fun NodeInfo.isSeller() = this.legalIdentities.first().name.organisationUnit == ORGANISATION_MAP[OrganisationUnit.SELLER]
fun NodeInfo.isBuyer() = this.legalIdentities.first().name.organisationUnit == ORGANISATION_MAP[OrganisationUnit.BUYER]
fun NodeInfo.isLogistics() = this.legalIdentities.first().name.organisationUnit == ORGANISATION_MAP[OrganisationUnit.LOGISTICS]
fun NodeInfo.isManufacturer() = this.legalIdentities.first().name.organisationUnit == ORGANISATION_MAP[OrganisationUnit.MANUFACTURER]
fun NodeInfo.isBank() = this.legalIdentities.first().name.organisationUnit == ORGANISATION_MAP[OrganisationUnit.BANK]

/** Helpers for converting Map to ByteArray */
//fun Any.toByteArray() = ByteArrayOutputStream().also { ObjectOutputStream(it).use { it.writeObject(this) } }.toByteArray()!!

/** Helpers for converting ByteArray to Map */
//@Suppress("UNCHECKED_CAST")
//fun <T> ByteArray.decode(): T = ObjectInputStream(this.inputStream()).use { it.readObject() as T }

/** Math Utils */
fun <T : Any> AMOUNT(amount: Double, token: T): CordaAmount<T> = CordaAmount.fromDecimal(BigDecimal.valueOf(amount), token)

fun <T : Any> AMOUNT(amount: Long, token: T): CordaAmount<T> = CordaAmount.fromDecimal(BigDecimal.valueOf(amount), token)

fun <T : Any> DVAMOUNT(amount: Double, token: T): Amount<T> = Amount.fromDecimal(BigDecimal.valueOf(amount), token)
fun <T : Any> DVAMOUNT(amount: Long, token: T): Amount<T> = Amount.fromDecimal(BigDecimal.valueOf(amount), token)

fun BigDecimal.isEqual(other: BigDecimal): Boolean = this.compareTo(other) == 0
fun BigDecimal.isNotEqual(other: BigDecimal): Boolean = this.compareTo(other) != 0
fun BigDecimal.isGreaterThan(other: BigDecimal): Boolean = this.compareTo(other) == 1
fun BigDecimal.isLessThan(other: BigDecimal): Boolean = this.compareTo(other) == -1
fun BigDecimal.isLessOrEqual(other: BigDecimal): Boolean = this.isLessThan(other) || this.isEqual(other)
fun BigDecimal.isGreaterOrEqual(other: BigDecimal): Boolean = this.isGreaterThan(other) || this.isEqual(other)


// Warning, use with non-empty list, else will throw error
fun <T : Any> Iterable<Amount<T>>.sumOrThrow(): Amount<T> = this.sumOrThrow()

fun <T : Any> Iterable<Amount<T>>.sumOrNull(): Amount<T>? = this.sumOrNull()
fun <T : Any> Iterable<Amount<T>>.sumOrZero(token: T): Amount<T> = this.sumOrZero(token)

fun <T : Any> Iterable<CordaAmount<T>>.sumOrThrow(): CordaAmount<T> = this.sumOrThrow()
fun <T : Any> Iterable<CordaAmount<T>>.sumOrNull(): CordaAmount<T>? = this.sumOrNull()
fun <T : Any> Iterable<CordaAmount<T>>.sumOrZero(token: T): CordaAmount<T> = this.sumOrZero(token)

/**
 * A function to multiple Amount<Currency> with BigDecimal,
 * Rounds Half Up to token size
 * @return the calculation result with currency
 */
operator fun Amount<Currency>.times(other: BigDecimal): Amount<Currency> {
    val value = (this.toDecimal() * other).setScaleByCurrency(this.token)
    return Amount.fromDecimal(value, this.token)
}

operator fun CordaAmount<Currency>.times(other: BigDecimal): CordaAmount<Currency> {
    val value = (this.toDecimal() * other).setScaleByCurrency(this.token)
    return CordaAmount.fromDecimal(value, this.token)
}


/** Collection Utils */

/**
 * A function that replace a specific element that are existing in the list
 * [predicate] the condition to compare against the element in the list
 * [replacement] the new element to replace the old element
 */
fun <T> List<T>.replaceIf(predicate: Predicate<in T>, replacement: T): List<T> {
    val result = this.toMutableList()
    result.replaceAll { t -> if (predicate.test(t)) replacement else t }
    return result
}

/**
 * @return true if the all the elements has distinct keySelector or list is empty
 * else false
 */
inline fun <T, K> Iterable<T>.isDistinctBy(keySelector: (T) -> K): Boolean {
    return this.distinctBy(keySelector).count() == this.count() || this.count() == 0
}

/**
 * @return true if the all the elements has equal/same keySelector or list is empty
 * else false
 */
inline fun <T, K> Iterable<T>.isEqualBy(keySelector: (T) -> K): Boolean {
    return this.distinctBy(keySelector).count() == 1 || this.count() == 0
}

/**
 * Method to convert any object to map with specific conversion for [BigDecimal].
 * As normal conversion, value 0.0000000000 is converted to 0E-10, so we need to convert it to plain text.
 */
fun Any.convertToMapSpecific(): Map<String, Any?> {
    val clazz = this::class.java
    val map = mutableMapOf<String, Any?>()
    clazz.declaredFields.forEach {
        val value = this.getFieldValue<Any>(it.name)
        map[it.name] = when (value) {
            is BigDecimal -> value.toPlainString()
            else -> value
        }
    }
    return map
}

/**
 * Class converter that convert map object to string and convert from string back to map
 */
class HashMapConverter : AttributeConverter<Map<String, Any>, String> {

    override fun convertToDatabaseColumn(customerInfo: Map<String, Any>): String? {

        val customerInfoJson: String?
        val objectMapper = jacksonObjectMapper()
        try {
            customerInfoJson = objectMapper.writeValueAsString(customerInfo)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("HashMapConverter writing error", e)
        }

        return customerInfoJson
    }

    override fun convertToEntityAttribute(customerInfoJSON: String): Map<String, Any>? {

        val customerInfo: Map<String, Any>?
        val objectMapper = jacksonObjectMapper()
        try {
            customerInfo = objectMapper.readValue(customerInfoJSON)
        } catch (e: IOException) {
            throw IllegalArgumentException("HashMapConverter reading error", e)
        }

        return customerInfo
    }

}

/**
 * Class converter that convert quantity to string and convert from string back to quantity
 */
class QuantityConverter : AttributeConverter<Quantity?, String?> {

    override fun convertToDatabaseColumn(quantity: Quantity?): String? {

        val quantityJson: String?
        val objectMapper = jacksonObjectMapper()
        try {
            val quantityMap = quantity?.convertToMapSpecific()
            quantityJson = quantityMap?.let { objectMapper.writeValueAsString(it) }
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("QuantityConverter writing error", e)
        }

        return quantityJson
    }

    override fun convertToEntityAttribute(quantityJson: String?): Quantity? {

        val quantity: Quantity?
        val objectMapper = jacksonObjectMapper()
        try {
            quantity = quantityJson?.let { objectMapper.readValue(it) }
        } catch (e: IOException) {
            throw IllegalArgumentException("QuantityConverter reading error", e)
        }

        return quantity
    }

}

/**
 * Class converter that convert net.corda.core.contracts.Amount<Currency> to string and convert from string back to quantity
 */
class CordaAmountConverter : AttributeConverter<CordaAmount<Currency>?, String?> {

    override fun convertToDatabaseColumn(amount: CordaAmount<Currency>?): String? {

        val amountJson: String?
        val objectMapper = jacksonObjectMapper()
        try {
            amountJson = amount?.let { objectMapper.writeValueAsString(it) }
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("CordaAmountConverter writing error", e)
        }

        return amountJson
    }

    override fun convertToEntityAttribute(amountJson: String?): CordaAmount<Currency>? {

        val amount: CordaAmount<Currency>?
        val objectMapper = jacksonObjectMapper()
        try {
            amount = amountJson?.let { objectMapper.readValue(it) }
        } catch (e: IOException) {
            throw IllegalArgumentException("CordaAmountConverter reading error", e)
        }

        return amount
    }

}

/**
 * Class converter that convert list to string and convert from string back to list
 */
class ListConverter : AttributeConverter<List<String>?, String?> {

    private val delimiters = ", "
    override fun convertToDatabaseColumn(collection: List<String>?): String? {

        return try {
            collection?.joinToString(delimiters)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("ListConverter writing error", e)
        }
    }

    override fun convertToEntityAttribute(collectionString: String?): List<String>? {

        return try {
            collectionString?.split(delimiters)
        } catch (e: IOException) {
            throw IllegalArgumentException("ListConverter reading error", e)
        }
    }

}


/**
 * Class converter that convert list to string and convert from string back to list
 */
class PartyModelConverter : AttributeConverter<PartyModel?, String?> {

    override fun convertToDatabaseColumn(partyModel: PartyModel?): String? {

        val partyModelJson: String?
        val objectMapper = jacksonObjectMapper()
        try {
            partyModelJson = partyModel?.let { objectMapper.writeValueAsString(it) }
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("PartyModelConverter writing error", e)
        }

        return partyModelJson
    }

    override fun convertToEntityAttribute(partyModelJson: String?): PartyModel? {

        val partyModel: PartyModel?
        val objectMapper = jacksonObjectMapper()
        try {
            partyModel = partyModelJson?.let { objectMapper.readValue(it) }
        } catch (e: IOException) {
            throw IllegalArgumentException("PartyModelConverter reading error", e)
        }

        return partyModel
    }

}

/**
 * Class converter that convert DelegationOfAuthorityModel to string and convert from string back to DelegationOfAuthorityModel
 */
class DelegationOfAuthorityModelConverter : AttributeConverter<List<DelegationOfAuthorityModel>?, String?> {

    override fun convertToDatabaseColumn(data: List<DelegationOfAuthorityModel>?): String? {

        val jsonString: String?
        val objectMapper = jacksonObjectMapper()
        try {
            jsonString = data?.let { objectMapper.writeValueAsString(it) }
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("DelegationOfAuthorityModelConverter writing error", e)
        }

        return jsonString
    }

    override fun convertToEntityAttribute(jsonString: String?): List<DelegationOfAuthorityModel>? {

        val data: List<DelegationOfAuthorityModel>?
        val objectMapper = jacksonObjectMapper()
        try {
            data = jsonString?.let { objectMapper.readValue(it) }
        } catch (e: IOException) {
            throw IllegalArgumentException("DelegationOfAuthorityModelConverter reading error", e)
        }

        return data
    }

}


/**
 * This function is validate null or empty field of Any Object.
 * @param targetFieldNames: List of field name to validate.
 * @param message: Message when getting leadRecordMonitoringInvalid can use with string format {0} is field name
 * @param allowBlankString: flag to determine that will we allow String to be blank or empty
 * @return List of error message
 */
fun Any.validateRequiredFields(targetFieldNames: List<String>, message: String = "{0} is required.", allowBlankString: Boolean = false): List<String> {
    val clazz = this::class.java
    val targetFields = clazz.declaredFields
            .filter { targetFieldNames.contains(it.name) }
            .map {
                it.isAccessible = true
                it
            }
    val errorMessages = mutableListOf<String>()
    targetFields.forEach {
        val isNullOrBlank = when (val value = it.get(this)) {
            is String? -> if (allowBlankString) value == null else value.isNullOrBlank()
            is Double? -> value == null
            else -> value == null
        }
        if (isNullOrBlank) {
            errorMessages.add(MessageFormat.format(message, it.name))
        }
    }
    return errorMessages
}

/**
 * This function is validate updating uneditable field.
 * @param uneditableFieldNames: List of field name to validate.
 * @param newObject: New object to validate uneditable field
 * @param message: Message when getting leadRecordMonitoringInvalid can use with string format {0} is field name
 * @return List of error message
 */
fun Any.validateEditableFields(uneditableFieldNames: List<String>, newObject: Any, message: String = "{0} is not allow to edit."): List<String> {
    val clazz = this::class.java
    val uneditableFields = clazz.declaredFields
            .filter { uneditableFieldNames.contains(it.name) }
            .map {
                it.isAccessible = true
                it
            }
    val errorMessages = mutableListOf<String>()
    uneditableFields.forEach {
        val startValue = it.get(this)
        val newValue = newObject.getFieldValue<Any>(it.name)
        val isEditUneditableField = when (startValue) {
            is BigDecimal -> startValue.compareTo(newValue as BigDecimal) != 0
            else -> startValue != newValue
        }
        if (isEditUneditableField) {
            errorMessages.add(MessageFormat.format(message, it.name))
        }
    }

    return errorMessages
}

/**
 * Use for get field value via dynamic field name.
 * @param targetFieldName: field that we need to get value
 * @param ignoreCase: field to support getting field name with case insensitive, default value is false (case sensitive)
 * example:
 *  val vendor = Vendor(code = "Test Code", legalName = "Test Legalname")
 *      vendor.getFieldValue<String>("code")
 *      vendor.getFieldValue<Boolean>("allowInvoiceFinancing")
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.getFieldValue(targetFieldName: String, ignoreCase: Boolean = false): T? {
    val clazz = this::class.java
    val targetField = clazz.declaredFields.singleOrNull { it.name.equals(targetFieldName, ignoreCase = ignoreCase) }
    return if (targetField != null) {
        targetField.isAccessible = true
        targetField.get(this) as T
    } else {
        throw IllegalArgumentException("Field: $targetFieldName not exist in class ${clazz.name}")
    }
}

/**
 * This function use to cast input string to SearchInput object
 * and extract value for build criteria when query data
 * @param input input param for search
 * @param isExactly flag to build value for search exactly or partial match
 * @return value after build may include %value% or not based on flag and operation in SearchInput
 */
@Suppress("UNCHECKED_CAST")
fun <T> castSearchInput(input: String?, isExactly: Boolean = true): T? {
    if (input == null) return null
    val finalInput = extractSearchInput(input) ?: input
    return when (finalInput) {
        is SearchInput -> when (finalInput.oper) {
            SearchCriteriaOperation.EQUAL.name -> finalInput.value
            SearchCriteriaOperation.STARTS_WITH.name -> "${finalInput.value}%"
            else -> "%${finalInput.value}%"
        }
        else -> (if (isExactly) input else "${input}%")
    } as T
}

/**
 * Method to extract input string to SearchInput model else return null
 * @param input string that we want to extract
 * @return SearchInput model or null
 */
fun extractSearchInput(input: String?): SearchInput? {
    if (input == null) return null
    return try {
        jacksonObjectMapper().readValue(input, SearchInput::class.java)
    } catch (e: IOException) {
        null
    }
}

/**
 * Method to determine operation from value if value surround with percent
 * we will set operation to CONTAIN else return null
 * @param value value for search data
 * @return pair of plain value without percent and operation
 */
fun determineOperationFromValue(value: String): Pair<String, String?> {
    return when {
        value.startsWith(percent, true) && value.endsWith(percent, true) ->
            value.removeSurrounding(percent) to SearchCriteriaOperation.CONTAIN.name
        value.endsWith(percent, true) ->
            value.removeSuffix(percent) to SearchCriteriaOperation.STARTS_WITH.name
        value.startsWith(percent, true) ->
            value.removePrefix(percent) to SearchCriteriaOperation.ENDS_WITH.name
        else ->
            value to null
    }
}


/**
 * Method for check field of document in list have same value
 * by checking fieldName is item or not
 * then validate field
 * @param headerList list of header state
 * @param itemList list of item state
 * @param fieldName if fieldName is same as itemStateName will be like PurchaseItem.linearId
 */
fun <H : Any, I : Any> isListEqualsByFieldName(headerList: List<H>?,
                                               itemList: List<I>?,
                                               fieldName: String): Boolean? {
    val isItem = fieldName.contains(DOT)
    val processList = if (isItem) itemList else headerList
    if (processList.isNullOrEmpty()) return null
    val finalFieldName = fieldName.substringAfter(DOT)
    return processList!!.isEqualBy { it.getFieldValue<Any>(finalFieldName) }
}

/**
 * Method to determine operation from list value
 * if list has only 1 value will use [SearchCriteriaOperation.EQUAL.name]
 * otherwise, use [SearchCriteriaOperation.IN.name]
 * @return param value to operation value
 */
fun List<Any>.determineParamOperation(): Pair<Any, String> {
    return when (this.size == 1) {
        true -> this.first() to SearchCriteriaOperation.EQUAL.name
        false -> this to SearchCriteriaOperation.IN.name
    }
}
