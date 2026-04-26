description = "kslides-core"

dependencies {
    api(libs.kotlinx.html)
    api(libs.kotlin.css)
    api(libs.ktor.server.html.builder)
    api(libs.srcref)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.common.utils.core)
    implementation(libs.common.utils.ktor.server)
    implementation(libs.commons.text)
    implementation(libs.kotlin.logging)
    implementation(libs.logback)
}
