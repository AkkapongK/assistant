package th.co.dv.p2p.common.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import th.co.dv.p2p.corda.base.models.ConfigNameModel
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GetValidatedConfigurationModel (val companyTaxId: String,
                                           val inputDate: Instant,
                                           val configNameModel: ConfigNameModel,
                                           val counterPartyTaxId: String? = null,
                                           val currency: String? = null,
                                           val companyCode: String? = null,
                                           val vendorCode: String? = null)