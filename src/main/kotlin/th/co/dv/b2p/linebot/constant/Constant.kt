package th.co.dv.b2p.linebot.constant

object Constant {

    const val PREFIX_SYMBOL = "THB_"
    const val COLON = ":"
    const val COMMA = ","
    const val NOT_DEPLOY = "NOT-DEPLOY"
    enum class Command(val key: List<String>) {
        RELEASE(listOf("release")),
        JIRA(listOf("jira")),
        COVID(listOf("covid", "โควิด")),
        GOLD(listOf("gold", "ทอง")),
        BITCOIN(listOf("bitcoin", "บิทคอยน์")),
        PLAN(listOf("plan")),
        DOC(listOf("doc")),
        SUBSCRIBE(listOf("sub", "subscribe")),
        BROADCAST(listOf("broadcast"))
    }

    enum class Services {
        INV,
        PO,
        GR,
        CN,
        DN,
        PAYMENT,
        REQUEST,
        AGGREGATE,
        COMMON
    }

    object Curl {
        const val command = "curl"
        const val jiraUrl = "https://scb-digitalventures.atlassian.net/rest/api/3/search?jql=issue="
    }

    const val ASSIGNEE = "[ASSIGNEE]"
    const val DEVELOPER = "[DEVELOPER]"
    const val REPORTER = "[REPORTER]"
    const val STORY = "[STORY]"
    const val COMPONENT = "[COMPONENT]"
    const val FIXVERSION = "[FIXVERSION]"
    const val STATUS = "[STATUS]"
    const val INFORMATION = """
        Assignee name: $ASSIGNEE
        Developer name: $DEVELOPER
        Reporter name: $REPORTER
        
        Status: $STATUS
        Components: $COMPONENT
        Fix versions: $FIXVERSION
        
        
        Link jira: https://scb-digitalventures.atlassian.net/browse/$STORY
    """

    const val DEVELOPER_TAG = "customfield_10100"
    const val UNASSIGN = "UNASSIGN"

    enum class SubscriptionCommand(val value: String) {
        ME("me"),
        ALL("all"),
        ADD("add"),
        REMOVE("remove")
    }

}