package th.co.dv.b2p.linebot.controller

import com.linecorp.bot.model.action.URIAction
import com.linecorp.bot.model.message.FlexMessage
import com.linecorp.bot.model.message.flex.component.*
import com.linecorp.bot.model.message.flex.container.Bubble
import com.linecorp.bot.model.message.flex.container.Carousel
import com.linecorp.bot.model.message.flex.unit.FlexFontSize
import com.linecorp.bot.model.message.flex.unit.FlexLayout
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize
import th.co.dv.b2p.linebot.constant.Constant.UNASSIGN
import th.co.dv.b2p.linebot.model.IssueJiraModel
import java.util.function.Supplier

class JiraStoryFlexMessage(val data: IssueJiraModel) : Supplier<FlexMessage> {

    private val image = "https://marketplacelive.blob.core.windows.net/solution-logo/47dda901f2fa40c1a9c983b1a352e8fb.png"

    override fun get(): FlexMessage {

        val jiraResult = createBubble()
        val carousel = Carousel.builder()
                .contents(listOf(jiraResult))
                .build()
        return FlexMessage("Jira story information", carousel)
    }

    private fun createBubble(): Bubble {
        val heroBlock = createHeroBlock(image)
        val fixVersion = data.fields?.fixVersions?.mapNotNull { it.name }
        val components = data.fields?.components?.mapNotNull { it.name }
        val bodyBlock = createBodyBlock(
                title = data.fields?.summary?: UNASSIGN,
                assignee = data.fields?.assignee?.displayName ?: UNASSIGN,
                developer = data.fields?.customfield_10100?.displayName ?: UNASSIGN,
                reporter = data.fields?.creator?.displayName ?: UNASSIGN,
                fixVersion = if (fixVersion == null || fixVersion.isEmpty()) UNASSIGN else fixVersion.joinToString(),
                components = if (components == null || components.isEmpty()) UNASSIGN else components.joinToString(),
                status = data.fields?.status?.name ?: UNASSIGN)
        val footerBlock = createFooterBlock("https://scb-digitalventures.atlassian.net/browse/${data.key}")
        return Bubble.builder()
                .hero(heroBlock)
                .body(bodyBlock)
                .footer(footerBlock)
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
                                .margin(FlexMarginSize.SM)
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

        val assigneeBlock = createBlock("Assignee :", assignee)
        val developerBlock = createBlock("Developer :", developer)
        val reporterBlock = createBlock("Reporter :", reporter)
        val fixVersionBlock = createBlock("Fix versions :", fixVersion, "#5555E2", "#6666CC")
        val statusBlock = createBlock("Status :", status, "#17A62A", "#5FD26E")
        val componentBlock = createBlock(
                label = "Components :",
                desc = components,
                labelColor = "#EC9B21",
                descColor = "#F3C073",
                labelSize = FlexFontSize.SM,
                descSize = FlexFontSize.XS)

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