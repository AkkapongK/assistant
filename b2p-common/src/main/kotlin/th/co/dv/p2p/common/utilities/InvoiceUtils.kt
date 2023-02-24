package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.adjustmentTypeNotSupport
import th.co.dv.p2p.common.constants.invalidRole
import th.co.dv.p2p.common.enums.AdjustmentType
import th.co.dv.p2p.common.enums.InvoiceSubType
import th.co.dv.p2p.common.enums.Lifecycle
import th.co.dv.p2p.common.utilities.Conditions.using
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.*
import java.math.BigDecimal

object InvoiceUtils {

    internal val logger: Logger = LoggerFactory.getLogger(InvoiceUtils::class.java)
    internal val className = InvoiceUtils::class.java.simpleName

    /**
     * Method for cancel credit note from invoice item and tied the new one to invoice item
     * we un-link credit note previous from invoice item and link the new one from current credit note
     *
     * Step 1: Call un-link [createOutputInvoiceItemsForDeleteCreditNote]
     * Step 2: Replace invoice that updated (from cancel) to list that using to linked
     * Step 3: Call link [createOutputInvoiceItems]
     *
     * @param invoiceItemsFromPreviousIn: Invoice item list that related with previous credit note
     * @param previousCreditNoteModel: Previous credit note
     * @param invoiceItemsIn: Invoice item list that related with current credit note
     * @param creditNoteModel: Current credit note
     */
    fun updateInvoiceItemFromUpdatedCreditNote(
            invoiceItemsFromPreviousIn: List<InvoiceItemModel>,
            previousCreditNoteModel: CreditNoteModel,
            invoiceItemsIn: List<InvoiceItemModel>,
            creditNoteModel: CreditNoteModel
    ): List<InvoiceItemModel> {
        // Cancel credit note value from invoice by previous credit note
        val invoiceItemCancelled = createOutputInvoiceItemsForDeleteCreditNote(
                invoiceItems = invoiceItemsFromPreviousIn,
                adjustmentType = previousCreditNoteModel.adjustmentType!!,
                creditNoteItems = previousCreditNoteModel.creditNoteItems)

        // Call combine InvoiceItem that cancelled with new invoice item
        val invoiceItemAfterCancelled = replaceItemWithUpdatedItem(
                existingList = invoiceItemsIn,
                updatedList = invoiceItemCancelled
        )
        val invoiceItemAfterIssued = createOutputInvoiceItems(
                invoiceItems = invoiceItemAfterCancelled,
                adjustmentType = creditNoteModel.adjustmentType!!,
                creditNoteItems = creditNoteModel.creditNoteItems)

        return combineInvoiceItem(
                subList = invoiceItemCancelled,
                mainList = invoiceItemAfterIssued
        )
    }

    /**
     * Method for cancel debit note from invoice item and tied the new one to invoice item
     * we un-link debit note previous from invoice item and link the new one from current debit note
     *
     * Step 1: Call un-link [createOutputInvoiceItemsForDeleteDebitNote]
     * Step 2: Replace invoice that updated (from cancel) to list that using to linked
     * Step 3: Call link [createOutputInvoiceItems]
     *
     * @param invoiceItemsFromPreviousIn: Invoice item list that related with previous debit note
     * @param previousDebitNoteModel: Previous debit note
     * @param invoiceItemsIn: Invoice item list that related with current debit note
     * @param debitNoteModel: Current debit note
     */
    fun updateInvoiceItemFromUpdatedDebitNote(
            invoiceItemsFromPreviousIn: List<InvoiceItemModel>,
            previousDebitNoteModel: DebitNoteModel,
            invoiceItemsIn: List<InvoiceItemModel>,
            debitNoteModel: DebitNoteModel
    ): List<InvoiceItemModel> {
        // Cancel debit note value from invoice by previous debit note
        val invoiceItemCancelled = createOutputInvoiceItemsForDeleteDebitNote(
                invoiceItems = invoiceItemsFromPreviousIn,
                debitNoteItems = previousDebitNoteModel.debitNoteItems)

        // Call combine InvoiceItem that cancelled with new invoice item
        val invoiceItemAfterCancelled = replaceItemWithUpdatedItem(
                existingList = invoiceItemsIn,
                updatedList = invoiceItemCancelled
        )
        val invoiceItemAfterIssued = createOutputInvoiceItemsForCurrentDebitNote(
                invoiceItems = invoiceItemAfterCancelled,
                debitNoteItems = debitNoteModel.debitNoteItems
        )

        return combineInvoiceItem(
                subList = invoiceItemCancelled,
                mainList = invoiceItemAfterIssued
        )
    }

