package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Model for additional data to generate withholding tax certificate
 */
data class AdditionalWhtCertInformationModel(
    val payerName: String? = null,
    val payerAddress: String? = null,
    val payeeName: String? = null,
    val payeeAddress: String? = null,
    @get:JsonProperty("eSignature")
    val eSignature: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AdditionalWhtCertInformationModel

        if (payerName != other.payerName) return false
        if (payerAddress != other.payerAddress) return false
        if (payeeName != other.payeeName) return false
        if (payeeAddress != other.payeeAddress) return false
        if (eSignature != null) {
            if (other.eSignature == null) return false
            if (!eSignature.contentEquals(other.eSignature)) return false
        } else if (other.eSignature != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payerName?.hashCode() ?: 0
        result = 31 * result + (payerAddress?.hashCode() ?: 0)
        result = 31 * result + (payeeName?.hashCode() ?: 0)
        result = 31 * result + (payeeAddress?.hashCode() ?: 0)
        result = 31 * result + (eSignature?.contentHashCode() ?: 0)
        return result
    }
}