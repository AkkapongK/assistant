package th.co.dv.p2p.common.utilities

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.base.CaseFormat
import th.co.dv.p2p.common.constants.caseFormatsInvalidLength
import th.co.dv.p2p.common.kafka.models.EventStateModel
import javax.persistence.Column
import kotlin.reflect.KClass

object MapUtility {

    private val mapper = ObjectMapper()
    val jacksonObjectMapperInstance = jacksonObjectMapper()

    /**
     * Method for convert all keys in map by using guava
     *
     * @param [caseFormats] guava CaseFormat execute format from order
     *
     * @return new converted keys Map
     */
    fun <K: Any> Map<String, K?>.convertKeys(caseFormats: List<CaseFormat> = listOf(CaseFormat.UPPER_CAMEL, CaseFormat.LOWER_UNDERSCORE, CaseFormat.LOWER_CAMEL)): Map<String, K?> {

        if (caseFormats.size < 2) throw RuntimeException(caseFormatsInvalidLength)

        var map = this
        for (index in 0..(caseFormats.size - 2)) {
            map = map.map { (key, value) -> caseFormats[index].to(caseFormats[index + 1], key) to value}.toMap()
        }

        return map
    }

    /**
     * Method for convert all keys from field to @Column(name=###)
     *
     * @param [clazz] class that contain @Column
     *
     * @return new converted keys Map
     */
    fun <K: Any, C: Any> Map<String, K?>.convertToColumnName(clazz: KClass<C>): Map<String, K?> {
        val nameToField = clazz.java.declaredFields.mapNotNull { field ->
            field.getAnnotation(Column::class.java)?.name.takeIf { it.isNullOrBlank().not() }?.let { it to field }
        }.toMap()
        return this.map { (key, value) -> (nameToField[key]?.name ?: key) to value }.toMap()
    }

    /**
     * Method for convert map to model entity
     *
     * @param [clazz] class of entity
     *
     * @return entity model
     */
    fun <K: Any, C: Any> Map<String, K?>.toEntityModel(clazz: KClass<C>): C {
        return mapper.convertValue(this.convertToColumnName(clazz).convertKeys(), clazz.java)
    }

    fun eventStateModelMapper(data: String): EventStateModel {
        return jacksonObjectMapperInstance.readValue(data, EventStateModel::class.java)
    }

}