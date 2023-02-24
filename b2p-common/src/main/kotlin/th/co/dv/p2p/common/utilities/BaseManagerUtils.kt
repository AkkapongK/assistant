package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.constants.ASC
import th.co.dv.p2p.common.constants.DESC
import th.co.dv.p2p.common.constants.DOT
import th.co.dv.p2p.common.utilities.search.*
import java.lang.reflect.Field
import java.sql.Timestamp
import java.time.Instant
import java.util.*

object BaseManagerUtils {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private const val prefixItem = "item."

    /**
     * This class typically represent a period of start and end date with a length in days
     * i.e <1st Oct 00:00am, 7th Oct 00:00am, 5>
     *
     * @property periodStartDate the start date
     * @property periodEndDate the end date, typically the end of day which will be the next day 00:00am
     * @property lengthOfPeriod the length in days between the start and end date
     */
    data class Period(
            val periodStartDate: Date,
            val periodEndDate: Date,
            val lengthOfPeriod: Long)

    /**
     * This function will try to get the start date and end date of each period.
     * i.e today is 25th, periods are [7, 15, 23, 2,000,000]
     *
     * Output will be
     * <18th 00:00, 26th 00:00, 7>
     * <10th 00:00, 18th 00:00, 15>
     * <2nd 00:00, 10th 00:00, 23>
     * <...th 00:00, 2nd 00:00, 2mil>
     *
     * Take note that end date is using half-open concept which assumes the comparison is always using less than (<)
     */
    fun computePeriodsOfStartAndEnd(periodDate: List<String>): List<Period> {

        // We parse the length of each period
        // then we sort by ascending order [7, 15, 23, 2,000,0000]
        val eachPeriodLengthInDays = periodDate.map { it.toLong() }.sorted()

        val today = Instant.now()
        var currentEndDate = Timestamp.from(getEndOfDay(today))

        return eachPeriodLengthInDays.map { days ->

            // We know that the end of day actually means the next day 00:00am
            // So to calculate the start day, we need to minus additional 1 day
            // startDate = endDate - length - 1
            // i.e endDate = 26th 00:00am, length = 7
            // startDate = 26 - 7 - 1 = 18th 00:00 am

            val startDate = maxOf (
                    Timestamp.from(getStartOfDay(
                            getEndOfDay(today).minusDays(days + 1)
                            // In case startDate less than 0001-01-01T00:00:00.00Z or become negative date
                            // We take the max of to avoid getting negatives.
                    )), MINIMUM_INSTANT)

            // So we now have a Period(startDate, endDate, length)
            // i.e (18th 00:00, 26th 00:00, 7)
            val currentPeriod = Period(startDate, currentEndDate, days)
            // We need to move the end date back to the current start date
            // so if current start date = 18th 00:00 am, the new end date = 18th 00:00 am
            // so we assume the next loop will produce something similar to
            // i.e (10th 00:00, 18th 00:00, 15)
            currentEndDate = startDate

            currentPeriod
        }
    }

    /**
     * @param value 1 will return ASC, else will return DESC
     * @return String of asc or desc
     */
    fun inferSortDirection(value: Int): String {
        return when (value) {
            1 -> ASC
            else -> DESC
        }
    }

    /**
     * Method for generate select sql statement fields
     *
     * @param fields : The field that we wat to see
     */
    fun generateSelectStatement(fields: List<String>): String {
        if (fields.isEmpty()) {
            throw IllegalArgumentException("Select fields not found.")
        }

        return "SELECT ${fields.joinToString(",")}"
    }

    private val MINIMUM_INSTANT = Timestamp.from(Instant.parse("0001-01-01T00:00:00.00Z"))

