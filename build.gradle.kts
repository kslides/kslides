plugins {
    // Versions come from buildSrc — apply by id without a version to avoid classpath conflicts.
    id("org.jetbrains.dokka")
    id("kslides.stable-versions")
}

// Note: -PoverrideVersion is handled per-module in the kslides.kotlin-module convention plugin,
// which sets each published project's version. The root project is not published, so it needs no
// version override of its own.

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

tasks.register("stage") {
    val examples = ":kslides-examples"
    group = "build"
    description = "Builds the project and the examples fat jar for Heroku deployment."
    dependsOn("$examples:build", "$examples:buildFatJar")
}
