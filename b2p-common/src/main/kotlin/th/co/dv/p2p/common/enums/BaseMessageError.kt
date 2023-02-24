package th.co.dv.p2p.common.enums

interface BaseMessageError {
    fun getCode(): String
    fun getMessage(): String
}