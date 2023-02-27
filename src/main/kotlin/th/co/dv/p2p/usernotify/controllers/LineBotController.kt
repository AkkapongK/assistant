package th.co.dv.p2p.usernotify.controllers

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import th.co.dv.p2p.usernotify.services.RestService
import java.util.concurrent.ExecutionException

@LineMessageHandler
class LineBotController {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(LineBotController::class.java)
        private val className = LineBotController::class.java.simpleName
    }

    @Autowired
    lateinit var lineMessagingClient: LineMessagingClient

    @Autowired
    lateinit var restService: RestService

    @Value("\${chat.apiKey:xxx}")
    private var apiKey: String = "XXXX"

    @EventMapping
    fun handleTextMessage(event: MessageEvent<TextMessageContent>) {
        logger.info("$className.handleTextMessage")
        logger.info(event.toString())
        val message: TextMessageContent = event.message!!
        handleTextContent(event.replyToken, message)
    }

    fun getHeader(): HttpHeaders {
        val headers = HttpHeaders()
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json")
        return headers
    }

    fun sendRequestToChatOpenAI(prompt: String) : String{


        val url = "https://api.openai.com/v1/completions"

        val json = """
            {
                "prompt": "$prompt",
                "model": "text-davinci-002",
                "max_tokens": 1024,
                "user": "my-user-9991212121"
            }
        """.trimIndent()


        val responseBody = restService.exchange(
            url = url,
            method = HttpMethod.POST,
            requestEntity = HttpEntity(json, getHeader()),
            object : ParameterizedTypeReference<String>() {}
        ).body!!

        println("responseBody: $responseBody")
        val objectMapper = jacksonObjectMapper()
        val responseObject = objectMapper.readValue(responseBody, ResponseObject::class.java)

        return responseObject.choices?.firstOrNull()?.text ?: ""
    }

    /**
     * Method for handle text message
     */
    private fun handleTextContent(replyToken: String, content: TextMessageContent) {
        val response = sendRequestToChatOpenAI(content.text)
        //TODO
        return this.reply(replyToken, TextMessage(response))
    }

    fun reply(replyToken: String, message: Message) {
        reply(replyToken, listOf(message))
    }

    private fun reply(replyToken: String, messages: List<Message>) {
        try {
            lineMessagingClient.replyMessage(
                ReplyMessage(replyToken, messages)
            ).get()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseObject(val choices: List<ResponseChoice>? = null)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseChoice(val text: String? = null)