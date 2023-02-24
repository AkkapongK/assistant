package th.co.dv.p2p.common.utilities

import th.co.dv.p2p.common.constants.CORDA_UI
import th.co.dv.p2p.common.constants.MAX_YEAR_TO_STORE
import th.co.dv.p2p.common.constants.cannotCastToLong
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.Node
import th.co.dv.p2p.common.models.SellerModel
import th.co.dv.p2p.common.models.ThresholdModel
import th.co.dv.p2p.corda.base.IllegalFlowException
import th.co.dv.p2p.corda.base.models.InvoiceModel
import th.co.dv.p2p.corda.base.models.PartyModel
import th.co.dv.p2p.corda.base.models.PostingDetailModel
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import java.lang.reflect.Field
import java.math.BigDecimal
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.Id
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties

/**
 * Method for validate node type is target node or not
 *
 * we will validate from legal name in case have no legal name we return false
 */
fun validateNodeType(partyModel: PartyModel, targetNode: Node): Boolean {
    val legalName = partyModel.legalName ?: return false

    return legalName.contains(targetNode.name)
}


/**
 * Method for copy value in properties that have same name and type to destination object
 */
fun <T : Any, R : Any> T.copyPropsFrom(fromObject: R, specificFields: List<String> = emptyList()) {
    // only consider mutable properties
    val mutableProps = this::class.memberProperties
    // if source list is provided use that otherwise use all available properties
    val sourceProps = when (specificFields.isEmpty()) {
        true -> fromObject::class.memberProperties
        false -> fromObject::class.memberProperties.filter { specificFields.contains(it.name) }
    }
    val outputClass = this::class.java
    // copy all matching
    mutableProps.forEach { targetProp ->
        val matchType = sourceProps.find {
            // make sure properties have same name and compatible types
            it.name == targetProp.name && targetProp.returnType.isSupertypeOf(it.returnType)
        }
        if (matchType != null) {
            val targetFieldName = matchType.name
            val targetField = outputClass.declaredFields.singleOrNull { it.name.equals(targetFieldName, ignoreCase = false) }
                    ?: return@forEach

            targetField.isAccessible = true
            targetField.set(this, matchType.getter.call(fromObject))
        } else {
            val matchTypeWithName = sourceProps.find { it.name == targetProp.name } ?: return@forEach
            // Convert between string and date
            specificConvert(
                    source = matchTypeWithName,
                    destination = targetProp,
                    outputClass = this,
                    sourceClass = fromObject
            )
        }
    }

}

/**
 * Method to convert specific type
 * Now support
 * 1. Date to String
 * 2. String to Date
 * 3. BigDecimal to String
 * 4. String to BigDecimal (after convert you need to set scale by yourself)
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
private fun specificConvert(source: KProperty1<*, Any?>, destination: KProperty1<*, Any?>, outputClass: Any, sourceClass: Any) {
    val targetFieldName = destination.name
    val targetField = outputClass::class.java.declaredFields.singleOrNull { it.name.equals(targetFieldName, ignoreCase = false) }
            ?: return

    targetField.isAccessible = true
    val valueFromSource = source.getter.call(sourceClass)
    val newValue = when {
        (valueFromSource == null) -> if (destination.returnType.isMarkedNullable) null else return
        (source.returnType.classifier == String::class) && (destination.returnType.classifier == Date::class) -> {
            val dateResult = Date.from((valueFromSource as String).toZonedDateTime().toInstant())
            val dateLocalDate = dateResult.toInstant().toZonedDateTime().toLocalDate()
            if (dateLocalDate.year > MAX_YEAR_TO_STORE) throw IllegalFlowException("Field name '${source.name}' is out or range for date format.")
            dateResult
        }
        (source.returnType.classifier == Date::class) && (destination.returnType.classifier == String::class) -> DateUtility.convertDateToString(valueFromSource as Date, DATE_TIME_FORMAT)
        (source.returnType.classifier == Timestamp::class) && (destination.returnType.classifier == String::class) -> DateUtility.convertDateToString(valueFromSource as Date, DATE_TIME_FORMAT)
        (source.returnType.classifier == BigDecimal::class) && (destination.returnType.classifier == String::class) -> valueFromSource.toString()
        (source.returnType.classifier == String::class) && (destination.returnType.classifier == BigDecimal::class) -> valueFromSource.toString().toBigDecimal()
        else -> return
    }

    targetField.set(outputClass, newValue)
}

/**
 * Method for reduce quantity of purchase item
 *
 */
fun PurchaseItemModel.depleteQuantity(quantityToDeplete: BigDecimal): PurchaseItemModel {
    return if (quantityToDeplete > quantity!!.remaining) {
        copy(quantity = quantity.deplete(quantity.remaining),
                overDeliveryQuantity = overDeliveryQuantity!!.deplete(quantityToDeplete - quantity.remaining))
    } else {
        copy(quantity = this.quantity.deplete(quantityToDeplete))
    }
}


/**
 * This function check when we try to restore purchase item's quantity (i.e. cancel invoice)
 * If overDeliveryQuantity has been consumed, restore it first, then we restore the remaining quantity.
 */
