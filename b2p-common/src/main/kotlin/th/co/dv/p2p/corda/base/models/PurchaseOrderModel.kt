package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.beanutils.BeanUtils
import java.math.BigDecimal

/**
 * @property initialTotal this is the initial total amount of PO which is initial quantity times unit price
 * @property total this is the sum of initial total include TAX of each tax rate
 * @property remainingTotal this is the remaining total amount of PO which is remaining quantity times unit price
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PurchaseOrderModel(
        val accounting: PartyModel? = null,
        val businessPlaceAddress1: String? = null,
        val businessPlaceAddress2: String? = null,
        val businessPlaceCity: String? = null,
        val businessPlaceCountry: String? = null,
        val businessPlaceDepartment: String? = null,
        val businessPlaceDistrict: String? = null,
        val businessPlaceEmail: String? = null,
        val businessPlaceOfficerEmail: String? = null,
        val businessPlaceOfficerName: String? = null,
        val businessPlaceOfficerTelephone: String? = null,
        val businessPlaceOrganization: String? = null,
        val businessPlacePostalCode: String? = null,
        val businessPlaceTaxNumber: String? = null,
        val businessPlaceTelephone: String? = null,
        val buyer: PartyModel? = null,
        val companyBranchCode: String? = null,
        val companyBranchName: String? = null,
        val companyCode: String? = null,
        val companyName: String? = null,
        val currency: String?  = null,
        val customisedFields: Map<String, Any>? = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val documentEntryDate: String? = null,
        val deleteFlag: String? = null,
        val issuedDate: String? = null,
        val lastConfirmedBy: String? = null,
        val lastConfirmedDate: String? = null,
        val lastPartyConfirmedBy: PartyModel? = null,
        val lastPartyUpdatedBy: PartyModel? = null,
        val lastRejectedBy: String? = null,
        val lastRejectedDate: String? = null,
        val lastRejectedReason: String? = null,
        val lastRejectedRemark: String? = null,
        val lastUpdatedBy: String? = null,
        val lastUpdatedDate: String? = null,
        val lifecycle: String? = null,
        val linearId: String? = null,
        val purchaseOrderNumber: String? = null,
        val paymentTermCode: String? = null,
        val paymentTermDays: Long? = null,
        val paymentTermMonths: Int? = null,
        val paymentTermDescription: String? = null,
        val purchaseItems: List<PurchaseItemModel> = emptyList(),
        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        val seller: PartyModel? = null,
        val status: String? = null,
        val vendorAddress1: String? = null,
        val vendorAddress2: String? = null,
        val vendorBranchCode: String? = null,
        val vendorBranchName: String? = null,
        val vendorCity: String? = null,
        val vendorCountry: String? = null,
        val vendorDepartment: String? = null,
        val vendorDistrict: String? = null,
        val vendorEmail: String? = null,
        val vendorName: String? = null,
        val vendorNumber: String? = null,
        val vendorOfficerEmail: String? = null,
        val vendorOfficerName: String? = null,
        val vendorOfficerTelephone: String? = null,
        val vendorOrganization: String? = null,
        val vendorPostalCode: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorTelephone: String? = null,
        val invoiceList: String? = null,
        val fileAttachments: List<FileAttachmentModel> = listOf(),
        val initialTotal: BigDecimal? = null,
        val total: BigDecimal? = null,
        val remainingTotal: BigDecimal? = null,
        val initialOverDeliveryAmount: BigDecimal? = null,
        val remainingOverDeliveryAmount: BigDecimal? = null,
        val withholdingTaxFormType: String? = null,
        val withholdingTaxPayType: String? = null,
        val withholdingTaxRemark: String? = null,
        val retentionPercent: BigDecimal? = null,
        val retentionAmount: BigDecimal? = null,
        val retentionRemainingAmount: BigDecimal? = null,
        val cashPerfGuaranteeRate: BigDecimal? = null,
        val cashPerfGuaranteePercent: BigDecimal? = null,
        val cashPerfGuaranteeAmount: BigDecimal? = null,
        val cashPerfGuaranteeRemainingAmount: BigDecimal? = null,
        val cashWarrantyRate: BigDecimal? = null,
        val cashWarrantyPercent: BigDecimal? = null,
        val cashWarrantyAmount: BigDecimal? = null,
        val cashWarrantyRemainingAmount: BigDecimal? = null,
        val retentionTermDays: Int? = null,
        val advancePaymentPercent: BigDecimal? = null,
        val advancePaymentAmount: BigDecimal? = null,
        val calendarKey: String? = null,
        val contractNumber: String? = null,
        val contractLinearId: String? = null,
        val requiredAcknowledgement: Boolean? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null,
        val vendorSiteId: String? = null
) {
    fun display(): PurchaseOrderModel {
        return copy(status = lifecycle?.let { generateStatus(it) })
    }
    //  This Function take in a List of Strings eg: List < externalId >
    //  and returns a map with value eg: List < externalId, externalId.value >
    fun summarise(fields: List<String>) = fields.map { it to BeanUtils.getProperty(this, it) }.toMap()

    fun updateSeller(seller: PartyModel) = copy(seller = seller)

    fun updateInvoiceList(invoiceList: String?) = copy(invoiceList = invoiceList)

    private fun generateStatus(lifecycle: String): String {
        return PurchaseStatus.PurchaseOrder().valueOf(lifecycle).displayName
    }
}
