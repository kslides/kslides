import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.ben-manes.versions")
}

// Applies the Ben-Manes versions plugin and rejects non-stable dependency update candidates from
// the `dependencyUpdates` report. Replaces the com.pambrose.stable-versions wrapper, which applied
// ben-manes and configured this same rejection rule.
fun isNonStable(version: String): Boolean =
    listOf("-RC", "-BETA", "-ALPHA", "-M").any { version.uppercase().contains(it) }

tasks.withType<DependencyUpdatesTask>().configureEach {
    // The dependency updates plugin resolves configurations at execution time, which the
    // configuration cache (enabled in gradle.properties) does not support.
    notCompatibleWithConfigurationCache(
        "the dependency updates plugin is not compatible with the configuration cache",
    )
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
