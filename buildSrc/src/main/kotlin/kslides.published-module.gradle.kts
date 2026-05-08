import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar

plugins {
    `java-library`
    id("kslides.kotlin-module")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

val repoSlug = "kslides/kslides"
val repoUrl = "https://github.com/$repoSlug"

extensions.configure<MavenPublishBaseExtension> {
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = SourcesJar.Sources(),
        ),
    )

    pom {
        name.set(project.name)
        description.set(provider { project.description ?: project.name })
        url.set(repoUrl)
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
            connection.set("scm:git:https://github.com/$repoSlug.git")
            developerConnection.set("scm:git:ssh://github.com/$repoSlug.git")
            url.set(repoUrl)
        }
    }

    publishToMavenCentral(automaticRelease = true)
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
}
