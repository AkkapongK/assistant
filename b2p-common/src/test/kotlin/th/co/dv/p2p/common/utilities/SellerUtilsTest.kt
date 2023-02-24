package th.co.dv.p2p.common.utilities

import org.junit.Test
import th.co.dv.p2p.common.models.SellerModel
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals

class SellerUtilsTest {

    @Test
    fun testFilterSellerWithRdDate() {
        val today = Instant.now()
        val mockSellerModelList = listOf(
                SellerModel(taxId = "001", rdActiveEndDate = Date.from(today.plusMonths(1)), rdActiveStartDate = Date.from(today.minusDays(1))),
                SellerModel(taxId = "001", rdActiveEndDate = Date.from(today.plusMonths(1)), rdActiveStartDate = Date.from(today.minusDays(1))),
                SellerModel(taxId = "001", rdActiveEndDate = Date.from(today.minusDays(3)), rdActiveStartDate = Date.from(today.minusDays(1))))

        val result = mockSellerModelList.filterSellerWithRdDate()
        assertEquals(2, result.size)

    }
}