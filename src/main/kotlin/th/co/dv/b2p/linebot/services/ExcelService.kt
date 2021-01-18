package th.co.dv.b2p.linebot.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.io.FileUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import th.co.dv.b2p.linebot.constant.PLAN_MODE_NOT_FOUND
import th.co.dv.b2p.linebot.constant.PROJECT_NOT_FOUND
import java.io.File
import java.net.URL
import java.util.*


@Service
class ExcelService {

    @Autowired
    lateinit var restTemplate: RestTemplate

    private val mapper = jacksonObjectMapper()

    private val releasePlanProjectMapping = mapOf(
        "b2p" to "B2P Release Plan")

    private val releasePlanModeMapping = mapOf(
            "deploy" to "Deploy",
            "uat" to "UAT",
            "qa" to "QA",
            "sit" to "SIT",
            "pdt" to "PDT",
            "mgt" to "MGT"
    )
    private val releasePlanItemUrl = "https://graph.microsoft.com/v1.0/sites/ac2142d8-7f2f-4c27-9a23-dc015b3871cc/lists/edfed0fe-0b2e-4b82-a2dd-03028348e19b/items/1971/driveItem/children"

    private val token = "eyJ0eXAiOiJKV1QiLCJub25jZSI6Im5FenJtUjBQa29JQ1Z3eTk3NlQ1WlpHWUVJM2lCeTgxV1VtMXJ0b0YwbDQiLCJhbGciOiJSUzI1NiIsIng1dCI6IjVPZjlQNUY5Z0NDd0NtRjJCT0hIeEREUS1EayIsImtpZCI6IjVPZjlQNUY5Z0NDd0NtRjJCT0hIeEREUS1EayJ9.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC80MTM0OGRjNi01NGIzLTQ0MTItYjczNC05MmE3MjM0YTk4ZTkvIiwiaWF0IjoxNjEwMDE1MDM3LCJuYmYiOjE2MTAwMTUwMzcsImV4cCI6MTYxMDAxODkzNywiYWlvIjoiRTJKZ1lBaVpwTm94MTlTbDBmVFlvM015Q3hOUEF3QT0iLCJhcHBfZGlzcGxheW5hbWUiOiJMaW5lQm90MiIsImFwcGlkIjoiZGRlYWQ3OWItMGJmNS00Y2JjLTk3MjItODc4MDhkY2VlMWFiIiwiYXBwaWRhY3IiOiIxIiwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvNDEzNDhkYzYtNTRiMy00NDEyLWI3MzQtOTJhNzIzNGE5OGU5LyIsImlkdHlwIjoiYXBwIiwib2lkIjoiY2I0NGFiYjUtOWFmMi00OTI3LTg2OTctNDE2ZWY1ZDBiOGI0IiwicmgiOiIwLkFBQUF4bzAwUWJOVUVrUzNOSktuSTBxWTZadlg2dDMxQzd4TWx5S0hnSTNPNGF0S0FBQS4iLCJyb2xlcyI6WyJTaXRlcy5SZWFkLkFsbCJdLCJzdWIiOiJjYjQ0YWJiNS05YWYyLTQ5MjctODY5Ny00MTZlZjVkMGI4YjQiLCJ0ZW5hbnRfcmVnaW9uX3Njb3BlIjoiQVMiLCJ0aWQiOiI0MTM0OGRjNi01NGIzLTQ0MTItYjczNC05MmE3MjM0YTk4ZTkiLCJ1dGkiOiJXc3FDTVpkWlIwcUdTRWF4M3NFZUFBIiwidmVyIjoiMS4wIiwieG1zX3RjZHQiOjE0NTk4MzMwMzN9.1q_iz5kZgq8Ia09VKntCTMr5pkQZaigXp5f2QWWHUO5aWh42k3bdAt1E_JN5L2J7l_NZN_hW8tJGHG19EBcriSEAXQ-8KwmY4Jwpbr0AhkocgRgNM6Q3lXSXZ6zSPvAI1HAQuBNxOxM3EQxd_7D7fHqd_12rv7X2L_6O5_QLx2Vyk7M-pHz3GEQgqFbMMKt1oXWeIP5qza_s-mS-Y9djZYoMLL3j6XWIFrY4b5TOfN5jaeZG7UeUSOgGtEyoYvLS3yybBmXJ-RJGGQmn-lydRMVNrnN3DjAWRjIRr1mQuWhIuq-O7Ta_4LxQ5XoWy6KGTwY3L12J6eb_tWHkYh21gw"
//    private val releasePlanFile = "B2P Release Plan_20201215.xlsx"
    private val releasePlanFile = "B2P Release Plan_20210115.xlsx"


    private val clientId = "ddead79b-0bf5-4cbc-9722-87808dcee1ab"
    private val clientSecret = "8~u0C-B2m7E3ViHbl2HS0U2e_v8iN_lM5Z"

    // Excel constant //
    private val releaseIndex = 1

    private fun getReleasePlanUrl() : String? {
        val builder: UriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(releasePlanItemUrl)

        val headers = HttpHeaders()
        val newToken = requestAuthorization()?: token
        headers.setBearerAuth(newToken)

        try {
            val response = restTemplate.exchange(
                    builder.build(true).toUri(),
                    HttpMethod.GET,
                    HttpEntity(null, headers),
                    object : ParameterizedTypeReference<String>() {}
            ).body!!
            val nodeResponse = mapper.readTree(response)
            val values = nodeResponse.get("value")
            values.forEach {
                val name = it.get("name").toString().trim('"')
                if (name == releasePlanFile) {
                    return it.get("@microsoft.graph.downloadUrl").toString().trim('"')
                }
            }
            return null

        } catch (e: Exception) {
            throw Exception(e.message, e)
        }
    }


