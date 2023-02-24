package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import junit.framework.TestCase
import junit.framework.TestCase.assertTrue
import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.adjustmentTypeNotSupport
import th.co.dv.p2p.common.enums.*
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.*
import th.co.dv.p2p.corda.base.models.InvoiceStatus.PENDING_SELLER_AFTER_RD_SUBMITTED
import th.co.dv.p2p.corda.base.models.InvoiceStatus.PENDING_SELLER_BEFORE_RD_SUBMITTED
import th.co.dv.p2p.corda.base.models.InvoiceStatus.PENDING_SELLER_BEFORE_RD_SUBMITTED_MATCHER
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InvoiceUtilsTest {

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
            invoiceItems = listOf(
                    InvoiceItemModel(
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
                            status = RecordStatus.VALID.name)
            ),
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
            status = RecordStatus.VALID.name)

    private val creditNoteItemModel = CreditNoteItemModel(
            linearId = "linear-item-id-001",
            externalId = "001",
            creditNoteLinearId = "linear-id-001",
            invoiceItemLinearId = invoiceModel.invoiceItems.first().linearId,
            quantity = Quantity(BigDecimal.TEN, "TON"),
            unit = "TON",
            subTotal = BigDecimal.TEN
    )

    private val creditNoteModel = CreditNoteModel(
            linearId = "linear-id-001",
            externalId = "CN-001",
            adjustmentType = AdjustmentType.QUANTITY.name,
            invoiceLinearId = invoiceModel.linearId,
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
            creditNoteItems = listOf(
                    creditNoteItemModel
            )
    )

    private val debitNoteItemModel = DebitNoteItemModel(
            linearId = "linear-item-id-001",
            externalId = "001",
            debitNoteLinearId = "linear-id-001",
            invoiceItemLinearId = invoiceModel.invoiceItems.first().linearId,
            subTotal = 50.0.toBigDecimal()
    )

    private val debitNoteModel = DebitNoteModel(
            linearId = "linear-id-001",
            externalId = "DN-001",
            adjustmentType = AdjustmentType.PRICE.name,
            invoiceLinearId = invoiceModel.linearId,
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
            debitNoteItems = listOf(
                    debitNoteItemModel
            )
    )

    @Test
    fun testCreateOutputInvoiceItems() {
        // Invoice item first will used to update and return because id match with credit note item
        val invoiceItems = invoiceModel.invoiceItems.map {
            it.copy(
                    creditNoteQuantity = Quantity(50.0.toBigDecimal(), "TON"),
                    creditNoteAdjustedSubtotal = 50.0.toBigDecimal()
            )
        } + invoiceModel.invoiceItems.map {
            it.copy(
                    linearId = "MOCK",
                    creditNoteQuantity = Quantity(10.0.toBigDecimal(), "TON"),
                    creditNoteAdjustedSubtotal = 10.0.toBigDecimal()
            )
        }
        val creditNoteItems = creditNoteModel.creditNoteItems

        // Case QUANTITY
        var invoiceItemsResult = InvoiceUtils.createOutputInvoiceItems(
                invoiceItems = invoiceItems,
                adjustmentType = AdjustmentType.QUANTITY.name,
                creditNoteItems = creditNoteItems.map {
                    it.copy(quantity = Quantity(100.0.toBigDecimal(), "TON"),
                            unit = "TON")}
        )
        assertEquals(1, invoiceItemsResult.size)
        var invoiceItemResult = invoiceItemsResult.single()
        assertTrue(150.0.toBigDecimal().compareTo(invoiceItemResult.creditNoteQuantity!!.initial) == 0)

        // Case QUANTITY FAIL
        var expectedResult = Try.on {
            InvoiceUtils.createOutputInvoiceItems(
                    invoiceItems = invoiceItems,
                    adjustmentType = AdjustmentType.QUANTITY.name,
                    creditNoteItems = creditNoteItems.map { it.copy(
                            quantity = Quantity(151.0.toBigDecimal(), "TON")
                    ) }
            )
        }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("Invoice Item return quantity cannot exceed initial value."))

        // Case PRICE
        invoiceItemsResult =  InvoiceUtils.createOutputInvoiceItems(
                invoiceItems = invoiceItems,
                adjustmentType = AdjustmentType.PRICE.name,
                creditNoteItems = creditNoteItems.map {
                    it.copy(subTotal = 100.0.toBigDecimal())
                }
        )
        assertEquals(1, invoiceItemsResult.size)
        invoiceItemResult = invoiceItemsResult.single()
        assertTrue(150.0.toBigDecimal().compareTo(invoiceItemResult.creditNoteAdjustedSubtotal!!) == 0)

        // Case PRICE FAIL
        expectedResult = Try.on {
            InvoiceUtils.createOutputInvoiceItems(
                    invoiceItems = invoiceItems,
                    adjustmentType = AdjustmentType.PRICE.name,
                    creditNoteItems = creditNoteItems.map { it.copy(
                            subTotal = 71151.0.toBigDecimal()
                    ) }
            )
        }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("Invoice Item adjust amount cannot exceed initial value."))

        // Case OTHER
        expectedResult = Try.on {
            InvoiceUtils.createOutputInvoiceItems(
                    invoiceItems = invoiceItems,
                    adjustmentType = AdjustmentType.OTHER.name,
                    creditNoteItems = creditNoteItems
            )
        }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains(adjustmentTypeNotSupport))
    }

    @Test
    fun testCreateOutputInvoiceItemsForCurrentDebitNote() {

        // Invoice item first will used to update and return because id match with debit note item
        val invoiceItems = invoiceModel.invoiceItems.map {
            it.copy(
                    debitNoteAdjustedSubTotal = 50.0.toBigDecimal()
            )
        } + invoiceModel.invoiceItems.map {
            it.copy(
                    linearId = "MOCK",
                    debitNoteAdjustedSubTotal = 10.0.toBigDecimal()
            )
        }
        val debitNoteItems = debitNoteModel.debitNoteItems

        val invoiceItemsResult = InvoiceUtils.createOutputInvoiceItemsForCurrentDebitNote(
                invoiceItems = invoiceItems,
                debitNoteItems = debitNoteItems
        )
        assertEquals(1, invoiceItemsResult.size)
        val invoiceItemResult = invoiceItemsResult.single()
        assertEquals(100.0.toBigDecimal(), invoiceItemResult.debitNoteAdjustedSubTotal!!)
    }

    @Test
    fun testCreateOutputInvoiceItemsForDeleteCreditNote() {
        // Invoice item first will used to update and return because id match with credit note item
        val invoiceItems = invoiceModel.invoiceItems.map {
            it.copy(
                    creditNoteQuantity = Quantity(50.0.toBigDecimal(), "TON"),
                    creditNoteAdjustedSubtotal = 50.0.toBigDecimal()
            )
        } + invoiceModel.invoiceItems.map {
            it.copy(
                    linearId = "MOCK",
                    creditNoteQuantity = Quantity(10.0.toBigDecimal(), "TON"),
                    creditNoteAdjustedSubtotal = 10.0.toBigDecimal()
            )
        }
        val creditNoteItems = creditNoteModel.creditNoteItems

        // Case QUANTITY
        var invoiceItemsResult = InvoiceUtils.createOutputInvoiceItemsForDeleteCreditNote(
                invoiceItems = invoiceItems,
                adjustmentType = AdjustmentType.QUANTITY.name,
                creditNoteItems = creditNoteItems
        )
        assertEquals(1, invoiceItemsResult.size)
        var invoiceItemResult = invoiceItemsResult.single()
        assertTrue(40.0.toBigDecimal().compareTo(invoiceItemResult.creditNoteQuantity!!.initial) == 0)

        // Case QUANTITY FAIL
        var expectedResult = Try.on {
            InvoiceUtils.createOutputInvoiceItemsForDeleteCreditNote(
                    invoiceItems = invoiceItems,
                    adjustmentType = AdjustmentType.QUANTITY.name,
                    creditNoteItems = creditNoteItems.map { it.copy(
                            quantity = Quantity(51.0.toBigDecimal(), "TON")
                    ) }
            )
        }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("CreditNote quantity in invoice item cannot less than zero."))

        // Case PRICE
        invoiceItemsResult =  InvoiceUtils.createOutputInvoiceItemsForDeleteCreditNote(
                invoiceItems = invoiceItems,
                adjustmentType = AdjustmentType.PRICE.name,
                creditNoteItems = creditNoteItems
        )
        assertEquals(1, invoiceItemsResult.size)
        invoiceItemResult = invoiceItemsResult.single()
        assertTrue(40.0.toBigDecimal().compareTo(invoiceItemResult.creditNoteAdjustedSubtotal!!) == 0)

        // Case PRICE FAIL
        expectedResult = Try.on {
            InvoiceUtils.createOutputInvoiceItemsForDeleteCreditNote(
                    invoiceItems = invoiceItems,
                    adjustmentType = AdjustmentType.PRICE.name,
                    creditNoteItems = creditNoteItems.map { it.copy(
                            subTotal = 51.0.toBigDecimal()
                    ) }
            )
        }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("Credit note subtotal cannot less than zero."))

        // Case OTHER
        expectedResult = Try.on {
            InvoiceUtils.createOutputInvoiceItemsForDeleteCreditNote(
                    invoiceItems = invoiceItems,
                    adjustmentType = AdjustmentType.OTHER.name,
                    creditNoteItems = creditNoteItems
            )
        }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains(adjustmentTypeNotSupport))
    }

    @Test
    fun testCreateOutputInvoiceItemsForDeleteDebitNote() {
        // Invoice item first will used to update and return because id match with debit note item
        val invoiceItems = invoiceModel.invoiceItems.map {
            it.copy(
                    debitNoteAdjustedSubTotal = 50.0.toBigDecimal()
            )
        } + invoiceModel.invoiceItems.map {
            it.copy(
                    linearId = "MOCK",
                    debitNoteAdjustedSubTotal = 10.0.toBigDecimal()
            )
        }
        val debitNoteItems = debitNoteModel.debitNoteItems

        // Case SUCCESS
        val invoiceItemsResult =  InvoiceUtils.createOutputInvoiceItemsForDeleteDebitNote(
                invoiceItems = invoiceItems,
                debitNoteItems = debitNoteItems
        )
        assertEquals(1, invoiceItemsResult.size)
        val invoiceItemResult = invoiceItemsResult.single()
        assertEquals(0.0.toBigDecimal(), invoiceItemResult.debitNoteAdjustedSubTotal!!)

        // Case FAIL
        val expectedResult = Try.on {
            InvoiceUtils.createOutputInvoiceItemsForDeleteDebitNote(
                    invoiceItems = invoiceItems,
                    debitNoteItems = debitNoteItems.map { it.copy(
                            subTotal = 70.0.toBigDecimal()
                    ) }
            )
        }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains("Debit note subtotal cannot less than zero."))

    }

    @Test
    fun testReplaceItemWithUpdatedItem() {

        val existingList = listOf(
                InvoiceItemModel(linearId = "inv-item-01", externalId = "NO-1"),
                InvoiceItemModel(linearId = "inv-item-02", externalId = "NO-2"))
        val updatedList = listOf(InvoiceItemModel(linearId = "inv-item-01", externalId = "NO-1-Updated"))

        val result = InvoiceUtils.replaceItemWithUpdatedItem(
                existingList = existingList,
                updatedList = updatedList
        )

        assertEquals(2, result.size)
        assertEquals("NO-1-Updated", result.first { it.linearId == "inv-item-01" }.externalId)
        assertEquals("NO-2", result.first { it.linearId == "inv-item-02" }.externalId)
    }

    @Test
    fun testUpdateInvoiceItemFromUpdatedCreditNote() {
        val invoiceItemsFromPreviousIn = listOf(
                InvoiceItemModel(linearId = "inv-item-01", externalId = "NO-1")
        )
        val previousCreditNoteModel = CreditNoteModel(
                adjustmentType = AdjustmentType.QUANTITY.name,
                creditNoteItems = listOf(CreditNoteItemModel(invoiceItemLinearId = "inv-item-01"))
        )
        val invoiceItemsIn = listOf(
                InvoiceItemModel(linearId = "inv-item-02", externalId = "NO-2")
        )
        val creditNoteModel = CreditNoteModel(
                adjustmentType = AdjustmentType.QUANTITY.name,
                creditNoteItems = listOf(CreditNoteItemModel(invoiceItemLinearId = "inv-item-02"))
        )

        mockkObject(InvoiceUtils)
        every { InvoiceUtils.createOutputInvoiceItemsForDeleteCreditNote(
                invoiceItems = invoiceItemsFromPreviousIn,
                adjustmentType = previousCreditNoteModel.adjustmentType!!,
                creditNoteItems = previousCreditNoteModel.creditNoteItems) } returns invoiceItemsFromPreviousIn

        every { InvoiceUtils.replaceItemWithUpdatedItem(
                existingList = invoiceItemsIn,
                updatedList = invoiceItemsFromPreviousIn) } returns invoiceItemsIn

        every { InvoiceUtils.createOutputInvoiceItems(
                invoiceItems = invoiceItemsIn,
                adjustmentType = creditNoteModel.adjustmentType!!,
                creditNoteItems = creditNoteModel.creditNoteItems) } returns invoiceItemsIn

        every { InvoiceUtils.combineInvoiceItem(
                subList = invoiceItemsFromPreviousIn,
                mainList = invoiceItemsIn) } returns invoiceItemsIn

        val result = InvoiceUtils.updateInvoiceItemFromUpdatedCreditNote(
                invoiceItemsFromPreviousIn = invoiceItemsFromPreviousIn,
                previousCreditNoteModel = previousCreditNoteModel,
                invoiceItemsIn = invoiceItemsIn,
                creditNoteModel = creditNoteModel
        )

        assertEquals(invoiceItemsIn, result)
        unmockkObject(InvoiceUtils)
    }

    @Test
    fun testUpdateInvoiceItemFromUpdatedDebitNote() {
        val invoiceItemsFromPreviousIn = listOf(
                InvoiceItemModel(linearId = "inv-item-01", externalId = "NO-1")
        )
        val previousDebitNoteModel = DebitNoteModel(
                debitNoteItems = listOf(DebitNoteItemModel(invoiceItemLinearId = "inv-item-01"))
        )
        val invoiceItemsIn = listOf(
                InvoiceItemModel(linearId = "inv-item-02", externalId = "NO-2")
        )
        val debitNoteModel = DebitNoteModel(
                debitNoteItems = listOf(DebitNoteItemModel(invoiceItemLinearId = "inv-item-02"))
        )

        mockkObject(InvoiceUtils)
        every { InvoiceUtils.createOutputInvoiceItemsForDeleteDebitNote(
                invoiceItems = invoiceItemsFromPreviousIn,
                debitNoteItems = previousDebitNoteModel.debitNoteItems) } returns invoiceItemsFromPreviousIn

        every { InvoiceUtils.replaceItemWithUpdatedItem(
                existingList = invoiceItemsIn,
                updatedList = invoiceItemsFromPreviousIn) } returns invoiceItemsIn

        every { InvoiceUtils.createOutputInvoiceItemsForCurrentDebitNote(
                invoiceItems = invoiceItemsIn,
                debitNoteItems = debitNoteModel.debitNoteItems) } returns invoiceItemsIn

        every { InvoiceUtils.combineInvoiceItem(
                subList = invoiceItemsFromPreviousIn,
                mainList = invoiceItemsIn) } returns invoiceItemsIn

        val result = InvoiceUtils.updateInvoiceItemFromUpdatedDebitNote(
                invoiceItemsFromPreviousIn = invoiceItemsFromPreviousIn,
                previousDebitNoteModel = previousDebitNoteModel,
                invoiceItemsIn = invoiceItemsIn,
                debitNoteModel = debitNoteModel
        )

        assertEquals(invoiceItemsIn, result)
        unmockkObject(InvoiceUtils)
    }

    @Test
    fun testCombineInvoiceItem() {
        val subList = listOf(
                InvoiceItemModel(linearId = "01", externalId = "INV-01"),
                InvoiceItemModel(linearId = "02", externalId = "INV-02"))

        val mainList = listOf(
                InvoiceItemModel(linearId = "01", externalId = "INV-01-NEW"),
                InvoiceItemModel(linearId = "03", externalId = "INV-03"))
        val result = InvoiceUtils.combineInvoiceItem(
                subList = subList,
                mainList = mainList)

        assertEquals(3, result.size)
        assertEquals("INV-01-NEW", result.single { it.linearId == "01"}.externalId)
        assertEquals("INV-02", result.single { it.linearId == "02"}.externalId)
        assertEquals("INV-03", result.single { it.linearId == "03"}.externalId)
    }

    @Test
    fun testParseStatus() {

        val invoiceUtility = spyk<InvoiceUtils>()

        // All status is null
        var result = invoiceUtility.parseStatus(null, null, true)

        var translatedStatuses = result.first
        var isRdSubmitted = result.second
        var translatedInvoiceStatuses = translatedStatuses.first
        var translatedMatchingStatus = translatedStatuses.second

        assertNull(translatedInvoiceStatuses)
        assertNull(translatedMatchingStatus)
        assertNull(isRdSubmitted)

        // Send status to convert for seller status
        result = invoiceUtility.parseStatus(listOf("Waiting Payment Due Date"), listOf("Waiting Payment Due Date"), true)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedInvoiceStatuses)
        assertNotNull(translatedMatchingStatus)
        assertNull(isRdSubmitted)

        assertEquals(3, translatedInvoiceStatuses.size)
        assertNotNull(translatedInvoiceStatuses.find { it == "APPROVED" })
        assertNotNull(translatedInvoiceStatuses.find { it == "RESERVED" })
        assertNotNull(translatedInvoiceStatuses.find { it == "PAID_WITHOUT_FINANCED" })

        assertEquals(3, translatedMatchingStatus.size)
        assertNotNull(translatedMatchingStatus.find { it == "APPROVED" })
        assertNotNull(translatedMatchingStatus.find { it == "RESERVED" })
        assertNotNull(translatedMatchingStatus.find { it == "FINANCED" })

        // Send status to convert for buyer status
        result = invoiceUtility.parseStatus(listOf("Waiting Payment Due Date"), null, false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedInvoiceStatuses)
        assertNull(translatedMatchingStatus)
        assertNull(isRdSubmitted)

        assertEquals(3, translatedInvoiceStatuses.size)
        assertNotNull(translatedInvoiceStatuses.find { it == "APPROVED" })
        assertNotNull(translatedInvoiceStatuses.find { it == "RESERVED" })
        assertNotNull(translatedInvoiceStatuses.find { it == "FINANCED" })

        // Send status Request to Cancel for search invoice that have been submitted to RD
        result = invoiceUtility.parseStatus(listOf(PENDING_SELLER_AFTER_RD_SUBMITTED), null, false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedInvoiceStatuses)
        assertNull(translatedMatchingStatus)
        assertTrue(isRdSubmitted!!)

        assertEquals(1, translatedInvoiceStatuses.size)
        assertNotNull(translatedInvoiceStatuses.find { it == "PENDING_SELLER" })

        result = invoiceUtility.parseStatus(null, listOf(PENDING_SELLER_AFTER_RD_SUBMITTED), false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNull(translatedInvoiceStatuses)
        assertNotNull(translatedMatchingStatus)
        assertTrue(isRdSubmitted!!)

        assertEquals(1, translatedMatchingStatus.size)
        assertNotNull(translatedMatchingStatus.find { it == "PENDING_SELLER" })

        // Send status Request to Resubmit for search invoice that have not been submitted to RD
        result = invoiceUtility.parseStatus(listOf(PENDING_SELLER_BEFORE_RD_SUBMITTED), null, false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedInvoiceStatuses)
        assertNull(translatedMatchingStatus)
        assertFalse(isRdSubmitted!!)

        assertEquals(1, translatedInvoiceStatuses.size)
        assertNotNull(translatedInvoiceStatuses.find { it == "PENDING_SELLER" })

        // Send status Request Invoice Resubmission for search invoice that have not been submitted to RD
        result = invoiceUtility.parseStatus(null, listOf(PENDING_SELLER_BEFORE_RD_SUBMITTED_MATCHER), false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNull(translatedInvoiceStatuses)
        assertNotNull(translatedMatchingStatus)
        assertFalse(isRdSubmitted!!)

        assertEquals(1, translatedMatchingStatus.size)
        assertNotNull(translatedMatchingStatus.find { it == "PENDING_SELLER" })

        // Send status Request to Cancel and Request to Resubmit
        result = invoiceUtility.parseStatus(listOf(PENDING_SELLER_AFTER_RD_SUBMITTED, PENDING_SELLER_BEFORE_RD_SUBMITTED), null, false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedInvoiceStatuses)
        assertNull(translatedMatchingStatus)
        assertNull(isRdSubmitted)

        assertEquals(1, translatedInvoiceStatuses.size)
        assertNotNull(translatedInvoiceStatuses.find { it == "PENDING_SELLER" })

        result = invoiceUtility.parseStatus(listOf(PENDING_SELLER_BEFORE_RD_SUBMITTED), listOf(PENDING_SELLER_AFTER_RD_SUBMITTED), false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedInvoiceStatuses)
        assertNotNull(translatedMatchingStatus)
        assertNull(isRdSubmitted)

        assertEquals(1, translatedInvoiceStatuses.size)
        assertEquals(1, translatedMatchingStatus.size)
        assertNotNull(translatedInvoiceStatuses.find { it == "PENDING_SELLER" })
        assertNotNull(translatedMatchingStatus.find { it == "PENDING_SELLER" })

        // Send status Request to Cancel and Request Invoice Resubmission
        result = invoiceUtility.parseStatus(null, listOf(PENDING_SELLER_AFTER_RD_SUBMITTED, PENDING_SELLER_BEFORE_RD_SUBMITTED_MATCHER), false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNull(translatedInvoiceStatuses)
        assertNotNull(translatedMatchingStatus)
        assertNull(isRdSubmitted)

        assertEquals(1, translatedMatchingStatus.size)
        assertNotNull(translatedMatchingStatus.find { it == "PENDING_SELLER" })

        result = invoiceUtility.parseStatus(listOf(PENDING_SELLER_AFTER_RD_SUBMITTED), listOf(PENDING_SELLER_BEFORE_RD_SUBMITTED_MATCHER), false)

        translatedStatuses = result.first
        isRdSubmitted = result.second
        translatedInvoiceStatuses = translatedStatuses.first
        translatedMatchingStatus = translatedStatuses.second

        assertNotNull(translatedInvoiceStatuses)
        assertNotNull(translatedMatchingStatus)
        assertNull(isRdSubmitted)

        assertEquals(1, translatedInvoiceStatuses.size)
        assertEquals(1, translatedMatchingStatus.size)
        assertNotNull(translatedInvoiceStatuses.find { it == "PENDING_SELLER" })
        assertNotNull(translatedMatchingStatus.find { it == "PENDING_SELLER" })
    }


    @Test
    fun testUpdateInvoiceStatus() {

        val invoiceUtility = spyk<InvoiceUtils>()
        val invoiceItemModel = listOf(InvoiceItemModel(
                lifecycle = Lifecycle.InvoiceItemLifecycle.PARTIAL.name,
                status = RecordStatus.VALID.name))

        val invoiceModel = InvoiceModel(
                lifecycle = Lifecycle.InvoiceLifecycle.FINANCED.name,
                status = RecordStatus.VALID.name,
                invoiceItems = invoiceItemModel)

        // Seller status
        var result = invoiceUtility.updateInvoiceStatus(listOf(invoiceModel), true)

        assertEquals(1, result.size)
        assertEquals("Financed", result.first().status)
        assertEquals(1, result.first().invoiceItems.size)
        assertEquals("Partial GR", result.first().invoiceItems.first().status)

        // Buyer status
        result = invoiceUtility.updateInvoiceStatus(listOf(invoiceModel), false)

        assertEquals(1, result.size)
        assertEquals("Waiting Payment Due Date", result.first().status)
        assertEquals(1, result.first().invoiceItems.size)
        assertEquals("Partial GR", result.first().invoiceItems.first().status)

        // Case not send status
        result = invoiceUtility.updateInvoiceStatus(listOf(invoiceModel))

        assertEquals(1, result.size)
        assertEquals("Financed", result.first().status)
        assertEquals(1, result.first().invoiceItems.size)
        assertEquals("Partial GR", result.first().invoiceItems.first().status)

    }

    @Test
    fun testShowStatusAsSeller() {

        val resultTrue = InvoiceUtils.showStatusAsSeller(StatusType.SELLER.name)
        assertTrue(resultTrue)

        val resultFalse = InvoiceUtils.showStatusAsSeller(StatusType.BUYER.name)
        TestCase.assertFalse(resultFalse)

        val resultFailure = Try.on { InvoiceUtils.showStatusAsSeller("SUPPLIER") }
        assertTrue(resultFailure.isFailure)
        assertTrue(resultFailure.toString().contains("Role not allow for using invoice api."))
    }

    @Test
    fun `Test InvoiceModel isRejectDirectInvoiceWithContract`() {

        val invoiceRejectAdvanceDirectWithContract = invoiceModel.copy(
            lifecycle = Lifecycle.InvoiceLifecycle.CANCELLED.name,
            subtype = InvoiceSubType.DIRECT.name,
            invoiceItems = listOf(InvoiceItemModel(itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name, contractNumber = "contractNumber-1"))
        )

        // Case is reject advance direct invoice with contract
        var result = invoiceRejectAdvanceDirectWithContract.isRejectDirectInvoiceWithContract()
        assertTrue(result)

        // Case isn't reject advance direct invoice with contract due to lifecycle != CANCELLED
        result = invoiceRejectAdvanceDirectWithContract.copy(lifecycle = Lifecycle.InvoiceLifecycle.ISSUED.name).isRejectDirectInvoiceWithContract()
        assertFalse(result)

        // Case isn't reject advance direct invoice with contract due to subtype != DIRECT
        result = invoiceRejectAdvanceDirectWithContract.copy(subtype = null).isRejectDirectInvoiceWithContract()
        assertFalse(result)

        // Case isn't reject advance direct invoice with contract due to invoiceItems don't have any contractNumber != null
        result = invoiceRejectAdvanceDirectWithContract.copy(
            invoiceItems = listOf(
                InvoiceItemModel(contractNumber = null),
                InvoiceItemModel(contractNumber = null)
            )
        ).isRejectDirectInvoiceWithContract()
        assertFalse(result)
    }

    @Test
    fun `test isDirectInvoiceWithContract`(){
        val invoiceItemsWithContract = InvoiceItemModel(contractNumber = "xxx")
        val directInvoiceWithContract = InvoiceModel(subtype = InvoiceSubType.DIRECT.name, invoiceItems = listOf(invoiceItemsWithContract, InvoiceItemModel()))

        var result = directInvoiceWithContract.isDirectInvoiceWithContract()
        assertTrue(result)

        var input = directInvoiceWithContract.copy(invoiceItems = listOf(InvoiceItemModel()))
        result = input.isDirectInvoiceWithContract()
        assertFalse(result)

        input = InvoiceModel(invoiceItems = listOf(invoiceItemsWithContract))
        result = input.isDirectInvoiceWithContract()
        assertFalse(result)

        input = InvoiceModel()
        result = input.isDirectInvoiceWithContract()
        assertFalse(result)
    }

}