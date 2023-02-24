package th.co.dv.p2p.common.utilities.search

import org.apache.commons.logging.LogFactory
import th.co.dv.p2p.common.constants.DOT
import th.co.dv.p2p.common.constants.ESCAPE_SQL
import th.co.dv.p2p.common.constants.escapeChar
import th.co.dv.p2p.common.utilities.StringUtility
import java.lang.reflect.Field
import java.math.BigDecimal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.persistence.Column
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.From
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate

class SearchCriteria : SearchCondition {

    companion object {
        const val START_DATE = "startDate"
        const val END_DATE = "endDate"
        const val DATE_FROM = "dateFrom"
        const val DATE_TO = "dateTo"
        const val DIRECTION = "direction"
        const val SORT_FIELD = "sortField"
        const val SORT_ORDER = "sortOrder"
        const val PAGE_SIZE = "pageSize"
        const val PAGE = "page"
        const val PAGE_NUMBER = "pageNumber"
        const val SELECT_FIELDS = "selectFields"
    }

    val OP_EQUAL = " = "
    val OP_NOT_EQUAL = " <> "
    val OP_LESSTHAN = " < "
    val OP_LESSTHAN_OR_EQUAL = " <= "
    val OP_GREATERTHAN = " > "
    val OP_GREATERTHAN_OR_EQUAL = " >= "
    val OP_STARTS_WITH = " START "
    val OP_ENDS_WITH = " END "
    val OP_CONTAIN = " CONTAIN "
    val OP_IN = " IN "
    val OP_NOT_IN = " NOT IN "
    val OP_BETWEEN = " BETWEEN "
    val OP_IN_SUBQUERY = " IN SUBQUERY "
    val OP_NOT_IN_SUBQUERY = " NOT IN SUBQUERY "


    private val logger = LogFactory.getLog(SearchCriteria::class.java)

    private var path: String? = null

    private var field: String? = null

    private var type: String? = null

    private var op = SearchCriteriaOperation.EQUAL

    private var value: Any? = null

    private var value2: Any? = null

    private var and = true

    /**
     * @return the field
     */
    fun getField(): String? {
        return field
    }

    /**
     * @param field the field to set
     */
    fun setField(field: String) {
        this.field = field
    }

    /**
     * @return the value
     */
    fun getValue(): Any? {
        return value
    }

    /**
     * @param value the value to set
     */
    fun setValue(value: Any) {
        this.value = value
    }

    /**
     * @return the type
     */
    fun getType(): String? {
        return type
    }

    /**
     * @param type the type to set
     */
    fun setType(type: String) {
        this.type = type
    }

    /**
     * @return the op
     */
    fun getRealOp(): String {
        return op.realOp
    }

    fun getOp(): SearchCriteriaOperation {
        return op
    }

    /**
     * @param op the op to set
     */
    fun setOp(op: String) {
        when {
            OP_EQUAL == op -> this.op = SearchCriteriaOperation.EQUAL
            OP_NOT_EQUAL == op -> this.op = SearchCriteriaOperation.NOT_EQUAL
            OP_LESSTHAN == op -> this.op = SearchCriteriaOperation.LESSTHAN
            OP_LESSTHAN_OR_EQUAL == op -> this.op = SearchCriteriaOperation.LESSTHAN_OR_EQUAL
            OP_GREATERTHAN == op -> this.op = SearchCriteriaOperation.GREATERTHAN
            OP_GREATERTHAN_OR_EQUAL == op -> this.op = SearchCriteriaOperation.GREATERTHAN_OR_EQUAL
            OP_STARTS_WITH == op -> this.op = SearchCriteriaOperation.STARTS_WITH
            OP_ENDS_WITH == op -> this.op = SearchCriteriaOperation.ENDS_WITH
            OP_CONTAIN == op -> this.op = SearchCriteriaOperation.CONTAIN
            OP_IN == op -> this.op = SearchCriteriaOperation.IN
            OP_NOT_IN == op -> this.op = SearchCriteriaOperation.NOT_IN
            OP_BETWEEN == op -> this.op = SearchCriteriaOperation.BETWEEN
            OP_IN_SUBQUERY == op -> this.op = SearchCriteriaOperation.IN_SUBQUERY
            OP_NOT_IN_SUBQUERY == op -> this.op = SearchCriteriaOperation.NOT_IN_SUBQUERY
            else -> this.op = SearchCriteriaOperation.EQUAL
        }

    }

