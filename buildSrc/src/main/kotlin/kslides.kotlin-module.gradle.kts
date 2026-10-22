import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("dev.detekt")
    id("com.pambrose.kotlinter")
    id("com.pambrose.testing")
    id("kslides.stable-versions")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(libs.findVersion("jvm").get().requiredVersion.toInt())

    // Run the unused-return-value checker over production code only. Kotest's
    // assertion DSL (e.g. shouldBe) returns its receiver, and tests intentionally
    // discard that result, so applying the checker to the test source set would
    // emit only false-positive warnings.
    tasks.named<KotlinCompile>("compileKotlin") {
        compilerOptions {
            freeCompilerArgs.add("-Xreturn-value-checker=check")
        }
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

// com.pambrose.testing adds kotest-runner-junit5 and logback-classic to every module's tests. Hand it
// the catalog's versions rather than declaring the same dependencies again: two declarations resolve
// to the higher version, so the catalog would stop deciding whenever the plugin's default ran ahead.
// kotlin-test, which the plugin also adds by default, is unused: the tests are all Kotest.
pambroseTesting {
    kotestVersion.set(libs.findVersion("kotest").get().requiredVersion)
    logbackVersion.set(libs.findVersion("logback").get().requiredVersion)
    addKotlinTest.set(false)
}
