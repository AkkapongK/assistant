package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.beanutils.BeanUtils
import th.co.dv.p2p.corda.base.domain.DeliveryStatus
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PurchaseItemModel(
        val accountCode: String? = null,
        val accountCodeDescription: String? = null,
        val poNumber: String? = null,
        val accountAssignment: String? = null,
        val buyer: PartyModel? = null,
        val businessPlace: String? = null,
        val businessPlaceTaxNumber: String? = null,
        val businessPlaceOrganization: String? = null, // new
        val businessPlaceDescription: String? = null,
        val businessPlaceName1: String? = null,
        val businessPlaceName2: String? = null,
        val businessPlaceName3: String? = null,
        val businessPlaceName4: String? = null,
        val businessPlaceStreet1: String? = null,
        val businessPlaceStreet2: String? = null,
        val businessPlaceStreet3: String? = null,
        val businessPlaceStreet4: String? = null,
        val businessPlaceStreet5: String? = null,
        val businessPlaceCity: String? = null,
        val businessPlaceDistrict: String? = null,
        val businessPlacePostalCode: String? = null,
        val businessPlacePostalTelephone: String? = null,
        val businessPlaceCountry: String? = null, // new
        val businessPlaceDepartment: String? = null, // new
        val businessPlaceEmail: String? = null, // new
        val businessPlaceOfficerName: String? = null, // new
        val businessPlaceOfficerEmail: String? = null, // new
        val businessPlaceOfficerTelephone: String? = null, // new
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,
        val companyCode: String? = null,
        val companyBranchCode: String? = null,
        val companyName: String? = null,
        val calendarKey: String? = null,
        val costCenter: String? = null, // new
        val costCenterDescription: String? = null, // new
        val costType: String? = null, // new
        val costTypeDescription: String? = null, // new
        val documentEntryDate: String? = null, // new
        val deliveryStatus: String? = null,
        val deleteFlag: String? = null,
        val externalId: String? = null,
        val expectedDeliveryDate: String? = null,
        val estimatedUnitPrice: String? = null,
        val freeItemIndicator: String? = null,
        val issuedDate: String? = null,
        val itemCategory: String? = null,
        val invoiceList: String? = null,
        val lastConfirmedBy: String? = null,
        val lastConfirmedDate: String? = null,
        val lastPartyConfirmedBy: PartyModel? = null,
        val lastPartyUpdatedBy: PartyModel? = null,
        val lastUpdatedBy: String? = null,
        val lastUpdatedDate: String? = null,
        val lifecycle: String? = null,
        val linearId: String? = null,
        val materialDescription: String? = null,
        val materialNumber: String? = null,
        val materialGroup: String? = null,
        val overDeliveryTolerance: BigDecimal? = null,
        val overDeliveryQuantity: Quantity? = null,
        val paymentTermDescription: String? = null,
        val purchasingOrg: String? = null,
        val purchasingGroupName: String? = null,
        val purchasingGroupTel: String? = null,
        val purchasingEmail: String? = null,
        val purchaseOrderLinearId: String? = null,
        val plantDescription: String? = null,
        val paymentTermCode: String? = null,
        val paymentTermDays: Long? = null,
        val purchaseRequestNumber: String? = null,
        val purchaseRequestItem: String? = null,
        val purchasingGroup: String? = null,
        val poItemNo: String? = null,
        val poItemUnitPrice: BigDecimal? = null,
        val poItemUnitPriceCurrency: String? = null,
        val quantity: Quantity? = null,
        val poItemDeliveryDate: String? = null,
        val proposedRevisedDeliveryDate: String? = null,
        val initialDeliveryDate: String? = null,
        val referenceField1: String? = null, // new
        val referenceField2: String? = null, // new
        val referenceField3: String? = null, // new
        val referenceField4: String? = null, // new
        val referenceField5: String? = null, // new
        val revisedDeliveryDate: String? = null,
        val revisedReason: String? = null,
        val section: String? = null, // new
        val sectionDescription: String? = null, // new
        val status: String? = null,
        val site: String? = null,
        val siteDescription: String? = null,
        val seller: PartyModel? = null,
        val taxCode: String? = null,
        val taxRate: BigDecimal? = null,
        val timeZone: String? = null,
        val unitDescription: String? = null,
        val underDeliveryTolerance: BigDecimal? = null,
        val vatTriggerPoint: String? = null,
        val vendorNumber: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorEmail: String? = null,
        val vendorBranchCode: String? = null,
        val vendorBranchName: String? = null,
        val vendorCountry: String? = null, // new
        val vendorDepartment: String? = null, // new
        val vendorName: String? = null,
        val vendorName2: String? = null,
        val vendorName3: String? = null,
        val vendorName4: String? = null,
        val vendorStreet1: String? = null,
        val vendorStreet2: String? = null,
        val vendorStreet3: String? = null,
        val vendorStreet4: String? = null,
        val vendorStreet5: String? = null,
        val vendorCity: String? = null,
        val vendorDistrict: String? = null,
        val vendorOrganization: String? = null, // new
        val vendorPostalCode: String? = null,
        val vendorTelephone: String? = null,
        val vendorOfficerName: String? = null, // new
        val vendorOfficerEmail: String? = null, // new
        val vendorOfficerTelephone: String? = null, // new
        val withholdingTaxCalculationPoint: String? = null,
        val withholdingTaxIncomeType: String? = null,
        val withholdingTaxIncomeDescription: String? = null,
        val withholdingTaxPercent: BigDecimal? = null,
        val withholdingTaxCode: String? = null,
        val withholdingTaxDescription: String? = null,
        val taxDescription: String? = null,
        val requesterEmail: String? = null,
        val requesterName: String? = null,
        val contractNumber: String? = null,
        val amount: BigDecimal? = null,
        val remainingAmount: BigDecimal? = null,
        val consumedAmount: BigDecimal? = null,
        val effectiveDate: String? = null,
        val advancePaymentRemainingAmount: BigDecimal? = null,
        val advancePaymentToBeDeducted: BigDecimal? = null,
        val advanceInitialAmount:BigDecimal? = null,
        val paymentDueDate: String? = null
)
{

    fun display(): PurchaseItemModel {
        return copy(
                status = lifecycle?.let { generateStatus(it) },
                deliveryStatus = DeliveryStatus.fromCode(deliveryStatus).singleOrNull()?.description)
    }

    //  This Function take in a List of Strings eg: List < poNumber >
    //  and returns a map with value eg: List < poNumber, poNumber.value >
    fun summarise(fields: List<String>) = fields.map { it to BeanUtils.getProperty(this, it) }.toMap()

    // fun updateBranchAndSeller(seller: PartyModel, branch: String) = copy(seller = seller, vendorBranchCode = branch)

    fun updateSeller(seller: PartyModel) = copy(seller = seller)

    fun updatePaymentTerm(code: String, day: Long) = copy(paymentTermCode = code, paymentTermDays = day)

    fun updateTaxCode(taxCode: String, percentage: Int) = copy(taxCode = taxCode, taxRate = BigDecimal(percentage))

    fun updateCompanyName(companyName: String?) = copy(companyName = companyName)

    fun updateTaxDescription(taxDescription: String?) = copy(taxDescription = taxDescription)

    fun updateWithholdingTaxDescription(withholdingTaxDescription: String?) = copy(withholdingTaxDescription = withholdingTaxDescription)

    fun updateInvoiceList(invoiceList: String?) = copy(invoiceList = invoiceList)

    private fun generateStatus(lifecycle: String): String {
        return PurchaseStatus.PurchaseOrderItem().valueOf(lifecycle).displayName
    }
}
