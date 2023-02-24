package th.co.dv.p2p.common.utilities.manager

import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.PartyModel
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*

interface InterfaceBaseCreditNoteItem {
    val accountCode: String?
    val accountCodeDescription: String?
    val accounting: PartyModel?
    val bank: PartyModel?
    val buyer: PartyModel?
    val createdDate: Timestamp?
    val creditNoteLinearId: String?
    val currency: String?
    val customisedFields: Map<String, Any>?
    val customisedFieldsUpdatedDate: Date?
    val externalId: String?
    val invoiceItemExternalId: String?
    val invoiceItemLinearId: String?
    val lastMatchUpdatedDate: Date?
    val lifecycle: String?
    val linearId: String?
    val materialDescription: String?
    val materialGroup: String?
    val materialNumber: String?
    val purchaseItemLinearId: String?
    val quantity: Quantity?
    val referenceField1: String?
    val referenceField2: String?
    val referenceField3: String?
    val referenceField4: String?
    val referenceField5: String?
    val requestItemExternalId: String?
    val requestItemLinearId: String?
    val section: String?
    val sectionDescription: String?
    val seller: PartyModel?
    val site: String?
    val siteDescription: String?
    var status: String?
    val subTotal: BigDecimal?
    val taxRate: BigDecimal?
    val unitDescription: String?
    val unitPrice: BigDecimal?
    val unmatchedCode: List<String>?
    val unmatchedReason: String?
    val updatedDate: Timestamp?
    val vatCode: String?
    val vatTotal: BigDecimal?
    val withholdingTaxCode: String?
    val withholdingTaxFormType: String?
    val withholdingTaxIncomeDescription: String?
    val withholdingTaxIncomeType: String?
    val withholdingTaxPayType: String?
    val withholdingTaxRate: BigDecimal?
    val withholdingTaxRemark: String?
    val withholdingTaxBaseAmount: BigDecimal?
    val correctedAmount: BigDecimal?
    val correctedUnitPrice: BigDecimal?
    val contractNumber: String?
}