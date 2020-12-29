package th.co.dv.b2p.linebot.constant

object Constant {

    const val PREFIX_SYMBOL = "THB_"
    enum class Command(val key: List<String>) {
        RELEASE(listOf("release")),
        JIRA(listOf("jira")),
        COVID(listOf("covid", "โควิด")),
        GOLD(listOf("gold", "ทอง")),
        BITCOIN(listOf("bitcoin", "บิทคอยน์"))
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
                - fixversions : release branch that merge
                - status : current status
        usage: jira [story] [tag(optional)]
        example: jira BLOC-13500 status
        show information example: jira BLOC-13500 

    3. covid: Show updated covid information in Thailand
    
    4. gold: Show updated gold, THB and silver price
    
    5. bitcoin: Show Bitcoin information
        argument: 
            - Target symbol : THB_BTC
            
        usage 1 : bitcoin => Show available symbol 
        usage 2 : bitcoin [symbol] => Show symbol information
    
    """

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
}