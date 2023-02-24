pluginManagement {
    repositories {
        jcenter()
        maven { url = uri("https://repo.spring.io/milestone") }
        gradlePluginPortal()
    }
    plugins {
        id("org.springframework.boot") version "2.5.13"
        id("io.spring.dependency-management") version "1.0.11.RELEASE"

        kotlin("jvm") version "1.5.31"
        kotlin("kapt") version "1.5.31"
        kotlin("plugin.spring") version "1.5.31"
    }
}
rootProject.name = "p2p-common"
