package th.co.dv.p2p.common.utilities.search

import org.junit.Test
import th.co.dv.p2p.common.constants.escapeChar
import th.co.dv.p2p.common.utilities.manager.MockPurchaseOrder
import kotlin.reflect.jvm.javaField
import kotlin.test.assertEquals

class SearchCriteriaTest {

    @Test
    fun `Test getColumnName`() {
        val result = MockPurchaseOrder::accounting.javaField!!.getColumnName()
        assertEquals("accounting", result)
    }

    @Test
    fun testEscapeLikeOperation() {

        val input = "TEST\\ESCAPE_%SQL'"
        // Case input is null
        var result = escapeLikeOperation(null)
        assertEquals(null, result)

        // Case input is blank
        result = escapeLikeOperation("")
        assertEquals("", result)

        // Case input is blank
        result = escapeLikeOperation(input)
        assertEquals("TEST${escapeChar}${escapeChar}ESCAPE${escapeChar}_${escapeChar}%SQL${escapeChar}'", result)

    }

    @Test
    fun `Test getFullColumnName`() {
        val result = MockPurchaseOrder::accounting.javaField!!.getFullColumnName()
        assertEquals("mock_purchase_order.accounting", result)
    }
}