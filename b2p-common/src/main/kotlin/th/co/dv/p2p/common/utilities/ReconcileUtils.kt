package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.*
import th.co.dv.p2p.common.constants.ReconcileStatements.ADDITIONAL_JOIN_STATEMENT
import th.co.dv.p2p.common.constants.ReconcileStatements.SCHEMA_STATEMENT
import th.co.dv.p2p.common.constants.ReconcileStatements.TABLE_NAME_STATEMENT
import th.co.dv.p2p.common.constants.ReconcileStatements.WHERE_CONDITION_STATEMENT
import java.text.SimpleDateFormat

object ReconcileUtils {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)


    /**
     * Method for convert states to mapping of linear id and data
     */
    fun convertDataToLinearIdMap(dataMap: List<Map<String, Any>>): Map<String, MutableMap<String, Any>> {
        logger.info("convertDataToLinearIdMap dataMap : $dataMap")
        return dataMap.map { it["linear_id"].toString() to it.toMutableMap() }.toMap()
    }


    /**
     * Method for generate sql for get linear id of the target state and criterias
     * 1. linear id
     * 2. start date
     * 3. end date
     *
     * @param schema: service's schema
     * @param tableName: table name
     * @param linearIds: Target linear id (optional)
     * @param startDate: Target start date (optional)
     * @param endDate: Target end date (optional)
     */
    fun buildGetLinearIdMSCriteria(schema: MicroServiceSchema,
                                   tableName: String,
                                   linearIds: List<String>? = null,
                                   lifeCycles: List<String>? = null,
                                   startDate: String? = null,
                                   endDate: String? = null,
                                   additionalJoinStatement: String = EMPTY_STRING): String {

        logger.info("buildGetLinearIdCriteria schema : ${schema.schema} tableName : $tableName linearIds : $linearIds startDate : $startDate  endDate : $endDate")
        val criterias = mutableListOf<String>()

        if (linearIds != null) criterias.add("t.linear_id IN (${linearIds.toSet().joinToString(",") { "'$it'" }}) ")

        if (lifeCycles != null) criterias.add("t.lifecycle IN (${lifeCycles.toSet().joinToString(",") { "'$it'" }}) ")

        if (startDate != null) {
            val formattedStartDate = SimpleDateFormat(DATE_TIME_FORMAT_WO_TZ).parse(startDate).let {
                SimpleDateFormat(DateUtility.WS_DATE_TIME_FORMAT).format(it)
            }
            criterias.add("t.created_date >= '$formattedStartDate'")
        }

        if (endDate != null) {
            val formattedEndDate = SimpleDateFormat(DATE_TIME_FORMAT_WO_TZ).parse(endDate).let {
                SimpleDateFormat(DateUtility.WS_DATE_TIME_FORMAT).format(it)
            }
            criterias.add("t.created_date <= '$formattedEndDate'")
        }

        val whereSql = if (criterias.isNotEmpty()) {
            criterias.joinToString(separator = AND, prefix = WHERE_CLAUSE, postfix = ";")
        } else {
            EMPTY_STRING
        }


        return ReconcileStatements.QUERY_LINEAR_ID_MS_SQL
                .replace(SCHEMA_STATEMENT, schema.schema)
                .replace(TABLE_NAME_STATEMENT, tableName)
                .replace(ADDITIONAL_JOIN_STATEMENT, additionalJoinStatement)
                .replace(WHERE_CONDITION_STATEMENT, whereSql)
    }
}