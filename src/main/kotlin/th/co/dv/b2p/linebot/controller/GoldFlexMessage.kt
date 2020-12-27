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
import th.co.dv.b2p.linebot.model.GoldUpdatedModel
import th.co.dv.b2p.linebot.services.GoldService
import th.co.dv.b2p.linebot.utilities.Utils.stringify
import java.time.Instant
import java.util.function.Supplier


class GoldFlexMessage(val goldService: GoldService): Supplier<FlexMessage> {

    enum class Mode(val key: String, val image: String) {
        UPDATE (
                key = "Update",
                image = ""
        ),
        GOLD_SPOT(
                key = "GoldSpot",
                image = "https://i1.wp.com/www.goldaround.com/wp-content/uploads/2020/02/gold_%E0%B8%A3%E0%B8%B2%E0%B8%84%E0%B8%B2%E0%B8%97%E0%B8%AD%E0%B8%87%E0%B8%84%E0%B8%B3_pic_051.jpg?w=700&ssl=1"),
        GOLD(
                key = "99.99%",
                image = "https://www.mitihoon.com/wp-content/uploads/2020/08/Gold-bullion-bullion-Gold-bar-shut.jpg"),
        SILVER(
                key = "Silver",
                image = "https://www.prachachat.net/wp-content/uploads/2020/11/%E0%B8%A0%E0%B8%B2%E0%B8%9E%E0%B8%9B%E0%B8%A3%E0%B8%B0%E0%B8%81%E0%B8%AD%E0%B8%9A%E0%B8%82%E0%B9%88%E0%B8%B2%E0%B8%A7%E0%B9%82%E0%B8%A5%E0%B8%AB%E0%B8%B0%E0%B9%80%E0%B8%87%E0%B8%B4%E0%B8%99-Silver-Silver-Bar.jpg"),
        DOLLAR(
                key = "THB",
                image = "https://www.advisor.ca/wp-content/uploads/sites/5/2018/08/800x600_money_US_earnings_41427604_123RFStockPhoto.jpg")
    }
    /**
     * Method for get updated gold
     */
    private fun getUpdatedItems(): List<GoldUpdatedModel> {
        return goldService.getUpdatedInformation()
    }

    /**
     * Get item by key name
     */
    private fun List<GoldUpdatedModel>.findByMode(mode: Mode): GoldUpdatedModel {
        return this.first { it.name == mode.key }
    }

    /**
     * get updated date
     */
    private fun GoldUpdatedModel.getUpdated(): String {
        if (this.bid.isNullOrEmpty()) return ""
        return Instant.ofEpochMilli(this.bid!!.toLong() * 1000).stringify()

    }


    override fun get(): FlexMessage {

        val allItems = getUpdatedItems()

        val updateDateTime = allItems.findByMode(Mode.UPDATE).getUpdated()
        val goldSpotResult = createBubble(allItems, Mode.GOLD_SPOT, updateDateTime)
        val goldResult = createBubble(allItems, Mode.GOLD, updateDateTime)
        val dollarResult = createBubble(allItems, Mode.DOLLAR, updateDateTime)
        val silverResult = createBubble(allItems, Mode.SILVER, updateDateTime)

        val carousel = Carousel.builder()
                .contents(listOf(
                        goldSpotResult,
                        goldResult,
                        dollarResult,
                        silverResult))
                .build()
        return FlexMessage("Gold updated information", carousel)
    }

    private fun createBubble(allItems: List<GoldUpdatedModel>, mode: Mode, updateDateTime: String): Bubble {
        val heroBlock = createHeroBlock(mode.image)
        val data = allItems.findByMode(mode)

        val bodyBlock = createBodyBlock(
                name = data.name ?: "",
                bid = data.bid ?: "",
                updateDateTime = updateDateTime
        )
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

    private fun createBodyBlock(name: String,
                                bid: String,
                                updateDateTime: String): Box {

        val titleBlock = Text.builder()
                .text(name)
                .wrap(true)
                .weight(Text.TextWeight.BOLD)
                .size(FlexFontSize.XL).build()

        val priceBlock = createBlock("Price :  ", bid, "#1191B7", "#05BBF2")


        val updatedBlock = Text.builder()
                .text("Updated: $updateDateTime")
                .wrap(true)
                .size(FlexFontSize.XXS)
                .margin(FlexMarginSize.MD)
                .color("#FF5551")
                .build()
        val flexComponents = listOf<FlexComponent>(
                titleBlock,
                priceBlock,
                updatedBlock)

        val listComponent = flexComponents.toMutableList()
        return Box.builder()
                .layout(FlexLayout.VERTICAL)
                .spacing(FlexMarginSize.MD)
                .contents(listComponent)
                .build()
    }


}