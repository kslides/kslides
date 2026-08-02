plugins {
    id("kslides.published-module")
}

description = "PDF export for kslides: prints presentations to PDF via headless Chromium (Playwright)."

dependencies {
    api(projects.kslidesCore)

    implementation(libs.playwright)
    implementation(libs.kotlin.logging)
}