    /**
     * Method for combine invoice item
     * We add item in [subList] to [mainList] in case that item still not exist in main list
     *
     * @param subList: Invoice item that we want to add in case not exist in main
     * @param mainList: Invoice item that main list
     */
    fun combineInvoiceItem(subList: List<InvoiceItemModel>, mainList: List<InvoiceItemModel>): List<InvoiceItemModel> {
        val idsInMain = mainList.mapNotNull { it.linearId }
        val finalUpdated = subList.filterNot { it.linearId in idsInMain }
        return mainList + finalUpdated
    }

    /**
    * Function to create invoice output from input credit note model
    * This function will update credit note fields depend on adjustment type
    *
    * @param invoiceItems: Invoice item input that related with credit note item
    * @param adjustmentType: Adjustment type (QUANTITY/PRICE)
    * @param creditNoteItems: credit note item that issued
    */
    fun createOutputInvoiceItems(invoiceItems: List<InvoiceItemModel>,
                                 adjustmentType: String,
                                 creditNoteItems: List<CreditNoteItemModel>): List<InvoiceItemModel> {

        return invoiceItems.mapNotNull { invoiceItem ->
            val targetCreditNoteItem = creditNoteItems.find {
                it.invoiceItemLinearId == invoiceItem.linearId
            } ?: return@mapNotNull null

            when (adjustmentType)  {
                AdjustmentType.QUANTITY.name -> {
                    val invoiceItemOut = invoiceItem.adjustQuantity(
                            quantity = targetCreditNoteItem.quantity!!.initial,
                            unit = targetCreditNoteItem.unit ?: "")
                    "Invoice Item return quantity cannot exceed initial value." using (
                            invoiceItemOut.creditNoteQuantity!!.initial.isLessOrEqual(invoiceItemOut.quantity!!.initial))
                    invoiceItemOut
                }

                AdjustmentType.PRICE.name -> {
                    val invoiceItemOut = invoiceItem.adjustSubtotal(
                            adjustPrice = targetCreditNoteItem.subTotal ?: BigDecimal.ZERO)
                    "Invoice Item adjust amount cannot exceed initial value." using (
                            invoiceItemOut.creditNoteAdjustedSubtotal!!.isLessOrEqual(invoiceItemOut.itemSubTotal ?: BigDecimal.ZERO))
                    invoiceItemOut
                }

                else -> throw IllegalArgumentException(adjustmentTypeNotSupport)
            }
        }
    }

    /**
    * Function to create invoice output from credit note model that we want to delete
    * This function will delete credit note fields depend on adjustment type
    *
    * @param invoiceItems: Invoice item input that related with credit note item
    * @param adjustmentType: Adjustment type (QUANTITY/PRICE)
    * @param creditNoteItems: credit note item that issued
    */
    fun createOutputInvoiceItemsForDeleteCreditNote(
            invoiceItems: List<InvoiceItemModel>,
            adjustmentType: String,
            creditNoteItems: List<CreditNoteItemModel>): List<InvoiceItemModel> {

        return invoiceItems.mapNotNull { invoiceItem ->
            val targetCreditNoteItem = creditNoteItems.find {
                it.invoiceItemLinearId == invoiceItem.linearId
            } ?: return@mapNotNull null

            when (adjustmentType)  {
                AdjustmentType.QUANTITY.name -> {
                    val invoiceItemOut = invoiceItem.cancelAdjustedQuantity(
                            quantity = targetCreditNoteItem.quantity!!.initial)
                    "CreditNote quantity in invoice item cannot less than zero." using (
                            invoiceItemOut.creditNoteQuantity!!.initial.isGreaterOrEqual(BigDecimal.ZERO))


                    invoiceItemOut
                }

                AdjustmentType.PRICE.name -> {
                    val invoiceItemOut = invoiceItem.cancelAdjustedSubtotal(
                            adjustPrice = targetCreditNoteItem.subTotal ?: BigDecimal.ZERO)
                    "Credit note subtotal cannot less than zero." using (
                            invoiceItemOut.creditNoteAdjustedSubtotal!!.isGreaterOrEqual(BigDecimal.ZERO))

                    invoiceItemOut
                }

                else -> throw IllegalArgumentException(adjustmentTypeNotSupport)
            }
        }

    }

