package th.co.dv.p2p.common.models

/**
 * Model for table invoice_custom_rule_validation on configuration service
 */
data class InvoiceCustomRuleValidationModel (
        val buyerTaxId: String? = null,
        val fieldId: Int? = null,
        val errorMessage: String? = null

)
