package th.co.dv.p2p.common.models

/**
 * Model to store attachment of action history
 *
 * @property id unique id of action history attachment record
 * @property attachmentHash hash of attachment
 * @property attachmentName attachment name
 * @property attachmentType type of attachment Ex. TaxInvoice, Other
 * @property storageLocation type of storage location of this attachment
 * @property attachmentUrl attachment url
 */
data class ActionHistoryAttachmentModel(
        var id: Long? = null,
        var attachmentHash: String? = null,
        var attachmentName: String? = null,
        var attachmentType: String? = null,
        var storageLocation: String? = null,
        var attachmentUrl: String? = null
)