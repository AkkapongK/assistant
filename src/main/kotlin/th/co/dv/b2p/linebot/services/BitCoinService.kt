package th.co.dv.b2p.linebot.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import th.co.dv.b2p.linebot.model.BitCoinAvailableModel
import th.co.dv.b2p.linebot.model.BitCoinSymbolInfoModel
import th.co.dv.b2p.linebot.model.ResponseBitCoinAvailableModel

@Service
class BitCoinService {

    @Autowired
    lateinit var restTemplate: RestTemplate

    private val baseUrl = "https://api.bitkub.com"

    private val urlMapping = mapOf(
            "available_symbol" to "/api/market/symbols",
            "symbol_ticker" to "/api/market/ticker"
    )

    /**
     * Get available bitcoin
     */
    fun getAvailable(): List<BitCoinAvailableModel> {

        val builder: UriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + urlMapping["available_symbol"])

        return  try {
            val response = restTemplate.exchange(
                    builder.build(true).toUri(),
                    HttpMethod.GET,
                    null,
                    object : ParameterizedTypeReference<ResponseBitCoinAvailableModel>() {}
            ).body!!

            response.result ?: emptyList()

        } catch (e: Exception) {
            throw Exception(e.message, e)
        }
    }

    /**
     * Get bitcoin information
     */
    fun getInfoBySymbol(symbol: String? = null): List<BitCoinSymbolInfoModel> {

        var builder: UriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + urlMapping["symbol_ticker"])

        if (symbol != null) builder = builder.queryParam("sym", symbol)

        return  try {
            val mapBitCoinSymbolInfoModel = restTemplate.exchange(
                    builder.build(true).toUri(),
                    HttpMethod.GET,
                    null,
                    object : ParameterizedTypeReference<Map<String, BitCoinSymbolInfoModel>>() {}
            ).body!!
            mapBitCoinSymbolInfoModel.map { (symbol, bitCoinSymbolInfoModel) ->
                bitCoinSymbolInfoModel.symbol = symbol
                bitCoinSymbolInfoModel
            }
        } catch (e: Exception) {
            throw Exception(e.message, e)
        }
    }

}