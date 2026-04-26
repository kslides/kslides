import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.pambrose.stable.versions) apply false
    alias(libs.plugins.pambrose.kotlinter) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.pambrose.testing) apply false
}

// Disable publishing for the root project — only subprojects should be published
tasks.withType<PublishToMavenRepository>().configureEach { enabled = false }
tasks.withType<PublishToMavenLocal>().configureEach { enabled = false }

// Consolidate dokka docs into the root build/ — list publishing modules explicitly
dependencies {
    dokka(project(":kslides-core"))
    dokka(project(":kslides-letsplot"))
}

dokka {
    moduleName.set("kslides")
    pluginsConfiguration.html {
        homepageLink.set("https://github.com/kslides/kslides")
        footerMessage.set("kslides")
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
    version = findProperty("overrideVersion")?.toString() ?: "0.25.0"
    group = "com.kslides"

    apply {
        plugin("java-library")
        plugin(serializationLib)
        plugin(ktlinterLib)
        plugin(testingLib)
        plugin(versionsLib)
    }

    configureKotlin()
    configurePublishing()

    val libs = rootProject.the<LibrariesForLibs>()

    dependencies {
        add("testImplementation", libs.kotest)
    }

    configurations.all {
        resolutionStrategy {
            // Pin kotlinx-html — ktor-server-html-builder pulls an older transitive that breaks the DSL.
            force(libs.kotlinx.html.get().toString())
        }
    }
}

fun Project.configureKotlin() {
    apply {
        plugin(kotlinLib)
    }

    extensions.configure<KotlinJvmProjectExtension> {
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
        // Skip signing when no GPG key is provided (e.g., local publishing)
        if (project.findProperty("signingInMemoryKey") != null) {
            signAllPublications()
        }
    }
}
