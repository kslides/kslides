plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.dokka)
    implementation(libs.plugin.maven.publish)
    implementation(libs.plugin.pambrose.kotlinter)
    implementation(libs.plugin.pambrose.stable.versions)
    implementation(libs.plugin.pambrose.testing)
}
