package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.common.utilities.copyPropsFrom
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.CreditNoteItemModel
import th.co.dv.p2p.corda.base.models.PartyModel
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*

data class MockCreditNoteItem(
        override val linearId: String? = UUID.randomUUID().toString(),
        override val buyer: PartyModel? = null,
        override val seller: PartyModel? = null,
        override val accounting: PartyModel? = null,
        override val bank: PartyModel? = null,
        override val creditNoteLinearId: String? = null,
        override val externalId: String? = null,
        override val purchaseItemLinearId: String? = null,
        override val invoiceItemLinearId: String? = null,
        override val invoiceItemExternalId: String? = null,
        override val materialNumber: String? = null,
        override val materialDescription: String? = null,
        override val materialGroup: String? = null,
        override val quantity: Quantity? = null,
        override val unitDescription: String? = null,
        override val unitPrice: BigDecimal? = null,
        override val subTotal: BigDecimal? = null,
        override val taxRate: BigDecimal? = null,
        override val vatTotal: BigDecimal? = null,
        override val vatCode: String? = null,
        override val withholdingTaxRate: BigDecimal? = null,
        override val withholdingTaxFormType: String? = null,
        override val withholdingTaxPayType: String? = null,
        override val withholdingTaxRemark: String? = null,
        override val withholdingTaxIncomeType: String? = null,
        override val withholdingTaxIncomeDescription: String? = null,
        override val withholdingTaxBaseAmount: BigDecimal? = null,
        override val withholdingTaxCode: String? = null,
        override val unmatchedCode: List<String>? = null,
        override val unmatchedReason: String? = null,
        override val lastMatchUpdatedDate: Date? = null,
        override val site: String? = null,
        override val siteDescription: String? = null,
        override val section: String? = null,
        override val sectionDescription: String? = null,
        override val referenceField1: String? = null,
        override val referenceField2: String? = null,
        override val referenceField3: String? = null,
        override val referenceField4: String? = null,
        override val referenceField5: String? = null,
        override val lifecycle: String? = null,
        override val customisedFields: Map<String, Any> = emptyMap(),
        override val customisedFieldsUpdatedDate: Date? = null,
        override val requestItemExternalId: String? = null,
        override val requestItemLinearId: String? = null,
        override val accountCode: String? = null,
        override val accountCodeDescription: String? = null,
        override var status: String? = null,
        override val currency: String? = null,
        override val correctedAmount: BigDecimal? = null,
        override val correctedUnitPrice: BigDecimal? = null,
        override val contractNumber: String? = null,
        override val createdDate: Timestamp? = null,
        override val updatedDate: Timestamp? = null
) : InterfaceBaseCreditNoteItem


/**
 * Method for convert credit note item model to entity
 */
fun MockCreditNoteItem.toCreditNoteItemModel(): CreditNoteItemModel {
    val creditNoteItemModel = CreditNoteItemModel()
    creditNoteItemModel.copyPropsFrom(this)
    return creditNoteItemModel
}