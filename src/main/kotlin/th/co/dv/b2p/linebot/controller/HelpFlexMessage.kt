package th.co.dv.b2p.linebot.controller

import com.linecorp.bot.model.message.FlexMessage
import com.linecorp.bot.model.message.flex.component.Box
import com.linecorp.bot.model.message.flex.component.FlexComponent
import com.linecorp.bot.model.message.flex.component.Image
import com.linecorp.bot.model.message.flex.component.Text
import com.linecorp.bot.model.message.flex.container.Bubble
import com.linecorp.bot.model.message.flex.container.Carousel
import com.linecorp.bot.model.message.flex.unit.FlexFontSize
import com.linecorp.bot.model.message.flex.unit.FlexLayout
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize
import th.co.dv.b2p.linebot.utilities.Utils.convertToString
import java.util.function.Supplier

class HelpFlexMessage : Supplier<FlexMessage> {

    enum class Document(val image: String, val description: String, val args: List<String>? = null, val usages: List<String>? = null) {
        RELEASE(
                image = "https://miro.medium.com/max/600/1*XFYOE5-2A5WMHhBDPLgQiw.jpeg",
                description = "Get current release branch in git",
                args = listOf("service name:  inv, po, gr, payment, cn, dn, request, aggregate, common, " +
                        "commonpuksa, commonirpc, commonscg, commonsnp, commonmint"),
                usages = listOf("release inv: Show branch of invoice service")
        ),
        JIRA(
                image = "https://marketplacelive.blob.core.windows.net/solution-logo/47dda901f2fa40c1a9c983b1a352e8fb.png",
                description = "Track information of target story",
                args = listOf(
                        "Mode: story or sprint",
                        "Value: bloc-xxx for story or b2p-xx for sprint"),
                usages = listOf(
                        "jira story bloc-16000: Show information of bloc 16000",
                        "jira sprint b2p-60: Show information of sprint 60")),
        COVID(
                image = "https://d18lkz4dllo6v2.cloudfront.net/cumulus_uploads/entry/2020-04-01/COVID%20Getty%20Image.jpg?w=660",
                description = "Show updated covid information in Thailand"),
        GOLD(
                image = "https://responsive.fxempire.com/v7/_fxempire_/2016/01/gold-stack-forexwords.jpg?func=cover&q70&width=615",
                description = "Show updated gold, THB and silver price"),
        BITCOIN(
                image = "https://images.theconversation.com/files/194266/original/file-20171113-27585-1gdvg8x.jpg?ixlib=rb-1.1.0&rect=0%2C127%2C997%2C498&q=45&auto=format&w=1356&h=668&fit=crop",
                description = "Show Bitcoin information",
                args = listOf("Target symbol : THB_BTC"),
                usages = listOf(
                        "bitcoin : Show available symbol",
                        "bitcoin [symbol] : Show symbol information")),
        PLAN(
                image = "https://project-management.com/wp-content/uploads/2017/01/Project-Management-Plan.jpeg",
                description = "Show start date(expected) for each release",
                args = listOf(
                        "Project: b2p",
                        "Environment: pdt, mgt, sit, uat, qa, deploy",
                        "Target release: 9.2.3 (optional)"),
                usages = listOf(
                        "plan b2p deploy: Show expected deploy date for all release",
                        "plan b2p deploy 9.2.3: Show expected deploy date for release 9.2.3"
                )),

    }

    override fun get(): FlexMessage {

        val releaseBubble = createBubble(Document.RELEASE)
        val jiraBubble = createBubble(Document.JIRA)
        val covidBubble = createBubble(Document.COVID)
        val bitcoinBubble = createBubble(Document.BITCOIN)
        val goldBubble = createBubble(Document.GOLD)
        val planBubble = createBubble(Document.PLAN)

        val carousel = Carousel.builder()
                .contents(listOf(
                        releaseBubble,
                        jiraBubble,
                        covidBubble,
                        bitcoinBubble,
                        goldBubble,
                        planBubble))
                .build()
        return FlexMessage("Document information", carousel)
    }

    private fun createBubble(mode: Document): Bubble {
        val heroBlock = createHeroBlock(mode.image)


        val bodyBlock = createBodyBlock(mode = mode)

        return Bubble.builder()
                .hero(heroBlock)
                .body(bodyBlock)
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
            labelSize: FlexFontSize = FlexFontSize.SM,
            descSize: FlexFontSize = FlexFontSize.SM) : Box {
        return Box.builder()
                .layout(FlexLayout.BASELINE)
                .contents(listOf(
                        Text.builder().text(label)
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
                                //.flex(0)
                                .build()
                )).build()
    }

    private fun createBodyBlock(mode: Document): Box {

        val title = mode.name
        val desc = mode.description
        val args = mode.args
        val usage = mode.usages

        val titleBlock = Text.builder()
                .text(title)
                .wrap(true)
                .weight(Text.TextWeight.BOLD)
                .size(FlexFontSize.XL).build()

        val descBlock = createBlock("Desc :", desc)

        val flexComponents = mutableListOf<FlexComponent>(
                titleBlock,
                descBlock)

        args?.let { flexComponents.add(createBlock("Args :", it.convertToString("*"))) }
        usage?.let { flexComponents.add(createBlock("Usages :", it.convertToString("*"))) }

        val listComponent = flexComponents.toMutableList()
        return Box.builder()
                .layout(FlexLayout.VERTICAL)
                .spacing(FlexMarginSize.MD)
                .contents(listComponent)
                .build()
    }

}