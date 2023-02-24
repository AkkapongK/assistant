package th.co.dv.p2p.common.models

/**
 * Model for table email_configuration on configuration-service
 */
data class EmailConfiguration (
        var emailTemplate: String? = null,
        var emailSubject: String? = null,
        var emailFrom: String? = null,
        var emailContent: String? = null,
        var emailRecipients: List<EmailRecipient> = mutableListOf()
)