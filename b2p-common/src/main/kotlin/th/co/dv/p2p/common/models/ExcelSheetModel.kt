package th.co.dv.p2p.common.models

/**
 * Data class for generate excel file
 *
 * @property sheetName name of the sheet in excel file
 * @property headers header column of excel sheet
 * @property content data for each row contain all the data for every column the that row
 */
data class ExcelSheetModel(
        val sheetName: String? = null,
        val headers : List<String>? = null,
        val content : List<List<String?>?>? = null
)