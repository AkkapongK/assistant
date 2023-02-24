package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import junit.framework.TestCase.*
import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.models.BuyerModel
import th.co.dv.p2p.common.models.TransactionModel
import th.co.dv.p2p.common.utilities.TransactionUtils.buildTransactionModel
import th.co.dv.p2p.common.utilities.TransactionUtils.groupSendList
import th.co.dv.p2p.common.utilities.TransactionUtils.groupSendListItem
import th.co.dv.p2p.common.utilities.TransactionUtils.itemField
import th.co.dv.p2p.common.utilities.TransactionUtils.splitTransaction
import th.co.dv.p2p.corda.base.models.*

class TransactionUtilsTest {

    private val invoices = listOf(
            InvoiceModel(
                    linearId = "1",
                    invoiceItems = listOf(
                            InvoiceItemModel(linearId = "1-01")
                    )),
            InvoiceModel(
                    linearId = "2",
                    invoiceItems = listOf(
                            InvoiceItemModel(linearId = "2-01"),
                            InvoiceItemModel(linearId = "2-02"),
                            InvoiceItemModel(linearId = "2-03")
                    )),
            InvoiceModel(
                    linearId = "3",
                    invoiceItems = listOf(
                            InvoiceItemModel(linearId = "3-01"),
                            InvoiceItemModel(linearId = "3-02"),
                            InvoiceItemModel(linearId = "3-03"),
                            InvoiceItemModel(linearId = "3-04"),
                            InvoiceItemModel(linearId = "3-05")
                    )),
            InvoiceModel(
                    linearId = "4",
                    invoiceItems = listOf(
                            InvoiceItemModel(linearId = "4-01"),
                            InvoiceItemModel(linearId = "4-02"),
                            InvoiceItemModel(linearId = "4-03"),
                            InvoiceItemModel(linearId = "4-04"),
                            InvoiceItemModel(linearId = "4-05"),
                            InvoiceItemModel(linearId = "4-06"),
                            InvoiceItemModel(linearId = "4-07"),
                            InvoiceItemModel(linearId = "4-08"),
                            InvoiceItemModel(linearId = "4-09")
                    )),
            InvoiceModel(
                    linearId = "5",
                    invoiceItems = listOf(
                            InvoiceItemModel(linearId = "5-01"),
                            InvoiceItemModel(linearId = "5-02"),
                            InvoiceItemModel(linearId = "5-03"),
                            InvoiceItemModel(linearId = "5-04"),
                            InvoiceItemModel(linearId = "5-05"),
                            InvoiceItemModel(linearId = "5-06"),
                            InvoiceItemModel(linearId = "5-07"),
                            InvoiceItemModel(linearId = "5-08"),
                            InvoiceItemModel(linearId = "5-09")
                    )),
            InvoiceModel(
                    linearId = "6",
                    invoiceItems = listOf(
                            InvoiceItemModel(linearId = "6-01"),
                            InvoiceItemModel(linearId = "6-02"),
                            InvoiceItemModel(linearId = "6-03"),
                            InvoiceItemModel(linearId = "6-04"),
                            InvoiceItemModel(linearId = "6-05"),
                            InvoiceItemModel(linearId = "6-06"),
                            InvoiceItemModel(linearId = "6-07"),
                            InvoiceItemModel(linearId = "6-08"),
                            InvoiceItemModel(linearId = "6-09"),
                            InvoiceItemModel(linearId = "6-10")
                    )),
            InvoiceModel(
                    linearId = "7",
                    invoiceItems = listOf(
                            InvoiceItemModel(linearId = "7-01"),
                            InvoiceItemModel(linearId = "7-02"),
                            InvoiceItemModel(linearId = "7-03"),
                            InvoiceItemModel(linearId = "7-04"),
                            InvoiceItemModel(linearId = "7-05"),
                            InvoiceItemModel(linearId = "7-06"),
                            InvoiceItemModel(linearId = "7-07"),
                            InvoiceItemModel(linearId = "7-08"),
                            InvoiceItemModel(linearId = "7-09"),
                            InvoiceItemModel(linearId = "7-10"),
                            InvoiceItemModel(linearId = "7-11")
                    ))
    )

    private val splitTransactionModel1: List<TransactionModel<InvoiceModel, InvoiceItemModel>> = listOf(
            TransactionModel(headerState = invoices[5].copy(invoiceItems = emptyList())),
            TransactionModel(itemStates = invoices[5].invoiceItems)
    )

