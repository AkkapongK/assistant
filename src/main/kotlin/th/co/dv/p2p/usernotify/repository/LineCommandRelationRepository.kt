package th.co.dv.p2p.usernotify.repository

import th.co.dv.p2p.common.repositories.CustomJpaRepository
import th.co.dv.p2p.usernotify.entities.LineCommandRelation

interface LineCommandRelationRepository: CustomJpaRepository<LineCommandRelation, Long>  {
    fun findByCommand(command: String): LineCommandRelation
}