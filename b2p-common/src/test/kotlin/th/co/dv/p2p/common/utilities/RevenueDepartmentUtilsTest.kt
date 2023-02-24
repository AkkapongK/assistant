package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.spyk
import net.corda.core.node.services.vault.NullOperator
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.DOCUMENT_NOT_SUPPORT_RD_SUBMIT_DATE
import th.co.dv.p2p.common.constants.EMPTY_STRING
import th.co.dv.p2p.common.constants.FROM
import th.co.dv.p2p.common.constants.TO
import th.co.dv.p2p.common.enums.AdjustmentType
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.models.RdSearchFieldsDto
import th.co.dv.p2p.common.utilities.search.SearchCriteriaOperation
import th.co.dv.p2p.corda.base.models.CreditNoteModel
import th.co.dv.p2p.corda.base.models.DebitNoteModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import th.co.dv.p2p.corda.base.models.TaxDocumentModel
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class RevenueDepartmentUtilsTest {


    private val invoiceFields =
            RdSearchFieldsDto(
                    fieldDate = InvoiceModel::invoiceDate.name,
                    fieldVendorTaxNumber = InvoiceModel::vendorTaxNumber.name,
                    fieldIsETax = InvoiceModel::isETaxInvoice.name,
                    fieldRdSubmittedDate = InvoiceModel::rdSubmittedDate.name
            )
    private val creditNoteFields =
            RdSearchFieldsDto(
                    fieldDate = CreditNoteModel::creditNoteDate.name,
                    fieldVendorTaxNumber = CreditNoteModel::vendorTaxNumber.name,
                    fieldIsETax = CreditNoteModel::isETaxCreditNote.name,
                    fieldRdSubmittedDate = CreditNoteModel::rdSubmittedDate.name
            )
    private val debitNoteFields =
            RdSearchFieldsDto(
                    fieldDate = DebitNoteModel::debitNoteDate.name,
                    fieldVendorTaxNumber = DebitNoteModel::vendorTaxNumber.name,
                    fieldIsETax = DebitNoteModel::isETaxDebitNote.name,
                    fieldRdSubmittedDate = DebitNoteModel::rdSubmittedDate.name
            )

    private val taxDocumentFields =
            RdSearchFieldsDto(
                    fieldDate = TaxDocumentModel::documentDate.name,
                    fieldVendorTaxNumber = TaxDocumentModel::vendorTaxNumber.name,
                    fieldIsETax = null,
                    fieldRdSubmittedDate = TaxDocumentModel::rdSubmittedDate.name
            )

    private val adjustmentDebitNote = (
            mutableMapOf(DebitNoteModel::adjustmentType.name to AdjustmentType.OTHER.name) to
                    mutableMapOf(DebitNoteModel::adjustmentType.name to SearchCriteriaOperation.NOT_EQUAL.name))

    private val adjustmentCreditNote = (
            mutableMapOf(CreditNoteModel::adjustmentType.name to AdjustmentType.OTHER.name) to
                    mutableMapOf(CreditNoteModel::adjustmentType.name to SearchCriteriaOperation.NOT_EQUAL.name))

    @Test
    fun buildCriteriasGetDocumentUpdateRdDate() {
        val revenueDepartmentUtils = spyk<RevenueDepartmentUtils>()
        val now = Date.from(Instant.now())
        val exceptedInvoiceParams = mutableMapOf<String, Any>(
                InvoiceModel::vendorTaxNumber.name to "123",
                InvoiceModel::invoiceDate.name + FROM to now,
                InvoiceModel::invoiceDate.name + TO to now,
                InvoiceModel::isETaxInvoice.name to false,
                InvoiceModel::rdSubmittedDate.name to NullOperator.IS_NULL.name
        )

        val exceptedCreditNoteParams = mutableMapOf<String, Any>(
                CreditNoteModel::vendorTaxNumber.name to "123",
                CreditNoteModel::creditNoteDate.name + FROM to now,
                CreditNoteModel::creditNoteDate.name + TO to now,
                CreditNoteModel::isETaxCreditNote.name to false,
                CreditNoteModel::adjustmentType.name to AdjustmentType.OTHER.name,
                CreditNoteModel::rdSubmittedDate.name to NullOperator.IS_NULL.name
        )

        val exceptedDebitNoteParams = mutableMapOf<String, Any>(
                DebitNoteModel::vendorTaxNumber.name to "123",
                DebitNoteModel::debitNoteDate.name + FROM to now,
                DebitNoteModel::debitNoteDate.name + TO to now,
                DebitNoteModel::isETaxDebitNote.name to false,
                DebitNoteModel::adjustmentType.name to AdjustmentType.OTHER.name,
                DebitNoteModel::rdSubmittedDate.name to NullOperator.IS_NULL.name
        )

        val exceptedTaxDocumentParams = mutableMapOf<String, Any>(
                TaxDocumentModel::vendorTaxNumber.name to "123",
                TaxDocumentModel::documentDate.name + FROM to now,
                TaxDocumentModel::documentDate.name + TO to now,
                TaxDocumentModel::rdSubmittedDate.name to NullOperator.IS_NULL.name
        )

        val exceptedInvoiceOperations = mutableMapOf(
                InvoiceModel::vendorTaxNumber.name to SearchCriteriaOperation.EQUAL.name,
                InvoiceModel::invoiceDate.name + FROM to SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name,
                InvoiceModel::invoiceDate.name + TO to SearchCriteriaOperation.LESSTHAN.name,
                InvoiceModel::isETaxInvoice.name to SearchCriteriaOperation.EQUAL.name,
                InvoiceModel::rdSubmittedDate.name to SearchCriteriaOperation.ISNULL.name
        )

        val exceptedCreditNoteOperations = mutableMapOf(
                CreditNoteModel::vendorTaxNumber.name to SearchCriteriaOperation.EQUAL.name,
                CreditNoteModel::creditNoteDate.name + FROM to SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name,
                CreditNoteModel::creditNoteDate.name + TO to SearchCriteriaOperation.LESSTHAN.name,
                CreditNoteModel::isETaxCreditNote.name to SearchCriteriaOperation.EQUAL.name,
                CreditNoteModel::adjustmentType.name to SearchCriteriaOperation.NOT_EQUAL.name,
                CreditNoteModel::rdSubmittedDate.name to SearchCriteriaOperation.ISNULL.name
        )

        val exceptedDebitNoteOperations = mutableMapOf(
                DebitNoteModel::vendorTaxNumber.name to SearchCriteriaOperation.EQUAL.name,
                DebitNoteModel::debitNoteDate.name + FROM to SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name,
                DebitNoteModel::debitNoteDate.name + TO to SearchCriteriaOperation.LESSTHAN.name,
                DebitNoteModel::isETaxDebitNote.name to SearchCriteriaOperation.EQUAL.name,
                DebitNoteModel::adjustmentType.name to SearchCriteriaOperation.NOT_EQUAL.name,
                DebitNoteModel::rdSubmittedDate.name to SearchCriteriaOperation.ISNULL.name
        )

        val exceptedTaxDocumentOperations = mutableMapOf(
                TaxDocumentModel::vendorTaxNumber.name to SearchCriteriaOperation.EQUAL.name,
                TaxDocumentModel::documentDate.name + FROM to SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name,
                TaxDocumentModel::documentDate.name + TO to SearchCriteriaOperation.LESSTHAN.name,
                TaxDocumentModel::rdSubmittedDate.name to SearchCriteriaOperation.ISNULL.name
        )


        //Case invoice with vendor tax id
        every { revenueDepartmentUtils["generateFieldsByService"](Services.INVOICE.name) } returns invoiceFields
        every { revenueDepartmentUtils["getAdjustmentFieldsInDocumentByService"](Services.INVOICE) } returns EMPTY_STRING
        every { revenueDepartmentUtils["buildAdjustmentTypeCriterias"](EMPTY_STRING) } returns (emptyMap<String, Any>() to emptyMap<String, String>())
        var exceptedResult = (exceptedInvoiceParams to exceptedInvoiceOperations)
        var result = revenueDepartmentUtils.buildCriteriasGetDocumentUpdateRdDate(service = Services.INVOICE, vendorTaxId = "123", dateFrom = now, dateTo = now)
        assertEquals(exceptedResult, result)

        //Case credit note with vendor tax id
        every { revenueDepartmentUtils["generateFieldsByService"](Services.CREDIT_NOTE.name) } returns creditNoteFields
        every { revenueDepartmentUtils["getAdjustmentFieldsInDocumentByService"](Services.CREDIT_NOTE) } returns CreditNoteModel::adjustmentType.name
        every { revenueDepartmentUtils["buildAdjustmentTypeCriterias"](CreditNoteModel::adjustmentType.name) } returns adjustmentCreditNote
        exceptedResult = (exceptedCreditNoteParams to exceptedCreditNoteOperations)
        result = revenueDepartmentUtils.buildCriteriasGetDocumentUpdateRdDate(service = Services.CREDIT_NOTE, vendorTaxId = "123", dateFrom = now, dateTo = now)
        assertEquals(exceptedResult, result)


        //Case debit note with vendor tax id
        every { revenueDepartmentUtils["generateFieldsByService"](Services.DEBIT_NOTE.name) } returns debitNoteFields
        every { revenueDepartmentUtils["getAdjustmentFieldsInDocumentByService"](Services.DEBIT_NOTE) } returns DebitNoteModel::adjustmentType.name
        every { revenueDepartmentUtils["buildAdjustmentTypeCriterias"](DebitNoteModel::adjustmentType.name) } returns adjustmentDebitNote
        exceptedResult = (exceptedDebitNoteParams to exceptedDebitNoteOperations)
        result = revenueDepartmentUtils.buildCriteriasGetDocumentUpdateRdDate(service = Services.DEBIT_NOTE, vendorTaxId = "123", dateFrom = now, dateTo = now)
        assertEquals(exceptedResult, result)

        //Case taxDocument with vendor tax id
        every { revenueDepartmentUtils["generateFieldsByService"](Services.TAX_DOCUMENT.name) } returns taxDocumentFields
        every { revenueDepartmentUtils["getAdjustmentFieldsInDocumentByService"](Services.TAX_DOCUMENT) } returns EMPTY_STRING
        every { revenueDepartmentUtils["buildAdjustmentTypeCriterias"](EMPTY_STRING) } returns (emptyMap<String, Any>() to emptyMap<String, String>())
        exceptedResult = (exceptedTaxDocumentParams to exceptedTaxDocumentOperations)
        result = revenueDepartmentUtils.buildCriteriasGetDocumentUpdateRdDate(service = Services.TAX_DOCUMENT, vendorTaxId = "123", dateFrom = now, dateTo = now)
        assertEquals(exceptedResult, result)

        //Case invoice with out vendor tax id
        every { revenueDepartmentUtils["generateFieldsByService"](Services.INVOICE.name) } returns invoiceFields
        exceptedInvoiceParams.remove(InvoiceModel::vendorTaxNumber.name)
        exceptedInvoiceOperations.remove(InvoiceModel::vendorTaxNumber.name)

        exceptedResult = (exceptedInvoiceParams to exceptedInvoiceOperations)
        result = revenueDepartmentUtils.buildCriteriasGetDocumentUpdateRdDate(service = Services.INVOICE, vendorTaxId = null, dateFrom = now, dateTo = now)

        assertEquals(exceptedResult, result)

        //Case credit note with out vendor tax id
        every { revenueDepartmentUtils["generateFieldsByService"](Services.CREDIT_NOTE.name) } returns creditNoteFields
        every { revenueDepartmentUtils["getAdjustmentFieldsInDocumentByService"](Services.CREDIT_NOTE) } returns CreditNoteModel::adjustmentType.name
        every { revenueDepartmentUtils["buildAdjustmentTypeCriterias"](CreditNoteModel::adjustmentType.name) } returns adjustmentCreditNote
        exceptedCreditNoteParams.remove(CreditNoteModel::vendorTaxNumber.name)
        exceptedCreditNoteOperations.remove(CreditNoteModel::vendorTaxNumber.name)

        exceptedResult = (exceptedCreditNoteParams to exceptedCreditNoteOperations)
        result = revenueDepartmentUtils.buildCriteriasGetDocumentUpdateRdDate(service = Services.CREDIT_NOTE, vendorTaxId = null, dateFrom = now, dateTo = now)

        assertEquals(exceptedResult, result)

        //Case debit note with out vendor tax id
        every { revenueDepartmentUtils["generateFieldsByService"](Services.DEBIT_NOTE.name) } returns debitNoteFields
        every { revenueDepartmentUtils["getAdjustmentFieldsInDocumentByService"](Services.DEBIT_NOTE) } returns DebitNoteModel::adjustmentType.name
        every { revenueDepartmentUtils["buildAdjustmentTypeCriterias"](DebitNoteModel::adjustmentType.name) } returns adjustmentDebitNote
        exceptedDebitNoteParams.remove(DebitNoteModel::vendorTaxNumber.name)
        exceptedDebitNoteOperations.remove(DebitNoteModel::vendorTaxNumber.name)

        exceptedResult = (exceptedDebitNoteParams to exceptedDebitNoteOperations)
        result = revenueDepartmentUtils.buildCriteriasGetDocumentUpdateRdDate(service = Services.DEBIT_NOTE, vendorTaxId = null, dateFrom = now, dateTo = now)

        assertEquals(exceptedResult, result)

        //Case taxDocument with out vendor tax id
        every { revenueDepartmentUtils["generateFieldsByService"](Services.TAX_DOCUMENT.name) } returns taxDocumentFields
        exceptedTaxDocumentParams.remove(TaxDocumentModel::vendorTaxNumber.name)
        exceptedTaxDocumentOperations.remove(TaxDocumentModel::vendorTaxNumber.name)

        exceptedResult = (exceptedTaxDocumentParams to exceptedTaxDocumentOperations)
        result = revenueDepartmentUtils.buildCriteriasGetDocumentUpdateRdDate(service = Services.TAX_DOCUMENT, vendorTaxId = null, dateFrom = now, dateTo = now)

        assertEquals(exceptedResult, result)


        //Case document not support
        every { revenueDepartmentUtils["generateFieldsByService"](Services.REQUEST.name) } throws Exception(DOCUMENT_NOT_SUPPORT_RD_SUBMIT_DATE)
        val resultDocumentNotSupport = Try.on {
            revenueDepartmentUtils.buildCriteriasGetDocumentUpdateRdDate(service = Services.REQUEST, vendorTaxId = null, dateFrom = now, dateTo = now)
        }

        assertTrue(resultDocumentNotSupport.isFailure)
        assertTrue(resultDocumentNotSupport.toString().contains(DOCUMENT_NOT_SUPPORT_RD_SUBMIT_DATE))


    }


    @Test
    fun generateFieldsByService() {

        val revenueDepartmentUtils = spyk<RevenueDepartmentUtils>()

        val documentNotSupport = DOCUMENT_NOT_SUPPORT_RD_SUBMIT_DATE

        //Case invoice service
        every {
            revenueDepartmentUtils["assignFieldsName"](
                    InvoiceModel::invoiceDate.name,
                    InvoiceModel::vendorTaxNumber.name,
                    InvoiceModel::isETaxInvoice.name,
                    InvoiceModel::rdSubmittedDate.name)
        } returns invoiceFields
        var result = callMethod<RevenueDepartmentUtils, RdSearchFieldsDto>(revenueDepartmentUtils, "generateFieldsByService", Services.INVOICE.name)
        assertEquals(invoiceFields, result)

        //Case credit note
        every {
            revenueDepartmentUtils["assignFieldsName"](
                    CreditNoteModel::creditNoteDate.name,
                    CreditNoteModel::vendorTaxNumber.name,
                    CreditNoteModel::isETaxCreditNote.name,
                    CreditNoteModel::rdSubmittedDate.name
            )
        } returns creditNoteFields
        result = callMethod<RevenueDepartmentUtils, RdSearchFieldsDto>(revenueDepartmentUtils, "generateFieldsByService", Services.CREDIT_NOTE.name)
        assertEquals(creditNoteFields, result)


        //Case debit note
        every {
            revenueDepartmentUtils["assignFieldsName"](
                    DebitNoteModel::debitNoteDate.name,
                    DebitNoteModel::vendorTaxNumber.name,
                    DebitNoteModel::isETaxDebitNote.name,
                    DebitNoteModel::rdSubmittedDate.name
            )
        } returns debitNoteFields
        result = callMethod<RevenueDepartmentUtils, RdSearchFieldsDto>(revenueDepartmentUtils, "generateFieldsByService", Services.DEBIT_NOTE.name)
        assertEquals(debitNoteFields, result)

        //Case taxDocument note
        every {
            revenueDepartmentUtils["assignFieldsName"](
                    TaxDocumentModel::documentDate.name,
                    TaxDocumentModel::vendorTaxNumber.name,
                    any<String>(),
                    TaxDocumentModel::rdSubmittedDate.name
            )
        } returns taxDocumentFields
        result = callMethod<RevenueDepartmentUtils, RdSearchFieldsDto>(revenueDepartmentUtils, "generateFieldsByService", Services.TAX_DOCUMENT.name)
        assertEquals(taxDocumentFields, result)


        val documentNotSupportResult = Try.on {
            callMethod<RevenueDepartmentUtils, RdSearchFieldsDto>(revenueDepartmentUtils, "generateFieldsByService", Services.REQUEST.name)
        }

        assertTrue(documentNotSupportResult.isFailure)
        assertTrue(documentNotSupportResult.toString().contains(documentNotSupport))

    }

    @Test
    fun buildAdjustmentTypeCriterias() {
        val revenueDepartmentUtils = spyk<RevenueDepartmentUtils>()
        val emptyCriterias = (emptyMap<String, Any>() to emptyMap<String, String>())

        var result = callMethod<RevenueDepartmentUtils, Pair<Map<String, Any>, Map<String, String>>>(
                revenueDepartmentUtils,
                "buildAdjustmentTypeCriterias",
                CreditNoteModel::adjustmentType.name)
        assertEquals(adjustmentCreditNote, result)

        result = callMethod<RevenueDepartmentUtils, Pair<Map<String, Any>, Map<String, String>>>(
                revenueDepartmentUtils,
                "buildAdjustmentTypeCriterias",
                DebitNoteModel::adjustmentType.name)
        assertEquals(adjustmentDebitNote, result)


        result = callMethod<RevenueDepartmentUtils, Pair<Map<String, Any>, Map<String, String>>>(
                revenueDepartmentUtils,
                "buildAdjustmentTypeCriterias",
                EMPTY_STRING)

        assertEquals(emptyCriterias, result)


    }

    @Test
    fun getAdjustmentFieldsInDocumentByService() {

        val revenueDepartmentUtils = spyk<RevenueDepartmentUtils>()

        var result = callMethod<RevenueDepartmentUtils, String>(revenueDepartmentUtils, "getAdjustmentFieldsInDocumentByService", Services.CREDIT_NOTE)

        assertEquals(CreditNoteModel::adjustmentType.name, result)

        result = callMethod<RevenueDepartmentUtils, String>(revenueDepartmentUtils, "getAdjustmentFieldsInDocumentByService", Services.DEBIT_NOTE)

        assertEquals(DebitNoteModel::adjustmentType.name, result)


        result = callMethod<RevenueDepartmentUtils, String>(revenueDepartmentUtils, "getAdjustmentFieldsInDocumentByService", Services.INVOICE)

        assertEquals(EMPTY_STRING, result)

    }

    @Test
    fun assignFieldsName() {

        val revenueDepartmentUtils = spyk<RevenueDepartmentUtils>()

        val result = callMethod<RevenueDepartmentUtils, RdSearchFieldsDto>(revenueDepartmentUtils,
                "assignFieldsName",
                InvoiceModel::invoiceDate.name,
                InvoiceModel::vendorTaxNumber.name,
                InvoiceModel::isETaxInvoice.name,
                InvoiceModel::rdSubmittedDate.name)

        assertEquals(invoiceFields, result)

    }
}