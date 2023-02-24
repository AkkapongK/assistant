package th.co.dv.p2p.common.utilities

import io.netty.handler.codec.http.HttpResponseStatus
import org.junit.Test
import org.springframework.security.access.AccessDeniedException
import th.co.dv.p2p.common.enums.ServiceErrorClassType
import th.co.dv.p2p.common.exceptions.*
import th.co.dv.p2p.corda.base.IllegalFlowException
import javax.persistence.PersistenceException
import kotlin.test.assertEquals


class BaseResponseHandlerTest {

    @Test
    fun `Test getErrorInfo`() {
        val mapInputWithExpectedResult = mapOf(
                IllegalFlowException("Test Exception") to Triple(HttpResponseStatus.BAD_REQUEST, ServiceErrorClassType.VALIDATION, "Test Exception"),
                IllegalArgumentException("Test Exception") to Triple(HttpResponseStatus.BAD_REQUEST, ServiceErrorClassType.ILLEGAL_ARGUMENT, null),
                SendToKafkaException(Exception()) to Triple(HttpResponseStatus.INTERNAL_SERVER_ERROR, ServiceErrorClassType.KAFKA, null),
                SendToRabbitException(Exception()) to Triple(HttpResponseStatus.INTERNAL_SERVER_ERROR, ServiceErrorClassType.RABBITMQ, null),
                ExternalServiceException("Test Exception", Exception()) to Triple(HttpResponseStatus.INTERNAL_SERVER_ERROR, ServiceErrorClassType.EXTERNAL_SERVICE, "Test Exception"),
                PersistenceException("Test Exception") to Triple(HttpResponseStatus.INTERNAL_SERVER_ERROR, ServiceErrorClassType.DATABASE, null),
                LockingRecordException("Test Exception") to Triple(HttpResponseStatus.LOCKED, ServiceErrorClassType.LOCKED_RECORD, "Test Exception"),
                AuthorizationException("Test Exception") to Triple(HttpResponseStatus.UNAUTHORIZED, ServiceErrorClassType.VALIDATION, "Test Exception"),
                AuthorizationPermissionException("Test Exception") to Triple(HttpResponseStatus.FORBIDDEN, ServiceErrorClassType.AUTHORIZATION_PERMISSION, "Test Exception"),
                AccessDeniedException("Test Exception") to Triple(HttpResponseStatus.UNAUTHORIZED, ServiceErrorClassType.AUTHORIZATION_PERMISSION, "Test Exception"),
                IllegalStateException("Test Exception") to Triple(HttpResponseStatus.INTERNAL_SERVER_ERROR, ServiceErrorClassType.UNKNOWN, null)
        )

        mapInputWithExpectedResult.forEach { (input, expectedResult) ->
            val (expectedHttpResponseStatus, expectedClassType, expectedMessage) = expectedResult
            val result = BaseResponseHandler.getErrorInfo(input)
            assertEquals(expectedHttpResponseStatus, result.httpResponseStatus)
            assertEquals(expectedClassType, result.errorClassType)
            assertEquals(expectedMessage, result.errorMessage)
        }
    }


}
