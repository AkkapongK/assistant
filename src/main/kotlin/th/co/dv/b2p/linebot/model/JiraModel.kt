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
        var status: StatusJiraModel? = null
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
