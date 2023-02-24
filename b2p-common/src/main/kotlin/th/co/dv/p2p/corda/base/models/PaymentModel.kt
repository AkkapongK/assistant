package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

/**
 * The payment model
 *
 * @property linearId Unique Identifier of the payment
 * @property companyCode Company Code of Buyer
 * @property companyName Company Name of Buyer
 * @property companyTaxNumber Company Tax Number of Buyer
 * @property buyerBankCode Buyer Bank code
 * @property buyerBankAccountNumber Buyer bank account number
 * @property vendorNumber Company Code of Seller
 * @property vendorName Company Name of Seller
 * @property sellerBankCode Seller Bank code
 * @property sellerBankBranchCode Seller Bank branch code
 * @property sellerBankAccountNumber Seller Bank Account Number
 * @property sellerBankAccountName Seller Bank Account Name
 * @property sellerBankName Seller Preferred Bank
 * @property paymentAmount the Amount to be paid to seller
 * @property payerPaymentStatus Payment Status for buyer (Ex. Posting, Clearing Status)
 * @property payerPaymentMessage Message from buyer payment status (Ex. Posting message)
 * @property payerPaymentDate Date that received buyer payment status
 * @property payeePaymentStatus Payment Status for seller (Ex. Pending)
 * @property payeePaymentMessage Message from seller payment status
 * @property payeePaymentDate Date that received seller payment status
 * @property bankPaymentStatus Payment Status for bank (Ex. GENERATED, PAID)
 * @property bankPaymentMessage Message from bank payment status (Ex. Settled message)
 * @property bankPaymentDate Date that received bank payment status
 * @property paymentDate Expected payment due date
 * @property feeChargeTo Specific person that buyer would like to charge the fee for process the payment
 * @property paymentSystem Payment System to be used for this payment (Calculate from external service)
 * @property settledDate Date that seller received money
 * @property lifecycle main lifecycle of Payment
 * @property paymentNumber Running number of Payment (Can be equal to customer reference for some company)
 * @property paymentFee Fee charged from the payment (Calculate from external service)
 * @property batchReference File reference that come from payment batch processing
 * @property withholdingTax Withholding Tax information for this payment
 * @property customerReference the customer reference in credit line format it's generate from running or external system for some company
 *                             BCN + Blockchain generated unique ID for each payment with format YYYYNNNNNNNN (Y = Year, N = Running number).
 *                             For example, BCN20180000000001.
 * @property notificationMethod Flag used to specify which way to notify seller when payment is done
 * @property documentItems document item that use keep minimal information of (invoice, credit note, debit note)
 * @property sponsor The sponsor name that refer to Buyer company
 * @property payerPaymentDocFiscalYear Payer payment doc fiscal year
 * @property payerPaymentDocNumber Payer payment doc number
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentModel(

        var linearId: String? = null,
        var companyCode: String? = null,
        var companyName: String? = null,
        var companyTaxNumber: String? = null,
        var buyerBankCode: String? = null,
        var buyerBankAccountNumber: String? = null,
        var vendorNumber: String? = null,
        var vendorName: String? = null,
        var vendorTaxNumber: String? = null,
        var sellerBankCode: String? = null,
        var sellerBankBranchCode: String? = null,
        var sellerBankAccountNumber: String? = null,
        var sellerBankAccountName: String? = null,
        var sellerBankName: String? = null,
        var paymentAmount: BigDecimal? = null,
        var currency: String? = null,
        var payerPaymentStatus: String? = null,
        var payerPaymentMessage: String? = null,
        var payerPaymentDate: String? = null,
        var payeePaymentStatus: String? = null,
        var payeePaymentMessage: String? = null,
        var payeePaymentDate: String? = null,
        var bankPaymentStatus: String? = null,
        var bankPaymentMessage: String? = null,
        var bankPaymentDate: String? = null,
        var paymentDate: String? = null,
        var feeChargeTo: String? = null,
        var paymentSystem: String? = null,
        var settledDate: String? = null,
        var lifecycle: String? = null,
        var paymentNumber: String? = null,
        var paymentFee: BigDecimal? = null,
        var batchReference: String? = null,
        var withholdingTax: WithholdingTaxModel? = null,
        var customerReference: String? = null,
        var notificationMethod: String? = null,
        var documentItems: List<DocumentItemModel>? = null,
        var calendarKey: String? = null,
        var createdDate: String? = null,
        var updatedDate: String? = null,
        var payerPaymentDocFiscalYear: String? = null,
        var payerPaymentDocNumber: String? = null,
        val shiftPaymentDate: Boolean? = null,
        @get:JsonProperty("isEwht")
        var isEwht: Boolean? = null,
        var status: String? = null,
        val paymentDescription: String? = null,
        val vendorSiteId: String? = null,
        // Use for Bank custom
        var filler: String? = null,
        var billerCode: String? = null,
        var vendorEmail: String? = null,
        var vendorTelephone: String? = null,
        var vendorAddress: String? = null,
        var sponsor: String? = null,
        val selfWhtCert: Boolean? = null
)