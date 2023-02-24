package th.co.dv.p2p.common.utilities

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SponsorContextHolderTest {

    @Test
    fun testSponsorContextHolder() {
        SponsorContextHolder.clear()
        var sponsor = SponsorContextHolder.getCurrentSponsor()
        assertNull(sponsor)

        SponsorContextHolder.setSponsor("Sponsor")
        sponsor = SponsorContextHolder.getCurrentSponsor()
        assertEquals("Sponsor", sponsor)

        SponsorContextHolder.setSponsor("New_Sponsor")
        sponsor = SponsorContextHolder.getCurrentSponsor()
        assertEquals("New_Sponsor", sponsor)

        SponsorContextHolder.clear()
        sponsor = SponsorContextHolder.getCurrentSponsor()
        assertNull(sponsor)
    }
}