package th.co.dv.p2p.usernotify.models

import th.co.dv.p2p.usernotify.constants.LineCommand
import th.co.dv.p2p.usernotify.entities.LineCommandRelation

data class LineCommandModel(
    val command: LineCommand,
    val description: String?,
    val choices: List<LineCommandModel>? = null,
    val answer: String? = null
)

fun List<LineCommandRelation>.toLineCommandModels(): List<LineCommandModel> {
    return this.map { it.toLineCommandModel() }
}

fun LineCommandRelation.toLineCommandModel(): LineCommandModel {
    val commands = this.command!!.split(" ")
    return LineCommandModel(
        command = enumValueOf(commands.first()),
        description = this.description,
        answer = commands.getOrNull(1)
    )
}