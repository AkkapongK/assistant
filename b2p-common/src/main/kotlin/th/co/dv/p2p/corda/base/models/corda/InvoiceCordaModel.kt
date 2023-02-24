package th.co.dv.p2p.corda.base.models.corda

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Amount
import th.co.dv.p2p.corda.base.models.PartyModel
import java.util.*

/**
 * data class use for convert invoice model to corda model
 *
 * @property vendorBranchCode the seller's branch code in the external world
 * @property vendorName the seller's common name in the external world
 * @property vendorTaxNumber the seller's Tax Number in the external world
 * @property vendorAddress the seller's address in the external world
 * @property companyName the buyer's company name in the external world
 * @property companyBranchCode the buyer's company branch code in the external world
 * @property companyBranchName the buyer's company branch name in the external world
 * @property companyTaxNumber the buyer's company tax number in the external world
 * @property companyAddress the buyer's company address in the external world
 * @property externalId this state's line item number/id in the external world
 * @property invoiceDate the invoice date in the external world
 * @property entryDate the invoice date created in the system
 * @property invoiceFinancing the invoice financing flag , 'Y' means yes for invoice Financing
 * @property cancelledCode the invoice document cause  description [E-Tax]
 * @property dueDate the latest due date of the invoice either from system generated or user manually input
 * @property subTotal the sub total of the invoice without vat total, after including discount and surcharge
 * @property vatTotal the vat total of the invoice based on tax code
 * @property total the total of the invoice after include vat total
 * @property buyer the legal identity node who represents the buyer
 * @property seller the legal identity node who represents the seller
 * @property lifecycle the stages of evolution which this state will go through
 * @property cancelledUser the user of the SE team that cancelled invoice
 * @property cancelledDate the date of the SE team that cancelled invoice
 * @property cancelledRemark the remark by the SE team that cancelled invoice
 * @property invoiceFinancedDate the invoice financing date entered.
 * @property paymentDate the payment date of the invoice during settle payment in invoice
 * @property paymentItemLinearId the linear id of payment item that is auto populated when payment item is issued during GeneratePayment.Initiator
 * @property paymentReferenceNumber the reference code from bank's external payment system after paid the invoice to seller
 * @property isETaxInvoice the flag is true if is e-tax
 * @property receiptNumber the receipt number that payment has been made to finalize a sale
 * @property newInvoiceLinearId linear id of new Invoice that created from this invoice
 * @property originalInvoiceLinearId linear id of cancelled Invoice that used to create this invoice
 * @property originalInvoiceExternalId document number of cancelled Invoice that used to create this invoice
 * @property originalInvoiceDate document date of cancelled Invoice that used to create this invoice
 * @property originalInvoiceCancellationReason reason for cancelling the Invoice that used to create this invoice
 * @property originalInvoiceCancellationDescription description for cancelling the Invoice that used to create this invoice
 * @property createdBy The username that created invoice document
 * @property rdSubmittedDate the RD submitted date of the invoice
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InvoiceCordaModel(
    val buyerFinalApprovedDate: String? = null,
    val buyerFinalApprovedRemark: String? = null,
    val buyerFinalApprovedUser: String? = null,
    val vendorBranchCode: String? = null,
    val vendorBranchName: String? = null,
    val vendorName: String? = null,
    val vendorTaxNumber: String? = null,
    val vendorAddress: String? = null,
    val vendorTelephone: String? = null,
    val companyName: String? = null,
    val companyBranchCode: String? = null,
    val companyBranchName: String? = null,
    val companyTaxNumber: String? = null,
    val companyAddress: String? = null,
    val companyTelephone: String? = null,
    val externalId: String? = null,
    val invoiceDate: String? = null,
    val entryDate: String? = null,
    val invoiceFinancing: Char? = null,
    val cancelledCode: String? = null,
    val dueDate: String? = null,
    val subTotal: Amount<Currency>? = null,
    val vatTotal: Amount<Currency>? = null,
    val total: Amount<Currency>? = null,
    val buyer: PartyModel? = null,
    val seller: PartyModel? = null,
    val lifecycle: String? = null,
    val cancelledUser: String? = null,
    val cancelledDate: String? = null,
    val cancelledRemark: String? = null,
    val invoiceFinancedDate: String? = null,
    val paymentDate: String? = null,
    val paymentItemLinearId: String? = null,
    val paymentReferenceNumber: String? = null,
    val invoiceItems: List<InvoiceItemCordaModel> = emptyList(),
    val isETaxInvoice: Boolean? = null,
    val receiptNumber: String? = null,
    val newInvoiceLinearId: String? = null,
    val originalInvoiceExternalId: String? = null,
    val originalInvoiceLinearId: String? = null,
    val originalInvoiceDate: String? = null,
    val originalInvoiceCancellationReason: String? = null,
    val originalInvoiceCancellationDescription: String? = null,
    val createdBy: String? = null,
    val createdDate: String? = null,
    val rdSubmittedDate: String? = null,
    val linearId: String? = null,
    val documentCode: String? = null
)
