import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.13"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id ("com.github.ManifestClasspath") version "0.1.0-RELEASE"

	kotlin("jvm") version "1.5.31"
	kotlin("plugin.spring") version "1.5.31"

	id("org.jetbrains.kotlin.plugin.jpa") version "1.5.31"
	id("com.google.cloud.tools.jib") version "3.0.0"
	id("net.researchgate.release") version "2.8.1"
	id("org.sonarqube") version "3.3"
	id("de.aaschmid.cpd") version "3.3"
	jacoco
}

cpd {
	language = "kotlin"
	minimumTokenCount = 100 // approximately 5-10 lines
	isIgnoreFailures = true
}

group = "th.co.dv.p2p"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8


repositories {
	mavenCentral()
	jcenter()
	maven { url = uri("https://plugins.gradle.org/m2/") }
	maven { url = uri("https://software.r3.com/artifactory/corda") }
}
configurations.all {
	resolutionStrategy {
		cacheChangingModulesFor(0, TimeUnit.SECONDS)
		cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
	}
}

extra["springCloudVersion"] = "2020.0.3"
extra["azureVersion"] = "2.1.6"

dependencies {
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.flywaydb:flyway-core")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.microsoft.sqlserver:mssql-jdbc:7.0.0.jre8")
	implementation("javax.json:javax.json-api:1.1.4")
	implementation("com.google.code.gson:gson:${property("gsonVersion")}")
	implementation("com.google.guava:guava:21.0")
	implementation("org.apache.commons:commons-lang3:3.9")
	implementation("org.springframework.boot:spring-boot-starter-amqp")

	implementation("com.microsoft.azure:azure-keyvault-secrets-spring-boot-starter:${property("azureVersion")}")

	//updatefor930
	implementation("org.springframework.security:spring-security-rsa:1.0.11.RELEASE")
	implementation("org.wildfly.common:wildfly-common:1.5.4.Final")
	implementation("io.micrometer:micrometer-core:1.7.5"){
		isForce = true
	}
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
	implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
	implementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.5.12")
	implementation("io.springfox:springfox-swagger2:3.0.0")
	implementation("io.springfox:springfox-swagger-ui:3.0.0")
	implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
	implementation("io.netty:netty-all:4.1.69.Final")
	implementation("org.springframework.data:spring-data-redis:2.5.11")

	// corda libs
	implementation("net.corda:corda-rpc:4.4")

	// Spring Cloud
	implementation("org.springframework.cloud:spring-cloud-starter-kubernetes:1.1.10.RELEASE")
	implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-config:1.1.10.RELEASE")
	implementation("org.springframework.cloud:spring-cloud-commons:3.0.5")
	implementation("org.springframework.cloud:spring-cloud-context:3.0.5")

	// For redis
	implementation("redis.clients:jedis")

	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.springframework.security:spring-security-test")

	//updatefor930
	testImplementation("io.mockk:mockk:1.12.1")
	testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.13") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.springframework:spring-test:5.3.9")
	testImplementation("junit:junit:4.13.2")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.5.31")

//	// including sub-project
//	implementation(project(":b2p-common"))

	//upgrade log4j CVE-2021-44228
	implementation("org.apache.logging.log4j:log4j-core:2.17.1")
	implementation("org.apache.logging.log4j:log4j-api:2.17.1")

	// line
	implementation("com.linecorp.bot:line-bot-spring-boot:1.20.0")


	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")

}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
		mavenBom("com.microsoft.azure:azure-spring-boot-bom:${property("azureVersion")}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict", "-Xskip-metadata-version-check")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	setForkEvery(200) // prevent Java OOM error on over 1 GB of mem usage
	testLogging {
		// set options for log level LIFECYCLE
		events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
		exceptionFormat = TestExceptionFormat.FULL
		showExceptions = true
		showCauses = true
		showStackTraces = true

		afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
			if (desc.parent == null) { // will match the outermost suite
				val output = "|  Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)  |"
				val repeatLength = output.length
				val extraString = "".padEnd(repeatLength, '-')
				print(extraString + "\n" + output + "\n" + extraString)
			}
			null
		}))

	}
}

tasks.withType<Jar> {
	manifest {
		attributes["Main-Class"] = "th.co.dv.p2p.usernotify.Application"
	}
}

tasks.cpdCheck {
	reports {
		xml.required.set(false)
		text.required.set(true)
	}

	val main = fileTree("src/main/kotlin")
	source = main
}

subprojects {
   tasks.withType<Test> {
      setExcludes(listOf("**"))
   }
}

jib {
	container {
		ports = listOf("10550")
	}
	from {
		image = "b2papp.azurecr.io/b2p/basedocker:9.3.2"
	}
	to {
		image = "b2papp.azurecr.io/b2p/usernotify"
	}
}
