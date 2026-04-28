description = "Lets-Plot integration for kslides: embeds Lets-Plot figures via the letsPlot{} DSL."

plugins {
    id("kslides.published-module")
}

dependencies {
    api(projects.kslidesCore)

    api(libs.letsplot.kotlin)
}
