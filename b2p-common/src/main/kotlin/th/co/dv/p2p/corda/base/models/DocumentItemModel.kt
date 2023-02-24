package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

/**
 * Document item for payment contain information of invoice, credit note, debit note
 *
 * @property linearId linear id of document (invoice, credit note, debit note)
 * @property paymentLinearId linear id of payment
 * @property documentNumber document number get from (invoice, credit note, debit note)
 * @property documentType document type either invoice, credit note and debit note
 * @property documentDate document date of (invoice, credit note, debit note)
 * @property referenceDocNumber reference document for invoice, credit note, debit note
 * @property subTotal the sub total of the document without vat total, after including discount and surcharge
 * @property vatTotal the vat total of the document based on tax code
 * @property total the total of the document after include vat total
 * @property withholdingTaxTotal the total of the withholding tax amount for this document
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DocumentItemModel(
        var linearId: String? = null,
        var payment: PaymentModel? = null,
        var paymentLinearId: String? = null,
        var documentNumber: String? = null,
        var documentType: String? = null,
        var documentDate: String? = null,
        var referenceDocNumber: String? = null,
        var subTotal: BigDecimal? = null,
        var vatTotal: BigDecimal? = null,
        var total: BigDecimal? = null,
        var withholdingTaxTotal: BigDecimal? = null,
        var currency: String? = null

)