    private val splitTransactionModel2: List<TransactionModel<InvoiceModel, InvoiceItemModel>> = listOf(
            TransactionModel(headerState = invoices[6].copy(invoiceItems = emptyList()), itemStates = listOf(invoices[6].invoiceItems.last())),
            TransactionModel(itemStates = invoices[6].invoiceItems.subList(0, 10))
    )

    @Test
    fun testGroupSendListItem() {

        // Case list have more than max state
        var result = groupSendListItem(invoices.last().invoiceItems, 10)

        assertEquals(2, result.size)
        assertEquals(10, result.first().size)
        assertEquals(1, result.last().size)

        // Case list not have more than max state
        result = groupSendListItem(invoices[5].invoiceItems, 10)

        assertEquals(1, result.size)
        assertEquals(10, result.first().size)

    }

    @Test
    fun testManageDocumentTransaction() {

        val invoiceIn = listOf(invoices[5], invoices[6])
        val transactionUtils = spyk<TransactionUtils>(recordPrivateCalls = true)

        every { transactionUtils.groupSendList<InvoiceModel, InvoiceItemModel>(any(), any()) } returns mutableListOf(splitTransactionModel1, splitTransactionModel2)

        val result = transactionUtils.manageDocumentTransaction<InvoiceModel, InvoiceItemModel>(invoiceIn, 10)
        assertEquals(2, result.size)

        // First chuck
        assertEquals(2, result.first().size)
        // tx1 have 1 header with no items
        assertEquals(splitTransactionModel1.first().headerState, result.first().first().headerState)
        assertEquals(emptyList<InvoiceItemModel>(), result.first().first().itemStates)
        // tx2 have no header with 10 items
        assertNull(result.first().last().headerState)
        assertEquals(splitTransactionModel1.last().itemStates, result.first().last().itemStates)

        // Second chuck
        assertEquals(2, result.last().size)
        // tx1 heave header with 1 items inside Header model and no separate items
        assertEquals(splitTransactionModel2.first().headerState!!.copy(invoiceItems = listOf(invoices[6].invoiceItems.last())), result.last().first().headerState)
        assertEquals(emptyList<InvoiceItemModel>(), result.last().first().itemStates)
        // tx2 heave no header with 10 items
        assertNull(result.last().last().headerState)
        assertEquals(splitTransactionModel2.last().itemStates, result.last().last().itemStates)

        verify(exactly = 1) { transactionUtils.groupSendList<InvoiceModel, InvoiceItemModel>(any(), any()) }

    }

