plugins {
    id("kslides.published-module")
}

description = "Lets-Plot integration for kslides: embeds Lets-Plot figures via the letsPlot{} DSL."

dependencies {
    api(projects.kslidesCore)

    api(libs.letsplot.kotlin)
}
