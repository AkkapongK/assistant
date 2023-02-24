package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.beanutils.BeanUtils
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GoodsReceivedModel(
        val accounting: PartyModel? = null,
        val advanceDeductibleAmount: BigDecimal? = null,
        val buyer: PartyModel? = null,
        val companyCode: String? = null,
        val companyBranchCode: String? = null,
        val companyName: String? = null,
        val companyTaxNumber: String? = null,
        val companyAddress1: String? = null,
        val companyAddress2: String? = null,
        val companyBranchName: String? = null,
        val companyCity: String? = null,
        val companyCountry: String? = null,
        val companyDepartment: String? = null,
        val companyDistrict: String? = null,
        val companyOfficerEmail: String? = null,
        val companyOfficerName: String? = null,
        val companyOfficerTelephone: String? = null,
        val companyOrganization: String? = null,
        val companyPostalCode: String? = null,
        val companyEmail: String? = null,
        val companyTelephone: String? = null,
        val createdBy: String? = null,
        val customisedFields: Map<String, Any>? = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val deliveryNoteExternalId: String? = null,
        val documentEntryDate: String? = null,
        val documentEntryYear: String? = null,
        val externalId: String? = null,
        val goodsReceivedItems: List<GoodsReceivedItemModel> = emptyList(),
        val invoiceExternalId: String? = null,
        val initialInvoiceExternalId: String? = null,
        val lastEditedBy: String? = null,
        val lastEditedDate: String? = null,
        val lifecycle: String? = null,
        val linearId: String? = null,
        val paymentDueDate: String? = null,
        val postingDate: String? = null,
        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        val retentionDeductibleAmount: BigDecimal? = null,
        val status: String? = null,
        val seller: PartyModel? = null,
        val vendorBranchCode: String? = null,
        val vendorBranchName: String? = null,
        val vendorName: String? = null,
        val vendorNumber: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorAddress1: String? = null,
        val vendorAddress2: String? = null,
        val vendorCity: String? = null,
        val vendorCountry: String? = null,
        val vendorOrganization: String? = null,
        val vendorDepartment: String? = null,
        val vendorOfficerName: String? = null,
        val vendorOfficerEmail: String? = null,
        val vendorOfficerTelephone: String? = null,
        val vendorDistrict: String? = null,
        val vendorEmail: String? = null,
        val vendorPostalCode: String? = null,
        val vendorTelephone: String? = null,
        val purchaseOrderNumbers: String? = null,
        val fileAttachments: List<FileAttachmentModel>? = listOf(),
        var cashPerfGuaranteeDeductibleAmount: BigDecimal? = null,
        var cashWarrantyDeductibleAmount: BigDecimal? = null,
        var cashPerfDeductibleRemainingAmount: BigDecimal? = null,
        var cashWarrantyDeductibleRemainingAmount: BigDecimal? = null,
        val currency: String? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null,
        val invoiceDate: String? = null,
        val total: BigDecimal? = null,
        val pendingInvoice: Boolean? = null,
        val relatedInvoice: String? = null,
        val movementClass: String? = null,
        val initialGoodsReceivedExternalId: String? = null
) {

    fun display(): GoodsReceivedModel {
        return copy(status = lifecycle)
    }

    fun summarise(fields: List<String>) = fields.map { it to BeanUtils.getProperty(this, it) }.toMap()

    fun updateItem(items: List<GoodsReceivedItemModel>) = copy(goodsReceivedItems = items)

    fun updateSeller(seller: PartyModel) = copy(seller = seller)
}