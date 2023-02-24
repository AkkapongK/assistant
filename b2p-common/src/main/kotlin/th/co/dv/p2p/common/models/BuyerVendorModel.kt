package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BuyerVendorModel(
        var buyerTaxId: String? = null,
        var buyerCode: String? = null,
        var vendorTaxId: String? = null,
        var vendorCode: String? = null,
        var receivableBankAccountNo: String? = null,
        var receivableBankAccountName: String? = null,
        var receivableBankCode: String? = null,
        var receivableBankBranchCode: String? = null,
        var paymentCalendar: String? = null,
        var whtPaytype: String? = null,
        var whtPaytypeRemark: String? = null,
        var beneficiaryCharge: String? = null,
        var beneficiaryBankFee: BigDecimal? = null,
        val createdBy: String? = null,
        val updatedBy: String? = null,
        val createdDate: Date? = null,
        val updatedDate: Date? = null,
        var financingConfigurations: List<FinancingConfigurationModel> = emptyList(),
        var nonWorkingCalendar: List<NonWorkingCalendar> = emptyList(),
        val bank: BankModel? = null,
        val bankBranch: BankBranchModel? = null,
        val vendorCompany: CompanyModel? = null,
        val buyerCompany: CompanyModel? = null,
        @get:JsonProperty("isHold")
        var isHold: Boolean? = null,
        @get:JsonProperty("isActive")
        var isActive: Boolean? = null,
        val paymentAdviceEmail: String? = null
)