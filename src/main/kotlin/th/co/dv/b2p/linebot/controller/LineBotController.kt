package th.co.dv.b2p.linebot.controller

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
import th.co.dv.b2p.linebot.constant.Constant.HELP
import th.co.dv.b2p.linebot.constant.Constant.PREFIX_SYMBOL
import th.co.dv.b2p.linebot.services.BitCoinService
import th.co.dv.b2p.linebot.services.CovidService
import th.co.dv.b2p.linebot.services.ExcelService
import th.co.dv.b2p.linebot.services.GoldService
import th.co.dv.b2p.linebot.utilities.Utils.getEnumIgnoreCase
import java.io.IOException
import java.util.concurrent.ExecutionException

@LineMessageHandler
class LineBotController {

    @Autowired
    lateinit var gitConfig: GitConfig

    @Autowired
    lateinit var covidService: CovidService

    @Autowired
    lateinit var goldService: GoldService

    @Autowired
    lateinit var bitCoinService: BitCoinService

    @Autowired
    lateinit var excelService: ExcelService

    @Autowired
    lateinit var lineMessagingClient: LineMessagingClient

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @EventMapping
    fun handleTextMessage(event: MessageEvent<TextMessageContent>) {
        logger.info(".....handleTextMessage......")
        logger.info(event.toString())
        val message: TextMessageContent = event.message!!
        handleTextContent(event.replyToken, event, message)
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
        val argCommand = arg.removeAt(0).toLowerCase()
        val command = Constant.Command.values().firstOrNull {
            argCommand in it.key
        }
        logger.info("Command: $command")


        when (command) {
//            Constant.Command.RELEASE -> findReleaseForService(replyToken, arg)
            Constant.Command.JIRA -> this.replyJiraFlexMessage(replyToken, arg)
            Constant.Command.COVID -> this.replyCovidFlexMessage(replyToken)
            Constant.Command.GOLD -> this.replyGoldFlexMessage(replyToken)
            Constant.Command.BITCOIN -> processBitcoin(replyToken, arg)
            Constant.Command.PLAN -> processReleasePlan(replyToken, arg)
            else -> this.replyText(replyToken, HELP)
        }
    }

    /**
     * Method to process bitcoin
     */
    private fun processBitcoin(replyToken: String, arg: MutableList<String>) {

        when (arg.isEmpty()) {
            true -> {
                var output = """"""
                // Show available list
                bitCoinService.getAvailable().forEach {
                    if (output.isNotEmpty()) {
                        output += "\r\n"
                    }
                    output += it.symbol?.replace(PREFIX_SYMBOL, "")
                }

                this.replyText(replyToken, output)
            }
            false -> this.replyBitCoinFlexMessage(replyToken, arg.first())
        }
    }

    /**
     * Method to process release plan
     */
    private fun processReleasePlan(replyToken: String, arg: MutableList<String>) {

        when (arg.isNotEmpty()) {
            true -> {
                val list = excelService.getReleasePlan(arg)
                list?.let {
                    var output = """"""
                    it.forEach { each ->
                        if (output.isNotEmpty()) {
                            output += "\r\n"
                        }
                        output += each
                    }
                    if (output.isEmpty()) output = NOT_ASSIGN
                    this.replyText(replyToken, output)
                }
            }
            false -> this.replyText(replyToken, PROJECT_NOT_FOUND)
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
        val result = if (response.isNullOrEmpty()) UNKNOWN else response!!
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
     * Method for reply Flex message
     */
    private fun replyCovidFlexMessage(replyToken: String) {
        this.reply(replyToken, CovidFlexMessage(covidService).get())
    }

    /**
     * Method for reply Gold message
     */
    private fun replyGoldFlexMessage(replyToken: String) {
        this.reply(replyToken, GoldFlexMessage(goldService).get())
    }

    /**
     * Method for reply Bitcoin message
     */
    private fun replyBitCoinFlexMessage(replyToken: String, symbol: String) {
        val finalSymbol = PREFIX_SYMBOL + symbol.toUpperCase()
        this.reply(replyToken, BitCoinFlexMessage(bitCoinService, finalSymbol).get())
    }


    /**
     * Method for reply Flex message
     */
    private fun replyJiraFlexMessage(replyToken: String, arg: List<String>) {
        val story = arg.firstOrNull() ?: this.replyText(replyToken, STORY_NOT_FOUND)
        val jiraFlexMessage = JiraFlexMessage(story.toString())
        this.reply(replyToken, jiraFlexMessage.get())
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