pluginManagement {

	val kotlinVersion: String by settings
	val springBootVersion: String by settings
	val springDependencyManagementVersion: String by settings

	plugins {
		kotlin("jvm") version kotlinVersion apply false
		kotlin("plugin.spring") version kotlinVersion apply false
		kotlin("plugin.jpa") version kotlinVersion apply false
		`maven-publish`
		id("org.springframework.boot") version springBootVersion apply false
		id("io.spring.dependency-management") version springDependencyManagementVersion apply false
	}

	repositories {
		gradlePluginPortal()
		mavenCentral()
		maven {
			name = "maven-snapshots"
			url = uri("https://artifacts.wavesenterprise.com/repository/maven-snapshots/")
			mavenContent {
				snapshotsOnly()
			}
		}
		maven {
			name = "maven-releases"
			url = uri("https://artifacts.wavesenterprise.com/repository/maven-releases/")
			mavenContent {
				releasesOnly()
			}
		}
		mavenLocal()
	}
}

rootProject.name = "WavesPostal"

include("WavesPostalContract")