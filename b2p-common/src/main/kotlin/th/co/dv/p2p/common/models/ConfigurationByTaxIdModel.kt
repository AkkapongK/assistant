package th.co.dv.p2p.common.models

/**
 * For map the response of configuration by tax id from off-chain server
 * to Model Object
 */
data class ConfigurationByTaxIdModel (
        val id: Int? = null,
        val configOption: String? = null,
        val value: String? = null,
        val companyTaxId: String? = null,
        val counterPartyTaxId: String? = null,
        val mandatory: Boolean? = null,
        val description: String? = null
)