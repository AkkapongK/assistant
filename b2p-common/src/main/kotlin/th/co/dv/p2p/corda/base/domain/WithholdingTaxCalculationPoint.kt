package th.co.dv.p2p.corda.base.domain


/**
 * Method to calculate withholding tax amount either per invoice or per payment
 * since this two approaches may cause the different amount
 *
 * If WithholdingTaxCalculationPoint is
 *  - Invoice we calculate withholding tax per each document (invoice/debit note)
 *  - Payment we calculate withholding tax per payment item
 */
enum class WithholdingTaxCalculationPoint {
    Invoice,
    Payment
}