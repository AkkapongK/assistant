package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.beanutils.BeanUtils
import th.co.dv.p2p.corda.base.domain.Quantity
import java.math.BigDecimal

/**
 * The JSON deserialization data class that represents the input used to create a goods received item state
 * @property initialDocumentEntryYear This is to cater for reverse GR scenario that uses this key to link to the old gr.
 * @property initialGoodsReceivedExternalId This is to cater for reverse GR scenario that uses this key to link to the old gr.
 * @property initialExternalId This is to cater for reverse GR scenario that uses this key to link to the old gr.
 * @property reverseQuantity for store reverse quantity that related which this item (will have value if this data is normal or return gr)
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GoodsReceivedItemModel(
        val	accounting: PartyModel? = null,
        val	accountingNumber: String? = null,
        val	buyer: PartyModel? = null,
        val	companyBranchCode: String? = null,
        val companyBranchName: String? = null,
        val	companyCode: String? = null,
        val companyName: String? = null,
        val	companyTaxNumber: String? = null,
        val currency: String? = null,
        val	createdBy: String? = null,
        val	customisedFields: Map<String, Any> = emptyMap(),
        val	customisedFieldsUpdatedDate: String? = null,
        val	deliveryCompleteFlag: String? = null,
        val	deliveryNoteExternalId: String? = null,
        val	documentEntryDate: String? = null,
        val	documentEntryYear: String? = null,
        val	externalId: String? = null,
        val	externalIdPreInspection: String? = null,
        val	goodsReceivedExternalId: String? = null,
        val	goodsReceivedExternalIdPreInspection: String? = null,
        val goodsReceivedLinearId: String? = null,
        val	goodsReceivedPreInspectionYear: String? = null,
        val	initialDocumentEntryYear: String? = null,
        val	initialExternalId: String? = null,
        val	initialGoodsReceivedExternalId: String? = null,
        val	initialGoodsReceivedItemLinearId: String? = null,
        val	initialInvoiceExternalId: String? = null,
        val	invoiceExternalId: String? = null,
        val	invoiceItemLinearId: String? = null,
        val	lastEditedBy: String? = null,
        val	lastEditedDate: String? = null,
        val	lastTaggedBy: String? = null,
        val	lastTaggedDate: String? = null,
        val	lifecycle: String? = null,
        val	linearId: String? = null,
        val	materialDescription: String? = null,
        val materialGroup: String? = null,
        val	materialNumber: String? = null,
        val	movementClass: String? = null,
        val	movementType: String? = null,
        val	oracle: PartyModel? = null,
        val	site: String? = null,
        val	siteDescription: String? = null,
        val	postingDate: String? = null,
        val	purchaseItemExternalId: String? = null,
        val	purchaseItemLinearId: String? = null,
        val	purchaseOrderExternalId: String? = null,
        val	quantity: Quantity? = null,
        val reverseQuantity: Quantity? = null,
        val	referenceField1: String? = null,
        val	referenceField2: String? = null,
        val	referenceField3: String? = null,
        val	referenceField4: String? = null,
        val	referenceField5: String? = null,
        val	seller: PartyModel? = null,
        val	status: String? = null,
        val	section: String? = null,
        val	sectionDescription: String? = null,
        val	unitDescription: String? = null,
        val unitPrice: BigDecimal? = null,
        val	vendorBranchCode: String? = null,
        val	vendorName: String? = null,
        val	vendorNumber: String? = null,
        val	vendorTaxNumber: String? = null,
        val issuedDate: String? = null,
        val createdDate: String? = null,
        val updatedDate: String? = null,
        val totalAmount: BigDecimal? = null
) {

    fun display(): GoodsReceivedItemModel {
        return copy(status = lifecycle)
    }

    //  This Function take in a List of Strings eg: List < purchaseOrderExternalId >
    //  and returns a map with value eg: List < purchaseOrderExternalId, purchaseOrderExternalId.value >
    fun summarise(fields: List<String>) = fields.map { it to BeanUtils.getProperty(this, it) }.toMap()

    fun updateBranchAndSeller(seller: PartyModel, branch: String) = copy(seller = seller, vendorBranchCode = branch)

    fun updateSeller(seller: PartyModel) = copy(seller = seller)
}