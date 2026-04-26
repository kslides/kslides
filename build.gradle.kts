import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.pambrose.stable.versions)
    alias(libs.plugins.pambrose.kotlinter)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.pambrose.testing) apply false
}

val kslidesVersion = findProperty("overrideVersion")?.toString() ?: "0.25.0"
version = kslidesVersion
group = "com.kslides"

// Consolidate dokka docs into the root build/ — list publishing modules explicitly
dependencies {
    dokka(project(":kslides-core"))
    dokka(project(":kslides-letsplot"))
}

dokka {
    moduleName.set("kslides")
    configureDokkaHtml()
}

val kotlinPluginId = libs.plugins.kotlin.jvm.get().pluginId
val kotlinterPluginId = libs.plugins.pambrose.kotlinter.get().pluginId
val testingPluginId = libs.plugins.pambrose.testing.get().pluginId
val versionsPluginId = libs.plugins.pambrose.stable.versions.get().pluginId
val dokkaPluginId = libs.plugins.dokka.get().pluginId
val publishPluginId = libs.plugins.maven.publish.get().pluginId

subprojects {
    version = kslidesVersion
    group = "com.kslides"

    apply {
        if (name != "kslides-examples") plugin("java-library")
        plugin(kotlinterPluginId)
        plugin(testingPluginId)
        // Apply per-subproject so dependencyUpdates resolves classpath deps
        // (e.g. dokka-base/dokka-core/templating-plugin) in each subproject's
        // own resolution context — see commit history for rationale.
        plugin(versionsPluginId)
    }

    configureKotlin()
    configureDokka()
    configurePublishing()
}

fun Project.configureKotlin() {
    apply {
        plugin(kotlinPluginId)
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }
}

fun Project.configureDokka() {
    if (name == "kslides-examples") return
    apply {
        plugin(dokkaPluginId)
    }

    dokka {
        configureDokkaHtml()
    }
}

fun Project.configurePublishing() {
    if (name == "kslides-examples") return
    apply {
        plugin(publishPluginId)
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

fun DokkaExtension.configureDokkaHtml() {
    pluginsConfiguration.html {
        homepageLink.set("https://github.com/kslides/kslides")
        footerMessage.set("kslides")
    }
}