    /**
     * Method to get field from field name or return null if cannot find field from the class given
     */
    inline fun <reified T> getFieldOrNull(fieldName: String): Field? {
        val clazz = T::class.java
        return try {
            clazz.getDeclaredField(fieldName)
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * Method to get field from field name or return null if cannot find field from the class given
     */
    fun <T> getFieldOrNull(fieldName: String, clazz: Class<T>): Field? {
        return try {
            clazz.getDeclaredField(fieldName)
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * Method to compute sort field first convert input field to database field
     * If result is empty means sort field is at item level so we use default sort field instead
     *
     * @param sortField input sort field from search model
     * @param defaultField default sort field
     * @param headerClass header class for getting available field
     * @param itemClass item class for getting available field
     * @param isTableColumn flag to return field in table format or model format
     * @return an eligible sort field for query
     */
    fun <H, I> computeSortField(sortField: String, defaultField: String, headerClass: Class<H>, itemClass: Class<I>, isTableColumn: Boolean = true): String {

        // Convert sort field from input
        val formattedSortField = getColumnName(listOf(sortField), headerClass = headerClass, itemClass = itemClass)

        // Check if converted field is empty mean this is item field
        // since we don't use item field as a sort field we replace with default sort field
        val fieldIsEmpty = formattedSortField.isNullOrEmpty()

        return when {
            fieldIsEmpty && isTableColumn -> getColumnName(listOf(defaultField), headerClass = headerClass, itemClass = itemClass)
            fieldIsEmpty && isTableColumn.not() -> listOf(defaultField)
            fieldIsEmpty.not() && isTableColumn.not() -> listOf(sortField)
            else -> formattedSortField
        }.first()

    }

    /**
     * Method to get column name from list of select field
     * first check that field name start with 'item.' or not
     *  - If field start with item find item field and add prefix as purchase_item
     *  - else find header field name and add prefix as purchase_order
     * If flag includeItemField is false we will skip convert item field
     *
     * @param fields list of field to convert
     * @param includeItemField flag to include item field or not, default is false
     * @return list of converted field
     */
    fun <H, I> getColumnName(fields: List<String>, includeItemField: Boolean = false, headerClass: Class<H>, itemClass: Class<I>, prefixItem: String = BaseManagerUtils.prefixItem): List<String> {

        return fields.mapNotNull {

            val isItemFields = it.startsWith(prefixItem)
            when {
                isItemFields && includeItemField -> {

                    val fieldName = it.replace(prefixItem, "")
                    val field = getFieldOrNull(fieldName, itemClass) ?: return@mapNotNull null
                    val prefix = getTableName(itemClass)
                    prefix + DOT + field.getColumnName()
                }
                else -> {
                    val fieldName = it
                    val field = getFieldOrNull(fieldName, headerClass) ?: return@mapNotNull null
                    val prefix = getTableName(headerClass)
                    prefix + DOT + field.getColumnName()

                }
            }
        }
    }

    /**
     * Method to filter select field to return only eligible field in the class
     * first check that field name start with 'item.' or not
     *  - If field start with item find item field, check with item entity
     *  - else find header field name
     * If flag includeItemField is false we will skip convert item field
     *
     * @param fields list of field to convert
     * @param includeItemField flag to include item field or not, default is false
     * @return list of converted field
     */
    fun <H, I> filteredEligibleField(fields: List<String>, includeItemField: Boolean = false, headerClass: Class<H>, itemClass: Class<I>, prefixItem: String = BaseManagerUtils.prefixItem, itemFieldName: String? = null): List<String> {

        return fields.mapNotNull {

            val isItemFields = it.startsWith(prefixItem)
            when {
                isItemFields && includeItemField -> {
                    val fieldName = it.replace(prefixItem, "")
                    val field = getFieldOrNull(fieldName, itemClass) ?: return@mapNotNull null
                    val prefix = itemFieldName!!
                    prefix + DOT + field.name
                }
                else -> {

                    val fieldName = it
                    val field = getFieldOrNull(fieldName, headerClass) ?: return@mapNotNull null
                    field.name

                }
            }
        }
    }

    /**
     * Function to generate search criteria for specific field using specific condition
     *
     * @param field name of entity field
     * @param value value of the field
     * @param searchOperation operation for search the specific field
     */
    fun<H> generateCriteriaMap(headerClass: Class<H>, field: String, value: Any, searchOperation: SearchCriteriaOperation): SearchCriterias<H> {

        val param = mapOf(field to value)
        val operation = mapOf(field to searchOperation)
        val criteria = SearchCriterias(headerClass)
        addCriteria(criteria, param, operation)

        return criteria
    }

    /**
     * Method for add group criteria to [SearchCriterias]
     */
    private fun<H> addCriteria(sc: SearchCriterias<H>, param: Map<String, Any>  = mapOf(), operation: Map<String, SearchCriteriaOperation>  = mapOf()) {
        param.forEach {
            val s = SearchCriteria()
            s.setField(it.key)
            s.setValue(it.value)
            s.setOp(operation[it.key] ?: SearchCriteriaOperation.EQUAL)
            sc.getCriterias().add(s)
        }
    }
}