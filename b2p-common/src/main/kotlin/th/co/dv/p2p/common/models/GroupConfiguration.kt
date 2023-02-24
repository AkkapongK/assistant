package th.co.dv.p2p.common.models

import th.co.dv.p2p.common.enums.GroupingTopic

/**
 * Grouping model use for group document based on the list of fields
 * group by company tax number and grouping topic
 */
data class GroupConfiguration(
        val companyTaxNumber: String? = null,
        val groupId: String? = null,
        val groupTopic: GroupingTopic? = null,
        val fields: List<String>? = emptyList(),
        val priority: Int? = null
)