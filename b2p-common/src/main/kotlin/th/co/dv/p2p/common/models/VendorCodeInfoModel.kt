package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class VendorCodeInfoModel(
    val vendorCode: String? = null,
    val bankAccountNumber: String? = null,
    val bankAccountName: String? = null,
    val bankCode: String? = null,
    val bankBranchCode: String? = null,
    val beneficiaryCharge: String? = null,
    val beneficiaryBankFee: BigDecimal? = null,
    val paymentCalendar: String? = null,
    val whtPaytype: String? = null,
    val whtPaytypeRemark: String? = null,
    @get:JsonProperty("isActive")
    val isActive: Boolean? = null
)
