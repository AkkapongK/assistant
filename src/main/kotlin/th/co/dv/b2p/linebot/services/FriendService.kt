package th.co.dv.b2p.linebot.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import th.co.dv.b2p.linebot.model.FriendModel
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

@Service
class FriendService {

    @Autowired
    lateinit var friendProperties: FriendProperties

    private val fieldSeparator = "|"

    /**
     * Method to get all friend list
     */
    fun getAllFriends(): List<FriendModel> {
        return readFriendData()
    }

    /**
     * Method to update or insert friend data to friend list
     */
    fun updateFriend(friend: FriendModel) {
        val allFriends = getAllFriends().toMutableList()
        allFriends.removeIf { it.userId == friend.userId }
        allFriends.add(friend)
        writeFriendData(allFriends)
    }

    /**
     * Method to read friends data from file
     */
    private fun readFriendData(): List<FriendModel> {
        val file = File(friendProperties.path)
        return file.readLines().map {
            val data = it.split(fieldSeparator)
            FriendModel(
                    userId = data[0],
                    name = data[1],
                    squad = data[2]
            )
        }
    }

    /**
     * Method to write friends data to file
     */
    private fun writeFriendData(data: List<FriendModel>) {
        val sortedData = data.sortedBy { it.name }
        val file = File(friendProperties.path)
        val newLine= System.getProperty("line.separator")
        var fileWrite: FileWriter? = null
        var bufferedWriter: BufferedWriter? = null
        try {
            fileWrite = FileWriter(file)
            bufferedWriter = BufferedWriter(fileWrite)
            sortedData.forEach {
                bufferedWriter.write(it.toFileFormat() + newLine)
            }
        } finally {
            bufferedWriter?.close()
            fileWrite?.close()
        }
    }

    private fun FriendModel.toFileFormat(): String {
        return "${this.userId}${fieldSeparator}${this.name}${fieldSeparator}${this.squad}"
    }
}

@Configuration
@ConfigurationProperties("friend")
data class FriendProperties(
        var path: String? = null
)