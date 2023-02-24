package th.co.dv.p2p.common.models

import net.corda.core.serialization.CordaSerializable
import java.io.Serializable

@CordaSerializable
data class BuyerVendorPKModel(
    var buyerTaxId: String? = null,
    var buyerCode: String? = null,
    var vendorTaxId: String? = null,
    var vendorCode: String? = null
) : Serializable