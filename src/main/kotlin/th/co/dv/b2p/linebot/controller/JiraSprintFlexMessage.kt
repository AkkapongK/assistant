package th.co.dv.b2p.linebot.controller

import com.linecorp.bot.model.message.FlexMessage
import com.linecorp.bot.model.message.flex.component.*
import com.linecorp.bot.model.message.flex.container.Bubble
import com.linecorp.bot.model.message.flex.container.Carousel
import com.linecorp.bot.model.message.flex.unit.FlexFontSize
import com.linecorp.bot.model.message.flex.unit.FlexLayout
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize
import th.co.dv.b2p.linebot.model.IssueJiraModel
import java.util.function.Supplier

class JiraSprintFlexMessage(val data: List<IssueJiraModel>): Supplier<FlexMessage> {

    enum class Team(val image: String, val id: String) {
        GODZILLA(
                id = "10562",
                image = "https://www.denofgeek.com/wp-content/uploads/2019/05/godzillakingofmonsters-2.jpg?resize=768%2C432"),
        YODA(
                id = "10563",
                image = "https://observer.com/wp-content/uploads/sites/2/2020/05/yoda-art-observer.jpg"),
        EEVEE(
                id = "10657",
                image = "https://thumbor.forbes.com/thumbor/960x0/https%3A%2F%2Fblogs-images.forbes.com%2Fdavidthier%2Ffiles%2F2019%2F07%2F1200px-Ilima_Eevee-1200x675.jpg"),
        ROCKMAN(
                id = "10564",
                image = "https://image.appdisqus.com/wp-content/webp-express/webp-images/uploads/2018/05/rockman11-1.jpg.webp")

    }

    enum class Type(val id: String) {
        STORY("10001"),
        BUG("10004")
    }

    // TODO: not define all status yet
    enum class StoryStatus(val id: List<String>) {
        TOTAL(listOf()),
        DEV(listOf("10000", "10002", "10085")),
        CODE_REVIEW(listOf("10031")),
        DEPLOY(listOf("10013", "10033")),
        TEST(listOf("10006", "10032", "10061")),
        DONE(listOf("10001"))
    }
    override fun get(): FlexMessage {

        val bubbles = mutableListOf<Bubble>()

        createBubble(Team.GODZILLA)?.let { bubbles.add(it) }
        createBubble(Team.YODA)?.let { bubbles.add(it) }
        createBubble(Team.EEVEE)?.let { bubbles.add(it) }
        createBubble(Team.ROCKMAN)?.let { bubbles.add(it) }

        val carousel = Carousel.builder()
                .contents(bubbles)
                .build()
        return FlexMessage("Jira sprint information", carousel)

    }

    /**
     * Filter data by team
     */
    private fun filterDataByTeam(team: Team): List<IssueJiraModel> {
        return data.filter {
            it.fields?.customfield_10227?.id == team.id &&
                    it.fields?.issuetype?.id in listOf(Type.BUG.id, Type.STORY.id)
        }
    }

    private fun createBubble(team: Team): Bubble? {
        val heroBlock = createHeroBlock(team.image)
        val eligibleData = filterDataByTeam(team)
        println(">>>>>>>>> ${team.name} : ${eligibleData.size}")
        if (team == Team.ROCKMAN) println("ROCKMAN >>>>>>>>> $eligibleData")
        if (eligibleData.isEmpty()) return null

        val bodyBlock = createBodyBlock(
                team = team,
                eligibleData = eligibleData)
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
                                .margin(FlexMarginSize.SM)
                                .color(descColor)
                                .size(descSize)
                                .flex(0)
                                .build()
                )).build()
    }

    private fun List<IssueJiraModel>.getTotalByTypeAndMode(type: Type, mode: StoryStatus): String {
        val eligibleData = this.filter {
            val filed = it.fields!!
            filed.issuetype?.id == type.id
        }

        return when (mode) {
            StoryStatus.DEV -> eligibleData.filter { it.fields?.status?.id in StoryStatus.DEV.id }.size
            StoryStatus.CODE_REVIEW -> eligibleData.filter { it.fields?.status?.id in StoryStatus.CODE_REVIEW.id }.size
            StoryStatus.DEPLOY -> eligibleData.filter { it.fields?.status?.id in StoryStatus.DEPLOY.id }.size
            StoryStatus.TEST -> eligibleData.filter { it.fields?.status?.id in StoryStatus.TEST.id }.size
            StoryStatus.DONE -> eligibleData.filter { it.fields?.status?.id in StoryStatus.DONE.id }.size
            StoryStatus.TOTAL -> eligibleData.size
        }.toString()
    }

    private fun createBodyBlock(team: Team, eligibleData: List<IssueJiraModel>): Box {
        val teamName = eligibleData.first().fields?.customfield_10227?.value ?: team.name
        val titleBlock = Text.builder()
                .text(teamName)
                .wrap(true)
                .weight(Text.TextWeight.BOLD)
                .size(FlexFontSize.XL).build()

        val totalStoryBlock = createBlock("Total stories :", eligibleData.getTotalByTypeAndMode(Type.STORY, StoryStatus.TOTAL))
        val devStoryBlock = createBlock("Dev stories :", eligibleData.getTotalByTypeAndMode(Type.STORY, StoryStatus.DEV))
        val codeReviewStoryBlock = createBlock("Code review stories :", eligibleData.getTotalByTypeAndMode(Type.STORY, StoryStatus.CODE_REVIEW))
        val deployStoryBlock = createBlock("Deploy stories :", eligibleData.getTotalByTypeAndMode(Type.STORY, StoryStatus.DEPLOY))
        val testStoryBlock = createBlock("Test stories :", eligibleData.getTotalByTypeAndMode(Type.STORY, StoryStatus.TEST))
        val doneStoryBlock = createBlock("Done stories :", eligibleData.getTotalByTypeAndMode(Type.STORY, StoryStatus.DONE))

        val totalBugBlock = createBlock("Total bugs :", eligibleData.getTotalByTypeAndMode(Type.BUG, StoryStatus.TOTAL))
        val devBugBlock = createBlock("Dev bugs :", eligibleData.getTotalByTypeAndMode(Type.BUG, StoryStatus.DEV))
        val codeReviewBugBlock = createBlock("Code review bugs :", eligibleData.getTotalByTypeAndMode(Type.BUG, StoryStatus.CODE_REVIEW))
        val deployBugBlock = createBlock("Deploy bugs :", eligibleData.getTotalByTypeAndMode(Type.BUG, StoryStatus.DEPLOY))
        val testBugBlock = createBlock("Test bugs :", eligibleData.getTotalByTypeAndMode(Type.BUG, StoryStatus.TEST))
        val doneBugBlock = createBlock("Done bugs :", eligibleData.getTotalByTypeAndMode(Type.BUG, StoryStatus.DONE))

        val separator = Separator.builder().build()
        val flexComponents = listOf<FlexComponent>(
                titleBlock,
                totalStoryBlock,
                devStoryBlock,
                codeReviewStoryBlock,
                deployStoryBlock,
                testStoryBlock,
                doneStoryBlock,

                separator,

                totalBugBlock,
                devBugBlock,
                codeReviewBugBlock,
                deployBugBlock,
                testBugBlock,
                doneBugBlock)
        val listComponent = flexComponents.toMutableList()
        return Box.builder()
                .layout(FlexLayout.VERTICAL)
                .spacing(FlexMarginSize.SM)
                .contents(listComponent)
                .build()
    }
}