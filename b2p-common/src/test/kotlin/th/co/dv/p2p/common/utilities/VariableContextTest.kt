package th.co.dv.p2p.common.utilities

import junit.framework.TestCase
import org.junit.Test

class VariableContextTest {

    @Test
    fun testVariableContext() {
        // initial
        val initialValue = VariableContext.getCurrentVariable()
        TestCase.assertTrue(initialValue.isEmpty())

        // Update
        VariableContext.setCurrentVariable(listOf("123"))
        var updated = VariableContext.getCurrentVariable()
        TestCase.assertTrue(updated.containsAll(listOf("123")))

        VariableContext.setCurrentVariable(listOf("456"))
        updated = VariableContext.getCurrentVariable()
        TestCase.assertTrue(updated.containsAll(listOf("456")))

        // Clear
        VariableContext.clear()
        val afterClear = VariableContext.getCurrentVariable()
        TestCase.assertTrue(afterClear.isEmpty())
    }
}