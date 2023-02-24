package th.co.dv.p2p.common.enums

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class MovementClass(val value: String) {
    NORMAL("Normal GR"),
    RETURN("Return GR"),
    REVERSE("Reverse of Normal GR"),
    REVERSE_RETURN("Reverse of Return GR");

    companion object {
        fun getDisplayName(type: String?): String? {
            return values()
                .find { it.name.equals(type, ignoreCase = true) }?.value
                ?: type
        }
    }
}
