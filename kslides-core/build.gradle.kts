plugins {
    id("kslides.published-module")
    alias(libs.plugins.kotlin.serialization)
}

description = "Core kslides DSL: slide types, configuration, page rendering, and Ktor server."

dependencies {
    api(libs.kotlinx.html)
    api(libs.kotlin.css)
    api(libs.ktor.server.html.builder)
    api(libs.srcref)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation(libs.common.utils.core)
    implementation(libs.common.utils.ktor.server)
    implementation(libs.commons.text)
    implementation(libs.kotlin.logging)
}

// Single source of truth for reveal.js assets: docs/revealjs/ (committed for GitHub Pages).
// Copy them onto the classpath at revealjs/** so the JAR ships them for the Ktor static handler.
tasks.processResources {
    val revealjsDir = rootProject.layout.projectDirectory.dir("docs/revealjs")
    // Guard against a sparse/partial checkout silently publishing a JAR with no revealjs runtime.
    doFirst {
        require(revealjsDir.asFile.isDirectory) {
            "Missing reveal.js assets at ${revealjsDir.asFile} — the published JAR would ship without a revealjs runtime."
        }
    }
    from(revealjsDir) {
        into("revealjs")
        // ~5MB of reveal.js demo media that no slide references — keep it out of the published JAR.
        exclude("assets/video.mp4", "assets/beeping.wav", "assets/beeping.txt")
    }
}