fun PurchaseItemModel.restoreQuantity(quantityToRestore: BigDecimal): PurchaseItemModel {
    return if (overDeliveryQuantity!!.consumed > BigDecimal.ZERO) {

        // We want to prioritize restoring overDeliveryQuantity first
        // 1. if quantityToRestore > overDeliveryQuantity.consumed, we restore only overDeliveryQuantity.consumed
        //      and the balance we will restore in the quantity
        // 2. if quantityToRestore < overDeliveryQuantity.consumed, we restore quantityToRestore
        //      there's no balance to restore in the quantity
        val overDeliveryQuantityToRestore = minOf(quantityToRestore, overDeliveryQuantity.consumed)

        // if the delta [quantityToRestore - overDeliveryQuantity.consumed] is negative,
        // It signifies that quantityToRestore is less than overDeliveryQuantity.consumed, which means
        // We have more than enough to restore in overDeliveryQuantity.consumed, so we don't have to restore anything in quantity, so 0.
        val balanceToRestoreInQuantity = maxOf(BigDecimal.ZERO, quantityToRestore - overDeliveryQuantity.consumed)

        copy(quantity = quantity!!.restore(balanceToRestoreInQuantity),
                overDeliveryQuantity = overDeliveryQuantity.restore(overDeliveryQuantityToRestore))
    } else {
        copy(quantity = quantity!!.restore(quantityToRestore))
    }
}

/**
 * Method for convert string to long
 * in case null of cannot convert we throw [IllegalArgumentException]
 */
fun String?.convertToLong(): Long {
    return this?.let {
        try {
            it.toLong()
        } catch (e: Exception) {
            null
        }
    } ?: throw IllegalArgumentException(cannotCastToLong)
}

fun InvoiceModel?.checkInvoiceCategory(): ItemCategory.Invoice {
    return when {
        this?.invoiceItems?.any { it.itemCategory == ItemCategory.Invoice.ADVANCE_REDEEM.name }
                ?: false -> ItemCategory.Invoice.ADVANCE_REDEEM
        this?.invoiceItems?.any { it.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name }
                ?: false -> ItemCategory.Invoice.ADVANCE_DEDUCT
        this?.invoiceItems?.any { it.itemCategory == ItemCategory.Invoice.PROVISION.name }
            ?: false -> ItemCategory.Invoice.PROVISION
        else -> ItemCategory.Invoice.NORMAL
    }
}


/**
 * Method for add other data to target field
 *
 * @param otherData : data that we want to add
 * @param idKeys: key use to get value in map such as linear_id,
 * @param targetKey: target field that we want to add data
 */
@Suppress("UNCHECKED_CAST")
fun Map<String, MutableMap<String, Any>>.addOtherDataToTargetField(
        otherData: List<Map<String, Any>>,
        idKeys: List<String>,
        targetKey: String) {
    otherData.forEach { each ->
        val combineKey = idKeys.map { each[it] }.joinToString("-")
        this[combineKey]?.let {
            (it[targetKey] as MutableList<Map<String, Any>>).add(each)
        }
    }
}

/**
 * Method used to define priority
 * Non-UI Transactions that save to Corda will be prioritize to “1”
 * UI Transactions will be prioritize to “5”
 * In case [appId] be in [uiAppIds] we also treat that case same UI
 *
 * @param appId : app id
 * @param uiAppIds : list of ui app id
 */
fun definePriorityByAppId(appId: String?, uiAppIds: List<String>? = null): Int {
    val finalUiAppIds = (uiAppIds ?: emptyList()).plus(CORDA_UI)
    return when {
        finalUiAppIds.contains(appId) -> 5
        else -> 1
    }
}

/**
 * Method for covert all threshold config to positive value
 */
fun ThresholdModel.toAbs(): ThresholdModel {
    return ThresholdModel(
            minimumSubTotal = this.minimumSubTotal?.abs(),
            maximumSubTotal = this.maximumSubTotal?.abs(),
            minimumVatTotal = this.minimumVatTotal?.abs(),
            maximumVatTotal = this.maximumVatTotal?.abs(),
            minimumTotalAmount = this.minimumTotalAmount?.abs(),
            maximumTotalAmount = this.maximumTotalAmount?.abs(),
            minimumInvHeaderDiffPercent = this.minimumInvHeaderDiffPercent?.abs(),
            maximumInvHeaderDiffPercent = this.maximumInvHeaderDiffPercent?.abs(),
            minimumItemSubTotal = this.minimumItemSubTotal?.abs(),
            maximumItemSubTotal = this.maximumItemSubTotal?.abs()
    )
}

/**
 * Method for filter input parameters that relate with Entity
 * This method will return only field that match with target Class
 *
 * @param parameters : input param
 * @return MutableMap of input after validate
 */
inline fun <reified T> getRelateField(parameters: Map<String, Any>): MutableMap<String, Any> {
    // Get all field of target class
    val fieldsList = T::class.java.declaredFields.map { it.name }
    // filter only match field with target class
    return parameters.filter {
        it.key in fieldsList
    }.toMutableMap()
}


