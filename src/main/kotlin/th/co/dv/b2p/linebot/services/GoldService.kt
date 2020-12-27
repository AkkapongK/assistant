package th.co.dv.b2p.linebot.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import th.co.dv.b2p.linebot.model.GoldUpdatedModel

@Service
class GoldService {

    @Autowired
    lateinit var restTemplate: RestTemplate

    private val mapper = jacksonObjectMapper()

    private val url = "http://www.thaigold.info/RealTimeDataV2/gtdata_.txt"

    /**
     * Get Gold information
     */
    fun getUpdatedInformation(): List<GoldUpdatedModel> {

        val builder: UriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(url)

        return  try {
            val response = restTemplate.exchange(
                    builder.build(true).toUri(),
                    HttpMethod.GET,
                    null,
                    object : ParameterizedTypeReference<String>() {}
            ).body!!
            mapper.readValue(response, object : TypeReference<List<GoldUpdatedModel>>() {})
        } catch (e: Exception) {
            throw Exception(e.message, e)
        }
    }
}