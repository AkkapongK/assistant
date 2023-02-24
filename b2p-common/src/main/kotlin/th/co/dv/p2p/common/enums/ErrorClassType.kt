package th.co.dv.p2p.common.enums

enum class ErrorClassType(val code: String) {
    CODE("001"),
    DATABASE("002")
}

enum class ServiceErrorClassType(val code: String) {
    VALIDATION("E0001"),
    DATABASE("E0002"),
    KAFKA("E0003"),
    RABBITMQ("E0004"),
    EXTERNAL_SERVICE("E0005"),
    LOCKED_RECORD("E0006"),
    ILLEGAL_ARGUMENT("E0007"),
    AUTHORIZATION_PERMISSION("E0008"),
    UNKNOWN("E10000")
}
