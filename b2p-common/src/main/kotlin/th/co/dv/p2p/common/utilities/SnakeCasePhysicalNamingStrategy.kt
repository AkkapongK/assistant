package th.co.dv.p2p.common.utilities

import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.PhysicalNamingStrategy
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment

class SnakeCasePhysicalNamingStrategy: PhysicalNamingStrategy {

    private fun toSnakeCase(id: Identifier?): Identifier? {
        if (id == null) return id
        val name: String = id.text
        val snakeName = name.replace("([a-z]+)([A-Z]+)".toRegex(), "$1\\_$2").lowercase()
        return if (snakeName != name) Identifier(snakeName, id.isQuoted) else id
    }

    override fun toPhysicalCatalogName(name: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? {
        return toSnakeCase(name)
    }

    override fun toPhysicalSchemaName(name: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? {
        return toSnakeCase(name)
    }

    override fun toPhysicalTableName(name: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? {
        return toSnakeCase(name)
    }

    override fun toPhysicalSequenceName(name: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? {
        return toSnakeCase(name)
    }

    override fun toPhysicalColumnName(name: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? {
        return toSnakeCase(name)
    }
}