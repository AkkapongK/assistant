package th.co.dv.p2p.common.utilities

import com.natpryce.konfig.*
import java.io.File
import java.time.ZoneId

/**
 * A singleton to define the time zone the node should use during calculation.
 * The config is expected to be stored in node.config file as "timeZone = Asia/Bangkok"
 */
object NodeTimeZone {
    private val timeZone by stringType
    // TODO: this files are not in api.jar. we need to fix this.
    private const val pathName = "./node.conf"
    private const val resourceName = "default.properties"

    private val config = try {
        ConfigurationProperties.systemProperties() overriding
                EnvironmentVariables() overriding
                if (File(pathName).exists()) {
                    println("NodeTimeZone config Path pathName: $pathName")
                    ConfigurationProperties.fromFile(File(pathName))
                } else {
                    println("NodeTimeZone config Resource resourceName: $resourceName")
                    ConfigurationProperties.fromResource(resourceName)
                }
    } catch (ex: Exception) {
        // For whatever reason it threw error, we give back null
        null
    }

    @Suppress("deprecation")
    fun getTimezone(): String {
        return try {
            config?.get(timeZone) ?: ZoneId.of(BANGKOK_ZONE).id
        } catch (ex: Exception) {
            println("Unknown config path arguments: ${ex.message}")
            ZoneId.of(BANGKOK_ZONE).id
        }
    }
}