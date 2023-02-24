package th.co.dv.p2p.corda.base.models

class TaxDocumentStatusModel {

    enum class StatusMapping(val displayName: String){
        ISSUED("Submitted"),
        APPROVED("Approved"),
        REJECTED("Rejected");

        companion object {
            fun findByDisplayName(displayName: String) : String {
                return values().find { it.displayName == displayName }!!.name
            }
            fun fromDisplayName(displayName: String) = values().filter { it.displayName == displayName }
        }
    }
}