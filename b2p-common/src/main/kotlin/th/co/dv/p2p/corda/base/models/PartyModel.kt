package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.identity.AbstractParty
import net.corda.core.node.NodeInfo
import net.corda.core.serialization.CordaSerializable

/** For JSON serialisation */
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PartyModel(val legalName: String? = null,
                      val organisation: String? = null,
                      val organisationUnit: String? = null
) {
    companion object {
        fun fromNodeInfo(nodeInfo: NodeInfo): PartyModel {
            val party = nodeInfo.legalIdentities.first()
            return PartyModel(party.toString(), party.name.organisation, party.name.organisationUnit)
        }

        fun fromAbstractParty(abstractParty: AbstractParty?): PartyModel? {
            val party = abstractParty?.nameOrNull()
            return party?.let { PartyModel(party.toString(), party.organisation, party.organisationUnit) }
        }
    }
}