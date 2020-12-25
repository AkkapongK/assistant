import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.1.8.RELEASE"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	id ("com.github.ManifestClasspath") version "0.1.0-RELEASE"

	kotlin("jvm") version "1.2.71"
	kotlin("plugin.spring") version "1.2.71"

	id("org.sonarqube") version "2.7"
	id("org.jetbrains.kotlin.plugin.jpa") version "1.2.71"

	jacoco

}

group = "th.co.dv.b2p"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
	jcenter()
	maven { url = uri("https://plugins.gradle.org/m2/") }
	maven {
		name = "nexus"
		url = uri("${project.property("repoUrl")}")
		credentials {
			username = "${project.property("repoUser")}"
			password = "${project.property("repoPassword")}"
		}
	}
}

extra["azureVersion"] = "2.1.6"
extra["springCloudVersion"] = "Greenwich.SR3"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${property("jacksonModuleKotlinVersion")}")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.micrometer:micrometer-core:1.1.6") {
		isForce = true
	}
	implementation("com.linecorp.bot:line-bot-spring-boot:1.20.0")
	implementation("com.lordcodes.turtle:turtle:0.2.0")

	implementation("com.natpryce:konfig:${property("konfigVersion")}")
}

tasks.jar {
	exclude("**/*.yml")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict", "-Xskip-metadata-version-check")
		jvmTarget = "1.8"
	}
}
