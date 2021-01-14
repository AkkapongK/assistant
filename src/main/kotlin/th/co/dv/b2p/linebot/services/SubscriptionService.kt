package th.co.dv.b2p.linebot.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
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

    private val fieldSeparator = "|"
    private val userIdSeparator = ","

    enum class SubscriptionType {
        USER,
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
        if (name == null) throw Exception(INVALID_SUBSCRIBE_NAME)
        return when (type) {
            SubscriptionType.GIT -> {
                Utils.getEnumIgnoreCase<GitService.Service>(name)?.name
            }
            SubscriptionType.USER -> {
                val upperName = name.toUpperCase()
                subscriptionProperties.broadcaster[upperName]?.let { upperName }
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

    fun getAllSubscription() : String {
        val gitStr = "${SubscriptionType.GIT.name}: ${GitService.Service.values().joinToString()}"
        val userStr = "${SubscriptionType.USER.name}: ${subscriptionProperties.broadcaster.keys.joinToString()}"
        return "$gitStr\r\n$userStr"
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
        var path: String? = null,
        var broadcaster: MutableMap<String, String> = mutableMapOf()
)