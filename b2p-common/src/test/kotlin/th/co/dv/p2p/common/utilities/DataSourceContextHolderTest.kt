package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.Test
import th.co.dv.p2p.common.base.enums.MessageType
import th.co.dv.p2p.common.enums.InvoiceCommand
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.common.utilities.DataSourceContextHolder.Companion.prepareSponsor
import kotlin.test.assertEquals

class DataSourceContextHolderTest {

    @Test
    fun `Test prepare sponsor`() {
        val modelInput = StreamingModel(
            id = "1",
            command = InvoiceCommand.Issue.name,
            type = Services.INVOICE.name,
            data = null,
            messageType = MessageType.PROPOSAL,
            relatedServices = listOf()
        )
        val expectedResult = modelInput.copy(sponsor = "Sponsor")
        mockkObject(SponsorContextHolder)
        every { SponsorContextHolder.getCurrentSponsor() } returns "Sponsor"

        val result = prepareSponsor(modelInput)
        assertEquals(expectedResult, result)
        unmockkObject(SponsorContextHolder)
    }
}