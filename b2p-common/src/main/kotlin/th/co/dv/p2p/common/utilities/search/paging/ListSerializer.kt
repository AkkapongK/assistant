package th.co.dv.p2p.common.utilities.search.paging

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException

class ListSerializer<T>: JsonSerializer<List<T>>() {

    private val serialVersionUID = 9024326729987814606L

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
            value: List<T>, jgen: JsonGenerator, provider: SerializerProvider) {

        if (value is PagableList<T>) {
            jgen.writeStartObject()
            jgen.writeObjectField("rows", value.getData())
            jgen.writeNumberField("page", value.getPage())
            jgen.writeNumberField("pageSize", value.getPageSize())
            jgen.writeNumberField("totalRecords", value.getTotalSize())
            jgen.writeEndObject()
        } else {
            jgen.writeStartArray()
            for (obj in value)
                jgen.writeObject(obj)
            jgen.writeEndArray()
        }
    }
}