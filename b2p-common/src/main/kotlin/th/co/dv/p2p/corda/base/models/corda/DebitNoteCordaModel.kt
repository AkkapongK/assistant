package th.co.dv.p2p.corda.base.models.corda

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Amount
import th.co.dv.p2p.corda.base.models.PartyModel
import java.util.*

/**
 * data class that use model in service to corda model
 * @property adjustmentType : adjustment type of debit note
 * @property buyer : buyer
 * @property buyerFinalApprovedDate : buyer approve final date
 * @property buyerFinalApprovedUser : buyer user approve
 * @property cancelledCode : cancelled code
 * @property cancelledDate : cancelled date
 * @property cancelledRemark : cancelled remark
 * @property cancelledUser : cancelled user
 * @property companyAddress : company address
 * @property companyBranchCode : company branch code
 * @property companyBranchName : company branch name
 * @property companyName : company name
 * @property companyTaxNumber : company tax number
 * @property companyTelephone : company telephone
 * @property entryDate : debit note created date
 * @property debitNoteDate : debit note date
 * @property debitNoteItems : debit note items
 * @property dueDate : due date
 * @property externalId : external id
 * @property invoiceExternalId : invoice external id
 * @property invoiceLinearId : invoice linear id
 * @property isETaxDebitNote : isETax debit note
 * @property createdBy :issued by
 * @property createdDate : issued date
 * @property lifecycle : lifecycle
 * @property paymentDate : payment date
 * @property paymentItemLinearId : payment item linearId
 * @property paymentReferenceNumber : payment reference number
 * @property rdSubmittedDate : rd submitted date
 * @property reason : reason
 * @property receiptNumber : receipt number
 * @property seller : seller
 * @property subTotal : sub total
 * @property total : total
 * @property vatTotal : vat total
 * @property vendorAddress : vendor address
 * @property vendorBranchCode : vendor branch code
 * @property vendorBranchName : vendor branch name
 * @property vendorName : vendor name
 * @property vendorTaxNumber : vendor tax number
 * @property vendorTelephone : vendor telephone
 * @property linearId : linear id
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DebitNoteCordaModel(
        val adjustmentType: String? = null,
        val buyer: PartyModel? = null,
        val buyerFinalApprovedDate: String? = null,
        val buyerFinalApprovedRemark: String? = null,
        val buyerFinalApprovedUser: String? = null,
        val cancelledCode: String? = null,
        val cancelledDate: String? = null,
        val cancelledRemark: String? = null,
        val cancelledUser: String? = null,
        val companyAddress: String? = null,
        val companyBranchCode: String? = null,
        val companyBranchName: String? = null,
        val companyName: String? = null,
        val companyTaxNumber: String? = null,
        val companyTelephone: String? = null,
        val entryDate: String? = null,
        val debitNoteDate: String? = null,
        val debitNoteItems: List<DebitNoteItemCordaModel> = emptyList(),
        val dueDate: String? = null,
        val externalId: String? = null,
        val invoiceExternalId: String? = null,
        val invoiceLinearId: String? = null,
        val isETaxDebitNote: Boolean? = null,
        val createdBy: String? = null,
        val createdDate: String? = null,
        val lifecycle: String? = null,
        val paymentDate: String? = null,
        val paymentItemLinearId: String? = null,
        val paymentReferenceNumber: String? = null,
        val rdSubmittedDate: String? = null,
        val reason: String? = null,
        val receiptNumber: String? = null,
        val seller: PartyModel? = null,
        val subTotal: Amount<Currency>? = null,
        val total: Amount<Currency>? = null,
        val vatTotal: Amount<Currency>? = null,
        val vendorAddress: String? = null,
        val vendorBranchCode: String? = null,
        val vendorBranchName: String? = null,
        val vendorName: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorTelephone: String? = null,
        val linearId: String? = null,
        val documentCode: String? = null
)