package th.co.dv.p2p.common.models

import th.co.dv.p2p.common.enums.State
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.utilities.isListEqualsByFieldName

/**
 * For map the response of invoice custom rule validation from configuration service
 * and document field master from masterData service to Model Object
 */
data class CustomValidateFieldModel (
        val buyerTaxId: String? = null,
        val state: String? = null,
        val fieldName: String? = null,
        val errorMessage: String? = null

)

/**
 * Method for validate field in document is not combined of multiple value
 * if state in CustomValidateFieldModel is not valid will ignore validate
 * @return list of errorMessage in CustomValidateFieldModel
 */
fun List<CustomValidateFieldModel>.validateField(allStates: AllStates): List<String> {
    val errorMessages = mutableListOf<String>()

    this.forEach { customValidateFieldModel ->
        val fieldName = customValidateFieldModel.fieldName!!

        val isSameValueInList = when (customValidateFieldModel.state) {
            State.PurchaseOrder.name -> isListEqualsByFieldName(allStates.purchaseOrders, allStates.purchaseItems, fieldName)
            State.GoodsReceived.name -> isListEqualsByFieldName(allStates.goodsReceiveds, allStates.goodsReceivedItems, fieldName)
            State.CreditNote.name -> isListEqualsByFieldName(allStates.creditNotes, allStates.creditNoteItems, fieldName)
            State.DebitNote.name -> isListEqualsByFieldName(allStates.debitNotes, allStates.debitNoteItems, fieldName)
            else -> throw IllegalArgumentException("${customValidateFieldModel.state} state is not support for custom validation.")
        }
        if (isSameValueInList == false) errorMessages.add(customValidateFieldModel.errorMessage!!)
    }
    return errorMessages.toList()
}