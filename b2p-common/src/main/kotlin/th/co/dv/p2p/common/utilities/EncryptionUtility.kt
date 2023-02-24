package th.co.dv.p2p.common.utilities

import java.io.IOException
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec

object EncryptionUtility {

    private val PASSWORD = "enfldsgbnlsngdlksdsgm".toCharArray()

    private val SALT = byteArrayOf(0xde.toByte(), 0x33.toByte(), 0x10.toByte(), 0x12.toByte(), 0xde.toByte(), 0x33.toByte(), 0x10.toByte(), 0x12.toByte())

    fun encrypt(source: String, password: String?): String {
        val keySpecPassword = password?.toCharArray() ?: PASSWORD
        return try {
            val keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
            val key = keyFactory.generateSecret(PBEKeySpec(keySpecPassword))
            val pbeCipher = Cipher.getInstance("PBEWithMD5AndDES")
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, PBEParameterSpec(SALT, 20))
            base64Encode(pbeCipher.doFinal(source.toByteArray()))
        } catch (e: Exception) {
            source
        }

    }

    private fun base64Encode(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun decrypt(source: String?, password: String?): String? {
        val keySpecPassword = password?.toCharArray() ?: PASSWORD
        return try {
            val keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
            val key = keyFactory.generateSecret(PBEKeySpec(keySpecPassword))
            val pbeCipher = Cipher.getInstance("PBEWithMD5AndDES")
            pbeCipher.init(Cipher.DECRYPT_MODE, key, PBEParameterSpec(SALT, 20))
            var decryptedData = ""
            if (source != null && source != "") {
                decryptedData = String(pbeCipher.doFinal(base64Decode(source)))
            }
            decryptedData
        } catch (e: Exception) {
            source
        }

    }

    @Throws(IOException::class)
    private fun base64Decode(property: String): ByteArray {
        return Base64.getDecoder().decode(property)
    }

    /**
     * Method for create signature for financing survey
     */
    fun hashSurveySignature(email: String, vendorTaxId: String, key: String) = hashMD5(email + vendorTaxId + key)

    fun hashMD5(source: String): String {
        val sb = StringBuilder()
        val kk = source.toByteArray()
        return try {
            val md = MessageDigest.getInstance("MD5")
            md.reset();
            md.update(kk);
            val hash = md.digest()
            for (b in hash) {
                sb.append(String.format("%02x", b))
            }
            sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalArgumentException(e)
        }
    }
}