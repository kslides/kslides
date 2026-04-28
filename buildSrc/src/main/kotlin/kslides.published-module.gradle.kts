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
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
}
