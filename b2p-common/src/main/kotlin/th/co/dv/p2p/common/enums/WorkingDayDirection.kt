package th.co.dv.p2p.common.enums

/**
 * This enum specify the available direction we use to calculate the next working day.
 * The reason there's a backward direction is because some org wants to calculate backdated days by moving
 * backward by days or months
 */
enum class WorkingDayDirection {
    FORWARD,
    BACKWARD
}