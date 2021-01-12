package th.co.dv.b2p.linebot.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import th.co.dv.b2p.linebot.model.GitBranchModel
import java.net.URLEncoder

@Service
class GitService {
    @Autowired
    lateinit var restTemplate: RestTemplate

    private val gitUrl = "https://gitlab.com/api/v4"
    private val releaseUri = "/projects/:projectId/repository/branches?search=:searchValue"
    private val tokenKey = "PRIVATE-TOKEN"
    private val token = "B8co6P9qzdBY-jx8yzUF"
    private val searchValue = "^release-"

    enum class Service(val projectId: String) {
        INV("15648440"),
        PO("14039929"),
        GR("14039934"),
        REQUEST("19315673"),
        CN("18771949"),
        DN("18973739"),
        PAYMENT("19315673"),
        AGGREGATE("17023967"),
        COMMON("13948500"),
        CONFIG("15648869"),
        MASTER("15669617"),
        USER("16582011"),
        FINANCING("21562209"),
        NOTI("17415448"),
        STANDARD("19137345"),
        INTERFACE("14039955"),
        MATCHING("16456666"),
        ETAX("19067159"),
        DOA("20220026"),
        EMAIL("14248712"),
        MONITOR("15928850"),
        FILE("17700089"),

        // Custom
        CUSTOMBANK("6512449"),
        CUSTOMINV("16168388"),
        CUSTOMPRUKSA("19661864"),
        CUSTOMPIRPC("19661751"),
        CUSTOMPSNP("20881136"),
        CUSTOMPMINT("11423669"),
        CUSTOMPSSR("8412269"),
        CUSTOMPSCG("6123205")
    }

    /**
     * Method for complete url to get release branch
     */
    private fun Service.completeUrlForGetRelease(): String {
        return gitUrl + releaseUri.replace(":projectId", this.projectId)
                .replace(":searchValue", URLEncoder.encode(searchValue, "UTF-8"))
    }

    /**
     * Method for get release of target branch
     */
    fun getReleaseBranch(service: Service): List<GitBranchModel> {
        val url = service.completeUrlForGetRelease()

        val builder: UriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(url)

        val headers = HttpHeaders()
        headers.set(tokenKey, token)
        headers.set("Content-Type", "application/json")

        return try {
            restTemplate.exchange(
                    builder.build(true).toUri(),
                    HttpMethod.GET,
                    HttpEntity(null, headers),
                    object : ParameterizedTypeReference<List<GitBranchModel>>() {}
            ).body!!

        } catch (e: Exception) {
            throw Exception(e.message, e)
        }
    }


    /**
     * Method for get available project
     */
    fun getAvailableProject(): List<String> {
        return Service.values().map { it.name.toLowerCase() }
    }
}