package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable

@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DelegationOfAuthorityModel(
        val id: Long? = null,
        val assignee: String? = null,
        val commonName: String? = null,
        val level: Long? = null,
        val description: String? = null,
        val approvedDate: String? = null,
        val approvedBy: String? = null,
        val assignedDate: String? = null,
        @get:JsonProperty(value = "isApproved")
        val isApproved: Boolean? = null,
        val remark: String? = null
)


fun List<DelegationOfAuthorityModel>.withAssigneeDomains(domain: String): List<DelegationOfAuthorityModel> {
        return this.map {
                it.withAssigneeDomain(domain)
        }
}

fun DelegationOfAuthorityModel.withAssigneeDomain(domain: String): DelegationOfAuthorityModel {
        val assignee = this.assignee ?: return this
        val finalAssignee = if(assignee.contains("@").not()) {
                assignee + domain
        } else assignee
        return copy(
                assignee = finalAssignee
        )
}