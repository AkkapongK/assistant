package th.co.dv.b2p.linebot.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class JiraModel (
        var expand: String? = null,
        var startAt: Int? = null,
        var maxResults: Int? = null,
        var total: Int? = null,
        var issues: List<IssueJiraModel>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class IssueJiraModel(
        var id: String? = null,
        var self: String? = null,
        var key: String? = null,
        var fields: IssueFiledJiraModel? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class IssueFiledJiraModel(
        var assignee: UserJiraModel? = null,
        var creator: UserJiraModel? = null,
        var customfield_10100: UserJiraModel? = null,
        var customfield_10010: List<SprintJiraModel>? = null,
        var customfield_10227: SquadJiraModel? = null,
        var components: List<ComponentJiraModel>? = null,
        var fixVersions: List<ReleaseJiraModel>? = null,
        var summary: String? = null,
        var issuetype: IssueTypeJiraModel? = null,
        var status: StatusJiraModel? = null,

        var customfield_10222: String? = null,
        var customfield_10223: String? = null,
        var customfield_10235: String? = null,
        var customfield_10231: String? = null,
        var customfield_10232: String? = null,
        var customfield_10234: String? = null,
        var customfield_10239: String? = null,
        var customfield_10247: String? = null,
        var customfield_10248: String? = null,
        var customfield_10249: String? = null,
        var customfield_10254: String? = null,
        var customfield_10259: String? = null,
        var customfield_10260: String? = null,
        var customfield_10261: String? = null,
        var customfield_10262: String? = null,
        var customfield_10266: String? = null,
        var customfield_10278: String? = null,
        var customfield_10296: String? = null,
        var customfield_10208: String? = null,
        var customfield_10233: String? = null,
        var customfield_10275: String? = null,
        var customfield_10274: String? = null,
        var customfield_10276: String? = null,
        var customfield_10277: String? = null,
        var customfield_10295: String? = null,
        var customfield_10240: String? = null,
        var customfield_10238: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserJiraModel(
        var displayName: String? = null,
        var emailAddress: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ComponentJiraModel(
        var id: String? = null,
        var name: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class StatusJiraModel(
        var id: String? = null,
        var name: String? = null,
        var description: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReleaseJiraModel(
        var id: String? = null,
        var name: String? = null,
        var description: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SprintJiraModel(
        var id: String? = null,
        var name: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class IssueTypeJiraModel(
        var id: String? = null,
        var name: String? = null,
        var description: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SquadJiraModel(
        var id: String? = null,
        var value: String? = null
)


val mappingCustomField = mapOf(
        "customfield_10222" to "Email",
        "customfield_10223" to "Bot noi",
        "customfield_10235" to "Purchase",
        "customfield_10231" to "Goods",
        "customfield_10232" to "Invoice",
        "customfield_10234" to "Monitoring",
        "customfield_10239" to "Payment",
        "customfield_10247" to "Notification",
        "customfield_10248" to "Aggregate",
        "customfield_10249" to "File",
        "customfield_10254" to "Matching",
        "customfield_10259" to "Credit Note",
        "customfield_10260" to "Debit Note",
        "customfield_10261" to "Request",
        "customfield_10262" to "Standard",
        "customfield_10266" to "E-Tax",
        "customfield_10278" to "Doa",
        "customfield_10296" to "Financing",
        "customfield_10208" to "New UI",

        "customfield_10233" to "Master",
        "customfield_10236" to "Configuration",
        "customfield_10246" to "User",

        "customfield_10275" to "Custom-Mint",
        "customfield_10274" to "Custom-SSR",
        "customfield_10276" to "Custom-Irpc",
        "customfield_10277" to "Custom-Pruksa",
        "customfield_10295" to "Custom-Snp",
        "customfield_10240" to "SCG bank custom",
        "customfield_10238" to "Custom-Invoice",

        "customfield_10206" to "Corda",

        "customfield_10226" to "Onboard",
        "customfield_10251" to "Onboard admin",
        "customfield_10252" to "Self onboard"


        //TODO: Add more service here
)
