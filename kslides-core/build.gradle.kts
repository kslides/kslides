description = "kslides-core"

plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.html)
    api(libs.kotlin.css)
    api(libs.ktor.server.html.builder)

    implementation(libs.srcref)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.common.utils.core)
    implementation(libs.common.utils.ktor.server)
    implementation(libs.commons.text)
    implementation(libs.kotlin.logging)
    implementation(libs.logback)

    testImplementation(libs.kotest)
}
