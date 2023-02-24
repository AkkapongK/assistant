package th.co.dv.p2p.common.models

/**
 * Model for table email_recipient on configuration-service
 */
data class EmailRecipient (
        var sponsor: String? = null,
        var emailTemplate: String? = null,
        var emailTo: String? = null,
        var emailCc: String? = null

)