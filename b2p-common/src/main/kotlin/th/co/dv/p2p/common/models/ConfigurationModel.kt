package th.co.dv.p2p.common.models

/**
 * For map the response of configuration by tax id from configuration service
 * to Model Object
 */
data class ConfigurationModel (
        val ownerTaxId: String? = null,
        val counterPartyTaxId: String? = null,
        val category: String? = null,
        val documentType: String? = null,
        val configOption: String? = null,
        val value: String? = null
)