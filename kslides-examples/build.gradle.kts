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
// Gradle's run task otherwise defaults to this subproject's dir, which doubles those paths and
// throws FileNotFoundException. Run from the root so `./gradlew :kslides-examples:run` (and the
// kslides-dev.sh live-reload loop) resolve them.
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

ktor {
    fatJar {
        archiveFileName.set("kslides.jar")
    }
}

dependencies {
    implementation(projects.kslidesCore)
    implementation(projects.kslidesLetsplot)

    implementation(libs.junit4) // for junit playgrounds, which are in main
    runtimeOnly(libs.logback.classic) // logging implementation lives with the application, not the libraries
}
