package th.co.dv.p2p.common.enums

/**
 * Enum class for key type of bank code in [`masterdata_service`], table [`financing_configuration`], [`buyer_vendor`]
 */
enum class BankCode(val code: String) {
    SCB("014"),
    GovernmentHousingBank("033"),
    JPMorganBank("008")
}