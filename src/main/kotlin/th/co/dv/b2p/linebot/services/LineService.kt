package th.co.dv.b2p.linebot.services

import com.google.common.net.HttpHeaders.CONTENT_TYPE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import th.co.dv.b2p.linebot.config.LineConfiguration
import th.co.dv.b2p.linebot.constant.Constant.COLON
import th.co.dv.b2p.linebot.model.*


@Service
class LineService {

    @Autowired
    lateinit var lineConfiguration: LineConfiguration

    @Autowired
    lateinit var restTemplate: RestTemplate

    val uri = mapOf(
            "broadcast" to "/bot/message/multicast"
    )

    private fun getHeader(): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(lineConfiguration.channelToken!!)
        return headers
    }

    /**
     * Broadcast message
     */
    fun broadcastMessage(from: String, receivedIds: List<String>, messages: List<LineMessage>) {
        if (haveBroadcastPermission(from).not()) return

        val builder: UriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(lineConfiguration.baseUrl!! + uri["broadcast"])

        try {
            restTemplate.exchange(
                    builder.build(true).toUri(),
                    HttpMethod.POST,
                    HttpEntity(LineModel(
                            to = receivedIds,
                            messages = messages
                    ), getHeader()),
                    object : ParameterizedTypeReference<String>() {}
            )
        } catch (e: Exception) {
            throw Exception(e.message, e)
        }
    }

    /**
     * Method to validate permission
     */
    fun haveBroadcastPermission(userId: String): Boolean {
        val allowIds = lineConfiguration.broadcaster.values
        return allowIds.contains(userId)
    }

}