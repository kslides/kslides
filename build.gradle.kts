plugins {
    // Versions come from buildSrc — apply by id without a version to avoid classpath conflicts.
    id("org.jetbrains.dokka")
    id("com.pambrose.stable-versions")
}

providers.gradleProperty("overrideVersion").orNull?.let { version = it }

// Aggregate Dokka HTML across publishing modules into the root build/ output.
dependencies {
    dokka(projects.kslidesCore)
    dokka(projects.kslidesLetsplot)
}

dokka {
    moduleName.set("kslides")
    pluginsConfiguration.html {
        homepageLink.set("https://github.com/kslides/kslides")
        footerMessage.set("kslides")
    }
}

// Heroku deploy entry point: build everything plus the examples fat jar.
tasks.register("stage") {
    group = "build"
    description = "Builds the project and the examples fat jar for Heroku deployment."
    dependsOn(":kslides-examples:build", ":kslides-examples:buildFatJar")
}
