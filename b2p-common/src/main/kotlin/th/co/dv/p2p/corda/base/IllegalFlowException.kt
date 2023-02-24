package th.co.dv.p2p.corda.base

import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.common.enums.BaseMessageError

// The class we return for error that we checking in the flow
@CordaSerializable
class IllegalFlowException(msg: String) :IllegalArgumentException(msg) {
    constructor(prefix: String, error: BaseMessageError) : this(prefix.format(error.getMessage()) + " (Error Code: ${error.getCode()})")
    constructor(error: BaseMessageError) : this(error.getMessage() + " (Error Code: ${error.getCode()})")
}