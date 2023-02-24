package th.co.dv.p2p.common.utilities

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.CaseFormat
import net.corda.core.utilities.Try
import org.junit.Test
import th.co.dv.p2p.common.kafka.models.AllStates
import th.co.dv.p2p.common.kafka.models.EventStateModel
import th.co.dv.p2p.common.utilities.MapUtility.convertKeys
import th.co.dv.p2p.common.utilities.MapUtility.convertToColumnName
import th.co.dv.p2p.common.utilities.MapUtility.eventStateModelMapper
import th.co.dv.p2p.common.utilities.MapUtility.jacksonObjectMapperInstance
import th.co.dv.p2p.common.utilities.MapUtility.toEntityModel
import java.io.Serializable
import javax.persistence.Column
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapUtilityTest {

    @Test
    fun `test convertKeys`() {
        mapOf<String, Any>(
                "test" to "value1",
                "key_test" to "value2",
                "" to "value3",
                "TestTest" to "value4",
                "Test_Test_Test" to "value5",
                "correctTest" to "value6"
        ).convertKeys().let {
            assertEquals(mapOf<String, Any>(
                    "test" to "value1",
                    "keyTest" to "value2",
                    "" to "value3",
                    "testTest" to "value4",
                    "testTestTest" to "value5",
                    "correctTest" to "value6"
            ), it)
        }
    }

    @Test
    fun `test convertToColumnName`() {

        Try.on { emptyMap<String, Any>().convertKeys(listOf(CaseFormat.UPPER_CAMEL)) }.let {
            assertTrue { it.isFailure }
        }

        val mapper = ObjectMapper()
        mapOf<String, Any>(
                    "test_not_match_database" to "value1",
                    "empty_column" to "value2",
                    "normal" to "value3")
                .convertToColumnName(ForTest::class)
                .also {
                    assertEquals(mapOf<String, Any>(
                        "notMatchDatabase" to "value1",
                        "empty_column" to "value2",
                        "normal" to "value3"), it)}
                .convertKeys()
                .also {
                    val forTest = mapper.convertValue(it, ForTest::class.java)
                    assertEquals("value1", forTest.notMatchDatabase)
                    assertEquals("value2", forTest.emptyColumn)
                    assertEquals("value3", forTest.normal)
                    assertEquals(null, forTest.missingFromDatabase)
                }
    }

    @Test
    fun `test toModel`() {
        val mapper = ObjectMapper()
        mapOf<String, Any>(
                "test_not_match_database" to "value1",
                "empty_column" to "value2",
                "normal" to "value3").toEntityModel(ForTest::class)
                .also {
                    val forTest = mapper.convertValue(it, ForTest::class.java)
                    assertEquals("value1", forTest.notMatchDatabase)
                    assertEquals("value2", forTest.emptyColumn)
                    assertEquals("value3", forTest.normal)
                    assertEquals(null, forTest.missingFromDatabase)
                }
    }

    @Test
    fun testEventStateModelMapper() {
        val eventModel = EventStateModel(nextState = AllStates(), relatedServices = listOf())
        val stringValue = jacksonObjectMapperInstance.writeValueAsString(eventModel)
        val result = eventStateModelMapper(stringValue)
        assertEquals(eventModel, result)
    }

    data class ForTest (
            @Column(name = "test_not_match_database") val notMatchDatabase: String? = null,
            @Column val emptyColumn: String? = null,
            val normal: String? = null,
            val missingFromDatabase: String? = null
    ) : Serializable

}