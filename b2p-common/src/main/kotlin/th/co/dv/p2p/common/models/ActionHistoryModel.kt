package th.co.dv.p2p.common.models

/**
 * Model to store action history of the state
 *
 * @property id unique id of action history
 * @property documentType type of document Ex. invoice, purchase, goods_received
 * @property documentLinearId linear if of the document
 * @property actionBy user who take the action
 * @property commonName name of the user who approved the document (DOA)
 * @property actionName name of the action Ex. edit, resubmit
 * @property actionDate date that take the action
 * @property remark reason for approve or reject document
 * @property attachments list of attachment with current action
 * @property originalId original unique id of action history (Use for migration)
 */
data class ActionHistoryModel(
        var id: Long? = null,
        var documentType: String? = null,
        var documentLinearId: String? = null,
        var actionBy: String? = null,
        var commonName: String? = null,
        var actionName: String? = null,
        var actionDate: String? = null,
        var remark: String? = null,
        var attachments: List<ActionHistoryAttachmentModel>? = null,
        var originalId: Long? = null
)