package th.co.dv.p2p.common.enums

enum class GroupingTopic(val value: String) {
    PRE_GROUPING_PAYMENT("PRE_GROUPING_PAYMENT"),
    GROUPING_PAYMENT("GROUPING_PAYMENT"),
    GROUPING_KNOCKING("GROUPING_KNOCKING");

    companion object {
        fun from(findValue: String): GroupingTopic = values().first { it.value == findValue }
    }
}