package th.co.dv.p2p.common.utilities

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MapContextHolderTest {

    @Test
    fun testSetMap() {

        MapContextHolder.clear()

        val key ="key"
        val value = "value"
        assertNull(MapContextHolder.getValue(key))

        // Set first key
        MapContextHolder.setMap(key, value)

        assertNotNull(MapContextHolder.getValue(key))
        assertEquals(value, MapContextHolder.getValue(key))

        // Set another key
        val key2 ="key2"
        val value2 = "value2"
        MapContextHolder.setMap(key2, value2)

        assertNotNull(MapContextHolder.getValue(key2))
        assertEquals(value2, MapContextHolder.getValue(key2))

        MapContextHolder.clear()

    }

    @Test
    fun testGetValue() {

        MapContextHolder.clear()

        val key ="key"
        val value = "value"

        // Case no map return null
        var result = MapContextHolder.getValue(key)
        assertNull(result)

        MapContextHolder.setMap(key, value)

        // Case find key
        result = MapContextHolder.getValue(key)
        assertNotNull(result)
        assertEquals(value, result)

        // Case cannot find key
        result = MapContextHolder.getValue(key + "2")
        assertNull(result)

        MapContextHolder.clear()

    }

    @Test
    fun testClear() {
        MapContextHolder.clear()

        val key ="key"
        val value = "value"
        MapContextHolder.setMap(key, value)
        assertNotNull(MapContextHolder.getValue(key))

        MapContextHolder.clear()
        assertNull(MapContextHolder.getValue(key))
    }

}