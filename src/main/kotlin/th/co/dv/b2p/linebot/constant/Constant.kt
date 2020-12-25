package th.co.dv.b2p.linebot.constant

object Constant {

    enum class Command {
        RELEASE,
        JIRA
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

    const val HELP = """
    Command that supported
    1. release: get current release branch in git
        argument:   
            - service name [inv, po, gr, payment, cn, dn, request, aggregate, common]
        usage: release [service name]
        example: release inv
    2. jira: Track information of target story
        argument: 
            - Target story: BLOC-XXX
            - Tatget tag: 
                - fixversion : release branch that merge
                - status : current status
        usage: jira [story] [tag(optional)]
        example: jira BLOC-13500 status
        show information example: jira BLOC-13500 
    """

    const val ASSIGNEE = "[ASSIGNEE]"
    const val DEVELOPER = "[DEVELOPER]"
    const val REPORTER = "[REPORTER]"
    const val STORY = "[STORY]"
    const val INFORMATION = """
        Assignee name: $ASSIGNEE
        Developer name: $DEVELOPER
        Reporter name: $REPORTER
        
        Link jira: https://scb-digitalventures.atlassian.net/browse/$STORY
    """

    const val DEVELOPER_TAG = "customfield_10100"
}