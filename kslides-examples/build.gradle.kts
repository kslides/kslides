import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

description = "kslides-examples"

plugins {
    alias(libs.plugins.shadow)
}

// These are for the uber target
val mainName = "SlidesKt"
val appName = "kslides"

application {
    mainClass.set(mainName)
}

dependencies {
    implementation(project(":kslides-core"))
    implementation(project(":kslides-plotly"))

    implementation(libs.plotlykt.core)
}

// Include build uberjars in heroku deploy
tasks.register("stage") {
    dependsOn("uberjar", "build", "clean")
}
tasks.named("build") {
    mustRunAfter("clean")
}

tasks.named<ShadowJar>("shadowJar") {
    isZip64 = true
    mergeServiceFiles()
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("LICENSE*")
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar")

tasks.register<Jar>("uberjar") {
    dependsOn(shadowJarTask)
    isZip64 = true
    archiveFileName.set("kslides.jar")
    manifest {
        attributes["Implementation-Title"] = appName
        attributes["Implementation-Version"] = version
        attributes["Built-Date"] = Date()
        attributes["Built-JDK"] = System.getProperty("java.version")
        attributes["Main-Class"] = mainName
    }
    from(shadowJarTask.map { zipTree(it.archiveFile) })
}
