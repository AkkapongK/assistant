package th.co.dv.p2p.common.utilities

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import th.co.dv.p2p.common.exceptions.ExternalServiceException
import th.co.dv.p2p.common.models.ResponseModel
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

    /**
     * Method for handle response after got the response
     */
    fun <T: Any>handleRequestResult(sendRequest:() -> ResponseEntity<ResponseModel<T>>): ResponseModel<T> {
        val result = try {
            sendRequest().body!!
        } catch (e: Exception) {
            throw ExternalServiceException(e.message, e)
        }

        if (HttpStatus.valueOf(result.statusCode!!).is2xxSuccessful.not()) {
            throw ExternalServiceException("[${result.error!!.code}] ${result.error.message}", null)
        }
        return result
    }
}