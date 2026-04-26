description = "kslides-letsplot"

dependencies {
    api(project(":kslides-core"))

    api(libs.letsplot)

    testImplementation(libs.kotest)
}
