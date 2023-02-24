package th.co.dv.p2p.usernotify

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
//@EnableJpaRepositories(basePackages = ["th.co.dv.p2p.usernotify.repository"],
//		repositoryFactoryBeanClass = CustomJpaRepositoryFactoryBean::class
//)
@EnableRetry
@EnableAutoConfiguration(exclude = [FlywayAutoConfiguration::class, DataSourceAutoConfiguration::class])
class Application {

	companion object {
		private val logger: Logger = LoggerFactory.getLogger(Application::class.java)
		@JvmStatic
		fun main(args: Array<String>) {
			val isJob = args.any { it.lowercase().contains("job") }
			val web = if (isJob) {
				WebApplicationType.NONE
			} else {
				WebApplicationType.SERVLET
			}
			SpringApplicationBuilder(Application::class.java)
					.web(web) // .REACTIVE, .SERVLET
					.run(*args)
			logger.info("******************* SPRING BOOT STARTED ************************")
		}
	}
}