/**
 * Method for update rd submitted date to document
 */
fun <T : Any> List<T>.updateRdSubmittedDate(): List<T> {
    this.forEach { it.setFieldValue("rdSubmittedDate", Instant.now().stringify()) }
    return this
}


/**
 * Method for filter document with length of rd start date end date
 * @param documentDateKey : key date of document
 * @param sellers : list of seller to filter rd start end date
 */
fun <T : Any> List<T>.filterDocumentByRdActiveAndEndDate(documentDateKey: String, sellers: List<SellerModel>): List<T> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return this.filter {
        val taxNumber = it.getFieldValue<String>("vendorTaxNumber")
        val documentDate = dateFormat.parse(it.getFieldValue<String>(documentDateKey))
        val seller = sellers.find { seller -> seller.taxId == taxNumber }
        (documentDate.before(seller!!.rdActiveEndDate) && (documentDate.after(seller.rdActiveStartDate)))
    }
}


/**
 * Method for retry function with delay
 *
 * @param attempt : number of times to retry
 * @param delay : delay time (milliseconds)
 * @param function : function that want to retry
 */
fun <T> retry(attempt: Int, delay: Long, function: () -> T?): T? {
    repeat(attempt - 1) {
        try {
            return function()
        } catch (e: Exception) {

        }
        TimeUnit.MILLISECONDS.sleep(delay)
    }
    return function() // last attempt
}

/**
 * Method for get name of field id from entity
 */
fun Any.getFieldId(): Field? {
    return this::class.java.getFieldId()
}

/**
 * Method for get name of field id from class entity
 */
fun Class<*>.getFieldId(): Field? {
    return this.declaredFields.find { it.getAnnotation(Id::class.java) != null }
}

/**
 * Method for set Scale amount field
 * @param amountFields : list of field is required to set scale
 * @param currency : currency
 */
fun Any.setScaleAmountField(amountFields: List<String>, currency: String? = THB.currencyCode) {
    val finalCurrency = currency?:THB.currencyCode
    val allBigDecimalField = this::class.java.declaredFields.filter { it.type.simpleName == BigDecimal::class.java.simpleName }
    val targetField = allBigDecimalField.filter{ amountFields.contains(it.name) }

    targetField.forEach {
        val value = this.getFieldValue<BigDecimal>(it.name,true)?.setScaleByCurrency(finalCurrency)
        this.setFieldValue(it.name,value)
    }
}

/**
 * Method for find sponsor by command
 * @param arg : command
 */
fun findSponsorByCommand(arg: String):String {
    val command = arg.split("sponsor=")[1]
    val splitText = command.split(",")
    return when (splitText.size == 1) {
        true -> splitText[0].split("]")[0]
        false -> splitText[0]
    }
}

private val mappingPostingDetailToCustomiseField = mapOf(
    PostingDetailModel::message.name to  "messageText",
    PostingDetailModel::fiDocNumber.name to  "accountingDocumentNumber",
    PostingDetailModel::fiDocFiscalYear.name to  "fiscalYear",
    PostingDetailModel::fiDocHeaderText.name to  "FIDocHeaderText",
    PostingDetailModel::livDocNumber.name to  "LIVDocumentNo"
)

/**
 * Method to convert [PostingDetailModel] to customisedFields
 */
fun covertPostingDetailToCustomisedFields(postingDetailModels: List<PostingDetailModel>, keyObject: String = "LIV"): Map<String, Any> {
    val retentionPostingDetail = postingDetailModels.find { it.fiDocType == "RETENTION" }
    val targetPostingDetail = postingDetailModels.find { it.id != retentionPostingDetail?.id } ?: return mapOf()

    val postingDetailResult = mutableMapOf<String, Any>()
    mappingPostingDetailToCustomiseField.forEach { (postingDetailModelField, customisedField) ->
        postingDetailResult[customisedField] = targetPostingDetail.getFieldValue<String>(postingDetailModelField) ?: ""
    }

    if (retentionPostingDetail != null) postingDetailResult["FIDocRetention"] = retentionPostingDetail.fiDocNumber ?: ""

    return mapOf(keyObject to postingDetailResult)
}

/**
 * Method to select sponsor for Rabbit queue that sent to bank
 * @param needToGetSponsor: true for normal deployment, false for single stack.
 **/
fun selectSponsorForBankQueue(needToGetSponsor: Boolean): String? {
    return when (needToGetSponsor) {
        true -> SponsorContextHolder.getCurrentSponsor()
        else -> "BANK"
    }
}


/**
 * Method for get username from user authorization in security context
 * and default to "system" when user is INTERFACE or have an error
 */
fun getUsernameFromSecurityContext(): String {
    return try {
        RequestUtility.getUserAuthorization().username.let {
            when (it) {
                AuthorizationUtils.INTERFACE_AUTHORIZATION.username -> SYSTEM_USERNAME
                else -> it
            }
        }
    } catch (e: Exception) {
        SYSTEM_USERNAME
    }
}
