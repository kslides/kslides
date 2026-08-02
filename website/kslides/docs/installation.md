---
icon: lucide/package
---

# Installation

kslides targets the JVM (Kotlin 2.x, JDK 17+). Either scaffold a ready-to-run project, or add the core artifact to an existing Gradle build.

## Scaffold a new project

`kslides-init.sh` clones [kslides-template](https://github.com/kslides/kslides-template), strips its git history, renames the project and presentation title, and initializes a fresh repository — so you get a working deck with build config and a GitHub Pages workflow already wired up:

```bash
curl -fsSL https://raw.githubusercontent.com/kslides/kslides/master/kslides-init.sh | bash -s -- my-talk --title "My Talk"
```

Then:

```bash
cd my-talk
./gradlew run
```

The script refuses to overwrite an existing directory and cleans up after itself if anything fails partway. It runs on macOS's stock bash and modern Linux bash; on Windows, use WSL or [generate from the template on GitHub](https://github.com/kslides/kslides-template/generate) instead.

## Gradle (Kotlin DSL)

To add kslides to a project you already have:

```kotlin
repositories {
  mavenCentral()
}

dependencies {
  implementation("com.kslides:kslides-core:1.2.0")

  // Optional: Lets-Plot integration
  implementation("com.kslides:kslides-letsplot:1.2.0")

  // Optional: PDF export via headless Chromium (see the PDF export page)
  implementation("com.kslides:kslides-export:1.2.0")
}

kotlin {
  jvmToolchain(17)
}
```

## Gradle (version catalog)

If you use `gradle/libs.versions.toml`:

```toml
[versions]
kslides = "1.2.0"

[libraries]
kslides-core     = { module = "com.kslides:kslides-core",     version.ref = "kslides" }
kslides-letsplot = { module = "com.kslides:kslides-letsplot", version.ref = "kslides" }
kslides-export   = { module = "com.kslides:kslides-export",   version.ref = "kslides" }
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
