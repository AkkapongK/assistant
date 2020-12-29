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
import th.co.dv.b2p.linebot.model.BitCoinSymbolInfoModel
import th.co.dv.b2p.linebot.services.BitCoinService
import java.util.function.Supplier


class BitCoinFlexMessage(val bitCoinService: BitCoinService, val symbol: String): Supplier<FlexMessage> {

    private val bitcoinImg = "https://images.theconversation.com/files/194266/original/file-20171113-27585-1gdvg8x.jpg?ixlib=rb-1.1.0&rect=0%2C127%2C997%2C498&q=45&auto=format&w=1356&h=668&fit=crop"

    /**
     * Method for get updated gold
     */
    private fun getSymbolInfo(symbol: String): BitCoinSymbolInfoModel {
        val symbols = bitCoinService.getInfoBySymbol(symbol)
        return when (symbols.size < 1) {
            true -> BitCoinSymbolInfoModel()
            false -> {
                val symbolInfo = symbols.first()
                // Get Info
                val available= bitCoinService.getAvailable()
                val info = available
                        .find { it.symbol == symbolInfo.symbol }?.info
                symbolInfo.info = info
                symbolInfo
            }
        }
    }


    override fun get(): FlexMessage {

        val symbolInfo = getSymbolInfo(symbol)

        val symbolBubble = createBubble(symbolInfo)

        val carousel = Carousel.builder()
                .contents(listOf(symbolBubble))
                .build()
        return FlexMessage("Bitcoin information", carousel)
    }

    private fun createBubble(symbolInfo: BitCoinSymbolInfoModel): Bubble {
        val heroBlock = createHeroBlock(bitcoinImg)

        val bodyBlock = createBodyBlock(symbolInfo)
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
                                .margin(FlexMarginSize.SM)
                                .size(descSize)
                                .flex(0)
                                .build()
                )).build()
    }

    /**
     * "id": 22,
    "last": 2.19,
    "lowestAsk": 2.19,
    "highestBidhighestBid": 2.17,
    "percentChange": 3.3,
    "baseVolume": 292493.25235074,
    "quoteVolume": 637390.17,
    "isFrozen": 0,
    "high24hr": 2.22,
    "low24hr": 2.1,
    "change": 0.07,
    "prevClose": 2.19,
    "prevOpen": 2.19
     */
    private fun createBodyBlock(symbolInfo: BitCoinSymbolInfoModel): Box {

        val titleBlock = Text.builder()
                .text(symbolInfo.symbol)
                .wrap(true)
                .weight(Text.TextWeight.BOLD)
                .size(FlexFontSize.XL).build()

        val infoBlock = createBlock("Info :", symbolInfo.info?: "")
        val lastBlock = createBlock("Last :", symbolInfo.last?.toString() ?: "")
        val lowestAskBlock = createBlock("Lowest ask :", symbolInfo.lowestAsk?.toString() ?: "")
        val highestBidBlock = createBlock("Highest bid :", symbolInfo.highestBid?.toString() ?: "")
        val percentChangeBlock = createBlock("Percent change :", symbolInfo.percentChange?.toString() ?: "")
        val baseVolumeBlock = createBlock("Base volume :", symbolInfo.baseVolume?.toString() ?: "")
        val prevCloseBlock = createBlock("Prev close :", symbolInfo.prevClose?.toString() ?: "")
        val prevOpenBlock = createBlock("Prev open :", symbolInfo.prevOpen?.toString() ?: "")
        val isFrozenBlock = createBlock("Status :", "** Frozen **", "#706C6C", "#D0312D")

        val flexComponents = listOf<FlexComponent>(
                titleBlock,
                infoBlock,
                lastBlock,
                lowestAskBlock,
                highestBidBlock,
                percentChangeBlock,
                baseVolumeBlock,
                prevCloseBlock,
                prevOpenBlock)



        val listComponent = flexComponents.toMutableList()
        if (symbolInfo.isFrozen == 1) listComponent.add(isFrozenBlock)
        return Box.builder()
                .layout(FlexLayout.VERTICAL)
                .spacing(FlexMarginSize.MD)
                .contents(listComponent)
                .build()
    }


}