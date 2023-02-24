package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.common.utilities.copyPropsFrom
import th.co.dv.p2p.corda.base.models.CreditNoteModel
import th.co.dv.p2p.corda.base.models.CreditNoteSearchModel
import th.co.dv.p2p.corda.base.models.PartyModel
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*

data class MockCreditNote(
        override val linearId: String? = UUID.randomUUID().toString(),
        override val buyer: PartyModel? = null,
        override val seller: PartyModel? = null,
        override val accounting: PartyModel? = null,
        override val bank: PartyModel? = null,
        override val externalId: String? = null,
        override val invoiceLinearId: String? = null,
        override val invoiceExternalId: String? = null,
        override val vendorTaxNumber: String? = null,
        override val vendorAddress: String? = null,
        override val vendorNumber: String? = null,
        override val vendorBranchCode: String? = null,
        override val vendorBranchName: String? = null,
        override val vendorName: String? = null,
        override val vendorTelephone: String? = null,
        override val companyTaxNumber: String? = null,
        override val companyAddress: String? = null,
        override val companyBranchCode: String? = null,
        override val companyCode: String? = null,
        override val companyBranchName: String? = null,
        override val companyName: String? = null,
        override val companyTelephone: String? = null,
        override val businessPlace: String? = null,
        override val creditNoteDate: Date? = null,
        override val subTotal: BigDecimal? = null,
        override val vatTotal: BigDecimal? = null,
        override val total: BigDecimal? = null,
        override val totalReceivable: BigDecimal? = null,
        override val unmatchedCode: List<String>? = null,
        override val unmatchedReason: String? = null,
        override val lastMatchUpdatedDate: Date? = null,
        override val reason: String? = null,
        override val documentEntryDate: Date? = null,
        override val documentEntryMethod: String? = null,
        override var creditNoteItems: MutableList<MockCreditNoteItem> = mutableListOf(),
        override val lifecycle: String? = null,
        override val creditPostingUpdatedDate: Date? = null,
        override val customisedFields: Map<String, Any> = emptyMap(),
        override val customisedFieldsUpdatedDate: Date? = null,
        override val adjustmentType: String? = null,
        override val buyerApprovedDate: Date? = null,
        override val buyerApprovedRemark: String? = null,
        override val buyerApprovedUser: String? = null,
        override val buyerRejectedDate: Date? = null,
        override val buyerRejectedRemark: String? = null,
        override val buyerRejectedUser: String? = null,
        override val cancelledDate: Date? = null,
        override val cancelledRemark: String? = null,
        override val cancelledUser: String? = null,
        override val paymentItemLinearId: String? = null,
        override val resubmitCount: Int? = 0,
        override val referenceField1: String? = null,
        override val referenceField2: String? = null,
        override val referenceField3: String? = null,
        override val referenceField4: String? = null,
        override val referenceField5: String? = null,
        override val lastEditedBy: String? = null,
        override val lastEditedDate: Date? = null,
        override val isETaxCreditNote: Boolean? = null,
        override val requestExternalId: String? = null,
        override val requestLinearId: String? = null,
        override val postingStatus: String? = null,
        override val rejectBeforeDOARemark: String? = null,
        override val rejectBeforeDOADate: Date? = null,
        override val rejectBeforeDOABy: String? = null,
        override val paymentReferenceNumber: String? = null,
        override val paymentDate: Date? = null,
        override val currency: String? = null,
        override var status: String? = null,
        override val buyerPostingStatus: String? = null,
        override val buyerPostingDate: Date? = null,
        override val rdSubmittedDate: Date? = null,
        override val issuedBy: String? = null,
        override val correctedAmount: BigDecimal? = null,
        override val correctedUnitPrice: BigDecimal? = null,
        override val reasonCode: String? = null,
        override val vatTriggerPoint: String? = null,
        override val creditNoteSubType: String? = null,
        override val documentDueDate: Date? = null,
        override val taxDocumentNumber: String? = null,
        override val taxDocumentLinearId: String? = null,
        override val paymentDescription: String? = null,
        override val isEwht: Boolean? = null,
        override val vendorSiteId: String? = null,
        override val documentCode: String? = null,
        override val isOnHold: Boolean? = null,
        override val lastHeldBy: String? = null,
        override val lastHeldRemark: String? = null,
        override val lastHeldDate: Date? = null,
        override val lastUnheldBy: String? = null,
        override val lastUnheldRemark: String? = null,
        override val lastUnheldDate: Date? = null,
        override val voidVat: Boolean? = null,
        val createdDate: Timestamp? = null,
        val updatedDate: Timestamp? = null
        ) : InterfaceBaseCreditNote<MockCreditNoteItem> {
    override fun toModel(searchModel: Any): CreditNoteModel {
        searchModel as CreditNoteSearchModel
        return this.toCreditNoteModel(searchModel.returnCreditNoteItems)
    }
}


fun MockCreditNote.toCreditNoteModel(returnItems: Boolean = true): CreditNoteModel {
    var creditNoteModel = CreditNoteModel()
    creditNoteModel.copyPropsFrom(this)
    if (returnItems) {
        val creditNoteItemsModel = this.creditNoteItems.map { it.toCreditNoteItemModel() }
        creditNoteModel = creditNoteModel.copy(
                creditNoteItems = creditNoteItemsModel
        )
    }
    return creditNoteModel
}