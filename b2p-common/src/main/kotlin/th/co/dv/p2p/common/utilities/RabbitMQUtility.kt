package th.co.dv.p2p.common.utilities

import com.google.common.base.Splitter
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.models.StreamingModel
import th.co.dv.p2p.corda.base.models.PartyModel

object RabbitMQUtility {

    private val orderedLegalNameKey = listOf("OU", "O", "L", "C")

    /**
     * Method fro create routing key that send to rabbitMQ
     * by legal name in document depend on command that used
     *
     * We will decision party model in document that used from command
     * But we done it in MS (by lambda method)
     *
     * @param parties: [PartyModel] of buyer and seller that get from document
     * @param getTargetParty: lambda method for get buyer/seller party from command in streaming model
     */
    fun generateRoutingKey(parties: Pair<PartyModel, PartyModel>?, command: String, getTargetParty: (partiesModel: Pair<PartyModel, PartyModel>, command: String) -> PartyModel): String {
        // Get the party from streaming model
        // In case have no parties we return empty string (no routing key)
        val partiesModel = parties ?: return ""

        // Get party that we used to generate routing key from command (call lambda method that be in each Services)
        val targetParty = getTargetParty(partiesModel, command)
        return convertPartyToRoutingKey(targetParty)
    }

    /**
     * Method for generate routing key from party
     *
     * we have string "A=1, B=2, C=3" return value would be key value {A=1, B=2, C=3}
     * Then we join all value with '_' -> 1_2_3
     * @param targetParty: Party that used for generate routing key
     */
    fun convertPartyToRoutingKey(targetParty: PartyModel): String {
        try {
            val finalLegalName = formatLegalName(targetParty.legalName!!)
            val mapLegalName = Splitter.on(comma).withKeyValueSeparator(EQUALS).split(finalLegalName)
            return mapLegalName.values.joinToString(UNDERSCORE).uppercase()
        } catch (e: Exception) {
            throw IllegalArgumentException(cannotConvertParty)
        }
    }

    /**
     * Method to re-format legal name by list of [orderedLegalNameKey]
     *
     * First we may have string of of "C=TH,OU=SELLER, L=BANGKOK,O=SUPPLIER4"
     * we need to sort and format to be "OU=SELLER, O=SUPPLIER4, L=BANGKOK, C=TH"
     * @param legalName given legal name
     * @return new sorted map
     */
    fun formatLegalName(legalName: String): String {

        val keys = legalName.splitAndTrim(comma, true)
        return orderedLegalNameKey.mapNotNull { sort ->
            keys.find { it.startsWith(sort + EQUALS) }
        }.joinToString(commaWithSpace)

    }

    /**
     * Method to set sponsor from streaming model
     */
    fun setDataSourceAndSponsor(streamingModel: StreamingModel<*>) {
        SponsorContextHolder.clear()
        DataSourceContextHolder.clear()
        
        val sponsor = streamingModel.sponsor!!
        SponsorContextHolder.setSponsor(sponsor)
        DataSourceContextHolder.setCurrentDb(sponsor.lowercase())
    }

}
