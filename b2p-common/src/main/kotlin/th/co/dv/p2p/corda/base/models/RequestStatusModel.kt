package th.co.dv.p2p.corda.base.models

class RequestStatusModel {

    /**
     * Model for mapping state lifecycle to display name
     */
    enum class StatusMapping(val displayName: String) {
        ISSUED("Issued"),
        PENDING_RECEIVER("Pending Receiver Approval"),
        PENDING_REQUESTER("Pending Requester Approval"),
        CLOSED("Closed"),
        CANCELLED("Cancelled");

        companion object {
            fun findByDisplayName(displayName: String) : String {
                return values().find { it.displayName == displayName }!!.name
            }
        }
    }
}