package th.co.dv.p2p.common.utilities

import net.corda.core.node.services.vault.NullOperator
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
import java.util.*


object RevenueDepartmentUtils {

    /**
     * Method for build criteries params and operation
     * for create sql query
     * @param service : service need to get prefix fields
     * @param vendorTaxId : vendorTaxId
     * @param dateFrom : dateFrom
     * @param dateTo : dateTo
     */
    fun buildCriteriasGetDocumentUpdateRdDate(service: Services, vendorTaxId: String?, dateFrom: Date, dateTo: Date): Pair<Map<String, Any>, Map<String, String>> {

        val parameters = mutableMapOf<String, Any>()
        val operations = mutableMapOf<String, String>()

        val fields = generateFieldsByService(service.name)

        if (vendorTaxId.isNullOrEmpty().not()) {
            parameters[fields.fieldVendorTaxNumber] = vendorTaxId!!
            operations[fields.fieldVendorTaxNumber] = SearchCriteriaOperation.EQUAL.name
        }

        parameters[fields.fieldDate + FROM] = dateFrom
        operations[fields.fieldDate + FROM] = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL.name

        parameters[fields.fieldDate + TO] = dateTo
        operations[fields.fieldDate + TO] = SearchCriteriaOperation.LESSTHAN.name

        if (fields.fieldIsETax.isNullOrBlank().not()){
            parameters[fields.fieldIsETax!!] = false
            operations[fields.fieldIsETax] = SearchCriteriaOperation.EQUAL.name
        }

        parameters[fields.fieldRdSubmittedDate] = NullOperator.IS_NULL.name
        operations[fields.fieldRdSubmittedDate] = SearchCriteriaOperation.ISNULL.name


        val adjustmentTypeField = getAdjustmentFieldsInDocumentByService(service)


        val (adjustmentParams, adjustmentOperations) = buildAdjustmentTypeCriterias(adjustmentTypeField)

        parameters.putAll(adjustmentParams)
        operations.putAll(adjustmentOperations)

        return parameters to operations

    }


    /**
     * Method for build criterias to query adjuestment type is not other
     * @param fieldName : fieldName in document
     */

    private fun buildAdjustmentTypeCriterias(fieldName: String): Pair<Map<String, Any>, Map<String, String>> {

        val parameters = mutableMapOf<String, Any>()
        val operations = mutableMapOf<String, String>()

        if (fieldName.isBlank().not()) {
            parameters[fieldName] = AdjustmentType.OTHER.name
            operations[fieldName] = SearchCriteriaOperation.NOT_EQUAL.name

        }

        return parameters to operations

    }


    /**
     * Method for get adjustment type fields in document by service
     * @param service : service would to check of get fields name in document
     */
    private fun getAdjustmentFieldsInDocumentByService(service: Services): String {
        return when (service) {
            Services.CREDIT_NOTE -> CreditNoteModel::adjustmentType.name
            Services.DEBIT_NOTE -> DebitNoteModel::adjustmentType.name
            else -> EMPTY_STRING
        }

    }

    /**
     * Method for generate fields to build criteries
     * by prefix with service
     * @param service : service name to get prefix
     */
    private fun generateFieldsByService(service: String): RdSearchFieldsDto {
        return when (service) {
            Services.INVOICE.name -> assignFieldsName(InvoiceModel::invoiceDate.name, InvoiceModel::vendorTaxNumber.name, InvoiceModel::isETaxInvoice.name, InvoiceModel::rdSubmittedDate.name)
            Services.CREDIT_NOTE.name -> assignFieldsName(CreditNoteModel::creditNoteDate.name, CreditNoteModel::vendorTaxNumber.name, CreditNoteModel::isETaxCreditNote.name, CreditNoteModel::rdSubmittedDate.name)
            Services.DEBIT_NOTE.name -> assignFieldsName(DebitNoteModel::debitNoteDate.name, DebitNoteModel::vendorTaxNumber.name, DebitNoteModel::isETaxDebitNote.name, DebitNoteModel::rdSubmittedDate.name)
            Services.TAX_DOCUMENT.name -> assignFieldsName(TaxDocumentModel::documentDate.name, TaxDocumentModel::vendorTaxNumber.name, null, TaxDocumentModel::rdSubmittedDate.name)
            else -> throw IllegalAccessException(DOCUMENT_NOT_SUPPORT_RD_SUBMIT_DATE)
        }
    }

    /**
     * Method for mapping fields name to search criteria
     * @param fieldDate : field date form model state
     * @param fieldVendorTaxNumber : field vendor tax number form model state
     * @param fieldIsETax : field is ETax form model state
     * @param fieldRdSubmittedDate : field Rd Submitted Date form model state
     */
    private fun assignFieldsName(fieldDate: String,
                                 fieldVendorTaxNumber: String,
                                 fieldIsETax: String?,
                                 fieldRdSubmittedDate: String): RdSearchFieldsDto {

        return RdSearchFieldsDto(
                fieldDate = fieldDate,
                fieldVendorTaxNumber = fieldVendorTaxNumber,
                fieldIsETax = fieldIsETax,
                fieldRdSubmittedDate = fieldRdSubmittedDate
        )

    }

}