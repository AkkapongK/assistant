package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

/**
 * Model for vendor offchain data
 *
 * @property code Vendor code
 * @property name Vendor name
 * @property legalName The legal identity node who represents this vendor
 * @property taxId Vendor Tax Id
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Vendor(
        val code: String? = null,
        val taxId: String? = null,
        val name: String? = null,
        val legalName: String? = null,
        val beneficiaryNotification: String? = null,
        val withholdingTaxFormType: String? = null
)

/**
 * Model for vendor bank offchain data ( code of Vendor should equal Vendor Bank )
 *
 * @property code Vendor code
 * @property paymentMethod The flag to allow this vendor to do invoice financing with a bank
 * @property accountNumber Vendor bank code
 * @property bankBranchCode Vendor bank branch code - use when issue payment item
 * @property fee Fee of the Payment Method allocated ( eg. BAHTNET : 100 )
 * @property beneficiaryCharge possible value as (A - Charge All, B = Charge Buyer, S = Charge Seller)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class VendorBank(
        val code: String? = null,
        val paymentMethod: String? = null,
        val accountNumber: String? = null,
        val fullAccountNumber: String? = null,
        val bankCode: String? = null,
        val bankBranchCode: String? = null,
        val fee: BigDecimal? = null,
        val beneficiaryCharge: String? = null,
        // Bank
        var bankNameThai: String? = null,
        var bankNameEng: String? = null,
        // Bank Branch
        var bankBranchNameThai: String? = null,
        var bankBranchNameEng: String? = null
)