    /**
     * Function to create invoice output from input debit note model
     * This function will update debit note fields depend on adjustment type
     *
     * @param invoiceItems: Invoice item input that related with debit note item
     * @param debitNoteItems: debit note item that edited
     */
    fun createOutputInvoiceItemsForCurrentDebitNote(
            invoiceItems: List<InvoiceItemModel>,
            debitNoteItems: List<DebitNoteItemModel>): List<InvoiceItemModel> {

        return invoiceItems.mapNotNull { invoiceItem ->
            val targetDebitNoteItem = debitNoteItems.find {
                it.invoiceItemLinearId == invoiceItem.linearId
            } ?: return@mapNotNull null
                    val invoiceItemOut = invoiceItem.adjustSubtotalForDebitNote(
                            adjustPrice = targetDebitNoteItem.subTotal ?: BigDecimal.ZERO)
                    invoiceItemOut
            }
    }

    /**
     * Function to create invoice output from debit note model that we want to delete
     * This function will delete debit note fields depend on adjustment type
     *
     * @param invoiceItems: Invoice item input that related with debit note item
     * @param debitNoteItems: debit note item that issued
     */
    fun createOutputInvoiceItemsForDeleteDebitNote(
            invoiceItems: List<InvoiceItemModel>,
            debitNoteItems: List<DebitNoteItemModel>): List<InvoiceItemModel> {

        return invoiceItems.mapNotNull { invoiceItem ->
            val targetDebitNoteItem = debitNoteItems.find {
                it.invoiceItemLinearId == invoiceItem.linearId
            } ?: return@mapNotNull null
            val invoiceItemOut = invoiceItem.cancelAdjustedSubtotalForDebitNote(
                    adjustPrice = targetDebitNoteItem.subTotal ?: BigDecimal.ZERO)
            "Debit note subtotal cannot less than zero." using (
                    invoiceItemOut.debitNoteAdjustedSubTotal!!.isGreaterOrEqual(BigDecimal.ZERO))

            invoiceItemOut
        }
    }

    /**
     * Method for update item with updated list
     * if item have same id we replace existing with updated item
     *
     * @param existingList : Invoice item list
     * @param updatedList: Invoice item that updated
     */
    fun replaceItemWithUpdatedItem(existingList: List<InvoiceItemModel>,
                                           updatedList: List<InvoiceItemModel>): List<InvoiceItemModel> {
        return existingList.map { existingItem ->
            updatedList.find { it.linearId == existingItem.linearId } ?: existingItem
        }
    }

    /**
     * Receives a list of statuses that we then parse into backend lifecycles
     * @param [invoiceStatuses] list of invoice statuses to be translated
     * @param [matchingStatus] list of matching status to be translated
     * @param [isSeller] which map to use for translation
     * @return Pair of backend lifecycle for invoice status and matching status and flag that let us known the target invoice have been submitted to RD or not
     */
    fun parseStatus(invoiceStatuses: List<String>?,
                    matchingStatus: List<String>?,
                    isSeller: Boolean): Pair<Pair<Set<String>?, Set<String>?>, Boolean?> {

        logger.info("$className.parseStatus : invoiceStatuses $invoiceStatuses  matchingStatus $matchingStatus, isSellerStatus: $isSeller")

        var searchRequestToCancel = false
        var searchRequestToResubmit = false

        val translatedMatchingStatus = matchingStatus?.flatMap { status ->
            InvoiceStatus.Matcher().fromDisplayName(status).map { matchingStatusMapping ->
                when(status) {
                    InvoiceStatus.PENDING_SELLER_AFTER_RD_SUBMITTED -> searchRequestToCancel = true
                    InvoiceStatus.PENDING_SELLER_BEFORE_RD_SUBMITTED_MATCHER -> searchRequestToResubmit = true
                }
                matchingStatusMapping.key
            }
        }?.distinct()?.toSet()

        // To determine which map to use based on restrictedFlag
        val invoiceStatusTranslator = if (isSeller) {
            InvoiceStatus.Seller()
        } else {
            InvoiceStatus.Buyer()
        }

        val translatedInvoiceStatuses = invoiceStatuses?.flatMap { status ->
            invoiceStatusTranslator.fromDisplayName(status).map { invoiceStatusMapping ->
                when(status) {
                    InvoiceStatus.PENDING_SELLER_AFTER_RD_SUBMITTED -> searchRequestToCancel = true
                    InvoiceStatus.PENDING_SELLER_BEFORE_RD_SUBMITTED -> searchRequestToResubmit = true
                }
                invoiceStatusMapping.key
            }
        }?.distinct()?.toSet()

        logger.info("$className.parseStatus translatedStatuses in $isSeller are the following: $translatedInvoiceStatuses, translatedMatchingStatus: $translatedMatchingStatus")

        val isRdSubmitted = when {
            searchRequestToCancel && !searchRequestToResubmit -> true
            !searchRequestToCancel && searchRequestToResubmit -> false
            else -> null
        }
        val result = Pair(translatedInvoiceStatuses, translatedMatchingStatus)
        return result to isRdSubmitted
    }

