description = "kslides-examples"

plugins {
    alias(libs.plugins.ktor)
}

application {
    mainClass = "SlidesKt"
}

ktor {
    fatJar {
        archiveFileName.set("kslides.jar")
    }
}

dependencies {
    implementation(project(":kslides-core"))
    implementation(project(":kslides-letsplot"))

    implementation(libs.junit4) // for junit playgrounds, which are in main
}

// Include the fat jar in heroku deploy
tasks.register("stage") {
    dependsOn("build", "buildFatJar")
}

tasks.named("buildFatJar") {
    mustRunAfter("build")
}
