package th.co.dv.p2p.corda.base.models

import net.corda.core.node.services.Vault
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.models.SearchInput

@CordaSerializable
data class PaymentItemSearchModel(
        val linearIds: List<String>? = null,
        val companyCode: String? = null,
        val companyName: String? = null,
        val vendorNumber: String? = null,
        val vendorName: String? = null,
        val issueDateFrom: String? = null,
        val issueDateTo: String? = null,
        val lastUpdatedCommandSimpleName: List<String>? = null,
        val commandUpdatedBy: List<String>? = null,
        val commandUpdatedDateFrom: String? = null,
        val commandUpdatedDateTo: String? = null,
        val returnPayment: Boolean = true,
        val lifecycles: List<String>? = null,
        val pageNumber: Int = 1,
        val pageSize: Int = 500,
        val sortField: String = "externalId",
        val sortOrder: Int = 1,
        val lastGeneratedDateFrom: String? = null,
        val lastGeneratedDateTo: String? = null,
        val payerPaymentDateFrom: String? = null,
        val payerPaymentDateTo: String? = null,
        val exactPayerPaymentDateFrom: String? = null,
        val exactPayerPaymentDateTo: String? = null,
        val paymentItemDateFrom: String? = null,
        val paymentItemDateTo: String? = null,
        val settledDateFrom: String? = null,
        val settledDateTo: String? = null,
        val documentNumbers: String? = null,
        val returnInvoices: Boolean = false,
        val returnCreditNotes: Boolean = false,
        val returnDebitNotes: Boolean = false,
        val externalId: String? = null,
        val companyTaxNumber: String? = null,
        val vendorTaxNumber: String? = null,
        val expectedDate: String? = null,
        val paymentSystems: List<String>? = null,
        val requiredCustomerReference: Boolean? = null,
        val postingStatuses: List<String>? = null,
        val customerReference: String? = null,
        val postingStatusNotIn: List<String>? = null,
        val bankPaymentStatusNotIn: List<String>? = null,
        val statuses: List<String>? = null,
        val bankPaymentStatuses: List<String>? = null,
        val payerPaymentStatuses: List<String>? = null,
        val isPaging: Boolean? = null,
        val checkBuyerWorkingCalendar: Boolean? = null,
        val ignoreZeroPaymentAmount: Boolean? = null,
        val havePaymentSystem: Boolean? = null,
        val companyTaxNumbers: List<String>? = null,
        val vendorTaxNumbers: List<String>? = null,
        val lifecyclesNotIn: List<String>? = null,
        val searchStateStatus: String = Vault.StateStatus.UNCONSUMED.name,
        val returnStateStatus: String = Vault.StateStatus.UNCONSUMED.name,
        val lastPostedDateFrom: String? = null,
        val lastPostedDateTo: String? = null,
        val paymentDescription: String? = null,
        val customerReferenceList: List<String>? = null,
        val shiftPaymentDate: Boolean? = null,
        val selfWhtCert: Boolean? = null,
        val paymentSystem: SearchInput? = null,
        val batchReference:String? = null
)