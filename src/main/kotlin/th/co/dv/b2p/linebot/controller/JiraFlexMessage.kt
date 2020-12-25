package th.co.dv.b2p.linebot.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.linecorp.bot.model.action.URIAction
import com.linecorp.bot.model.message.FlexMessage
import com.linecorp.bot.model.message.flex.component.*
import com.linecorp.bot.model.message.flex.container.Bubble
import com.linecorp.bot.model.message.flex.container.Carousel
import com.linecorp.bot.model.message.flex.unit.FlexFontSize
import com.linecorp.bot.model.message.flex.unit.FlexGravity
import com.linecorp.bot.model.message.flex.unit.FlexLayout
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize
import com.lordcodes.turtle.shellRun
import th.co.dv.b2p.linebot.constant.Constant
import java.util.function.Supplier

class JiraFlexMessage(private val story: String) : Supplier<FlexMessage> {

    private val mapper = jacksonObjectMapper()

    lateinit var assignee: String
    lateinit var developer: String
    lateinit var reporter: String
    lateinit var status: String
    lateinit var fixVersions: String
    lateinit var components: String
    lateinit var title: String
    lateinit var link: String

    private val image = "https://marketplacelive.blob.core.windows.net/solution-logo/47dda901f2fa40c1a9c983b1a352e8fb.png"
    /**
     * Method for process jira
     */
    private fun processJira() {
        val response = shellRun(Constant.Curl.command, listOf(
                Constant.Curl.jiraUrl + story,
                "--user",
                "akkapong.k@dv.co.th:UgBUFfAzLelbbNQv4UHlDA55"))

        val node = mapper.readTree(response)

        getInformationFromStory(node, story)
    }

    /**
     * Get assignee
     */
    private fun JsonNode.getAssignee() = this.get("assignee").get("displayName").toString().replace("\"", "")
    /**
     * Get developer
     */
    private fun JsonNode.getDeveloper() = this.get(Constant.DEVELOPER_TAG).get("displayName").toString().replace("\"", "")
    /**
     * Get reporter
     */
    private fun JsonNode.getReport() = this.get("creator").get("displayName").toString().replace("\"", "")
    /**
     * Get reporter
     */
    private fun JsonNode.getComponents() = this.get("components").map {
        it.get("name").toString().replace("\"", "")
    }.joinToString()
    /**
     * Get Title
     */
    private fun JsonNode.getTitle() = this.get("summary").toString().replace("\"", "")

    /**
     * Method to get get assignee
     *
     */
    private fun JsonNode.getInformation(bloc: String) {
        val issue = this.get("issues").first()
        val fields = issue.get("fields")
        assignee = fields.getAssignee()
        developer = fields.getDeveloper()
        reporter = fields.getReport()
        components = fields.getComponents()
        fixVersions = fields.getFixVersion()
        status = fields.getStatus()
        title = fields.getTitle()
        link = "https://scb-digitalventures.atlassian.net/browse/$bloc"
    }

    /**
     * Method to get getFixVersion
     *
     */
    private fun JsonNode.getFixVersion(): String {

        val fixVersions = this.get("fixVersions")

        val versions = fixVersions.map {
            it.get("name").toString().replace("\"", "")
        }
        return versions.joinToString()
    }

    /**
     * Method to get getStatus
     *
     */
    private fun JsonNode.getStatus(): String {
        val status = this.get("status")
        return status.get("name").toString().replace("\"", "")
    }

    /**
     * get information from story
     */
    private fun getInformationFromStory(response: JsonNode, bloc: String) {
       response.getInformation(bloc)
    }

    override fun get(): FlexMessage {

        processJira()

        val jiraResult = createBubble()
        val carousel = Carousel.builder()
                .contents(listOf(jiraResult))
                .build()
        return FlexMessage("Catalogue", carousel)
    }

