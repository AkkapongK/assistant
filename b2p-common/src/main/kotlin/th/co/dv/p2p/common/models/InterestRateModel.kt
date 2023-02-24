package th.co.dv.p2p.common.models
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

/**
 * Model for interest rate in offchain
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InterestRateModel(

        var id: String? = null,

        var interestType: String? = null,

        var buyerTaxId: String? = null,

        var vendorTaxId: String? = null,

        var interestRate: String? = null,

        var createdDate: String? = null,

        var updatedDate: String? = null
)