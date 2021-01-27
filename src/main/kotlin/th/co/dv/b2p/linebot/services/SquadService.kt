package th.co.dv.b2p.linebot.services

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import th.co.dv.b2p.linebot.constant.SQUAD_NAME_IS_REQUIRED
import th.co.dv.b2p.linebot.model.SquadUpdatedModel
import th.co.dv.b2p.linebot.model.UserUpdatedModel
import th.co.dv.b2p.linebot.utilities.Utils.convertDateToString
import java.io.File
import java.util.*

@Service
class SquadService {

    private val updatedFileName = "squad_updated.xlsx"
    /**
     * first argument is squad name
     * second argument is nickname (optional: if not have return all)
     * third argument is date (optional: if not have return current)
     */
    fun getSquadUpdated(args: List<String>): List<UserUpdatedModel> {

        val excelFile = File(updatedFileName)
        val workbook = XSSFWorkbook(excelFile)

        val sheetName = args.firstOrNull()?.toLowerCase() ?: throw Exception(SQUAD_NAME_IS_REQUIRED)
        val sheet = workbook.getSheet(sheetName)
        val nickname = args.getOrNull(1)?.toLowerCase()
        val targetDate = args.getOrNull(2)?.toLowerCase() ?: convertDateToString(Date())

        val rows = sheet.iterator()
        var currentRowIndex = 0
        var users = mapOf<Int, String>()
        val squadUpdatedsModel = mutableListOf<SquadUpdatedModel>()
        while (rows.hasNext()) {

            val currentRow = rows.next()
            when {
                (currentRowIndex == 0) -> users = getUser(currentRow)
                else-> {
                    squadUpdatedsModel.add(getUpdatedData(currentRow, users))
                }
            }
            currentRowIndex++
        }

        workbook.close()
        return squadUpdatedsModel.getUpdatedData(targetDate, nickname)
    }

    /**
     * Method for get updated data by nickname and date
     */
    private fun List<SquadUpdatedModel>.getUpdatedData(date: String, nickname: String?): List<UserUpdatedModel> {
        val dataByDate = this.find { it.date == date } ?: return emptyList()
        return when (nickname != null) {
            true -> dataByDate.updated.filter { it.name?.toLowerCase() == nickname }
            false -> dataByDate.updated
        }
    }

    /**
     * Get user
     */
    private fun getUser(row: Row): Map<Int, String> {
        val output = mutableMapOf<Int, String>()
        val cellsInRow = row.iterator()
        var currentCol = 0
        while (cellsInRow.hasNext()) {
            val currentCell = cellsInRow.next()
            if (currentCol != 0) {
                val value = getValue(currentCell)
                output.put(currentCell.columnIndex, value)
            }

            currentCol++

        }
        return output
    }

    /**
     * Get user
     */
    private fun getUpdatedData(row: Row, user: Map<Int, String>): SquadUpdatedModel {
        val squadUpdatedModel = SquadUpdatedModel()
        val cellsInRow = row.iterator()
        val userUpdatedModel = mutableListOf<UserUpdatedModel>()
        var currentCol = 0
        while (cellsInRow.hasNext()) {
            val currentCell = cellsInRow.next()
            val value = getValue(currentCell)
            if (currentCol != 0) {
                // user updated
                val userName = user.get(currentCell.columnIndex)
                userUpdatedModel.add(UserUpdatedModel(
                        name = userName,
                        updated = value
                ))
            } else {
                // date
                squadUpdatedModel.date = value
            }

            currentCol++
        }
        squadUpdatedModel.updated = userUpdatedModel
        return squadUpdatedModel
    }


    private fun getValue(currentCell: Cell): String {
        return when {
            currentCell.cellTypeEnum === CellType.STRING -> currentCell.stringCellValue.trim('"')
            else -> ""
        }
    }
}