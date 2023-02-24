package th.co.dv.p2p.common.utilities

import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.Test
import th.co.dv.p2p.common.models.UserAuthorization
import kotlin.test.assertEquals
import io.mockk.every

class CommonUtilsKtTest {

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
}