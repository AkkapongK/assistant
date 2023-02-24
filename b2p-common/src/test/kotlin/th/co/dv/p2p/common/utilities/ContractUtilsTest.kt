package th.co.dv.p2p.common.utilities

import io.mockk.*
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.Lifecycle
import th.co.dv.p2p.common.enums.PostingStatuses
import th.co.dv.p2p.common.enums.RecordStatus
import th.co.dv.p2p.common.models.ContractModel
import th.co.dv.p2p.common.utilities.ContractUtils.hasAdvanceItemsWithContract
import th.co.dv.p2p.common.utilities.ContractUtils.updateAdvanceToBeDeductedFromRejectDirectInvoice
import th.co.dv.p2p.common.utilities.ContractUtils.updateRetention
import th.co.dv.p2p.common.utilities.ContractUtils.validateAndUpdateAdvanceToBeDeducted
import th.co.dv.p2p.common.utilities.ContractUtils.validateCalculateAdvanceDeDeduct
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import th.co.dv.p2p.corda.base.models.PartyModel
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContractUtilsTest {

    private val contractModel = ContractModel(
        sponsor = "Sponsor",
        contractNumber = "contractNumber-1",
        advanceToBeDeducted = 142400.0.toBigDecimal()
    )

    private val invoiceItemModel = InvoiceItemModel(
        purchaseOrderExternalId = "PO-001",
        purchaseItemExternalId = "1",
        externalId = "1",
        materialDescription = "BIG BAG,100X100X125CM,W/LOGO",
        invoiceLinearId = "inv-id",
        quantity = Quantity(
            initial = 200.0.toBigDecimal(),
            unit = "BAG"
        ),
        currency = "THB",
        unitPrice = 356.0.toBigDecimal(),
        itemSubTotal = 71200.0.toBigDecimal(),
        vatCode = "V7",
        vatRate = 7.0.toBigDecimal(),
        customisedFields = emptyMap(),
        customisedFieldsUpdatedDate = "2019-12-09T10:30:55.000Z",
        site = "1023",
        linearId = "invoice-item-1",
        siteDescription = "TPE Rayong Site3",
        lifecycle = "ISSUED",
        materialGroup = "MAT1",
        withholdingTaxFormType = "INVOICE",
        section = null,
        sectionDescription = null,
        buyer = PartyModel(
            legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
            organisation = "SCG1",
            organisationUnit = "BUYER"
        ),
        seller = PartyModel(
            legalName = "OU=SELLER, O=SUPPLIER1, L=Bangkok, C=TH",
            organisation = "SUPPLIER1",
            organisationUnit = "SELLER"
        ),
        status = RecordStatus.VALID.name,
        itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name,
        contractNumber = contractModel.contractNumber
    )

    private val invoiceModel = InvoiceModel(
        vendorNumber = "0001042940",
        vendorBranchCode = "00000",
        vendorName = "BANGKOK POLYBULK CO.,LTD.",
        vendorTaxNumber = "0745550001199",
        vendorAddress = "94/2 ม.8 ซ.สุขสวัสดิ์ 72 ถ.สุขสวัสดิ์ ต.บางครุ อ.พระประแดง จ.สมุทรปราการ 74110งเทพฯ 10600",
        companyCode = "0100",
        companyName = "บริษัท ไทยโพลิเอททีลีน จำกัด",
        companyTaxNumber = "0105556176239",
        companyBranchCode = "00002",
        companyBranchName = "TPE Rayong Site3",
        companyAddress = "271 ถนนสุขุมวิท ตำบลมาบตาพุด อำเภอเมืองระยอง กรุงเทพ 21150",
        businessPlace = "0021",
        paymentTermCode = "NT30",
        paymentTermDays = 30,
        paymentTermDesc = "",
        currency = "THB",
        subTotal = 71200.0.toBigDecimal(),
        vatTotal = 4984.0.toBigDecimal(),
        invoiceTotal = 76184.0.toBigDecimal(),
        totalPayable = 76184.0.toBigDecimal(),
        externalId = "INV-002",
        invoiceDate = "2019-12-09T10:30:55.000Z",
        dueDate = "2019-12-09T10:30:55.000Z",
        invoiceCreatedDate = "2019-12-09T10:30:55.000Z",
        paymentDate = null,
        invoiceFinancing = 'N',
        isETaxInvoice = null,
        receiptNumber = null,
        lifecycle = Lifecycle.InvoiceLifecycle.ISSUED.name,
        postingStatus = PostingStatuses.InvoicePostingStatus.PENDING_CANCEL.name,
        referenceField1 = "1023",
        linearId = "inv-id",
        calendarKey = "P1",
        invoiceItems = listOf(invoiceItemModel),
        buyer = PartyModel(
            legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
            organisation = "SCG1",
            organisationUnit = "BUYER"
        ),
        seller = PartyModel(
            legalName = "OU=SELLER, O=SUPPLIER1, L=Bangkok, C=TH",
            organisation = "SUPPLIER1",
            organisationUnit = "SELLER"
        ),
        status = RecordStatus.VALID.name
    )

    @Test
    fun `List ContractModel validateAndUpdateAdvanceToBeDeducted`() {
        mockkObject(ContractUtils)

        // Case invoiceItemsAdvanceDeductGroupByContractNumber is empty
        var result = Try.on {
            listOf(contractModel).validateAndUpdateAdvanceToBeDeducted(InvoiceModel())
        }
        assertTrue(result.isSuccess)
        assertEquals(listOf(contractModel), result.getOrThrow())
        verify(exactly = 0) {
            ContractUtils["calculateFinalAdvanceToBeDeducted"](any<BigDecimal>(), any<BigDecimal>(), any<BigDecimal>())
            ContractUtils["validateUpdatedDeductionAmount"](any<BigDecimal>(), any<BigDecimal>(), any<BigDecimal>())
        }

        val contractModel2 = contractModel.copy(contractNumber = "contractNumber-2")
        val invoiceItemModel2 = invoiceItemModel.copy(linearId = "invoice-item-2")
        val invoiceItemModel3 = invoiceItemModel.copy(linearId = "invoice-item-3", contractNumber = contractModel2.contractNumber)
        val invoiceModel = invoiceModel.copy(invoiceItems = listOf(invoiceItemModel, invoiceItemModel2, invoiceItemModel3))

        // Contract number contractNumber-1 advance to be deducted less than zero.
        result = Try.on {
            listOf(contractModel.copy(advanceToBeDeducted = BigDecimal(-10))).validateAndUpdateAdvanceToBeDeducted(invoiceModel)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Advance to be deducted mount of contract contractNumber-1 is less than zero."))

        // No cancelled items
        val createdInvoiceModel = invoiceModel.copy(invoiceItems = listOf(invoiceItemModel))
        every { ContractUtils["calculateFinalAdvanceToBeDeducted"](contractModel.advanceToBeDeducted, invoiceItemModel.itemSubTotal, any<BigDecimal>()) } returns BigDecimal.ZERO
        every { ContractUtils["validateUpdatedDeductionAmount"](contractModel.advanceToBeDeducted, invoiceItemModel.itemSubTotal, any<BigDecimal>()) } returns Unit
        result = Try.on {
            listOf(contractModel).validateAndUpdateAdvanceToBeDeducted(createdInvoiceModel)
        }
        assertTrue(result.isSuccess)
        assertEquals(listOf(contractModel.copy(advanceToBeDeducted = BigDecimal.ZERO)), result.getOrThrow())
        clearAllMocks()

        every { ContractUtils["calculateFinalAdvanceToBeDeducted"](contractModel.advanceToBeDeducted, invoiceItemModel.itemSubTotal, any<BigDecimal>()) } returns BigDecimal.TEN.negate()
        result = Try.on {
            listOf(contractModel).validateAndUpdateAdvanceToBeDeducted(createdInvoiceModel)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("The final advance to be deducted amount of contract contractNumber-1 is less than zero."))
        clearAllMocks()

        // Case have cancelled items
        val cancelledInvoice =
            invoiceModel.copy(invoiceItems = listOf(invoiceItemModel.copy(lifecycle = Lifecycle.InvoiceItemLifecycle.CANCELLED.name)))
        every { ContractUtils["calculateFinalAdvanceToBeDeducted"](contractModel.advanceToBeDeducted, BigDecimal.ZERO, invoiceItemModel.itemSubTotal) } returns BigDecimal.ZERO
        result = Try.on {
            listOf(contractModel).validateAndUpdateAdvanceToBeDeducted(cancelledInvoice, createdInvoiceModel)
        }
        assertTrue(result.isSuccess)
        assertEquals(listOf(contractModel.copy(advanceToBeDeducted = BigDecimal.ZERO)), result.getOrThrow())
        unmockkAll()
    }

    @Test
    fun `List ContractModel updateAdvanceToBeDeductedFromRejectDirectInvoice`() {
        // Case invoiceItemsAdvanceDeductGroupByContractNumber is empty
        var result = Try.on {
            listOf(contractModel).updateAdvanceToBeDeductedFromRejectDirectInvoice(InvoiceModel())
        }
        assertTrue(result.isSuccess)
        assertEquals(listOf(contractModel), result.getOrThrow())

        // Contract number contractNumber-1 advance to be deducted less than zero.
        result = Try.on {
            listOf(contractModel.copy(advanceToBeDeducted = BigDecimal(-10))).updateAdvanceToBeDeductedFromRejectDirectInvoice(invoiceModel)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Advance to be deducted mount of contract contractNumber-1 is less than zero."))

        // Unable to reject an invoice since the advance payment has already deducted.
        val invalidInvoiceItemsModel = listOf(invoiceItemModel.copy(itemSubTotal = 99999999999.0.toBigDecimal()))
        result = Try.on {
            listOf(contractModel).updateAdvanceToBeDeductedFromRejectDirectInvoice(invoiceModel.copy(invoiceItems = invalidInvoiceItemsModel))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Unable to reject an invoice since the advance payment has already deducted."))

        // Case Success
        result = Try.on {
            listOf(contractModel).updateAdvanceToBeDeductedFromRejectDirectInvoice(invoiceModel)
        }
        assertTrue(result.isSuccess)
        assertEquals(listOf(contractModel.copy(advanceToBeDeducted = 71200.0.toBigDecimal())), result.getOrThrow())
    }

    @Test
    fun `Test calculateFinalAdvanceToBeDeducted`() {
        val result =
            callMethod<ContractUtils, BigDecimal>(ContractUtils, "calculateFinalAdvanceToBeDeducted", 10.toBigDecimal(), 5.toBigDecimal(), 1.toBigDecimal())
        assertEquals(6.toBigDecimal(), result)
    }

    @Test
    fun `Test validateUpdatedDeductionAmount`() {
        var result = Try.on {
            callMethod<ContractUtils, Unit>(ContractUtils, "validateUpdatedDeductionAmount", 10.toBigDecimal(), 5.toBigDecimal(), 1.toBigDecimal())
        }
        assertTrue(result.isSuccess)

        result = Try.on {
            callMethod<ContractUtils, Unit>(ContractUtils, "validateUpdatedDeductionAmount", 10.toBigDecimal(), 12.toBigDecimal(), 1.toBigDecimal())
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Advance deduction items amount must less than or equal ${11.toBigDecimal()}."))

    }

    @Test
    fun `Test List ContractModel updateRetention`() {
        mockkObject(ContractUtils, recordPrivateCalls = true)

        val contracts = listOf(
            ContractModel(sponsor = "DV", contractNumber = "C001", initialRetentionCeilingAmount = BigDecimal.ONE, remainingRetentionCeilingAmount = BigDecimal.ZERO),
            ContractModel(sponsor = "DV", contractNumber = "C003"),
            ContractModel(sponsor = "DV", contractNumber = "C002", initialRetentionCeilingAmount = BigDecimal.ONE, remainingRetentionCeilingAmount = BigDecimal.ZERO)
        )

        val purchaseItems = listOf(
            PurchaseItemModel(linearId = "POI01", contractNumber = "C001"),
            PurchaseItemModel(linearId = "POI02", contractNumber = "C001"),
            PurchaseItemModel(linearId = "POI03", contractNumber = "C002"),
            PurchaseItemModel(linearId = "POI04", contractNumber = "C002")
        )

        val invoiceItem1 = InvoiceItemModel(purchaseItemLinearId = "POI01")
        val invoiceItem2 = InvoiceItemModel(purchaseItemLinearId = "POI02")
        val invoiceItem3 = InvoiceItemModel(purchaseItemLinearId = "POI03")
        val invoiceItem4 = InvoiceItemModel(purchaseItemLinearId = "POI04")

        val nextInvoices = InvoiceModel(lifecycle = "ISSUED", invoiceItems = listOf(invoiceItem1, invoiceItem4, invoiceItem2))
        val previousInvoice = InvoiceModel(lifecycle = "ISSUED", invoiceItems = listOf(invoiceItem1, invoiceItem3))
        val amount = Pair(BigDecimal.ONE, BigDecimal.ZERO)

        every { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem1, invoiceItem2), emptyList<InvoiceItemModel>(), "ISSUED") } returns amount
        every { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem4), emptyList<InvoiceItemModel>(), "ISSUED") } returns amount

        every { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem1, invoiceItem2), listOf(invoiceItem1), "ISSUED") } returns amount
        every { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem4), listOf(invoiceItem3), "ISSUED") } returns amount

        every { ContractUtils["restoreAmount"](contracts.first(), amount.first) } returns contracts.first()
        every { ContractUtils["depleteAmount"](contracts.first(), amount.second) } returns contracts.first()
        every { ContractUtils["restoreAmount"](contracts.last(), amount.first) } returns contracts.last()
        every { ContractUtils["depleteAmount"](contracts.last(), amount.second) } returns contracts.last()

        // Case no previous invoice
        var result = contracts.updateRetention(nextInvoices, purchaseItems, null)
        assertNotNull(result)
        assertEquals(contracts, result)

        verify(exactly = 1) { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem1, invoiceItem2), listOf<InvoiceItemModel>(), "ISSUED") }
        verify(exactly = 1) { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem4), listOf<InvoiceItemModel>(), "ISSUED") }

        verify(exactly = 0) { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem1, invoiceItem2), listOf(invoiceItem1), "ISSUED") }
        verify(exactly = 0) { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem4), listOf(invoiceItem3), "ISSUED") }

        verify(exactly = 1) { ContractUtils["restoreAmount"](contracts.first(), amount.first) }
        verify(exactly = 1) { ContractUtils["depleteAmount"](contracts.first(), amount.second) }
        verify(exactly = 1) { ContractUtils["restoreAmount"](contracts.last(), amount.first) }
        verify(exactly = 1) { ContractUtils["depleteAmount"](contracts.last(), amount.second) }
        verify(exactly = 0) { ContractUtils["restoreAmount"](contracts[1], any<BigDecimal>()) }
        verify(exactly = 0) { ContractUtils["depleteAmount"](contracts[1], any<BigDecimal>()) }
        clearMocks(ContractUtils, answers = false)

        // Case previous invoice
        result = contracts.updateRetention(nextInvoices, purchaseItems, previousInvoice)
        assertNotNull(result)
        assertEquals(contracts, result)

        verify(exactly = 0) { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem1, invoiceItem2), listOf<InvoiceItemModel>(), "ISSUED") }
        verify(exactly = 0) { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem4), listOf<InvoiceItemModel>(), "ISSUED") }

        verify(exactly = 1) { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem1, invoiceItem2), listOf(invoiceItem1), "ISSUED") }
        verify(exactly = 1) { ContractUtils["calculateRetentionAmount"](listOf(invoiceItem4), listOf(invoiceItem3), "ISSUED") }

        verify(exactly = 1) { ContractUtils["restoreAmount"](contracts.first(), amount.first) }
        verify(exactly = 1) { ContractUtils["depleteAmount"](contracts.first(), amount.second) }
        verify(exactly = 1) { ContractUtils["restoreAmount"](contracts.last(), amount.first) }
        verify(exactly = 1) { ContractUtils["depleteAmount"](contracts.last(), amount.second) }
        verify(exactly = 0) { ContractUtils["restoreAmount"](contracts[1], any<BigDecimal>()) }
        verify(exactly = 0) { ContractUtils["depleteAmount"](contracts[1], any<BigDecimal>()) }
    }

    @Test
    fun testCalculateRetentionAmount() {
        val nextInvoices = listOf(
            InvoiceItemModel(retentionAmount = BigDecimal.TEN),
            InvoiceItemModel(retentionAmount = BigDecimal.ONE),
            InvoiceItemModel(retentionAmount = null)
        )
        val previousInvoices = listOf(
            InvoiceItemModel(retentionAmount = BigDecimal.TEN),
            InvoiceItemModel(retentionAmount = null)
        )

        // Case invoice lifecycle is not CANCELLED
        var result = callMethod<ContractUtils, Pair<BigDecimal, BigDecimal>>(
            ContractUtils, "calculateRetentionAmount", nextInvoices, previousInvoices, Lifecycle.InvoiceLifecycle.MATCHED.name
        )
        assertNotNull(result)
        assertEquals(BigDecimal.TEN, result.first)
        assertEquals(BigDecimal(11), result.second)

        // Case invoice lifecycle is CANCELLED
        result = callMethod<ContractUtils, Pair<BigDecimal, BigDecimal>>(
            ContractUtils, "calculateRetentionAmount", nextInvoices, previousInvoices, Lifecycle.InvoiceLifecycle.CANCELLED.name
        )
        assertNotNull(result)
        assertEquals(BigDecimal(11), result.first)
        assertEquals(BigDecimal.ZERO, result.second)

    }

    @Test
    fun testRestoreAmount() {

        val contract = ContractModel(remainingRetentionCeilingAmount = BigDecimal(2), initialRetentionCeilingAmount = BigDecimal(10))
        val contract2 = ContractModel(remainingRetentionCeilingAmount = null, initialRetentionCeilingAmount = BigDecimal(10))

        // Case restore success
        var result = Try.on {
            callMethod<ContractUtils, ContractModel>(
                ContractUtils, "restoreAmount", contract, BigDecimal(8)
            )
        }
        assert(result.isSuccess)
        assertEquals(BigDecimal(10), result.getOrThrow()?.remainingRetentionCeilingAmount)

        // Case restore success with initial data null
        result = Try.on {
            callMethod<ContractUtils, ContractModel>(
                ContractUtils, "restoreAmount", contract2, BigDecimal(8)
            )
        }
        assert(result.isSuccess)
        assertEquals(BigDecimal(8), result.getOrThrow()?.remainingRetentionCeilingAmount)

        // Case restore amount is greater than initial amount
        result = Try.on {
            callMethod<ContractUtils, ContractModel>(
                ContractUtils, "restoreAmount", contract2, BigDecimal(100)
            )
        }
        assert(result.isSuccess)
        assertEquals(contract2.initialRetentionCeilingAmount, result.getOrThrow()?.remainingRetentionCeilingAmount)

    }

    @Test
    fun testDepleteAmount() {
        val contract = ContractModel(remainingRetentionCeilingAmount = BigDecimal(5), initialRetentionCeilingAmount = BigDecimal(10))
        val contract2 = ContractModel(remainingRetentionCeilingAmount = null, initialRetentionCeilingAmount = BigDecimal(10))

        // Case deplete success
        var result = Try.on {
            callMethod<ContractUtils, ContractModel>(
                ContractUtils, "depleteAmount", contract, BigDecimal(5)
            )
        }
        assert(result.isSuccess)
        assertEquals(BigDecimal(0), result.getOrThrow()?.remainingRetentionCeilingAmount)

        // Case deplete failed with initial data null
        result = Try.on {
            callMethod<ContractUtils, ContractModel>(
                ContractUtils, "depleteAmount", contract2, BigDecimal(4)
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains("Amount after deplete must not be negative."))

        // Case deplete failed with initial data not null
        result = Try.on {
            callMethod<ContractUtils, ContractModel>(
                ContractUtils, "depleteAmount", contract, BigDecimal(6)
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains("Amount after deplete must not be negative."))

    }

    @Test
    fun `test hasAdvanceItemsWithContract`(){
        val contracts = listOf(ContractModel(contractNumber = "C01"), ContractModel(contractNumber = "CXXX"))
        val advItemsWithContract = InvoiceItemModel(itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name, contractNumber = "C01")
        val itemWithContract = InvoiceItemModel(itemCategory = ItemCategory.Invoice.NORMAL.name, contractNumber = "C01")
        val item = InvoiceItemModel(itemCategory = ItemCategory.Invoice.NORMAL.name)

        var invoice = InvoiceModel(invoiceItems = listOf(advItemsWithContract, itemWithContract, item))
        var result = invoice.hasAdvanceItemsWithContract(contracts)
        assertTrue(result)

        invoice = InvoiceModel(invoiceItems = listOf(itemWithContract, item))
        result = invoice.hasAdvanceItemsWithContract(contracts)
        assertFalse(result)

        invoice = InvoiceModel(invoiceItems = listOf(item))
        result = invoice.hasAdvanceItemsWithContract(contracts)
        assertFalse(result)

    }

    @Test
    fun `test validateCalculateAdvanceDeDeduct`(){
        var contractModel = ContractModel(advanceToBeDeducted = 10.toBigDecimal(), accumulateAdvanceRedeem = 10.toBigDecimal())

        // case result is more than zero
        var result = Try.on {
            contractModel.validateCalculateAdvanceDeDeduct(4.toBigDecimal(), 3.toBigDecimal())
        }
        assert(result.isSuccess)
        assertEquals(9.toBigDecimal(), result.getOrThrow().advanceToBeDeducted)
        assertEquals(9.toBigDecimal(), result.getOrThrow().accumulateAdvanceRedeem)

        // case result is less than zero
        contractModel = ContractModel(advanceToBeDeducted = 1.toBigDecimal(), accumulateAdvanceRedeem = 1.toBigDecimal())
        result = Try.on {
            contractModel.validateCalculateAdvanceDeDeduct(5.toBigDecimal(), 3.toBigDecimal())
        }
        assert(result.isFailure)
        assert(result.toString().contains("Calculate new Advance to be deducted amount then should more than or equal zero."))
    }

    @Test
    fun `test validateNewDeductAmount`(){

        // case result is more than zero
        var result = Try.on {
            callMethod<ContractUtils, BigDecimal>(
                    ContractUtils, "validateNewDeductAmount", 4.toBigDecimal(), 3.toBigDecimal(), 10.toBigDecimal()
            )
        }
        assertTrue(result.isSuccess)
        assertEquals(9.toBigDecimal(), result.getOrThrow())

        // case result is equal zero
        result = Try.on {
            callMethod<ContractUtils, BigDecimal>(
                    ContractUtils, "validateNewDeductAmount", 15.toBigDecimal(), 5.toBigDecimal(), 10.toBigDecimal()
            )
        }
        assertTrue(result.isSuccess)
        assertEquals(0.toBigDecimal(), result.getOrThrow())

        // case result is less than zero
        result = Try.on {
            callMethod<ContractUtils, BigDecimal>(
                    ContractUtils, "validateNewDeductAmount", 5.toBigDecimal(), 3.toBigDecimal(), 1.toBigDecimal()
            )
        }
        assert(result.isFailure)
        assert(result.toString().contains("Calculate new Advance to be deducted amount then should more than or equal zero."))
    }
}