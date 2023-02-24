package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.common.utilities.copyPropsFrom
import th.co.dv.p2p.corda.base.models.PartyModel
import th.co.dv.p2p.corda.base.models.PurchaseOrderModel
import th.co.dv.p2p.corda.base.models.PurchaseSearchModel
import java.math.BigDecimal
import java.util.*

/**
 * This entity is mock implementation for InterfaceBasePurchaseOrder
 */
data class MockPurchaseOrder(
        override val accounting: PartyModel? = null,
        override val businessPlaceAddress1: String? = null,
        override val businessPlaceAddress2: String? = null,
        override val businessPlaceCity: String? = null,
        override val businessPlaceCountry: String? = null,
        override val businessPlaceDepartment: String? = null,
        override val businessPlaceDistrict: String? = null,
        override val businessPlaceEmail: String? = null,
        override val businessPlaceOfficerEmail: String? = null,
        override val businessPlaceOfficerName: String? = null,
        override val businessPlaceOfficerTelephone: String? = null,
        override val businessPlaceOrganization: String? = null,
        override val businessPlacePostalCode: String? = null,
        override val businessPlaceTaxNumber: String? = null,
        override val businessPlaceTelephone: String? = null,
        override val buyer: PartyModel? = null,
        override val companyBranchCode: String? = null,
        override val companyBranchName: String? = null,
        override val companyCode: String? = null,
        override val companyName: String? = null,
        override val currency: String? = null,
        override val customisedFields: Map<String, Any>? = emptyMap(),
        override val customisedFieldsUpdatedDate: Date? = null,
        override val documentEntryDate: Date? = null,
        override val deleteFlag: String? = null,
        override val issuedDate: Date? = null,
        override val lastConfirmedBy: String? = null,
        override val lastConfirmedDate: Date? = null,
        override val lastPartyConfirmedBy: PartyModel? = null,
        override val lastPartyUpdatedBy: PartyModel? = null,
        override val lastRejectedBy: String? = null,
        override val lastRejectedDate: Date? = null,
        override val lastRejectedReason: String? = null,
        override val lastRejectedRemark: String? = null,
        override val lastUpdatedBy: String? = null,
        override val lastUpdatedDate: Date? = null,
        override val lifecycle: String? = null,
        override val linearId: String? = UUID.randomUUID().toString(),
        override val purchaseOrderNumber: String? = null,
        override val paymentTermCode: String? = null,
        override val paymentTermDays: Long? = null,
        override val paymentTermMonths: Int? = null,
        override val paymentTermDescription: String? = null,
        override var purchaseItems: MutableList<MockPurchaseItem> = mutableListOf(),
        override val referenceField1: String? = null,
        override val referenceField2: String? = null,
        override val referenceField3: String? = null,
        override val referenceField4: String? = null,
        override val referenceField5: String? = null,
        override val seller: PartyModel? = null,
        override var status: String? = null,
        override val vendorAddress1: String? = null,
        override val vendorAddress2: String? = null,
        override val vendorBranchCode: String? = null,
        override val vendorBranchName: String? = null,
        override val vendorCity: String? = null,
        override val vendorCountry: String? = null,
        override val vendorDepartment: String? = null,
        override val vendorDistrict: String? = null,
        override val vendorEmail: String? = null,
        override val vendorName: String? = null,
        override val vendorNumber: String? = null,
        override val vendorOfficerEmail: String? = null,
        override val vendorOfficerName: String? = null,
        override val vendorOfficerTelephone: String? = null,
        override val vendorOrganization: String? = null,
        override val vendorPostalCode: String? = null,
        override val vendorTaxNumber: String? = null,
        override val vendorTelephone: String? = null,
        override val invoiceList: String? = null,
        override val initialTotal: BigDecimal? = null,
        override val remainingTotal: BigDecimal? = null,
        override val initialOverDeliveryAmount: BigDecimal? = null,
        override val remainingOverDeliveryAmount: BigDecimal? = null,
        override val withholdingTaxFormType: String? = null,
        override val withholdingTaxPayType: String? = null,
        override val withholdingTaxRemark: String? = null,
        override val retentionPercent: BigDecimal? = null,
        override val retentionAmount: BigDecimal? = null,
        override val retentionTermDays: Int? = null,
        override val advancePaymentPercent: BigDecimal? = null,
        override val advancePaymentAmount: BigDecimal? = null,
        override val calendarKey: String? = null,
        override val contractNumber: String? = null,
        override val createdDate: Date? = null,
        override val updatedDate: Date? = null,
        override val retentionRemainingAmount: BigDecimal? = null,
        override val cashPerfGuaranteeRate: BigDecimal? = null,
        override val cashPerfGuaranteeAmount: BigDecimal? = null,
        override val cashPerfGuaranteeRemainingAmount: BigDecimal? = null,
        override val cashWarrantyRate: BigDecimal? = null,
        override val cashWarrantyAmount: BigDecimal? = null,
        override val cashWarrantyRemainingAmount: BigDecimal? = null,
        override val vendorSiteId: String? = null,
        override val contractLinearId: String? = null
): InterfaceBasePurchaseOrder<MockPurchaseItem> {
    override fun toModel(searchModel: Any): PurchaseOrderModel {
        searchModel as PurchaseSearchModel
        return this.toPurchaseOrderModel(searchModel.returnPurchaseItems)
    }
}

/**
 * Method for convert entity to model
 */
fun MockPurchaseOrder.toPurchaseOrderModel(returnItems: Boolean = true): PurchaseOrderModel {
    val purchaseOrderModel = PurchaseOrderModel()
    purchaseOrderModel.copyPropsFrom(this)

    if (returnItems.not()) return purchaseOrderModel

    // convert invoice item
    val purchaseItemModels = this.purchaseItems.map { it.toPurchaseItemModel() }
    return purchaseOrderModel.copy(
            purchaseItems = purchaseItemModels
    )
}