    fun getReleasePlan(args: List<String>): List<String>? {

        val linkFile = getReleasePlanUrl() ?: return null
        val output = mutableListOf<String>()
        val excelFile = File("temp.xlsx")
        FileUtils.copyURLToFile(URL(linkFile), excelFile)

        val workbook = XSSFWorkbook(excelFile)

        val sheetName = releasePlanProjectMapping[args.first().toLowerCase()] ?: throw Exception(PROJECT_NOT_FOUND)
        val sheet = workbook.getSheet(sheetName)
        val mode = releasePlanModeMapping[args.get(1).toLowerCase()] ?: throw Exception(PLAN_MODE_NOT_FOUND)
        val targetRelease = args.getOrNull(2)
        val rows = sheet.iterator()
        var currentRowIndex = 0
        var months = mapOf<String, List<Int>>()
        var days = mapOf<String, List<Int>>()
        while (rows.hasNext()) {

            val currentRow = rows.next()
            when {
                (currentRowIndex == 0) -> months = getMount(currentRow)
                (currentRowIndex == 1) -> days = getDay(currentRow)
                else-> {

                    val (release, index, date) = getValueFromRow(
                            row = currentRow,
                            mode = mode,
                            targetRelease = targetRelease)

                    index?.let {output.add("$release $mode on ${getFinalDate(it, days, months, date)}")}

                }
            }
            currentRowIndex++
        }

        workbook.close()
        excelFile.deleteOnExit()
        return output
    }

    private fun getFinalDate(index: Int,
                             days: Map<String, List<Int>>,
                             months: Map<String, List<Int>>,
                             date: String?
    ): String {
        if (date != null) return date.trimStart('(').trimEnd(')')

        val finalDay = days.mapNotNull { (day, indexes) ->
            when (index in indexes) {
                true -> day.split(":").first()
                false -> null
            }
        }.firstOrNull() ?: ""

        val finalMonth = months.mapNotNull { (month, indexes) ->
            when (index in indexes) {
                true -> month.split(":").first()
                false -> null
            }
        }.firstOrNull() ?: ""

        return "$finalDay $finalMonth"
    }

    private fun getValueFromRow(row: Row, mode: String, targetRelease: String?): Triple<String, Int?, String?> {
        val cellsInRow = row.iterator()
        var release = ""
        var outputIndex: Int? = null
        val values = mutableListOf<String>()
        while (cellsInRow.hasNext()) {

            val currentCell = cellsInRow.next()
            val value = getValue(currentCell)
            if (currentCell.columnIndex == releaseIndex) release = value
            if (value.toLowerCase().contains(mode.toLowerCase())) outputIndex = currentCell.columnIndex

            values.add(value)
        }

        return when  {
            (targetRelease != null && release.contains(targetRelease).not()) -> Triple(release, null, null)
            (mode == releasePlanModeMapping["deploy"]) -> Triple(release, outputIndex, values.getDateFromList())
            else -> Triple(release, outputIndex, null)
        }
    }

    private fun List<String>.getDateFromList(): String? {
        val value = this.last()
        return if (value.startsWith("(") && value.endsWith(")"))
            value
        else null
    }

    private fun getValue(currentCell: Cell): String {
        return when {
            currentCell.cellTypeEnum === CellType.STRING -> currentCell.stringCellValue.trim('"')
            currentCell.cellTypeEnum === CellType.NUMERIC -> currentCell.numericCellValue.toString().trim('"')
            else -> ""
        }
    }

    private fun getMount(row: Row): Map<String, List<Int>> {
        val output = mutableMapOf<String, List<Int>>()
        val cellsInRow = row.iterator()
        val indexes = mutableListOf<Int>()
        var currentMonth = ""
        while (cellsInRow.hasNext()) {

            val currentCell = cellsInRow.next()
            val value = getValue(currentCell)
            if (value.isNotEmpty()) {
                if (indexes.isNotEmpty()) {
                    // assign
                    output.put(currentMonth, indexes.toList())
                    // clear
                    indexes.clear()
                }
                currentMonth = "$value:${UUID.randomUUID()}"

            }
            indexes.add(currentCell.columnIndex)
        }
        return output
    }

    private fun getDay(row: Row): Map<String, List<Int>> {
        val output = mutableMapOf<String, List<Int>>()
        val cellsInRow = row.iterator()
        val indexes = mutableListOf<Int>()
        var currentMonth = ""
        while (cellsInRow.hasNext()) {

            val currentCell = cellsInRow.next()
            val value = getValue(currentCell)
            if (value.isNotEmpty()) {
                if (indexes.isNotEmpty()) {
                    // assign
                    output.put(currentMonth, indexes.toList())
                    // clear
                    indexes.clear()
                }
                currentMonth = "${value.toBigDecimal().setScale(0)}:${UUID.randomUUID()}"

            }
            indexes.add(currentCell.columnIndex)
        }
        return output
    }

    private fun requestAuthorization(): String? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.setBasicAuth(clientId, clientSecret)
        val httpEntity = HttpEntity("grant_type=client_credentials&scope=https://graph.microsoft.com/.default", headers)
        val response = restTemplate.exchange(
                "https://login.microsoftonline.com/41348dc6-54b3-4412-b734-92a7234a98e9/oauth2/v2.0/token",
                HttpMethod.POST,
                httpEntity,
                object : ParameterizedTypeReference<Map<String, Any?>>() {}
        ).body!!

        return response["access_token"]?.let { it.toString() }
    }

}