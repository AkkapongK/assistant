package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable

/** Needed to serialise and deserialise JSON objects sent to and from API end-points. */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExceptionModel(var statusCode: Int? = null,
                          var txId: String? = null,
                          var message: String? = null)