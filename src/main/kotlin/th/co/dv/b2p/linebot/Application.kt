package th.co.dv.b2p.linebot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@SpringBootApplication
class Application {
    companion object {

        lateinit var downloadedContentDir: Path
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            downloadedContentDir = Files.createTempDirectory("line-bot")
            SpringApplicationBuilder(Application::class.java).run(*args)
            logger.info("******************* SPRING BOOT STARTED ************************")
        }
    }
}
