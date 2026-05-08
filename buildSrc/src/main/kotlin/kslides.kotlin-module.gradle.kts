plugins {
    kotlin("jvm")
    id("com.pambrose.kotlinter")
    id("com.pambrose.testing")
    id("com.pambrose.stable-versions")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(libs.findVersion("jvm").get().requiredVersion.toInt())
}

providers.gradleProperty("overrideVersion").orNull?.let { project.version = it }

dependencies {
    testImplementation(libs.findLibrary("kotest").get())
    testRuntimeOnly(libs.findLibrary("logback-classic").get())
}