    private fun createBubble(): Bubble {
        val heroBlock = createHeroBlock(image)
        val bodyBlock = createBodyBlock(
                title = title,
                assignee = assignee,
                developer = developer,
                reporter = reporter,
                fixVersion = fixVersions,
                components = components,
                status = status)
        val footerBlock = createFooterBlock(link)
        return Bubble.builder()
                .hero(heroBlock)
                .body(bodyBlock)
                .footer(footerBlock)
                .build()
    }

    private fun createSeeMoreBubble(): Bubble {
        return Bubble.builder()
                .body(Box.builder()
                        .layout(FlexLayout.VERTICAL)
                        .spacing(FlexMarginSize.SM)
                        .contents(listOf(
                                Button.builder()
                                        .flex(1)
                                        .gravity(FlexGravity.CENTER)
                                        .action(URIAction("See more", "http://www.amazon.com"))
                                        .build()
                        )).build()
                )
                .build()
    }

    private fun createHeroBlock(imageURL: String): Image {
        return Image.builder()
                .size(Image.ImageSize.FULL_WIDTH)
                .aspectRatio(Image.ImageAspectRatio.R20TO13)
                .aspectMode(Image.ImageAspectMode.Cover)
                .url(imageURL)
                .build()
    }

    private fun createBlock(
            label: String,
            desc: String,
            labelColor: String = "#706C6C",
            descColor: String = "#979494",
            labelSize: FlexFontSize = FlexFontSize.Md,
            descSize: FlexFontSize = FlexFontSize.SM) : Box {
        return Box.builder()
                .layout(FlexLayout.BASELINE)
                .contents(listOf(
                        Text.builder().text(label)
                                .wrap(true)
                                .weight(Text.TextWeight.BOLD)
                                .color(labelColor)
                                .size(labelSize)
                                .flex(0)
                                .build(),
                        Text.builder().text(desc)
                                .wrap(true)
                                .weight(Text.TextWeight.REGULAR)
                                .color(descColor)
                                .size(descSize)
                                .flex(0)
                                .build()
                )).build()
    }

    private fun createBodyBlock(title: String,
                                assignee: String,
                                developer: String,
                                reporter: String,
                                status: String,
                                fixVersion: String,
                                components: String): Box {
        val titleBlock = Text.builder()
                .text(title)
                .wrap(true)
                .weight(Text.TextWeight.BOLD)
                .size(FlexFontSize.XL).build()

        val assigneeBlock = createBlock("Assignee: ", assignee)
        val developerBlock = createBlock("Developer: ", developer)
        val reporterBlock = createBlock("Reporter: ", reporter)
        val fixVersionBlock = createBlock("Fix versions: ", fixVersion, "#5555E2", "#6666CC")
        val statusBlock = createBlock("Status: ", status, "#17A62A", "#5FD26E")
        val componentBlock = createBlock(
                label = "Components: ",
                desc = components,
                labelColor = "#EC9B21",
                descColor = "#F3C073",
                labelSize = FlexFontSize.SM,
                descSize = FlexFontSize.XS)

//        val outOfStock = Text.builder()
//                .text("Temporarily out of stock")
//                .wrap(true)
//                .size(FlexFontSize.XXS)
//                .margin(FlexMarginSize.MD)
//                .color("#FF5551")
//                .build()
//        println("----- 3 -----")
        val flexComponents = listOf<FlexComponent>(
                titleBlock,
                assigneeBlock,
                developerBlock,
                reporterBlock,
                statusBlock,
                fixVersionBlock,
                componentBlock)
        val listComponent = flexComponents.toMutableList()
        return Box.builder()
                .layout(FlexLayout.VERTICAL)
                .spacing(FlexMarginSize.MD)
                .contents(listComponent)
                .build()
    }

    private fun createFooterBlock(link: String): Box {
        val goToJiraButton: Button = Button.builder()
                .style(Button.ButtonStyle.PRIMARY)
                .action(URIAction("Go to jira", link))
                .build()

        return Box.builder()
                .layout(FlexLayout.VERTICAL)
                .spacing(FlexMarginSize.SM)
                .contents(listOf(goToJiraButton))
                .build()
    }
}