    fun setOp(op: SearchCriteriaOperation) {
        this.op = op
    }

    override fun createPredicate(cb: CriteriaBuilder, root: Path<*>, froms: Hashtable<String, From<*, *>>): Predicate? {
        if (SearchCriteriaOperation.EQUAL == op) {
            return createEqualPredicate(cb, root)
        }
        if (SearchCriteriaOperation.NOT_EQUAL == op) {
            return createNotEqualPredicate(cb, root)
        }

        if (SearchCriteriaOperation.LESSTHAN == op) {
            return createLessThanPredicate(cb, root)
        }
        if (SearchCriteriaOperation.LESSTHAN_OR_EQUAL == op) {
            return createLessThanOrEqualPredicate(cb, root)
        }
        if (SearchCriteriaOperation.GREATERTHAN == op) {
            return createGreaterThanPredicate(cb, root)
        }
        if (SearchCriteriaOperation.GREATERTHAN_OR_EQUAL == op) {
            return createGreaterThanOrEqualPredicate(cb, root)
        }
        if (SearchCriteriaOperation.STARTS_WITH == op || SearchCriteriaOperation.ENDS_WITH == op || SearchCriteriaOperation.CONTAIN == op) {
            return createLikePredicate(cb, root)
        }
        if (SearchCriteriaOperation.IN == op) {
            return createInPredicate(cb, root)
        }
        if (SearchCriteriaOperation.NOT_IN == op) {
            return createNotInPredicate(cb, root)
        }
        if (SearchCriteriaOperation.ISNULL == op) {
            return createIsNullPredicate(cb, root)
        }
        if (SearchCriteriaOperation.NOTNULL == op) {
            return createNotNullPredicate(cb, root)
        }
        if (SearchCriteriaOperation.BETWEEN == op) {
            return createBetweenPredicate(cb, root)
        }
        if (SearchCriteriaOperation.LIKE == op) {
            return createLikeWithoutAddAnyPercent(cb, root)
        }
        if (SearchCriteriaOperation.NOT_LIKE == op) {
            return createNotLikeWithoutAddAnyPercent(cb, root)
        }

        return null

    }

