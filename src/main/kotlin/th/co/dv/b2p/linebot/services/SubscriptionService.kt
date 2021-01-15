package th.co.dv.b2p.linebot.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import th.co.dv.b2p.linebot.config.LineConfiguration
import th.co.dv.b2p.linebot.constant.Constant.COMMA
import th.co.dv.b2p.linebot.constant.INVALID_SUBSCRIBE_NAME
import th.co.dv.b2p.linebot.constant.INVALID_SUBSCRIBE_TYPE
import th.co.dv.b2p.linebot.model.SubscriptionModel
import th.co.dv.b2p.linebot.utilities.Utils
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

@Service
class SubscriptionService {

    @Autowired
    lateinit var subscriptionProperties: SubscriptionProperties

    @Autowired
    lateinit var lineService: LineService

    private val fieldSeparator = "|"
    private val userIdSeparator = ","

    enum class SubscriptionType {
        BROADCASTER,
        GIT
    }

    fun readSubscriptionData(): List<SubscriptionModel> {
        val file = File(subscriptionProperties.path)
        return file.readLines().map {
            val data = it.split(fieldSeparator)
            SubscriptionModel(
                    type = data[0],
                    name = data[1],
                    userIds = data[2].split(userIdSeparator).filter { it.isNullOrBlank().not() }
            )
        }
    }

    private fun writeSubscriptionData(data: List<SubscriptionModel>) {
        val file = File(subscriptionProperties.path)
        val newLine= System.getProperty("line.separator")
        var fileWrite: FileWriter? = null
        var bufferedWriter: BufferedWriter? = null
        try {
            fileWrite = FileWriter(file)
            bufferedWriter = BufferedWriter(fileWrite)
            data.forEach {
                bufferedWriter.write(it.toFileFormat() + newLine)
            }
        } finally {
            bufferedWriter?.close()
            fileWrite?.close()
        }

    }

    private fun SubscriptionModel.toFileFormat(): String {
        return "${this.type}${fieldSeparator}${this.name}${fieldSeparator}${this.userIds.joinToString(userIdSeparator)}"
    }

    private fun getSubscriptionName(type: SubscriptionType, name: String?): String {
        return when (type) {
            SubscriptionType.GIT -> {
                if (name == null) throw Exception(INVALID_SUBSCRIBE_NAME)
                Utils.getEnumIgnoreCase<GitService.Service>(name)?.name
            }
            SubscriptionType.BROADCASTER -> {
                SubscriptionType.BROADCASTER.name
            }
        } ?: throw Exception(INVALID_SUBSCRIBE_NAME)
    }

    fun doSubscribe(userId: String, args: List<String>) : String {

        val subscriptionType = Utils.getEnumIgnoreCase<SubscriptionType>(args[0]) ?: throw Exception(INVALID_SUBSCRIBE_TYPE)
        val subscriptionName = getSubscriptionName(subscriptionType, args.getOrNull(1))

        val allSubscriptionData = readSubscriptionData().toMutableList()

        val subscriptionData = allSubscriptionData.find { it.type == subscriptionType.name && it.name == subscriptionName }
                ?: SubscriptionModel(type = subscriptionType.name, name = subscriptionName)
        allSubscriptionData.remove(subscriptionData)

        val newUserIdList = subscriptionData.userIds.plus(userId).distinct()

        val finalSubscriptionData = subscriptionData.copy(userIds = newUserIdList)
        allSubscriptionData.add(finalSubscriptionData)
        allSubscriptionData.sortBy { "${it.type}${fieldSeparator}${it.name}" }

        writeSubscriptionData(allSubscriptionData)

        return "Subscribe success: ${subscriptionType.name} $subscriptionName"
    }

    fun doUnsubscribe(userId: String, args: List<String>) : String {

        val subscriptionType = Utils.getEnumIgnoreCase<SubscriptionType>(args[0]) ?: throw RuntimeException(INVALID_SUBSCRIBE_TYPE)
        val subscriptionName = getSubscriptionName(subscriptionType, args.getOrNull(1))

        val allSubscriptionData = readSubscriptionData().toMutableList()

        val subscriptionData = allSubscriptionData.find { it.type == subscriptionType.name && it.name == subscriptionName }
                ?: SubscriptionModel(type = subscriptionType.name, name = subscriptionName)
        allSubscriptionData.remove(subscriptionData)

        val newUserIdList = subscriptionData.userIds.filterNot { it == userId }

        val finalSubscriptionData = subscriptionData.copy(userIds = newUserIdList)
        allSubscriptionData.add(finalSubscriptionData)
        allSubscriptionData.sortBy { "${it.type}${fieldSeparator}${it.name}" }

        writeSubscriptionData(allSubscriptionData)

        return "Unsubscribe success: ${subscriptionType.name} $subscriptionName"
    }

    fun getMySubscription(userId: String) : String {

        val allSubscriptionData = readSubscriptionData().toMutableList()
        val data = allSubscriptionData.filterByUserId(userId)
        return data.formatToString()
    }

    fun getAllSubscriptionType() : String {
        val gitStr = "${SubscriptionType.GIT.name}: ${GitService.Service.values().joinToString()}"
        val userStr = SubscriptionType.BROADCASTER.name
        return "- $userStr\r\n- $gitStr"
    }

    /**
     * Methof to get all user id that subscribe this broadcaster user
     */
    fun getBroadcastSubscription(userId: String) : List<String> {
        if (lineService.haveBroadcastPermission(userId).not()) return emptyList()
        val subscriptionData = readSubscriptionData()
        val broadcasterSubscription = subscriptionData.find {
            it.type == SubscriptionType.BROADCASTER.name && it.name == SubscriptionType.BROADCASTER.name
        }
        return broadcasterSubscription?.userIds ?: emptyList()

    }

    fun List<SubscriptionModel>.formatToString(): String {
        return this.groupBy { it.type }.map { (type, data) ->
            val dataStr = data.map { it.name }.joinToString()
            "$type: $dataStr"
        }.joinToString("\r\n")
    }

    fun List<SubscriptionModel>.filterByUserId(userId: String): List<SubscriptionModel> {
        return this.filter { it.userIds.contains(userId) }
    }
}

@Configuration
@ConfigurationProperties("subscription")
data class SubscriptionProperties(
        var path: String? = null
)