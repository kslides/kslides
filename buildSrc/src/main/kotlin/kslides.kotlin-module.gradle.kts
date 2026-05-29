plugins {
    kotlin("jvm")
    id("dev.detekt")
    id("com.pambrose.kotlinter")
    id("com.pambrose.testing")
    id("com.pambrose.stable-versions")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(libs.findVersion("jvm").get().requiredVersion.toInt())
    compilerOptions {
        // Enable context parameters (experimental in Kotlin 2.4) so DSL extension functions can
        // receive the enclosing kotlinx.html SECTION implicitly instead of via a mutable field.
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

// Detekt 2.0 is alpha; report findings without failing the build. Override per-project
// or via -Pdetekt.failOnViolation=true once a baseline is in place.
detekt {
    ignoreFailures = providers.gradleProperty("detekt.failOnViolation").orNull != "true"
    buildUponDefaultConfig = true
    autoCorrect = false
    config.from(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
}

providers.gradleProperty("overrideVersion").orNull?.let { project.version = it }

dependencies {
    testImplementation(libs.findLibrary("kotest").get())
    testRuntimeOnly(libs.findLibrary("logback-classic").get())
}
