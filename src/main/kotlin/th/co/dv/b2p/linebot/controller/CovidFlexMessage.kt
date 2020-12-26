package th.co.dv.b2p.linebot.controller

import com.linecorp.bot.model.message.FlexMessage
import com.linecorp.bot.model.message.flex.component.Box
import com.linecorp.bot.model.message.flex.component.Image
import com.linecorp.bot.model.message.flex.component.Separator
import com.linecorp.bot.model.message.flex.component.Text
import com.linecorp.bot.model.message.flex.container.Bubble
import com.linecorp.bot.model.message.flex.unit.FlexFontSize
import com.linecorp.bot.model.message.flex.unit.FlexGravity
import com.linecorp.bot.model.message.flex.unit.FlexLayout
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize
import org.springframework.stereotype.Service
import th.co.dv.b2p.linebot.model.CovidUpdatedModel
import th.co.dv.b2p.linebot.services.CovidService
import java.util.function.Supplier

@Service
class CovidFlexMessage(val covidService: CovidService) : Supplier<FlexMessage> {

    private val image = "https://d18lkz4dllo6v2.cloudfront.net/cumulus_uploads/entry/2020-04-01/COVID%20Getty%20Image.jpg?w=660"

    override fun get(): FlexMessage {
        val covidUpdatedModel = covidService.getUpdatedInformation()

        val headerBlock = createHeaderBlock(covidUpdatedModel.UpdateDate)
        val heroBlock = createHeroBlock()
        val bodyBlock = createBodyBlock(covidUpdatedModel)
        val bubble = Bubble.builder()
                .header(headerBlock)
                .hero(heroBlock)
                .body(bodyBlock)
//                .footer(footerBlock)
                .build()
        return FlexMessage("Covid updated", bubble)
    }

    private fun createHeaderBlock(updateDate: String?): Box {
        return Box.builder()
                .layout(FlexLayout.HORIZONTAL)
                .contents(listOf(
                        Text.builder()
                                .text("Covid updated date: ${updateDate ?: ""}")
                                .weight(Text.TextWeight.BOLD)
                                .wrap(true)
                                .color("#aaaaaa")
                                .size(FlexFontSize.SM).build()
                )).build()
    }

    private fun createHeroBlock(): Image {
        return Image.builder()
                .url(image)
                .size(Image.ImageSize.FULL_WIDTH)
                .aspectRatio(Image.ImageAspectRatio.R20TO13)
                .aspectMode(Image.ImageAspectMode.Cover)
                .build()
    }

    private fun createBodyBlock(covidUpdatedModel: CovidUpdatedModel): Box {
        val heightLightBlock = createNewsBlock(covidUpdatedModel)
        return Box.builder()
                .layout(FlexLayout.HORIZONTAL)
                .spacing(FlexMarginSize.MD)
                .contents(listOf(heightLightBlock))
                .build()
    }

//    private fun createThumbnailsBox(): Box {
//        val imagesContent1 = Image.builder()
//                .url("https://2553d2b9.ngrok.io/img/thumbnail1.png")
//                .aspectMode(Image.ImageAspectMode.Cover)
//                .aspectRatio(Image.ImageAspectRatio.R4TO3)
//                .size(Image.ImageSize.SM)
//                .gravity(FlexGravity.BOTTOM)
//                .build()
//        val imagesContent2 = Image.builder()
//                .url("https://2553d2b9.ngrok.io/img/thumbnail2.png")
//                .aspectMode(Image.ImageAspectMode.Cover)
//                .aspectRatio(Image.ImageAspectRatio.R4TO3)
//                .size(Image.ImageSize.SM)
//                .margin(FlexMarginSize.MD)
//                .build()
//        return Box.builder()
//                .layout(FlexLayout.VERTICAL)
//                .flex(1)
//                .contents(asList(imagesContent1, imagesContent2))
//                .build()
//    }

    private fun createNewsBlock(covidUpdatedModel: CovidUpdatedModel): Box {
        val separator = Separator.builder().build()
        return Box.builder()
                .layout(FlexLayout.VERTICAL)
                .flex(2)
                .contents(listOf(
                        Text.builder()
                                .text("New confirmed : ${covidUpdatedModel.NewConfirmed}")
                                .gravity(FlexGravity.TOP)
                                .size(FlexFontSize.XS)
                                .weight(Text.TextWeight.BOLD)
                                .flex(1)
                                .build(),
                        separator,
                        Text.builder()
                                .text("New deaths : ${covidUpdatedModel.NewDeaths}")
                                .gravity(FlexGravity.CENTER)
                                .size(FlexFontSize.XS)
                                .weight(Text.TextWeight.BOLD)
                                .color("#D2000E")
                                .flex(2)
                                .build(),
                        separator,
                        Text.builder()
                                .text("Confirmed : ${covidUpdatedModel.Confirmed}")
                                .gravity(FlexGravity.CENTER)
                                .size(FlexFontSize.XS)
                                .color("#21BAE8")
                                .flex(2)
                                .build(),
                        separator,
                        Text.builder()
                                .text("Hospitalized : ${covidUpdatedModel.Hospitalized}")
                                .gravity(FlexGravity.CENTER)
                                .size(FlexFontSize.XS)
                                .color("#EFE44A")
                                .flex(2)
                                .build(),
                        separator,
                        Text.builder()
                                .text("Recovered : ${covidUpdatedModel.Recovered}")
                                .gravity(FlexGravity.BOTTOM)
                                .size(FlexFontSize.XS)
                                .color("#54E158")
                                .flex(1)
                                .build()
                ))
                .build()
    }

//    private fun createFooterBlock(): Box {
//        return Box.builder()
//                .layout(FlexLayout.HORIZONTAL)
//                .contents(asList(
//                        Button.builder()
//                                .action(URIAction("more", "https://example.com"))
//                                .build()
//                )).build()
//    }
}