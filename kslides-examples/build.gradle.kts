plugins {
    id("kslides.kotlin-module")
    alias(libs.plugins.ktor)
}

description = "Example kslides presentations and runnable fat-jar entry point."

application {
    mainClass = "SlidesKt"
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
