import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  application
  `java-library`
  `maven-publish`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.pambrose.stable.versions)
  alias(libs.plugins.pambrose.kotlinter)
  alias(libs.plugins.pambrose.testing) apply false
  alias(libs.plugins.shadow) apply false
}

allprojects {
  apply(plugin = "application")
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "com.pambrose.kotlinter")
  apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

  version = findProperty("overrideVersion")?.toString() ?: "0.25.0"
  group = "com.kslides"
  description = "kslides"

  repositories {
    mavenCentral()
  }

  configure<PublishingExtension> {
    publications {
      create<MavenPublication>("mavenJava") {
        from(components["java"])
        versionMapping {
          usage("java-api") {
            fromResolutionOf("runtimeClasspath")
          }
          usage("java-runtime") {
            fromResolutionResult()
          }
        }
      }
    }
  }
}

subprojects {
  apply(plugin = "com.pambrose.testing")

  val libs = rootProject.the<org.gradle.accessors.dm.LibrariesForLibs>()

  dependencies {
    implementation(libs.kotlinx.html)

    implementation(libs.kotlin.stdlib.common)

    implementation(libs.kotlinx.serialization.json)

    api(libs.kotlin.css)

    implementation(libs.letsplot)

    implementation(libs.ktor.server)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.sessions)
    api(libs.ktor.server.html.builder)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.compression)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    implementation(libs.common.utils.core)
    implementation(libs.common.utils.ktor.server)

    api(libs.srcref)

    implementation(libs.commons.text)

    implementation(libs.kotlin.logging)
    implementation(libs.logback)
    implementation(libs.junit) // for junit playgrounds, which are in main

    testImplementation(libs.kotest.runner.junit5)
  }

//    tasks.register<Jar>("sourcesJar") {
//        dependsOn("classes")
//        from(the<SourceSetContainer>()["main"].allSource)
//        archiveClassifier.set("sources")
//    }

  val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
  }

  val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
  }

  configurations.all {
    resolutionStrategy {
      force(libs.kotlinx.html.get().toString())
    }
  }

  configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
    jvmToolchain(17)
  }

//  tasks.withType<Test> {
//    useJUnitPlatform()
//
//    testLogging {
//      events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
//      exceptionFormat = TestExceptionFormat.FULL
//      showStandardStreams = false
//    }
//  }

  configure<org.jmailen.gradle.kotlinter.KotlinterExtension> {
    ignoreFormatFailures = false
    ignoreLintFailures = false
    reporters = arrayOf("checkstyle", "plain")
  }

  fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA", "Beta1").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
  }

  tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
      isNonStable(candidate.version)
    }
  }
}
