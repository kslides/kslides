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

// Detekt 2.0 is alpha, but the config is valid and the tree is violation-free, so findings fail the
// build by default. Pass -Pdetekt.ignoreFailures=true to downgrade to report-only while iterating.
detekt {
    ignoreFailures = providers.gradleProperty("detekt.ignoreFailures").orNull == "true"
    buildUponDefaultConfig = true
    autoCorrect = false
    config.from(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
}

providers.gradleProperty("overrideVersion").orNull?.let { project.version = it }

dependencies {
    testImplementation(libs.findLibrary("kotest").get())
    testRuntimeOnly(libs.findLibrary("logback-classic").get())
}
