package th.co.dv.p2p.common.utilities

import net.corda.core.contracts.Requirements.using
import th.co.dv.p2p.common.constants.MAX_STATE_IN_TRANSACTION
import th.co.dv.p2p.common.models.TransactionModel
import th.co.dv.p2p.corda.base.models.*
import java.util.function.Predicate

/**
 * Utility for manage transaction detail
 */
object TransactionUtils {

    /**
     * Method for chuck document item
     *
     * @param itemDocuments list of item document
     * @param maxStateInTransaction maximum state allow per transaction
     * @return list of chunked item document
     */
    fun <C : Any> groupSendListItem(itemDocuments: List<C>, maxStateInTransaction: Int = MAX_STATE_IN_TRANSACTION): List<List<C>> {
        return itemDocuments.chunked(maxStateInTransaction)
    }

    /**
     * Composite method for group and update data before send transaction
     * After groupSendList we gel list of grouped transaction, the transaction can have header and items state
     *
     * In case the transaction have header and items we combine item to header state
     * In case the transaction only have item we do nothing
     *
     * @param sortedDocument list of document sorted by items size ascending
     * @param maxStateInTransaction maximum state allow per transaction
     * @return list of update grouped document per transaction
     */
    fun <P : Any, C : Any> manageDocumentTransaction(sortedDocument: List<P>, maxStateInTransaction: Int = MAX_STATE_IN_TRANSACTION): List<List<TransactionModel<P, C>>> {

        val result = groupSendList<P, C>(sortedDocument, maxStateInTransaction)
        val itemFieldName = itemField(sortedDocument.first()::class.java)

        return result.map { eachTransaction ->

            eachTransaction.map inner@ { documentData ->

                if (documentData.headerState == null) return@inner documentData

                documentData.headerState.setFieldValue(itemFieldName, documentData.itemStates ?: emptyList<C>())
                documentData.copy(itemStates = emptyList())
            }
        }

    }

    /**
     * Method for group invoices to send to Rabbit MQ together we considering by item size
     * and make sure that in one transaction does not contain state more than maxStateInTransaction
     *
     * Note: P is parent state
     *       C is child state
     *
     * @param sortedDocument list of document sorted by items size ascending
     * @param maxStateInTransaction maximum state allow per transaction
     * @return list of group document per transaction
     */
    fun <P : Any, C : Any> groupSendList(sortedDocument: List<P>, maxStateInTransaction: Int = MAX_STATE_IN_TRANSACTION): MutableList<List<TransactionModel<P, C>>> {

        if (sortedDocument.isNullOrEmpty()) return mutableListOf()

        val result = mutableListOf<List<TransactionModel<P, C>>>()
        val groupItems = mutableListOf<TransactionModel<P, C>>()

        val itemFieldName = itemField(sortedDocument.first()::class.java)

        fun addResultList(groupMap: MutableList<TransactionModel<P, C>>) {
            if (groupMap.isNotEmpty()) {
                result.add(groupMap.toList())
                // clear group
                groupItems.clear()
            }
        }

        sortedDocument.forEach { model ->

            val newModel = model.javaClass.getDeclaredConstructor().newInstance()
            newModel.copyPropsFrom(model)

            val itemModel = newModel.getFieldValue<List<C>>(itemFieldName) ?: emptyList()
            newModel.setFieldValue(itemFieldName, emptyList<List<C>>())

            // we get size from item plus header
            val currentModelSize = itemModel.size + 1

            val groupItemSize = groupItems.sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) }

            val isCurrentMoreThanLimitSize = currentModelSize > maxStateInTransaction
            val isGroupMoreThanLimitSize = (groupItemSize + currentModelSize) > maxStateInTransaction

            when {
                isCurrentMoreThanLimitSize -> {
                    // Add existing group to result
                    addResultList(groupItems)
                    // Split transaction for header that have item more than limit size
                    val splitTx = splitTransaction(header = newModel, items = itemModel, maxStateInTransaction = maxStateInTransaction)
                    splitTx.forEach { addResultList(mutableListOf(it)) }
                }
                isGroupMoreThanLimitSize -> {
                    // Add existing group to result
                    addResultList(groupItems)
                    // Add new item to group
                    groupItems.add(buildTransactionModel(header = newModel, items = itemModel))
                }
                else -> groupItems.add(buildTransactionModel(header = newModel, items = itemModel))
            }
        }
        // add not add group(last) to result
        addResultList(groupItems)

        return result
    }

    /**
     * Method for build transaction model from input header and item state
     * @param header header state model
     * @param items list of item state model
     * @return transaction model contain header and item for this transaction
     */
    fun <P : Any, C : Any> buildTransactionModel(header: P?, items: List<C>?): TransactionModel<P, C> {
        "Must have at least header state or items state when build TransactionModel." using (header != null || items.isNullOrEmpty().not())
        return TransactionModel(headerState = header, itemStates = items)
    }

    /**
     * Method to split and resize state per transaction for document that have state > allowed max state in transaction
     *
     * @param header header of the state
     * @param items list of item state in this header
     * @param maxStateInTransaction maximum state allow per transaction
     * @return list of transaction model after split this state
     */
    fun <P : Any, C : Any> splitTransaction(header: P, items: List<C>, maxStateInTransaction: Int): MutableList<TransactionModel<P, C>> {

        val chunkItems = items.chunked(maxStateInTransaction).toMutableList()
        val minItemList = chunkItems.minByOrNull { it.size }!!

        // Generate transaction with header
        // First we find the minimum items to combine with header in transaction but not exceed the limit
        // If over the limit we just create transaction with only header
        val transactionHeader = when ((minItemList.size + 1) <= maxStateInTransaction) {
            true -> {
                chunkItems.removeIf(Predicate.isEqual(minItemList))
                buildTransactionModel(header = header, items = minItemList)
            }
            false -> buildTransactionModel(header = header, items = null)

        }

        val transactionItems = chunkItems.map {
            buildTransactionModel<P, C>(header = null, items = it)
        }

        return (listOf(transactionHeader) + transactionItems).toMutableList()

    }

    /**
     * Method for get item field name of the given class
     *
     * @param clazz given class
     * @return item field name of the given class
     */
    fun <T : Any> itemField(clazz: Class<T>): String {

        return when (clazz) {

            InvoiceModel::class.java -> InvoiceModel::invoiceItems.name
            PurchaseOrderModel::class.java -> PurchaseOrderModel::purchaseItems.name
            GoodsReceivedModel::class.java -> GoodsReceivedModel::goodsReceivedItems.name
            CreditNoteModel::class.java -> CreditNoteModel::creditNoteItems.name
            DebitNoteModel::class.java -> DebitNoteModel::debitNoteItems.name
            else -> throw IllegalArgumentException("Unsupported state.")

        }

    }

}
