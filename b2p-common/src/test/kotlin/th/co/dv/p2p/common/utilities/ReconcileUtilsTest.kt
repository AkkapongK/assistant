package th.co.dv.p2p.common.utilities

import io.mockk.spyk
import org.junit.Test
import th.co.dv.p2p.common.constants.MicroServiceSchema
import th.co.dv.p2p.common.enums.Lifecycle
import kotlin.test.assertEquals

class ReconcileUtilsTest {


    @Test
    fun `Test convertDataToLinearIdMap`() {
        val reconcileUtils = spyk<ReconcileUtils>()
        val dataFromTable = mapOf(
                "company_code" to "400",
                "linear_id" to "06a6807f-68d6-43a9-9fc8-8b03ef06b36e"
        )

        val result = reconcileUtils.convertDataToLinearIdMap(listOf(dataFromTable))

        val expectedResult = mapOf<String, MutableMap<String, Any>>(
                "06a6807f-68d6-43a9-9fc8-8b03ef06b36e" to dataFromTable.toMutableMap()
        )

        assertEquals(expectedResult, result)

    }

    @Test
    fun `Test buildGetLinearIdMSCriteria`() {
        val reconcileUtils = spyk<ReconcileUtils>()
        val schema = MicroServiceSchema.INVOICE
        val tableName = "invoice"
        // Case linearIds , lifeCycles , startDate , endDate is null
        var expectedResult = """
            SELECT  t.linear_id
            FROM  invoice.invoice as t
           
        """.trimIndent()

        var result = reconcileUtils.buildGetLinearIdMSCriteria(schema, tableName, null, null, null, null)
        assertEquals(expectedResult, result.trimIndent())

        // Case linearIds , lifeCycles , startDate , endDate not null
        val linearIds = listOf("01050e81-f2d2-43b4-99cb-9aa5d26cf722")
        val startDate = "2020-03-04T10:37:48"
        val endDate = "2020-05-28T11:53:50"
        val lifeCycles = listOf(Lifecycle.InvoiceLifecycle.PENDING_AUTHORITY.name)

        expectedResult = """
            SELECT  t.linear_id
            FROM  invoice.invoice as t
            
            WHERE t.linear_id IN ('01050e81-f2d2-43b4-99cb-9aa5d26cf722')  AND t.lifecycle IN ('PENDING_AUTHORITY')  AND t.created_date >= '2020-03-04 10:37:48' AND t.created_date <= '2020-05-28 11:53:50';
        """.trimIndent()
        result = reconcileUtils.buildGetLinearIdMSCriteria(schema, tableName, linearIds, lifeCycles, startDate, endDate)
        assertEquals(expectedResult, result.trimIndent())

    }
}