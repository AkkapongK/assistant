package th.co.dv.b2p.linebot.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import com.lordcodes.turtle.GitCommands
import com.lordcodes.turtle.shellRun
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import th.co.dv.b2p.linebot.config.GitConfig
import th.co.dv.b2p.linebot.constant.*
import th.co.dv.b2p.linebot.constant.Constant.ASSIGNEE
import th.co.dv.b2p.linebot.constant.Constant.Curl.jiraUrl
import th.co.dv.b2p.linebot.constant.Constant.DEVELOPER
import th.co.dv.b2p.linebot.constant.Constant.DEVELOPER_TAG
import th.co.dv.b2p.linebot.constant.Constant.HELP
import th.co.dv.b2p.linebot.constant.Constant.INFORMATION
import th.co.dv.b2p.linebot.constant.Constant.REPORTER
import th.co.dv.b2p.linebot.constant.Constant.STORY
import th.co.dv.b2p.linebot.utilities.Utils.getEnumIgnoreCase
import java.io.IOException
import java.util.concurrent.ExecutionException

@LineMessageHandler
class LineBotController {

    @Autowired
    lateinit var gitConfig: GitConfig

    @Autowired
    lateinit var lineMessagingClient: LineMessagingClient

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val mapper = jacksonObjectMapper()

    @EventMapping
    fun handleTextMessage(event: MessageEvent<TextMessageContent>) {
        logger.info(".....handleTextMessage......")
        logger.info(event.toString())
        val message: TextMessageContent = event.message!!
        handleTextContent(event.getReplyToken(), event, message)
    }

    fun String.toArgument() = this.split(" ").map { it.trim() }.toMutableList()

    /**
     * Method for handle text message
     */
    private fun handleTextContent(replyToken: String, event: Event, content: TextMessageContent) {
        val text = content.text
        val userId = event.source.userId
        logger.info("Got text message from $userId => $replyToken : $text")
        val arg = text.toArgument()
        val command = getEnumIgnoreCase<Constant.Command>(arg.removeAt(0))
        logger.info("Command: $command")


        when (command) {
            Constant.Command.RELEASE -> findReleaseForService(replyToken, arg)
            Constant.Command.JIRA -> processJira(replyToken, arg)
            else -> this.replyText(replyToken, HELP)
        }
    }

    /**
     * Method for process jira
     */
    private fun processJira(replyToken: String, arg: MutableList<String>) {
        val bloc = arg.removeAt(0)
        val tag = if (arg.size >= 1) arg.removeAt(0) else ""
        val response = shellRun(Constant.Curl.command, listOf(
                jiraUrl+bloc,
                "--user",
                "akkapong.k@dv.co.th:UgBUFfAzLelbbNQv4UHlDA55"))

        val node = mapper.readTree(response)
        val output = getInformationFromStory(node, bloc, tag) ?: TAG_NOT_FOUND
        this.replyText(replyToken, output)
    }

    /**
     * Get assignee
     */
    private fun JsonNode.getAssignee() = this.get("assignee").get("displayName").toString()
    /**
     * Get developer
     */
    private fun JsonNode.getDeveloper() = this.get(DEVELOPER_TAG).get("displayName").toString()
    /**
     * Get reporter
     */
    private fun JsonNode.getReport() = this.get("creator").get("displayName").toString()

    /**
     * Method to get get assignee
     *
     */
    private fun JsonNode.getInformation(bloc: String): String {
        val issue = this.get("issues").first()
        val fields = issue.get("fields")
        val assignee = fields.getAssignee()
        val developer = fields.getDeveloper()
        val reporter = fields.getReport()

        return INFORMATION
                .replace(ASSIGNEE, assignee)
                .replace(DEVELOPER, developer)
                .replace(REPORTER, reporter)
                .replace(STORY, bloc)
    }

    /**
     * Method to get getFixVersion
     *
     */
    private fun JsonNode.getFixVersion(): String {
        val issue = this.get("issues").first()
        val fields = issue.get("fields")
        val fixVersions = fields.get("fixVersions")

        val versions = fixVersions.map {
            it.get("name").toString().replace("\"", "")
        }
        logger.info("getFixVersion versions : $versions")
        return versions.joinToString()
    }

    /**
     * Method to get getStatus
     *
     */
    private fun JsonNode.getStatus(): String {
        val issue = this.get("issues").first()
        val fields = issue.get("fields")
        val status = fields.get("status")
        return status.get("name").toString().replace("\"", "")
    }

    /**
     * get information from story
     */
    private fun getInformationFromStory(response: JsonNode, bloc: String, tag: String): String? {
        return when (tag.toLowerCase()) {
            "fixversions" -> response.getFixVersion()
            "status" -> response.getStatus()
            else -> response.getInformation(bloc)
        }
    }

    /**
     * Method for find the service that want to check the current release
     */
    private fun findReleaseForService(replyToken: String, arg: List<String>) {
        val serviceName = getEnumIgnoreCase<Constant.Services>(arg.first())

        when (serviceName) {
            Constant.Services.INV,
            Constant.Services.PO,
            Constant.Services.GR,
            Constant.Services.CN,
            Constant.Services.DN,
            Constant.Services.AGGREGATE,
            Constant.Services.PAYMENT,
            Constant.Services.COMMON,
            Constant.Services.REQUEST -> getServiceBranch(replyToken, serviceName)
            else -> this.replyText(replyToken, UNKNOWN_SERVICE)
        }
    }

    /**
     * Method for get service folder
     */
    fun Constant.Services.getFolder(): String? {
        return try {
            when(this) {
                Constant.Services.INV -> gitConfig.inv!!
                Constant.Services.PO -> gitConfig.po!!
                Constant.Services.GR -> gitConfig.gr!!
                Constant.Services.CN -> gitConfig.cn!!
                Constant.Services.DN -> gitConfig.dn!!
                Constant.Services.AGGREGATE -> gitConfig.aggregate!!
                Constant.Services.PAYMENT -> gitConfig.payment!!
                Constant.Services.REQUEST -> gitConfig.request!!
                Constant.Services.COMMON -> gitConfig.common!!
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Method for handle service command
     *
     */
    private fun getServiceBranch(replyToken: String, service: Constant.Services) {
        val folder = service.getFolder() ?: run {
            this.replyText(replyToken, SERVICE_PATH_NOT_FOUND)
            return
        }
        val pathDirectory = (gitConfig.directory?: "") + folder
        val response =  runCommand(pathDirectory)
        val result = if (response.isNullOrEmpty()) UNKNOW else response!!
        logger.info("Return result message: $result")
        this.replyText(replyToken, result)
    }


    fun GitCommands.getFetch() = gitCommand(listOf("fetch", "origin"))
    fun GitCommands.getBranch() = gitCommand(listOf("branch", "-r", "--list", "*/release-*"))

    fun runCommand(path: String): String? {
        try {

            val output = shellRun {
                changeWorkingDirectory(path)
                git.getFetch()
                git.getBranch()
            }

            logger.info("output: $output")
            return output
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Method for reply text
     */
    private fun replyText(replyToken: String, message: String) {
        var message = message
        if (replyToken.isEmpty()) {
            throw IllegalArgumentException("replyToken is not empty")
        }
        if (message.length > 1000) {
            message = message.substring(0, 1000 - 2) + "..."
        }
        this.reply(replyToken, TextMessage(message))
    }

    private fun reply(replyToken: String, message: Message) {
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