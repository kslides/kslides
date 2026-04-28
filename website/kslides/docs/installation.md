---
icon: lucide/package
---

# Installation

kslides targets the JVM (Kotlin 2.x, JDK 17+). Add the core artifact to a Gradle project.

## Gradle (Kotlin DSL)

```kotlin
repositories {
  mavenCentral()
}

dependencies {
  implementation("com.github.pambrose:kslides-core:0.25.0")

  // Optional: Lets-Plot integration
  implementation("com.github.pambrose:kslides-letsplot:0.25.0")
}

kotlin {
  jvmToolchain(17)
}
```

## Gradle (version catalog)

If you use `gradle/libs.versions.toml`:

```toml
[versions]
kslides = "0.25.0"

[libraries]
kslides-core    = { module = "com.github.pambrose:kslides-core",     version.ref = "kslides" }
kslides-letsplot = { module = "com.github.pambrose:kslides-letsplot", version.ref = "kslides" }
```

then in your build script:

```kotlin
dependencies {
  implementation(libs.kslides.core)
  implementation(libs.kslides.letsplot)
}
```

## Snapshot builds

Snapshots are published to Sonatype's snapshot repository. Add it explicitly:

```kotlin
repositories {
  mavenCentral()
  maven("https://central.sonatype.com/repository/maven-snapshots/")
}
```

## What's next?

Head to the [Quickstart](quickstart.md) to build your first deck.
