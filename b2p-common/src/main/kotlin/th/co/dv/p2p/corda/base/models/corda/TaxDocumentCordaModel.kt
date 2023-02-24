package th.co.dv.p2p.corda.base.models.corda

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import th.co.dv.p2p.corda.base.domain.Amount
import th.co.dv.p2p.corda.base.models.PartyModel
import java.util.*

/**
 * @property buyer the legal identity node who represents the buyer
 * @property seller the legal identity node who represents the seller
 * @property vendorTaxNumber the seller's Tax Number in the external world
 * @property vendorName the seller's common name in the external world
 * @property vendorBranchCode the seller's branch code in the external world
 * @property vendorBranchName the seller's branch name
 * @property vendorAddress the seller's address in the external world * @property companyTaxNumber the buyer's company tax number in the external world
 * @property companyName the buyer's company name in the external world
 * @property companyBranchCode the buyer's company branch code in the external world
 * @property companyBranchName the buyer's company branch name in the external world
 * @property companyAddress the buyer's company address in the external world
 * @property documentNumber this state's number/id in the external world
 * @property lifecycle the stages of evolution which this state will go through
 * @property buyerFinalApprovedRemark the remark of approved tax document
 * @property buyerFinalApprovedUser the user that performed the approve action
 * @property buyerFinalApprovedDate the date that tax document was approved
 * @property createdBy the user that issued this document
 * @property rdSubmittedDate the RD submitted date of the tax document
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaxDocumentCordaModel (
    val buyer: PartyModel? = null,
    val seller: PartyModel? = null,
    val vendorName: String? = null,
    val vendorBranchCode: String? = null,
    val vendorBranchName: String? = null,
    val vendorTaxNumber: String? = null,
    val vendorAddress: String? = null,
    val companyName: String? = null,
    val companyBranchCode: String? = null,
    val companyBranchName: String? = null,
    val companyTaxNumber: String? = null,
    val companyAddress: String? = null,
    val documentType: String? = null,
    val documentNumber: String? = null,
    val documentDate: String? = null,
    val createdDate: String? = null,
    val lifecycle: String? = null,
    val subTotal: Amount<Currency>? = null,
    val vatTotal: Amount<Currency>? = null,
    val total: Amount<Currency>? = null,
    val buyerFinalApprovedRemark: String? = null,
    val buyerFinalApprovedUser: String? = null,
    val buyerFinalApprovedDate: String? = null,
    val createdBy: String? = null,
    val rdSubmittedDate: String? = null,
    val linearId: String? = null,
    val documentCode: String? = null
)