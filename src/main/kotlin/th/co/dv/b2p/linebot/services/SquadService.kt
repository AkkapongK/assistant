package th.co.dv.b2p.linebot.services

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import th.co.dv.b2p.linebot.constant.Constant
import th.co.dv.b2p.linebot.constant.SQUAD_NAME_IS_REQUIRED
import th.co.dv.b2p.linebot.model.SquadUpdatedModel
import th.co.dv.b2p.linebot.model.UserUpdatedModel
import th.co.dv.b2p.linebot.utilities.Utils
import th.co.dv.b2p.linebot.utilities.Utils.convertDateToString
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*


@Service
class SquadService {

    @Autowired
    lateinit var friendService: FriendService

    @Autowired
    lateinit var squadConfiguration: SquadConfiguration

    /**
     * first argument is squad name
     * second argument is nickname (optional: if not have return all)
     * third argument is date (optional: if not have return current)
     */
    fun getSquadUpdated(args: List<String>): List<UserUpdatedModel> {

        val sheetName = Utils.getEnumIgnoreCase<Constant.Squad>(args.firstOrNull())?.name?.toLowerCase()
                ?: throw Exception(SQUAD_NAME_IS_REQUIRED)
        val nickname = args.getOrNull(1)?.toLowerCase()
        val targetDate = args.getOrNull(2)?.toLowerCase() ?: convertDateToString(Date())

        val squadUpdatedsModel = getUpdatedData(sheetName)
        println("getSquadUpdated squadUpdatedsModel >>>>>> $squadUpdatedsModel")
        println("getSquadUpdated targetDate >>>>>> $targetDate")
        val targetSquad = squadUpdatedsModel.filterByDate(targetDate)
        return targetSquad?.second?.getUpdatedData(nickname) ?: emptyList()
    }

    /**
     * Method to get and convert updated data to model
     */
    private fun getUpdatedData(sheetName: String): List<SquadUpdatedModel> {
        val excelFile = File(squadConfiguration.path)
        val workbook = XSSFWorkbook(excelFile)
        val sheet = workbook.getSheet(sheetName)

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
        return squadUpdatedsModel
    }

    /**
     * Method for master member update task for other member
     */
    fun masterUpdateData(masterId: String, member: String, date: String, data: String) {
        // validate master id
        val master = friendService.findFriendById(masterId)?.isMaster
        if (master != true) return
        // find user id from member name
        val user = friendService.findFriendByName(member)?: return
        // call updateData
        updateData(
            userId = user.userId!!,
            date = convertDateToString(Date()),
            data = data)
    }

    fun updateData(userId: String, date: String, data: String) {
        val user = friendService.getAllFriends().find { it.userId == userId }

        val squad = Utils.getEnumIgnoreCase<Constant.Squad>(user?.squad)?.name?.toLowerCase()
        val name = user?.name

        if (squad.isNullOrBlank() || name.isNullOrBlank()) throw IllegalArgumentException("Please introduce yourself with command: ME [squad] [name]")

        val existingSquadData = getUpdatedData(squad!!).toMutableList()

        // check if user exists in existing data
        val users = existingSquadData.firstOrNull()?.updated?.map { it.name!! }?.toMutableList() ?: mutableListOf()
        if (users.any{ it.equals(name, ignoreCase = true) }.not()) users.add(name!!)

        // get current date data or initialize
        val squadUpdatedModelWithIndex = existingSquadData.filterByDate(date)
        val index = squadUpdatedModelWithIndex?.first ?: -1
        var squadUpdatedModel = squadUpdatedModelWithIndex?.second

        if (squadUpdatedModel == null) {
            squadUpdatedModel = SquadUpdatedModel(date = date, updated = users.map { UserUpdatedModel(name = it) })
            existingSquadData.add(squadUpdatedModel)
        } else {
            existingSquadData.remove(squadUpdatedModel)
        }

        // update data for specific date and name
        squadUpdatedModel.updated.forEach {
            if (it.name!!.equals(name!!, ignoreCase = true)) { it.updated = data }
        }

        existingSquadData.add(squadUpdatedModel)
        println(existingSquadData)
        saveData(squad, squadUpdatedModel, index)
    }

    /**
     * Method to update data into file
     */
    private fun saveData(sheetName: String, squadData: SquadUpdatedModel, index: Int) {
        val inputStream = FileInputStream(File(squadConfiguration.path))
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheet(sheetName)

        val finalIndex = if (index == -1) {
            sheet.lastRowNum + 1
        } else index

        // column
        var columnCellIndex = 0
        val columnRow = sheet.createRow(0)
        var columnCell = columnRow.createCell(columnCellIndex)
        columnCell.setCellValue("Date")
        columnCellIndex++
        squadData.updated.forEach {
            columnCell = columnRow.createCell(columnCellIndex)
            columnCell.setCellValue(it.name)
            columnCellIndex++
        }

        columnCellIndex++

        val row = sheet.createRow(finalIndex)

        // date
        var cellIndex = 0
        var cell = row.createCell(cellIndex)
        cell.setCellValue(squadData.date)
        cellIndex++


        // member update
        squadData.updated.forEach {
            cell = row.createCell(cellIndex)
            cell.setCellValue(it.updated)
            cellIndex++
        }
        inputStream.close()

        val outputStream = FileOutputStream(squadConfiguration.path)
        workbook.write(outputStream)
        workbook.close()
        outputStream.close()
    }

    /**
     * Method for filter Squad updated by date
     */
    private fun List<SquadUpdatedModel>.filterByDate(date: String): Pair<Int, SquadUpdatedModel>? {
        val target = this.find { it.date == date } ?: return null
        val index = this.indexOf(target)
        return index to target
    }

    /**
     * Method for get updated data by nickname and date
     */
    private fun SquadUpdatedModel.getUpdatedData(nickname: String?): List<UserUpdatedModel> {
        return when (nickname != null) {
            true -> this.updated.filter { it.name?.toLowerCase() == nickname }
            false -> this.updated
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

@Configuration
@ConfigurationProperties("squad")
data class SquadConfiguration(
        var path: String? = null
)