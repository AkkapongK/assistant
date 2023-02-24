package th.co.dv.p2p.usernotify.constants

enum class LineCommand(val shortName: List<String>, val displayTitle: String, val storeField: String?, val mainMenu: Boolean, val descriptionFromMain: Boolean) {
    REGISTER(listOf("R", "REG"), "", null, false, true),
    MENU(listOf("M"), "Main menu", null, false, true),
    FETCH(listOf("GET"), "Get data", "sponsor", true, false),
    DOCUMENT(listOf("DOC"), "Document", "document_type", false, true),
    DOCUMENT_NUMBER(listOf("DOC_NO"), "Document number", "document_no", false, true)
}
