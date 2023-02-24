package th.co.dv.p2p.common.constants

import org.junit.Test
import kotlin.test.assertEquals

class BaseStatementsTest {

    @Test
    fun `test createFetchClause`(){
        val result = BaseStatements.createFetchClause(10)
        assertEquals("FETCH FIRST 10 ROWS ONLY", result.trim())
    }

    @Test
    fun `test createOffsetClause`(){
        val result = BaseStatements.createOffsetClause(1, 10)
        assertEquals("OFFSET 0 ROWS", result.trim())
    }
}