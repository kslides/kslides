import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("io.github.ben-manes.versions")
}

tasks.withType<DependencyUpdatesTask>().configureEach {
    notCompatibleWithConfigurationCache("the dependency updates plugin is not compatible with the configuration cache")
    // The plugin's built-in pre-release check rejects a pre-release candidate only when
    // the version in use is stable, so a dependency we intentionally track on a
    // pre-release line (e.g. a detekt alpha) is still offered newer pre-releases. It also
    // limits the Gradle check to the `current` (stable) channel.
    rejectPreReleases = true

    // Prints only what is behind, and what could not be checked. The plugin's own report,
    // which `make versions-all` asks for with --output-formatter=plain, first lists every
    // dependency already up to date. The root task merges every module's report, and a
    // coordinate whose latest version differs across modules arrives as one entry per
    // module, so identical lines are printed once.
    outputFormatter {
        val behind =
            outdated.dependencies
                .sortedBy { "${it.group}:${it.name}" }
                .map { d ->
                    val latest = with(d.available) { release ?: milestone ?: integration }
                    " - ${d.group}:${d.name} ${d.version} -> $latest"
                }.distinct()
        if (behind.isEmpty()) println("All dependencies are up to date")
        behind.forEach(::println)
        unresolved.dependencies
            .sortedBy { "${it.group}:${it.name}" }
            .map { " ? ${it.group}:${it.name}:${it.version} could not be checked" }
            .distinct()
            .forEach(::println)
        if (gradle.current.isUpdateAvailable) {
            println(" - Gradle ${gradle.running.version} -> ${gradle.current.version}")
        }
    }
}
