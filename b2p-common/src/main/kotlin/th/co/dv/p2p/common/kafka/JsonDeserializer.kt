package th.co.dv.p2p.common.kafka

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import th.co.dv.p2p.common.base.enums.MessageType
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.common.utilities.enumValueOrNull

class JsonDeserializer<T>(private val clazz: Class<T>) : Deserializer<StreamingModel<T>> {
    private val objectMapper = jacksonObjectMapper()

    override fun configure(props: Map<String, *>, isKey: Boolean) {
        /**
         * Not implement yet
         */
    }

    override fun deserialize(topic: String, bytes: ByteArray?): StreamingModel<T>? {
        if (bytes == null)
            return null

        val node: JsonNode
        return try {
            node = objectMapper.readTree(bytes)
            val sponsor = node.get("sponsor").asText()
            val id = node.get("id").asText()
            val command = node.get("command").asText()
            val type = node.get("type").asText()
            val redisKey = node.get("redisKey")?.asText()
            val message = node.get("message")?.asText()
            val relatedServices = objectMapper.convertValue<List<String>>(node["relatedServices"])
            val messageType = objectMapper.convertValue(node["messageType"], MessageType::class.java)
            val data = objectMapper.convertValue(node["data"], clazz)
            StreamingModel(
                    id = id,
                    command = command,
                    type = type,
                    data = data,
                    sponsor = sponsor,
                    messageType = messageType,
                    relatedServices = relatedServices.mapNotNull { enumValueOrNull<Services>(it, true) },
                    redisKey = redisKey,
                    message = message
            )
        } catch (e: Exception) {
            throw SerializationException(e)
        }
    }

    override fun close() {
        /**
         * Not implement yet
         */
    }
}