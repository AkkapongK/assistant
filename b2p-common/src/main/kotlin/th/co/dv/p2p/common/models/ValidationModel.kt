package th.co.dv.p2p.common.models

import th.co.dv.p2p.common.utilities.Conditions.using

data class ValidationModel(var valid: Boolean = false, var messages: MutableList<String> = mutableListOf()) {

    private fun printMessage(messages: List<String>): String {
        return messages.joinToString()
    }

    fun validate() {
        printMessage(this.messages) using (this.valid)
    }
}