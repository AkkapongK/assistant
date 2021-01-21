package th.co.dv.b2p.linebot.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import th.co.dv.b2p.linebot.constant.Constant.NOT_DEPLOY
import th.co.dv.b2p.linebot.model.IssueFiledJiraModel
import th.co.dv.b2p.linebot.model.IssueJiraModel
import th.co.dv.b2p.linebot.model.JiraModel
import th.co.dv.b2p.linebot.model.mappingCustomField
import th.co.dv.b2p.linebot.utilities.Utils.getFieldValue
import java.net.URLEncoder
import java.util.*

@Service
class JiraService {

    @Autowired
    lateinit var restTemplate: RestTemplate

    private val url = "https://scb-digitalventures.atlassian.net/rest/api/3/search?jql="
    private val accessToken = "akkapong.k@dv.co.th:UgBUFfAzLelbbNQv4UHlDA55"

    enum class Doc(val url: String, val project: String, val release: String) {
        B2P_R9(
                url = "https://scb-digitalventures.atlassian.net/wiki/spaces/BLOC/pages/1539047438/B2P+R9+Wiki",
                project = "b2p",
                release = "9")
    }

    /**
     * https://scb-digitalventures.atlassian.net/rest/api/3/search?jql=cf[10218]=newsit2+AND+scope=irpc&maxResults=5
     */
    enum class Mode(val key: String) {
        SPRINT("sprint="),
        ISSUE("issue="),
        DEPLOY("cf[10218]=:env+AND+scope=:scope")
    }
    private val AND = "+AND+"
    private val ANDENCODE = "%2BAND%2B"
    private val UTF8 = "UTF-8"

    /**
     * Method for complete url
     */
    private fun Mode.completeUrl(value: String, value2: String? = null, startAt: Int = 0, maxResults: Int? = null): String {
        return when (this) {
            Mode.DEPLOY -> url + URLEncoder.encode(this.key.replace(":scope", value).replace(":env", value2!!), UTF8)
                    .replace(ANDENCODE, AND) + "&startAt=$startAt" + (
                        if (maxResults != null) "&maxResults=$maxResults" else ""
                    )
            else -> url + URLEncoder.encode(this.key + value, UTF8) + "&startAt=$startAt" + (
                        if (maxResults != null) "&maxResults=$maxResults" else ""
                    )
        }

    }

    /**
     * Get Gold information
     */
    fun getInformation(mode: Mode, value: String, value2: String?= null, maxResults: Int? = null): List<IssueJiraModel> {

        val headers = HttpHeaders()

        val token = Base64.getEncoder().encodeToString(accessToken.toByteArray())
        headers.set(HttpHeaders.AUTHORIZATION, "Basic $token")

        var startAt = 0
        var total: Int
        var currentRecord: Int
        val outputs= mutableListOf<IssueJiraModel>()

        try {
            do {
                // codes inside body of do while loop
                val response = getDataByPage(
                        headers = headers,
                        mode = mode,
                        value = value,
                        value2 = value2,
                        startAt = startAt,
                        maxResults = maxResults)
                total = maxResults ?: response.total!!
                currentRecord =  response.startAt!! + response.maxResults!!
                startAt += response.maxResults!!

                outputs.addAll(response.issues ?: emptyList())

            } while (total > currentRecord)

        } catch (e: Exception) {
            throw Exception(e.message, e)
        }
        return outputs
    }

    private fun getDataByPage(headers: HttpHeaders,
                              mode: Mode,
                              value: String,
                              value2: String? = null,
                              startAt: Int = 0,
                              maxResults: Int? = null): JiraModel {
        val builder: UriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(mode.completeUrl(value, value2, startAt, maxResults))
        return restTemplate.exchange(
                builder.build(true).toUri(),
                HttpMethod.GET,
                HttpEntity(null, headers),
                object : ParameterizedTypeReference<JiraModel>() {}
        ).body!!
    }

    private fun List<IssueFiledJiraModel>.getTargetCustomField(customFiled: String): String? {
        val issueField = this.firstOrNull {
            val customVal = it.getFieldValue<String>(customFiled)
            customVal != null && customVal != NOT_DEPLOY
        } ?: return null

        return issueField.getFieldValue<String>(customFiled)
    }

    /**
     * Method get tag deploy
     */
    fun getDeployTag(issueJiraModel: List<IssueJiraModel>): List<String> {
        val fields = issueJiraModel.map { it.fields!! }
        return mappingCustomField.mapNotNull { (customFiled, customLabel) ->
             val value = fields.getTargetCustomField(customFiled) ?: return@mapNotNull null
            "$customLabel : $value"
        }
    }

    /**
     * Method for get doc url by project and release
     */
    fun getDocUrl(project: String, release: String): String? {
        return Doc.values().find {
            it.release == release.toLowerCase().trim() &&
                    it.project == project.toLowerCase().trim()
        }?.url
    }
}