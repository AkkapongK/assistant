package th.co.dv.p2p.common.utilities

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import th.co.dv.p2p.common.utilities.StringUtility.Companion.isValidCurrency
import th.co.dv.p2p.common.utilities.StringUtility.Companion.removeNewLine
import th.co.dv.p2p.common.utilities.StringUtility.Companion.toHex
import kotlin.test.assertEquals

class StringUtilityTest {

    @Test
    fun `Test isValidCurrency`() {

        // Case Valid currency
        var result = isValidCurrency("THB")
        assertTrue(result)

        // Case Invalid currency
        result = isValidCurrency("X")
        assertFalse(result)

        // Case Currency is null
        result = isValidCurrency("")
        assertFalse(result)

    }

    @Test
    fun `Test toHex`() {

        assertEquals("", null.toHex())
        assertEquals("74657374", "test".toHex())
        assertEquals("21402324255e262a2829214023313233343536373839302b3d", "!@#$%^&*()!@#1234567890+=".toHex())

    }

    @Test
    fun `Test removeNewLine`() {
        assertEquals(null, null.removeNewLine())
        assertEquals("test 12345", "test\r\n12345".removeNewLine())
        assertEquals("test-12345-67890", "test\r12345\n67890".removeNewLine("-"))
    }
}