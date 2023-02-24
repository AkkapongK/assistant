package th.co.dv.p2p.common.constants

import org.junit.Test
import kotlin.test.assertTrue

class GoodsReceivedStatementsTest {

    @Test
    fun `test getResolveNormalItemWhereClause`() {
        val result = GoodsReceivedStatements.getResolveNormalItemWhereClause("quantity", "reverse")
        assertTrue(result.contains(
            " ISNULL(CAST(JSON_VALUE(quantity, '$.initial') AS decimal(28,10)),0) - ISNULL(CAST(JSON_VALUE(reverse, '$.initial') AS decimal(28,10)),0) > 0 "
        )
        )
    }
}