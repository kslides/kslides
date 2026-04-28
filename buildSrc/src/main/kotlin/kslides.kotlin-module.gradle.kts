plugins {
    kotlin("jvm")
    id("com.pambrose.kotlinter")
    id("com.pambrose.testing")
    id("com.pambrose.stable-versions")
}

kotlin {
    jvmToolchain(17)
}

providers.gradleProperty("overrideVersion").orNull?.let { project.version = it }

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    testImplementation(libs.findLibrary("kotest").get())
    testRuntimeOnly(libs.findLibrary("logback-classic").get())
}
