import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  `java-library`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.pambrose.stable.versions) apply false
  alias(libs.plugins.pambrose.kotlinter) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.pambrose.testing) apply false
}

allprojects {
  version = findProperty("overrideVersion")?.toString() ?: "0.25.0"
  group = "com.kslides"
  description = "kslides"

  repositories {
    mavenCentral()
  }
}

val kotlinLib = libs.plugins.kotlin.jvm.get().pluginId
val serializationLib = libs.plugins.kotlin.serialization.get().pluginId
val ktlinterLib = libs.plugins.pambrose.kotlinter.get().pluginId
val testingLib = libs.plugins.pambrose.testing.get().pluginId
val versionsLib = libs.plugins.pambrose.stable.versions.get().pluginId
val dokkaLib = libs.plugins.dokka.get().pluginId
val publishLib = libs.plugins.maven.publish.get().pluginId

subprojects {
//  apply(plugin = "application")
//  apply(plugin = "java-library")
  apply {
    plugin(kotlinLib)
    plugin(serializationLib)
    plugin(ktlinterLib)
    plugin(testingLib)
    plugin(versionsLib)
  }

  configureKotlin()
  configurePublishing()

  val libs = rootProject.the<LibrariesForLibs>()

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

  configurations.all {
    resolutionStrategy {
      force(libs.kotlinx.html.get().toString())
    }
  }

  configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
    jvmToolchain(17)
  }
}

fun Project.configureKotlin() {
  apply {
    plugin(kotlinLib)
  }

  kotlin {
    jvmToolchain(17)
  }

  tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }
}

fun Project.configurePublishing() {
  if (name == "kslides-examples") return
  apply {
    plugin(dokkaLib)
    plugin(publishLib)
  }

  dokka {
    pluginsConfiguration.html {
      homepageLink.set("https://github.com/kslides/kslides")
      footerMessage.set("kslides")
    }
  }

  extensions.configure<MavenPublishBaseExtension> {
    configure(
      com.vanniktech.maven.publish.KotlinJvm(
        javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
        sourcesJar = SourcesJar.Sources(),
      ),
    )
    coordinates("com.kslides.kslides", project.name, version.toString())

    pom {
      name.set(project.name)
      description.set(provider { project.description })
      url.set("https://github.com/kslides/kslides")
      licenses {
        license {
          name.set("Apache License 2.0")
          url.set("https://www.apache.org/licenses/LICENSE-2.0")
        }
      }
      developers {
        developer {
          id.set("pambrose")
          name.set("Paul Ambrose")
          email.set("paul@pambrose.com")
        }
      }
      scm {
        connection.set("scm:git:https://github.com/kslides/kslides.git")
        developerConnection.set("scm:git:ssh://github.com/kslides/kslides.git")
        url.set("https://github.com/kslides/kslides")
      }
    }

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
  }

  // Skip signing when no GPG key is provided (e.g., local publishing)
  tasks.withType<Sign>().configureEach {
    isEnabled = project.findProperty("signingInMemoryKey") != null
  }
}
