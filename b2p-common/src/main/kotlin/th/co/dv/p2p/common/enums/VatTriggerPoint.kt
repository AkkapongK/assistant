package th.co.dv.p2p.common.enums

/**
 * Enum of VatTriggerPoint
 * VatTriggerPoint is method to calculate vat there will be 3 possible values None,Invoice,Payment
 * since this three approaches may cause the different amount
 */
enum class VatTriggerPoint {
    None,
    Invoice,
    Payment
}