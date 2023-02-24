package th.co.dv.p2p.common.utilities

import th.co.dv.p2p.common.constants.DOT
import th.co.dv.p2p.common.utilities.search.getFullColumnName
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

object StatementUtils {
    /**
     * Method for build all Class field that we using in select clause
     *
     * @param dtoClass: class that we want to build select clause
     * @param notSelectFields: field that we want to exclude in select clause
     * @param addAlias: flag for add alias or not
     *
     * @return pair of list of select field and select clause field
     */
    fun getSelectList(dtoClass: KClass<*>, entityClass: KClass<*>, notSelectFields: List<String> = emptyList(), addAlias: Boolean = true): Pair<List<String>, List<String>> {
        val selectFields = dtoClass.declaredMemberProperties.map { it.name }.filterNot { notSelectFields.contains(it) }
        val selectClauseFields = selectFields.map { selectFieldName ->
            val entityField = entityClass.java.declaredFields.single { it.name.equals(selectFieldName, ignoreCase = false) }
            buildSelectField(entityField, addAlias)
        }

        return selectFields to selectClauseFields
    }

    /**
     * Method for build column that we using in select clause
     * we will add alias for the column by replacing dot to slash
     * in case addAlias are true (default is true)
     *
     * @param field: field in entity
     * @param addAlias: flag for add alias or not
     */
    fun buildSelectField(field: Field, addAlias: Boolean = true): String {
        val columnName = field.getFullColumnName()
        return if (addAlias) {
            val alias = columnName.replace(DOT, "_")
            return "$columnName AS $alias"
        } else columnName
    }

    /**
     * Method for create order by clause
     */
    inline fun <reified T> createOrderByClause(sortField: String, sortOrder: Int): String {
        val sortColumnField = T::class.java.getDeclaredField(sortField)
        val sortColumnFullName = sortColumnField.getFullColumnName()
        val sortDirection = BaseManagerUtils.inferSortDirection(sortOrder)
        return "$sortColumnFullName $sortDirection"
    }
}