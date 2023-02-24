package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * Model for vendor offchain data
 *
 * @property code Company code
 * @property name1 Company name
 * @property name2 Company name2
 * @property taxId Company Tax Id Name
 * @property accountNo Company account number for transfer money
 * @property bankCode Company bank Code for transfer money
 * @property whtSignatory The field that make us know who suppose to handle with holding flag (Bank/Buyer)
 * @property paymentAdviceEmail: After invoice are paid, bank will send email to notify supplier to inform that amount is already put in
 * their account. [Only MINT maintain this field in offchain other buyer bank is getting vendor's email from PO].
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Company(
        var code: String? = null,
        var name1: String? = null,
        var name2: String? = null,
        var taxId: String? = null,
        var bankBranchCode: String? = null,
        var whtSignatory: String? = null,
        var accountNo: String? = null,
        var bankCode: String? = null,
        var paymentAdviceEmail: String? = null,
        var paymentCalendar: String? = null,
        var cmsCalendar: String? = null,
        var whtFormType: String? = null,
        var defaultPaymentCalendar: String? = null,
        var scfCode: String? = null,
        var paymentGroup: String? = null,
        var corpId: String? = null,
        var legalName: String? = null,
        var createdDate: String? = null,
        var isActive: Boolean? = null,
        var createdBy: String? = null,
        var updatedBy: String? = null,
        var foreign: String? = null
)