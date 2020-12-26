package th.co.dv.b2p.linebot.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import th.co.dv.b2p.linebot.model.CovidUpdatedModel

@Service
class CovidService {

    @Autowired
    lateinit var restTemplate: RestTemplate

    private val url = "https://covid19.th-stat.com/api/open/today"

    /**
     * Get convid information
     */
    fun getUpdatedInformation(): CovidUpdatedModel {

        val builder: UriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(url)

        return  try {
            restTemplate.exchange(
                    builder.build(true).toUri(),
                    HttpMethod.GET,
                    null,
                    object : ParameterizedTypeReference<CovidUpdatedModel>() {}
            ).body!!
        } catch (e: Exception) {
            throw Exception(e.message, e)
        }
    }
}