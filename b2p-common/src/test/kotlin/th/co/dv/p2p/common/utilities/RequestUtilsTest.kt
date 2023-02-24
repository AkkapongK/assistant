package th.co.dv.p2p.common.utilities

import io.mockk.spyk
import org.junit.Test
import th.co.dv.p2p.common.enums.Lifecycle
import th.co.dv.p2p.common.enums.RecordStatus
import th.co.dv.p2p.corda.base.models.RequestItemModel
import th.co.dv.p2p.corda.base.models.RequestModel
import kotlin.test.assertEquals

class RequestUtilsTest {

    @Test
    fun testUpdateRequestStatus() {
        val requestUtility = spyk<RequestUtils>()
        val requestItemModel = listOf(RequestItemModel(
                lifecycle = Lifecycle.RequestItemLifecycle.ISSUED.name,
                status = RecordStatus.VALID.name))

        val requestModel = RequestModel(
                lifecycle = Lifecycle.RequestLifecycle.ISSUED.name,
                status = RecordStatus.VALID.name,
                requestItems = requestItemModel)

        // Seller status
        val result = requestUtility.updateRequestStatus(listOf(requestModel))

        assertEquals(1, result.size)
        assertEquals("Issued", result.first().status)
        assertEquals(1, result.first().requestItems.size)
        assertEquals("Issued", result.first().requestItems.first().status)

    }
}