    @Suppress("UNCHECKED_CAST")
    override fun createWhere(tableName: String, tableClass: Class<*>, sc: SearchCriterias<*>): String? {
        val fieldFromTable = tableClass.getDeclaredField(field)
        val columnName = fieldFromTable.getColumnName()
        val initWhereCondition = " $tableName.$columnName "
        val finalCondition = when {
            (SearchCriteriaOperation.EQUAL == op) -> createSingleValueCondition(sc, "=", value)
            (SearchCriteriaOperation.NOT_EQUAL == op) -> createSingleValueCondition(sc, "!=", value)
            (SearchCriteriaOperation.LESSTHAN == op) -> createSingleValueCondition(sc, "<", value)
            (SearchCriteriaOperation.LESSTHAN_OR_EQUAL == op) -> createSingleValueCondition(sc, "<=", value)
            (SearchCriteriaOperation.GREATERTHAN == op) -> createSingleValueCondition(sc, ">", value)
            (SearchCriteriaOperation.GREATERTHAN_OR_EQUAL == op) -> createSingleValueCondition(sc, ">=", value)
            (SearchCriteriaOperation.ISNULL == op) -> " IS NULL "
            (SearchCriteriaOperation.NOTNULL == op) -> " IS NOT NULL "
            (SearchCriteriaOperation.LIKE == op) -> createSingleValueCondition(sc, "LIKE", "${escapeLikeOperation(value)}", true)
            (SearchCriteriaOperation.NOT_LIKE == op) -> createSingleValueCondition(sc, "NOT LIKE", "${escapeLikeOperation(value)}", true)
            (SearchCriteriaOperation.STARTS_WITH == op) -> createSingleValueCondition(sc, "LIKE", "${escapeLikeOperation(value)}%", true)
            (SearchCriteriaOperation.ENDS_WITH == op) -> createSingleValueCondition(sc, "LIKE", "%${escapeLikeOperation(value)}", true)
            (SearchCriteriaOperation.CONTAIN == op) -> createSingleValueCondition(sc, "LIKE", "%${escapeLikeOperation(value)}%", true)
            (SearchCriteriaOperation.IN == op) -> createMultiValueCondition(sc, "IN", value as List<Any>)
            (SearchCriteriaOperation.NOT_IN == op) -> createMultiValueCondition(sc, "NOT IN", value as List<Any>)
            (SearchCriteriaOperation.IN_SUBQUERY == op) -> createSubQueryCondition(sc, "IN", value)
            (SearchCriteriaOperation.NOT_IN_SUBQUERY == op) -> createSubQueryCondition(sc, "NOT IN", value)
            //TODO: add more condition here
            else -> null
        }

        return initWhereCondition + finalCondition
    }

    private fun createSingleValueCondition(sc: SearchCriterias<*>, operation: String, value: Any?, escapeChar: Boolean = false): String {
        if (sc.getApplyParameterValue()) sc.appendParameterValue(value)
        return when (escapeChar) {
            true -> " $operation ? $ESCAPE_SQL "
            false -> " $operation ? "
        }
    }

    private fun createMultiValueCondition(sc: SearchCriterias<*>, operation: String, value: List<Any>): String {
        if (sc.getApplyParameterValue()) sc.appendParameterValues(value)
        val alterValue = value.joinToString(separator = ", ", prefix = "(", postfix = ")") { " ? " }
        return " $operation $alterValue "
    }

    private fun createSubQueryCondition(sc: SearchCriterias<*>, operation: String, value: Any?): String {
        if (sc.getApplyParameterValue()) {
            val subQueryValue = sc.getSubQueryParameterValue().first()
            subQueryValue.forEach { sc.appendParameterValue(it) }
            sc.removeFirstSubQueryParameterValue()
        }
        return " $operation ($value) "
    }


    private fun createEqualPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        if (null == value) {
            return cb.isNull(root.get<Any>(field))
        }

        val c = root.get<Any>(field).javaType

