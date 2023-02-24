package th.co.dv.p2p.common.models

/**
 * Model for generate excel
 */
data class ExcelModel(
        val filename: String? = null,
        val excelSheets: List<ExcelSheetModel> = emptyList()
)