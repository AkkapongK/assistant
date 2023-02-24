package th.co.dv.p2p.usernotify.utility

import java.time.Instant

/**
 * Utilities for RestService in every micro service
 */
object RestServiceUtilities {
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_SPONSOR = "Sponsor"
    const val CLIENT_CREDENTIAL = "grant_type=client_credentials"
    const val ACCESS_TOKEN = "access_token"
    const val BEARER = "Bearer"
    const val EXPIRES_ON = "expires_on"

    /**
     * Method to build Bearer token
     */
    fun buildAuthorizationHeader(accessToken: String?): String? {
        return when (accessToken?.startsWith(BEARER)) {
            false -> "$BEARER $accessToken"
            else -> accessToken
        }
    }

    /**
     * Method to calculate is current token has expired
     * @param expiryEpochSecond token expiry epoch from response from auth service
     * @param thresholdSecond threshold for compare [expiryEpochSecond] to current time
     */
    fun isTokenExpired(expiryEpochSecond: Long, thresholdSecond: Long): Boolean {
        val expectedExpiryDate = Instant.now().plusSeconds(thresholdSecond).epochSecond
        return expiryEpochSecond <= expectedExpiryDate
    }


}