        return when (c.name) {
            "boolean" -> cb.equal(root.get<Any>(field).`as`(Boolean::class.javaPrimitiveType), value!!.toString().toBoolean())
            "java.lang.Boolean" -> cb.equal(root.get<Any>(field).`as`(Boolean::class.java), value!!.toString().toBoolean())
            "long" -> cb.equal(root.get<Any>(field).`as`(Long::class.javaPrimitiveType), value!!.toString().toLong())
            "java.lang.Long" -> cb.equal(root.get<Any>(field).`as`(Long::class.java), value!!.toString().toLong())
            "int" -> cb.equal(root.get<Any>(field).`as`(Integer.TYPE), value!!.toString().toInt())
            "java.lang.Integer" -> cb.equal(root.get<Any>(field).`as`(Int::class.java), value!!.toString().toInt())
            "double" -> cb.equal(root.get<Any>(field).`as`(Double::class.javaPrimitiveType), value!!.toString().toDouble())
            "java.lang.Double" -> cb.equal(root.get<Any>(field).`as`(Double::class.java), value!!.toString().toDouble())
            "java.math.BigDecimal" -> cb.equal(root.get<Any>(field).`as`(BigDecimal::class.java), BigDecimal(value!!.toString()))
            "java.util.Date" -> if (value is Date) cb.equal(root.get<Any>(field).`as`(Date::class.java), value as Date?) else cb.equal(root.get<Any>(field).`as`(Date::class.java), parseDate(value!!.toString()))
            else -> cb.equal(root.get<Any>(field), value)
        }

    }

    private fun parseDate(value: String): Date? {
        return try {
            SimpleDateFormat("dd/MM/yyyy").parse(value)
        } catch (ex: ParseException) {
            null
        }

    }

    private fun createNotEqualPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        return if (null == value) {
            cb.isNotNull(root.get<Any>(field))
        } else cb.notEqual(root.get<Any>(field), value)

    }

    private fun createIsNullPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        return cb.isNull(root.get<Any>(field))
    }

    private fun createNotNullPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        return cb.isNotNull(root.get<Any>(field))
    }

    private fun createLessThanPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        if (null == value) {
            throw UnsupportedOperationException("Cannot compare null with Less than operation.")
        }

        val c = root.get<Any>(field).javaType
        if (c.name.lowercase().contains("boolean")) {
            throw UnsupportedOperationException("Less than operation is not support type: " + c.name)
        }

        return when (c.name) {
            "long" -> cb.lessThan(root.get<Any>(field).`as`(Long::class.javaPrimitiveType), value!!.toString().toLong())
            "java.lang.Long" -> cb.lessThan(root.get<Any>(field).`as`(Long::class.java), value!!.toString().toLong())
            "int" -> cb.lessThan(root.get<Any>(field).`as`(Integer.TYPE), value!!.toString().toInt())
            "java.lang.Integer" -> cb.lessThan(root.get<Any>(field).`as`(Int::class.java), value!!.toString().toInt())
            "double" -> cb.lessThan(root.get<Any>(field).`as`(Double::class.javaPrimitiveType), value!!.toString().toDouble())
            "java.lang.Double" -> cb.lessThan(root.get<Any>(field).`as`(Double::class.java), value!!.toString().toDouble())
            "java.math.BigDecimal" -> cb.lessThan(root.get<Any>(field).`as`(BigDecimal::class.java), BigDecimal(value!!.toString()))
            "java.util.Date" -> cb.lessThan(root.get<Any>(field).`as`(Date::class.java), value as Date?)
            else -> cb.lessThan(root.get<Any>(field).`as`(String::class.java), value!!.toString())
        }

    }

    private fun createLessThanOrEqualPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        if (null == value) {
            throw UnsupportedOperationException("Cannot compare null with Less than or equal operation.")
        }

        val c = root.get<Any>(field).javaType
        if (c.name.lowercase().contains("boolean")) {
            throw UnsupportedOperationException("Less than or equal operation is not support type: " + c.name)
        }

        return when (c.name) {
            "long" -> cb.lessThanOrEqualTo(root.get<Any>(field).`as`(Long::class.javaPrimitiveType), value!!.toString().toLong())
            "java.lang.Long" -> cb.lessThanOrEqualTo(root.get<Any>(field).`as`(Long::class.java), value!!.toString().toLong())
            "int" -> cb.lessThanOrEqualTo(root.get<Any>(field).`as`(Integer.TYPE), value!!.toString().toInt())
            "java.lang.Integer" -> cb.lessThanOrEqualTo(root.get<Any>(field).`as`(Int::class.java), value!!.toString().toInt())
            "double" -> cb.lessThanOrEqualTo(root.get<Any>(field).`as`(Double::class.javaPrimitiveType), value!!.toString().toDouble())
            "java.lang.Double" -> cb.lessThanOrEqualTo(root.get<Any>(field).`as`(Double::class.java), value!!.toString().toDouble())
            "java.math.BigDecimal" -> cb.lessThanOrEqualTo(root.get<Any>(field).`as`(BigDecimal::class.java), BigDecimal(value!!.toString()))
            "java.util.Date" -> cb.lessThanOrEqualTo(root.get<Any>(field).`as`(Date::class.java), value as Date?)
            else -> cb.lessThanOrEqualTo(root.get<Any>(field).`as`(String::class.java), value!!.toString())
        }

    }

    private fun createGreaterThanPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        if (null == value) {
            throw UnsupportedOperationException("Cannot compare null with Greater than operation.")
        }

        val c = root.get<Any>(field).javaType
        if (c.name.lowercase().contains("boolean")) {
            throw UnsupportedOperationException("Greater than operation is not support type: " + c.name)
        }

        return when (c.name) {
            "long" -> cb.greaterThan(root.get<Any>(field).`as`(Long::class.javaPrimitiveType), value!!.toString().toLong())
            "java.lang.Long" -> cb.greaterThan(root.get<Any>(field).`as`(Long::class.java), value!!.toString().toLong())
            "int" -> cb.greaterThan(root.get<Any>(field).`as`(Integer.TYPE), value!!.toString().toInt())
            "java.lang.Integer" -> cb.greaterThan(root.get<Any>(field).`as`(Int::class.java), value!!.toString().toInt())
            "double" -> cb.greaterThan(root.get<Any>(field).`as`(java.lang.Double.TYPE), value!!.toString().toDouble())
            "java.lang.Double" -> cb.greaterThan(root.get<Any>(field).`as`(Double::class.java), value!!.toString().toDouble())
            "java.math.BigDecimal" -> cb.greaterThan(root.get<Any>(field).`as`(BigDecimal::class.java), BigDecimal(value!!.toString()))
            "java.util.Date" -> cb.greaterThan(root.get<Any>(field).`as`(Date::class.java), value as Date?)
            else -> cb.greaterThan(root.get<Any>(field).`as`(String::class.java), value!!.toString())
        }

    }

    private fun createGreaterThanOrEqualPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        if (null == value) {
            throw UnsupportedOperationException("Cannot compare null with Greater than or equal than operation.")
        }

        val c = root.get<Any>(field).javaType
        if (c.name.lowercase().contains("boolean")) {
            throw UnsupportedOperationException("Greater than or equal operation is not support type: " + c.name)
        }

        return when (c.name) {
            "long" -> cb.greaterThanOrEqualTo(root.get<Any>(field).`as`(Long::class.javaPrimitiveType), value!!.toString().toLong())
            "java.lang.Long" -> cb.greaterThanOrEqualTo(root.get<Any>(field).`as`(Long::class.java), value!!.toString().toLong())
            "int" -> cb.greaterThanOrEqualTo(root.get<Any>(field).`as`(Integer.TYPE), value!!.toString().toInt())
            "java.lang.Integer" -> cb.greaterThanOrEqualTo(root.get<Any>(field).`as`(Int::class.java), value!!.toString().toInt())
            "double" -> cb.greaterThanOrEqualTo(root.get<Any>(field).`as`(Double::class.javaPrimitiveType), value!!.toString().toDouble())
            "java.lang.Double" -> cb.greaterThanOrEqualTo(root.get<Any>(field).`as`(Double::class.java), value!!.toString().toDouble())
            "java.math.BigDecimal" -> cb.greaterThanOrEqualTo(root.get<Any>(field).`as`(BigDecimal::class.java), BigDecimal(value!!.toString()))
            "java.util.Date" -> cb.greaterThanOrEqualTo(root.get<Any>(field).`as`(Date::class.java), value as Date?)
            else -> cb.greaterThanOrEqualTo(root.get<Any>(field).`as`(String::class.java), value!!.toString())
        }

    }

    private fun createLikeWithoutAddAnyPercent(cb: CriteriaBuilder, root: Path<*>): Predicate {
        if (null == value) {
            return cb.isNull(root.get<Any>(field))
        }
        val v = escapeLikeOperation(value!!.toString())
        return cb.like(root.get<Any>(field).`as`(String::class.java), v, escapeChar)
    }

    private fun createNotLikeWithoutAddAnyPercent(cb: CriteriaBuilder, root: Path<*>): Predicate {
        if (null == value) {
            return cb.isNull(root.get<Any>(field))
        }
        val v = escapeLikeOperation(value!!.toString())
        return cb.notLike(root.get<Any>(field).`as`(String::class.java), v, escapeChar)
    }

    private fun createLikePredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        if (null == value) {
            return cb.isNull(root.get<Any>(field))
        }

        val c = root.get<Any>(field).javaType
        if (!c.name.lowercase().contains("string")) {
            throw UnsupportedOperationException("Like operation is not support type: " + c.name)
        }

        var v = escapeLikeOperation(value!!.toString())
        if (op == SearchCriteriaOperation.STARTS_WITH || op == SearchCriteriaOperation.CONTAIN) {
            v = "$v%"

        }
        if (op == SearchCriteriaOperation.ENDS_WITH || op == SearchCriteriaOperation.CONTAIN) {
            v = "%$v"
        }

        return cb.like(root.get<Any>(field).`as`(String::class.java), v, escapeChar)
    }


    private fun createInPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        return if (value !is Collection<*>) {
            createEqualPredicate(cb, root)
        } else cb.`in`(root.get<Any>(field)).value(value)

    }

    private fun createNotInPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        return if (value !is Collection<*>) {
            createNotEqualPredicate(cb, root)
        } else cb.`in`(root.get<Any>(field)).value(value).not()

    }

    private fun createBetweenPredicate(cb: CriteriaBuilder, root: Path<*>): Predicate {
        if (null == value) {
            throw UnsupportedOperationException("Cannot compare null with Between operation.")
        }
        if (null == value2) {
            throw UnsupportedOperationException("Cannot compare null with Between operation.")
        }

        return if (value is Date) cb.between(root.get<Any>(field).`as`(Date::class.java), value as Date?, value2 as Date?) else cb.between(root.get<Any>(field).`as`(BigDecimal::class.java), value as BigDecimal?, value2 as BigDecimal?)

    }

    /**
     * @return the path
     */
    fun getPath(): String? {
        return path
    }

    /**
     * @param path the path to set
     */
    fun setPath(path: String) {
        this.path = path
    }

    /**
     * @return the and
     */
    override fun isAnd(): Boolean {
        return and
    }

    /**
     * @param and the and to set
     */
    override fun setAnd(and: Boolean) {
        this.and = and
    }

    /**
     * @return the value2
     */
    fun getValue2(): Any? {
        return value2
    }

    /**
     * @param value2 the value2 to set
     */
    fun setValue2(value2: Any) {
        this.value2 = value2
    }
}


/**
 * Method for get column name in database
 * 1. we get from column anotation
 * 2. get from field name (Convert camel to snake case)
 *
 */
fun Field.getColumnName(): String {
    val nameFromAnnotationColumn = this.getAnnotation(Column::class.java)?.name
    return when (nameFromAnnotationColumn.isNullOrBlank()) {
        true -> StringUtility.camelToSnakeCase(this.name)
        false -> nameFromAnnotationColumn
    }
}

/**
 * Method to escape input value for like operation
 */
fun escapeLikeOperation(input: Any?): String? {
    val value = input?.toString()
    if (value.isNullOrBlank()) return value
    return value
        .replace("$escapeChar", "$escapeChar$escapeChar")
        .replace("'", "$escapeChar'")
        .replace("_", "${escapeChar}_")
        .replace("%", "$escapeChar%")
}

/**
 * Method for get column name
 * we create column from entity's field
 * example field externalId in Invoice
 * the output will be invoice.external_id
 *
 *
 */
fun Field.getFullColumnName(): String {
    val tableName = getTableName(this.declaringClass)
    val columnName = this.getColumnName()
    return "${tableName}$DOT${columnName}"
}