    @Test
    fun testGroupSendList() {

        mockkObject(TransactionUtils)

        every { itemField(InvoiceModel::class.java) } returns InvoiceModel::invoiceItems.name

        every { splitTransaction(
                match<InvoiceModel> { it.getFieldValue<String>("linearId") == "6" },
                any<List<InvoiceItemModel>>(),
                any())
        } returns splitTransactionModel1.toMutableList()

        every { splitTransaction(
                match<InvoiceModel> { it.getFieldValue<String>("linearId") == "7" },
                any<List<InvoiceItemModel>>(),
                any())
        } returns splitTransactionModel2.toMutableList()

        var result = groupSendList<InvoiceModel, InvoiceItemModel>(invoices, 10)

        // Print for checking result
        result.forEachIndexed { index, transactionList ->
            val sumState = transactionList.sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) }
            println("TX${index + 1}, with state $sumState")
            transactionList.forEach { println("inv ${it.headerState?.linearId}, items size ${it.itemStates?.size}") }
        }

        assertEquals(8, result.size)

        assertEquals(6, result[0].sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) })
        assertEquals(6, result[1].sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) })
        assertEquals(10, result[2].sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) })
        assertEquals(10, result[3].sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) })
        assertEquals(1, result[4].sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) })
        assertEquals(10, result[5].sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) })
        assertEquals(2, result[6].sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) })
        assertEquals(10, result[7].sumOf { (it.headerState?.let { 1 } ?: 0) + (it.itemStates?.size ?: 0) })

        assertEquals(listOf("1", "2"), result[0].mapNotNull { it.headerState?.linearId })
        assertEquals(listOf("3"), result[1].mapNotNull { it.headerState?.linearId })
        assertEquals(listOf("4"), result[2].mapNotNull { it.headerState?.linearId })
        assertEquals(listOf("5"), result[3].mapNotNull { it.headerState?.linearId })
        assertEquals(listOf("6"), result[4].mapNotNull { it.headerState?.linearId })
        assertEquals(listOf<String>(), result[5].mapNotNull { it.headerState?.linearId })
        assertEquals(listOf("7"), result[6].mapNotNull { it.headerState?.linearId })
        assertEquals(listOf<String>(), result[7].mapNotNull { it.headerState?.linearId })

        verify(exactly = 1) { itemField(InvoiceModel::class.java) }
        verify(exactly = 1) { splitTransaction(match<InvoiceModel> { it.getFieldValue<String>("linearId") == "6" }, any<List<InvoiceItemModel>>(), any()) }
        verify(exactly = 1) { splitTransaction(match<InvoiceModel> { it.getFieldValue<String>("linearId") == "7" }, any<List<InvoiceItemModel>>(), any()) }

        // Case every state include in one transaction
        result = groupSendList(invoices.subList(0, 4), 100)
        assertEquals(1, result.size)
        assertEquals(listOf("1", "2", "3", "4"), result[0].mapNotNull { it.headerState?.linearId })

        verify(exactly = 2) { itemField(InvoiceModel::class.java) }
        verify(exactly = 1) { splitTransaction(match<InvoiceModel> { it.getFieldValue<String>("linearId") == "6" }, any<List<InvoiceItemModel>>(), any()) }
        verify(exactly = 1) { splitTransaction(match<InvoiceModel> { it.getFieldValue<String>("linearId") == "7" }, any<List<InvoiceItemModel>>(), any()) }

    }

    @Test
    fun testBuildTransactionModel() {

        val invoiceNoItem = invoices.first().copy(invoiceItems = emptyList())
        val invoiceItems = invoices.first().invoiceItems

        // Case have both invoice and invoice item
        var result = Try.on { buildTransactionModel(invoiceNoItem, invoiceItems) }
        assert(result.isSuccess)
        assertEquals(TransactionModel(headerState = invoiceNoItem, itemStates = invoiceItems), result.getOrThrow())

        // Case have only invoice header
        result = Try.on { buildTransactionModel(invoiceNoItem, null) }
        assert(result.isSuccess)
        assertEquals(TransactionModel<InvoiceModel, InvoiceItemModel>(headerState = invoiceNoItem, itemStates = null), result.getOrThrow())

        // Case have only invoice item
        result = Try.on { buildTransactionModel(null, invoiceItems) }
        assert(result.isSuccess)
        assertEquals(TransactionModel<InvoiceModel, InvoiceItemModel>(headerState = null, itemStates = invoiceItems), result.getOrThrow())

        // Case don't have both invoice and invoice item
        result = Try.on { buildTransactionModel(null, null) }
        assert(result.isFailure)
        assert(result.toString().contains("Must have at least header state or items state when build TransactionModel."))

    }

    @Test
    fun testSplitTransaction() {

        var invoiceNoItem = invoices[5].copy(invoiceItems = emptyList())
        var invoiceItems = invoices[5].invoiceItems

        // Case item is equal max state per transaction
        var result = splitTransaction(invoiceNoItem, invoiceItems, 10)

        assertEquals(2, result.size)
        assertEquals(splitTransactionModel1, result)

        invoiceNoItem = invoices[6].copy(invoiceItems = emptyList())
        invoiceItems = invoices[6].invoiceItems

        // Case item is more than max state per transaction
        result = splitTransaction(invoiceNoItem, invoiceItems, 10)

        assertEquals(2, result.size)
        assertEquals(splitTransactionModel2, result)

    }

    @Test
    fun testItemField() {

        // Case Invoice
        var result = itemField(InvoiceModel::class.java)
        assertNotNull(result)
        assertEquals(InvoiceModel::invoiceItems.name, result)

        // Case Purchase Order
        result = itemField(PurchaseOrderModel::class.java)
        assertNotNull(result)
        assertEquals(PurchaseOrderModel::purchaseItems.name, result)

        // Case Goods Received
        result = itemField(GoodsReceivedModel::class.java)
        assertNotNull(result)
        assertEquals(GoodsReceivedModel::goodsReceivedItems.name, result)

        // Case Credit Note
        result = itemField(CreditNoteModel::class.java)
        assertNotNull(result)
        assertEquals(CreditNoteModel::creditNoteItems.name, result)

        // Case Debit Note
        result = itemField(DebitNoteModel::class.java)
        assertNotNull(result)
        assertEquals(DebitNoteModel::debitNoteItems.name, result)

        // Case unsupported model
        val failedCase = Try.on { itemField(BuyerModel::class.java) }
        assert(failedCase.isFailure)
        assert(failedCase.toString().contains("Unsupported state."))

    }

}