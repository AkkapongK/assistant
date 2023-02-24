package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.beanutils.BeanUtils
import th.co.dv.p2p.common.utilities.stringify
import th.co.dv.p2p.common.utilities.toZonedDateTime
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestModel(
        val buyer: PartyModel? = null,
        val seller: PartyModel? = null,
        val bank: PartyModel? = null,
        val initiator: String? = null,

        val companyCode: String? = null,
        val companyName: String? = null,
        val companyTaxNumber: String? = null,
        val companyBranchCode: String? = null,
        val companyBranchName: String? = null,
        val companyAddress: String? = null,
        val companyTelephone: String? = null,
        val companyOfficerName: String? = null,
        val companyOfficerPhone: String? = null,
        val companyOfficerEmail: List<String>? = null,

        val vendorNumber: String? = null,
        val vendorBranchCode: String? = null,
        val vendorBranchName: String? = null,
        val vendorName: String? = null,
        val vendorTaxNumber: String? = null,
        val vendorAddress: String? = null,
        val vendorTelephone: String? = null,
        val vendorOfficerName: String? = null,
        val vendorOfficerPhone: String? = null,
        val vendorOfficerEmail: List<String>? = null,
        val vendorSiteId: String? = null,

        val issuedDate: String? = null,
        val externalId: String? = null,
        val subTotal: BigDecimal? = null,
        val vatTotal: BigDecimal? = null,
        val total: BigDecimal? = null,
        val withholdingTaxAmount: BigDecimal? = null,
        val currency: String? = null,
        val withholdingTaxTotal: BigDecimal? = null,

        val createdBy: String? = null,
        val createdDate: String? = null,
        val lastEditedDate: String? = null,
        val lastEditedBy: String? = null,

        val linearId: String? = null,
        val lifecycle: String? = null,
        val status: String? = null,
        val requestItems: List<RequestItemModel> = emptyList(),

        val referenceField1: String? = null,
        val referenceField2: String? = null,
        val referenceField3: String? = null,
        val referenceField4: String? = null,
        val referenceField5: String? = null,
        val customisedFields: Map<String, Any> = emptyMap(),
        val customisedFieldsUpdatedDate: String? = null,

        val type: String? = null,
        val subType: String? = null,
        val typeName: String? = null,
        val subTypeName: String? = null,
        val referenceType: String? = null,
        val referenceNumber: String? = null,
        val referenceLinearId: String? = null,
        val requestReason: String? = null,
        val requestAttachment: List<FileAttachmentModel>? = emptyList(),

        val agreedBy: String? = null,
        val agreedDate: String? = null,
        val agreedRemark: String? = null,
        val agreementFlag: Boolean? = null,

        val documentNumber: String? = null,
        val documentEntryDate: String? = null,
        val documentType: String? = null,
        val documentDate: String? = null,
        val paymentDueDate: String? = null,
        val documentReason: String? = null,
        val documentAttachment: List<FileAttachmentModel>? = emptyList(),

        val clarifiedRemark: String? = null,
        val clarifiedBy: String? = null,
        val clarifiedDate: String? = null,

        val cancelledRemark: String? = null,
        val cancelledBy: String? = null,
        val cancelledDate: String? = null,

        // flag to tell custom api for customise conversion
        @get:JsonProperty("isReadyToConvert")
        val isReadyToConvert: Boolean? = null,
        val parentRequestId: String? = null,
        val parentRequestNumber: String? = null,
        val childRequest: List<RequestModel>? = null
) {

    fun display(): RequestModel {
        val mappedStatus = lifecycle?.let { generateStatus(it) }
        return copy(status = mappedStatus)
    }

    private fun generateStatus(lifecycle: String): String {
        return RequestStatusModel.StatusMapping.valueOf(lifecycle).displayName
    }

    fun updateItem(items: List<RequestItemModel>) = copy(requestItems = items)

    fun summarise(fields: List<String>) = fields.map { it to BeanUtils.getProperty(this, it) }.toMap()

    fun updateSeller(seller: PartyModel) = copy(seller = seller)

    fun readyToConvert() = copy(isReadyToConvert = true)

    fun updateWithholdingTax(withholdingTaxTotal: BigDecimal) = copy(withholdingTaxTotal = withholdingTaxTotal)

    fun toFormatDate() = copy(
            agreedDate = agreedDate?.toZonedDateTime()?.toInstant()?.stringify(),
            cancelledDate = cancelledDate?.toZonedDateTime()?.toInstant()?.stringify(),
            clarifiedDate = clarifiedDate?.toZonedDateTime()?.toInstant()?.stringify(),
            customisedFieldsUpdatedDate = customisedFieldsUpdatedDate?.toZonedDateTime()?.toInstant()?.stringify(),
            documentDate = documentDate?.toZonedDateTime()?.toInstant()?.stringify(),
            issuedDate = issuedDate?.toZonedDateTime()?.toInstant()?.stringify(),
            lastEditedDate = lastEditedDate?.toZonedDateTime()?.toInstant()?.stringify(),
            paymentDueDate = paymentDueDate?.toZonedDateTime()?.toInstant()?.stringify()
    )
}