    /**
     * Method to update invoice status for front-end
     * it will map lifecycle to front-end status and put it in field status instead of status from micro-service
     */
    fun updateInvoiceStatus(invoices: List<InvoiceModel>, isSellerStatus: Boolean = true): List<InvoiceModel> {
        return invoices.map { invoice ->
            val item = invoice.invoiceItems.map { it.display() }
            invoice.display(isSellerStatus).updateItem(item)
        }
    }

    /**
     * Method for check the role that pass in and return mode of display status
     *
     * If role is seller we return true
     * If role is buyer we return false
     * else we throw error
     *
     * @param role: User role (BUYER/SELLER)
     */
    fun showStatusAsSeller(role: String): Boolean {
        // We choose which translator to use based on the role given
        return when (role.uppercase()) {
            StatusType.SELLER.name -> true
            StatusType.BUYER.name -> false
            else -> throw IllegalArgumentException(invalidRole)

        }
    }
}

// This is for [CreditNote] to increase the [creditNoteQuantity] during issuance of [CreditNoteContract.AdjustmentType.QUANTITY]
fun InvoiceItemModel.adjustQuantity(quantity: BigDecimal, unit: String) = copy(
        creditNoteQuantity = Quantity(
                initial = (this.creditNoteQuantity?.initial ?: BigDecimal.ZERO) + quantity,
                unit = this.creditNoteQuantity?.unit ?: unit))

// This is for [CreditNote] to increase the [creditNoteSubtotal] during issuance of [CreditNoteContract.AdjustmentType.PRICE]
fun InvoiceItemModel.adjustSubtotal(adjustPrice: BigDecimal) = copy(
        creditNoteAdjustedSubtotal = (this.creditNoteAdjustedSubtotal ?: BigDecimal.ZERO) + adjustPrice)

// This is for [DebitNote] to increase the [debitNoteSubtotal] during issuance of [DebitNoteContract.AdjustmentType.PRICE]
fun InvoiceItemModel.adjustSubtotalForDebitNote(adjustPrice: BigDecimal) = copy(
        debitNoteAdjustedSubTotal = (this.debitNoteAdjustedSubTotal ?: BigDecimal.ZERO) + adjustPrice)

/**
 * Method for update creditNoteQuantity in invoiceItem
 * when creditNoteModel have adjustmentType is QUANTITY
 */
fun InvoiceItemModel.cancelAdjustedQuantity(quantity: BigDecimal): InvoiceItemModel {
    return this.copy(creditNoteQuantity = Quantity(this.creditNoteQuantity!!.initial - quantity, this.creditNoteQuantity.unit))
}

/**
 * Method for update creditNoteQuantity in invoiceItem
 * when creditNoteModel have adjustmentType is PRICE
 */
fun InvoiceItemModel.cancelAdjustedSubtotal(adjustPrice: BigDecimal): InvoiceItemModel{
    return this.copy(creditNoteAdjustedSubtotal = this.creditNoteAdjustedSubtotal!!.minus(adjustPrice))

}

/**
 * Method for update debit note adjust subtotal in invoiceItem
 * when debit note model have adjustmentType is PRICE
 */
fun InvoiceItemModel.cancelAdjustedSubtotalForDebitNote(adjustPrice: BigDecimal): InvoiceItemModel{
    return this.copy(debitNoteAdjustedSubTotal = this.debitNoteAdjustedSubTotal!!.minus(adjustPrice))
}


/**
 * Method for check this is reject direct invoice witch contract or not
 */
fun InvoiceModel.isRejectDirectInvoiceWithContract(): Boolean {
    return this.lifecycle == Lifecycle.InvoiceLifecycle.CANCELLED.name && this.isDirectInvoiceWithContract()
}

/**
 * Method for check this is direct invoice witch contract or not
 */
fun InvoiceModel.isDirectInvoiceWithContract(): Boolean {
    return this.subtype == InvoiceSubType.DIRECT.name
            && this.invoiceItems.any { it.contractNumber.isNullOrBlank().not() }
}