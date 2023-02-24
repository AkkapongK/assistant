package th.co.dv.p2p.common.utilities

import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import th.co.dv.p2p.common.base.enums.MessageType
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.cannotConvertParty
import th.co.dv.p2p.common.enums.InvoiceCommand
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.common.utilities.RabbitMQUtility.convertPartyToRoutingKey
import th.co.dv.p2p.common.utilities.RabbitMQUtility.formatLegalName
import th.co.dv.p2p.corda.base.models.PartyModel

class RabbitMQUtilityTest {

    private val buyerParty = PartyModel(legalName = "OU=BUYER, O=MINT, L=Bangkok, C=TH")
    private val sellerParty = PartyModel(legalName = "OU=SELLER, O=SUPPLIER4, L=Bangkok, C=TH")

    @Test
    fun testConvertPartyToRoutingKey() {

        mockkObject(RabbitMQUtility)

        every { formatLegalName("OU=BUYER, O=MINT, L=Bangkok, C=TH") } returns "OU=BUYER, O=MINT, L=Bangkok, C=TH"
        every { formatLegalName("OU=SELLER, O=SUPPLIER4, L=Bangkok, C=TH") } returns "OU=SELLER, O=SUPPLIER4, L=Bangkok, C=TH"
        every { formatLegalName("OU=SELLER, O=, L=Bangkok, C=TH") } returns "OU=SELLER, O=, L=Bangkok, C=TH"
        every { formatLegalName("OU=SELLER, O, L=Bangkok, C=TH") } returns "OU=SELLER, L=Bangkok, C=TH"

        // buyer:  "OU=BUYER, O=MINT, L=Bangkok, C=TH"
        var result = convertPartyToRoutingKey(buyerParty)
        assertEquals("BUYER_MINT_BANGKOK_TH", result)

        // seller:  "OU=SELLER, O=SUPPLIER4, L=Bangkok, C=TH"
        result = convertPartyToRoutingKey(sellerParty)
        assertEquals("SELLER_SUPPLIER4_BANGKOK_TH", result)

        var customParty = PartyModel(legalName = "OU=SELLER, O=, L=Bangkok, C=TH")
        result = convertPartyToRoutingKey(customParty)
        assertEquals("SELLER__BANGKOK_TH", result)

        customParty = PartyModel(legalName = "OU=SELLER, O, L=Bangkok, C=TH")
        result = convertPartyToRoutingKey(customParty)
        assertEquals("SELLER_BANGKOK_TH", result)

        // case no legal name
        val expectedResult = Try.on {
            convertPartyToRoutingKey(buyerParty.copy(legalName = null))
        }
        assertTrue(expectedResult.isFailure)
        assertTrue(expectedResult.toString().contains(cannotConvertParty))

        verify(exactly = 1) { formatLegalName("OU=BUYER, O=MINT, L=Bangkok, C=TH") }
        verify(exactly = 1) { formatLegalName("OU=SELLER, O=SUPPLIER4, L=Bangkok, C=TH") }
        verify(exactly = 1) { formatLegalName("OU=SELLER, O=, L=Bangkok, C=TH") }
        verify(exactly = 1) { formatLegalName("OU=SELLER, O, L=Bangkok, C=TH") }

        unmockkObject(RabbitMQUtility)

    }

    @Test
    fun testFormatLegalName() {

        // Case map already sort
        var result = formatLegalName(buyerParty.legalName!!)
        assertEquals("OU=BUYER, O=MINT, L=Bangkok, C=TH", result)

        // Case map is not sort
        result = formatLegalName("C=TH,L=Bangkok,OU=SELLER,O=SUPPLIER4")
        assertEquals("OU=SELLER, O=SUPPLIER4, L=Bangkok, C=TH", result)

        // Case key is not complete
        result = formatLegalName("C=TH,L=Bangkok,XX=,O=SUPPLIER4")
        assertEquals("O=SUPPLIER4, L=Bangkok, C=TH", result)

        result = formatLegalName("C=TH, OU=SELLER, O, L=Bangkok")
        assertEquals("OU=SELLER, L=Bangkok, C=TH", result)

    }

    @Test
    fun testGenerateRoutingKey() {
        val rabbitMQUtilityTest = spyk<RabbitMQUtility>()

        val parties = Pair(buyerParty, sellerParty)
        val routingKey = "BUYER_MINT_BANGKOK_TH"
        every { rabbitMQUtilityTest.convertPartyToRoutingKey(any()) } returns routingKey

        var result = rabbitMQUtilityTest.generateRoutingKey(parties, InvoiceCommand.Issue.name) { _, _ -> buyerParty }
        assertEquals(routingKey, result)

        // case parties is null
        result = rabbitMQUtilityTest.generateRoutingKey(null, InvoiceCommand.Issue.name) { _, _ -> buyerParty }
        assertEquals("", result)
    }

    @Test
    fun testSetDataSourceAndSponsor() {
        val streamingModel = StreamingModel(sponsor = "DV", id = "", data = "", command = "", type = "", messageType = MessageType.PROPOSAL, relatedServices = listOf())
        val rabbitMQUtilityTest = spyk<RabbitMQUtility>()

        val result = Try.on { rabbitMQUtilityTest.setDataSourceAndSponsor(streamingModel) }
        assertTrue(result.isSuccess)
        assertEquals("DV", SponsorContextHolder.getCurrentSponsor())
        assertEquals("dv", DataSourceContextHolder.getCurrentDb())
    }
}
