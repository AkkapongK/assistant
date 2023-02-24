package th.co.dv.p2p.common.utilities

import net.corda.core.serialization.CordaSerializable

// A data class that can contain an body or an exception
@CordaSerializable
data class SilentEvent<out T>(
         val body: T? = null,
         val exception : Throwable? = null)