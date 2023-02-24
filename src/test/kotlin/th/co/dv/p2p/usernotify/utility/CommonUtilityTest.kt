package th.co.dv.p2p.usernotify.utility

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.aspectj.weaver.patterns.TypePatternQuestions.Question
import org.json.JSONObject
import org.junit.Test
import th.co.dv.p2p.common.models.UserAuthorization
import th.co.dv.p2p.common.utilities.*
import th.co.dv.p2p.usernotify.utility.CommonUtility.completeRequestParameter
import th.co.dv.p2p.usernotify.utility.CommonUtility.getUsernameFromSecurityContext
import th.co.dv.p2p.usernotify.utility.web.RequestUtility

class CommonUtilityTest {

    @Test
    fun testCompleteRequestParameter() {
        val paramsInput = mapOf(
                "key1" to "value1",
                "key2" to 10.0.toBigDecimal(),
                "key3" to listOf("A", "B", "C"),
                "key4" to null)

        val result = paramsInput.completeRequestParameter()

        // Assert value
        assertEquals(3, result.keys.size)
        assertTrue(result.keys.containsAll(listOf("key1", "key2", "key3")))
        assertEquals(listOf("value1"), result["key1"])
        assertEquals(listOf("10.0"), result["key2"])
        assertEquals(listOf("A", "B", "C"), result["key3"])
    }

    @Test
    fun testGetUsernameFromContext() {

        val userAuth = UserAuthorization(username = "test")
        mockkObject(RequestUtility)

        // Case return user username
        every { RequestUtility.getUserAuthorization() } returns userAuth
        var result = getUsernameFromSecurityContext()
        assertEquals("test", result)

        // Case return INTERFACE
        every { RequestUtility.getUserAuthorization() } returns AuthorizationUtils.INTERFACE_AUTHORIZATION
        result = getUsernameFromSecurityContext()
        assertEquals(SYSTEM_USERNAME, result)

        // Case error
        every { RequestUtility.getUserAuthorization() } throws IllegalStateException("Error.")
        result = getUsernameFromSecurityContext()
        assertEquals(SYSTEM_USERNAME, result)

        unmockkObject(RequestUtility)

    }

    @Test
    fun xxx() {
        val date = "31/12/2022".toZonedDateTime(DATE_FORMAT).toInstant()
        println(date.stringify())

        val datePlus = date.plusMonths(2)
        println(datePlus.stringify())
    }

    fun sendRequestToChatOpenAI(prompt: String) : String{

        val apiKey = "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"
        val url = "https://api.openai.com/v1/completions"

        val json = """
            {
                "prompt": "$prompt",
                "model": "text-davinci-002",
                "max_tokens": 1024
            }
        """.trimIndent()


        val client = OkHttpClient()

        val requestBody = RequestBody.create(MediaType.parse("application/json"), json)

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body()!!.string()

        val jsonObject = JSONObject(responseBody)
        return jsonObject.getJSONArray("choices").getJSONObject(0).getString("text")
    }

    @Test
    fun yyy() {
        println(sendRequestToChatOpenAI("แต่งเพลงให้หน่อย"))
    }

}