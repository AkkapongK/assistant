package th.co.dv.p2p.common.enums

enum class PaymentSystem(val value: String) {

    /** Transfer within same bank (in this case, SCB). In this case, bank receive information before 20:00 */
    DIRECT_CREDIT("DIRECT_CREDIT"),

    /** Transfer with value < 2 million THB to another bank within same date that bank receives payment instruction
     *   (in case bank receive information before 11:00)  */
    SMART_SAME_DAY("SMART_SAME_DAY"),

    /** Transfer with value < 2 million to another bank in day+1 after bank receives payment instruction
     *   (in case bank receive information before 09:30) */
    SMART_NEXT_DAY("SMART_NEXT_DAY"),

    /** Transfer with value >= 2 million THB to another bank within same date
     *   (in case bank receive information before 15:00) that bank receives payment instruction */
    BAHTNET("BAHTNET"),

    /** Transfer with any value but invoice is marked as Invoice Financing.
     *  Cut off date is before 12:00 to get payment on same day */
    INVOICE_FINANCING("INVOICE_FINANCING"),
    /** External Payment */
    BUYER_EXTERNAL_PAYMENT("BUYER_EXTERNAL_PAYMENT")
}