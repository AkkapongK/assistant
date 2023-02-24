package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.Try
import java.util.*
import kotlin.test.assertEquals

class EncryptionUtilityTest {

    private val mockString = "2:2021-03-05T14:39:43.206+07:00"
    private val encryptedString = "wlQokU4ewo0F77AQAwY5trzXEsI/ZbLsYGbmaPvDyhk="

    @Test
    fun `Test encrypt`() {

        val encryptionUtility = spyk<EncryptionUtility>()
        // case success
        every { encryptionUtility["base64Encode"](any<ByteArray>()) } returns encryptedString
        var result = Try.on { encryptionUtility.encrypt(mockString, "ZXbkxhDrKi8yV6HU") }
        assert(result.isSuccess)
        assertEquals(encryptedString, result.getOrThrow())

        // case fail
        every { encryptionUtility["base64Encode"](any<ByteArray>()) } throws IllegalArgumentException("Error.")
        result = Try.on { encryptionUtility.encrypt(mockString, "ZXbkxhDrKi8yV6HU") }
        assert(result.isSuccess)
        assertEquals(mockString, result.getOrThrow())
    }

    @Test
    fun `Test decrypt`() {
        val encryptionUtility = spyk<EncryptionUtility>()

        // case success
        var result = Try.on { encryptionUtility.decrypt(encryptedString, "ZXbkxhDrKi8yV6HU") }
        assert(result.isSuccess)
        assertEquals(mockString, result.getOrThrow())

        // case fail
        every { encryptionUtility["base64Decode"](any<String>()) } throws IllegalArgumentException("Error.")
        result = Try.on { encryptionUtility.decrypt(encryptedString, "ZXbkxhDrKi8yV6HU") }
        assert(result.isSuccess)
        assertEquals(encryptedString, result.getOrThrow())
    }

    @Test
    fun `Test base64Encode`() {
        val encryptionUtility = spyk<EncryptionUtility>()

        mockkStatic("java.util.Base64")

        every { Base64.getEncoder().encodeToString(mockString.toByteArray()) } returns encryptedString

        val result = callMethod<EncryptionUtility, String>(encryptionUtility, "base64Encode", mockString.toByteArray())
        assertEquals(encryptedString, result)

        unmockkStatic("java.util.Base64")
    }

    @Test
    fun `Test base64Decode`() {
        val encryptionUtility = spyk<EncryptionUtility>()

        mockkStatic("java.util.Base64")
        val mockResult = mockString.toByteArray()
        every { Base64.getDecoder().decode(mockString) } returns mockResult

        val result = callMethod<EncryptionUtility, ByteArray>(encryptionUtility, "base64Decode", mockString)
        assertEquals(mockResult, result)

        unmockkStatic("java.util.Base64")
    }

    @Test
    fun `test encryptSurveySignature`() {
        val encryptionUtility = spyk<EncryptionUtility>()
        every { encryptionUtility["hashMD5"]("emailvendorkey") } returns "md5"
        val result = encryptionUtility.hashSurveySignature("email", "vendor", "key")
        assertEquals("md5", result)
    }

    @Test
    fun `Test encryptMD5`() {
        val encryptionUtility = spyk<EncryptionUtility>()

        val result = Try.on { encryptionUtility.hashMD5("Hello") }
        assert(result.isSuccess)
        assertEquals("8b1a9953c4611296a827abf8c47804d7", result.getOrThrow())
    }
}