/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package th.co.dv.p2p.common.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer

/**
 * Serialize Jackson JsonNode tree models objects to UTF-8 JSON. Using the tree models allows handling arbitrarily
 * structured data without corresponding Java classes. This serializer also supports Connect schemas.
 */
/**
 * Default constructor needed by Kafka
 */

class JsonSerializer : Serializer<Any> {
    private val objectMapper = jacksonObjectMapper()
    override fun configure(config: Map<String, *>, isKey: Boolean) {}

    override fun serialize(topic: String, data: Any?): ByteArray? {
        if (data == null)
            return null

        try {
            return objectMapper.writeValueAsBytes(data)
        } catch (e: Exception) {
            throw SerializationException("Error serializing JSON message", e)
        }

    }

    override fun close() {}

}