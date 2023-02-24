package th.co.dv.p2p.common.validators

import org.junit.Assert.assertFalse
import org.junit.Test
import th.co.dv.p2p.common.validators.AccountNumberValidator.validateAccountNumber
import kotlin.test.assertTrue


class AccountNumberValidatorTest {

    @Test
    fun `test ValidateAccountNumber`() {
        var result = validateAccountNumber("bankAccountNumber", "039")
        assertTrue(result)

        result = validateAccountNumber("bankAccountNumber", "079")
        assertTrue(result)

        result = validateAccountNumber("12345", "123")
        assertTrue(result)

        result = validateAccountNumber("bankAccountNumber", "bankCode")
        assertFalse(result)

        result = validateAccountNumber("123a45", "123")
        assertFalse(result)

        result = validateAccountNumber("abcdefg", "123")
        assertFalse(result)
    }

}