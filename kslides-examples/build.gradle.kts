plugins {
    id("kslides.kotlin-module")
    alias(libs.plugins.ktor)
}

description = "Example kslides presentations and runnable fat-jar entry point."

application {
    mainClass = "SlidesKt"
}

// The example deck resolves include()/source paths relative to the repo root (e.g.
// "kslides-examples/src/..."), matching the documented `java -jar` invocation from the root.
// Gradle otherwise defaults to this subproject's dir, which doubles those paths and throws
// FileNotFoundException. Applies to every JavaExec task in this module — `run` (and the
// kslides-dev.sh live-reload loop) plus the exportPdf task registered below.
tasks.withType<JavaExec>().configureEach {
    workingDir = rootProject.projectDir
}

ktor {
    fatJar {
        archiveFileName.set("kslides.jar")
    }
}

// PDF export (Export.kt) lives in its own source set so the Playwright dependency never reaches
// the runnable fat JAR: the Ktor plugin bundles only the main source set's runtime classpath.
sourceSets {
    create("export") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations {
    named("exportImplementation") { extendsFrom(configurations.implementation.get()) }
    named("exportRuntimeOnly") { extendsFrom(configurations.runtimeOnly.get()) }
}

// Custom source sets are not wired into `check`, so `build` compiles only main and test — a broken
// Export.kt would stay invisible until someone ran `make pdf`. Compile it as part of check so the
// build (and CI) fails on it instead.
tasks.named("check") {
    dependsOn(tasks.named("compileExportKotlin"))
}

dependencies {
    implementation(projects.kslidesCore)
    implementation(projects.kslidesLetsplot)

    implementation(libs.junit4) // for junit playgrounds, which are in main
    runtimeOnly(libs.logback.classic) // logging implementation lives with the application, not the libraries

    "exportImplementation"(projects.kslidesExport)
}

tasks.register<JavaExec>("exportPdf") {
    group = "distribution"
    description = "Print the example presentations to PDF via headless Chromium (-Pdeck=<name> for one deck)"
    mainClass = "ExportKt"
    classpath = sourceSets["export"].runtimeClasspath
    providers.gradleProperty("deck").orNull?.let { systemProperty("kslides.export.deck", it) }
}
