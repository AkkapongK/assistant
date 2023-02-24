package th.co.dv.p2p.common.enums

/**
 * Enum class for key type of [`storageLocation`] field in [`FileAttachmentModel`]
 *
 * null = store attachment in on-chain storage (old attachment)
 * cloud = store attachment on-cloud storage (new attachment)
 *
 */
enum class FileAttachmentLocation {
    CLOUD
}