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
import th.co.dv.b2p.linebot.model.IssueJiraModel
import th.co.dv.b2p.linebot.model.JiraModel
import java.net.URLEncoder
import java.util.*

@Service
class JiraService {

    @Autowired
    lateinit var restTemplate: RestTemplate

    private val mapper = jacksonObjectMapper()

    private val url = "https://scb-digitalventures.atlassian.net/rest/api/3/search?jql="
    private val accessToken = "akkapong.k@dv.co.th:UgBUFfAzLelbbNQv4UHlDA55"

    enum class Mode(val key: String) {
        SPRINT("sprint="),
        ISSUE("issue=")
    }

    /**
     * Method for complete url
     */
    private fun Mode.completeUrl(value: String, startAt: Int = 0): String {
        return url + URLEncoder.encode(this.key + value, "UTF-8") + "&startAt=$startAt"
    }

    /**
     * Get Gold information
     */
    fun getInformation(mode: Mode, value: String): List<IssueJiraModel> {

        val headers = HttpHeaders()

        val token = Base64.getEncoder().encodeToString(accessToken.toByteArray())
        println("token : $token")
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
                        startAt = startAt)
                total = response.total!!
                currentRecord =  response.startAt!! + response.maxResults!!
                startAt += response.maxResults!!

                outputs.addAll(response.issues ?: emptyList())

            } while (total > currentRecord)

        } catch (e: Exception) {
            throw Exception(e.message, e)
        }

        println("Jira output size ${outputs.size}")
        return outputs
    }

    private fun getDataByPage(headers: HttpHeaders, mode: Mode, value: String, startAt: Int = 0): JiraModel {
        println("JIRA url: ${mode.completeUrl(value, startAt)}")
        val builder: UriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(mode.completeUrl(value, startAt))
        return restTemplate.exchange(
                builder.build(true).toUri(),
                HttpMethod.GET,
                HttpEntity(null, headers),
                object : ParameterizedTypeReference<JiraModel>() {}
        ).body!!
    }
}