# Slide Font-Size API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add cascading `fontSize`, `codeFontSize`, and `codeWrap` properties to `SlideConfig` so slide/code font sizing no longer requires raw CSS, plus a `configBlock` parameter on `slideDefinition()`.

**Architecture:** The three properties ride the existing global → presentation → slide config cascade (`ConfigProperty` delegates + `AbstractConfig.merge`). `fontSize` renders as an inline `font-size` on the slide `<section>`. `codeFontSize`/`codeWrap` render as an auto-generated, value-deduped CSS class on the section plus a `<style>` element inserted into the DOM head after body generation (per-slide configs only exist once slide-content lambdas run during body rendering, so the head is patched afterward — the same DOM post-processing pattern `generatePage` already uses for `data-separator`).

**Tech Stack:** Kotlin 2.4.0 / JVM 17, kotlinx.html, Kotest 6 (`StringSpec()` + `init {}`), Gradle.

**Spec:** `specs/2026-07-06-slide-font-size-api-design.md`

## Global Constraints

- All commands run from the repo root: `/Users/pambrose/git/kslides/kslides`.
- Tests: Kotest 6 `StringSpec()` with an `init {}` block (user's global Kotlin testing convention).
- Lint/static analysis must stay green: `./gradlew lintKotlin` and `./gradlew detekt` (Detekt is fatal on findings).
- **Git workflow (overrides the commit steps below):** the user's global instructions forbid committing/pushing until explicitly instructed. Execute the commit steps ONLY if the user has authorized commits for this work; otherwise skip them and leave changes staged-or-unstaged in the working tree, noting the prepared commit message.
- Generated class names are `kslides-code-<N>` with N starting at 1, in first-appearance order per render.
- New config values are stored in `kslidesManagedValues` (NOT `revealjsManagedValues`) — they must never leak into the `Reveal.initialize({...})` JS.

---

### Task 1: `fontSize` / `codeFontSize` / `codeWrap` properties on SlideConfig

**Files:**
- Modify: `kslides-core/src/main/kotlin/com/kslides/config/SlideConfig.kt`
- Test (create): `kslides-core/src/test/kotlin/com/kslides/SlideFontSizeTest.kt`

**Interfaces:**
- Consumes: `ConfigProperty<T>(kslidesManagedValues)` delegate, `AbstractConfig.merge`, `SlideConfig.assignDefaults()` (all existing).
- Produces: `SlideConfig.fontSize: String`, `SlideConfig.codeFontSize: String`, `SlideConfig.codeWrap: Boolean` — cascading properties with defaults `""`, `""`, `false`. Tasks 2–4 read these via `Slide.mergedSlideConfig`.

- [ ] **Step 1: Write the failing tests**

Create `kslides-core/src/test/kotlin/com/kslides/SlideFontSizeTest.kt`:

```kotlin
package com.kslides

import com.kslides.config.SlideConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SlideFontSizeTest : StringSpec() {
  init {
    "font properties default to unset values" {
      val config = SlideConfig().apply { assignDefaults() }
      config.fontSize shouldBe ""
      config.codeFontSize shouldBe ""
      config.codeWrap shouldBe false
    }

    "font properties cascade global -> presentation -> slide" {
      // Mirrors the merge order in Slide.mergedSlideConfig
      val globalLevel =
        SlideConfig().apply {
          assignDefaults()
          codeFontSize = "0.60em"
          codeWrap = true
        }
      val presentationLevel = SlideConfig().apply { fontSize = "38px" }
      val slideLevel = SlideConfig().apply { codeFontSize = "0.40em" }

      val merged =
        SlideConfig().also { config ->
          config.merge(globalLevel)
          config.merge(presentationLevel)
          config.merge(slideLevel)
        }

      merged.fontSize shouldBe "38px"        // presentation level, untouched by slide
      merged.codeFontSize shouldBe "0.40em"  // slide overrides global
      merged.codeWrap shouldBe true          // inherited from global
    }
  }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :kslides-core:test --tests "com.kslides.SlideFontSizeTest"`
Expected: compilation FAILURE — `unresolved reference: fontSize` (etc.) in the test file.

- [ ] **Step 3: Add the properties**

In `kslides-core/src/main/kotlin/com/kslides/config/SlideConfig.kt`, after the `disableTrimIndent` property declaration (line ~101), add:

```kotlin
  /**
   * Font size for all content on the slide — any CSS length (e.g. `"34px"`, `"0.9em"`).
   * Rendered as an inline `font-size` on the slide's `<section>`; reveal.js themes size
   * headings/text in `em`, so everything scales. Blank inherits the theme default.
   */
  var fontSize by ConfigProperty<String>(kslidesManagedValues)

  /**
   * Font size for code blocks (`<pre>`) on the slide (e.g. `"0.60em"`). Rendered as a
   * generated CSS class + head rule because reveal.js renders Markdown client-side. Blank
   * inherits reveal.js's default (`0.55em`).
   */
  var codeFontSize by ConfigProperty<String>(kslidesManagedValues)

  /**
   * When `true`, long code lines wrap (`white-space: pre-wrap; word-break: break-word`)
   * instead of overflowing horizontally. A slide can set `false` to override a
   * presentation-level `true`.
   */
  var codeWrap by ConfigProperty<Boolean>(kslidesManagedValues)
```

In `assignDefaults()`, after the `disableTrimIndent = false` line, add:

```kotlin
    fontSize = ""
    codeFontSize = ""
    codeWrap = false
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :kslides-core:test --tests "com.kslides.SlideFontSizeTest"`
Expected: BUILD SUCCESSFUL, 2 tests passed.

- [ ] **Step 5: Commit (only if user has authorized commits)**

```bash
git add kslides-core/src/main/kotlin/com/kslides/config/SlideConfig.kt \
        kslides-core/src/test/kotlin/com/kslides/SlideFontSizeTest.kt
git commit -m "Add fontSize, codeFontSize, and codeWrap SlideConfig properties

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 2: Render `fontSize` as inline style on the section

**Files:**
- Modify: `kslides-core/src/main/kotlin/com/kslides/slide/Slide.kt` (`processSlide`, lines ~99–120)
- Test: `kslides-core/src/test/kotlin/com/kslides/SlideFontSizeTest.kt`

**Interfaces:**
- Consumes: `mergedSlideConfig.fontSize` (Task 1), existing `Slide.style` var, `kslidesTest {}`, `Page.generatePage(p)` (internal, same package in tests).
- Produces: `<section style="font-size: <value>; <user style>">` — font-size first, user style last so it wins on conflict. No behavior change when `fontSize` is blank.

- [ ] **Step 1: Write the failing tests**

Add to the `init {}` block of `SlideFontSizeTest.kt` (add imports `com.kslides.Page.generatePage`, `io.kotest.matchers.string.shouldContain`, `io.kotest.matchers.string.shouldNotContain`):

```kotlin
    "fontSize renders as inline font-size on the section" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              slideConfig { fontSize = "34px" }
              content { }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """style="font-size: 34px;""""
    }

    "fontSize combines with user style, user style last" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              style = "height: 600px"
              slideConfig { fontSize = "34px" }
              content { }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """style="font-size: 34px; height: 600px""""
    }

    "user style renders unchanged when fontSize is unset" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              style = "height: 600px"
              content { }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """style="height: 600px""""
      html shouldNotContain "font-size"
    }
```

- [ ] **Step 2: Run tests to verify the new ones fail**

Run: `./gradlew :kslides-core:test --tests "com.kslides.SlideFontSizeTest"`
Expected: FAIL — "fontSize renders as inline font-size on the section" and "fontSize combines with user style" fail (no `font-size` in output); the other tests pass.

- [ ] **Step 3: Implement the style merge in `processSlide`**

In `kslides-core/src/main/kotlin/com/kslides/slide/Slide.kt`, replace

```kotlin
    if (style.isNotBlank())
      section.style = style
```

with

```kotlin
    // font-size first, user style last — a user-declared font-size wins on conflict
    buildList {
      mergedSlideConfig.fontSize.also { if (it.isNotBlank()) add("font-size: $it;") }
      if (style.isNotBlank()) add(style)
    }.joinToString(" ")
      .also { if (it.isNotBlank()) section.style = it }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :kslides-core:test --tests "com.kslides.SlideFontSizeTest"`
Expected: BUILD SUCCESSFUL, 5 tests passed.

- [ ] **Step 5: Run the full core test suite (regression check)**

Run: `./gradlew :kslides-core:test`
Expected: BUILD SUCCESSFUL — the changed style handling must not break existing rendering tests.

- [ ] **Step 6: Commit (only if user has authorized commits)**

```bash
git add kslides-core/src/main/kotlin/com/kslides/slide/Slide.kt \
        kslides-core/src/test/kotlin/com/kslides/SlideFontSizeTest.kt
git commit -m "Render slideConfig fontSize as inline style on the slide section

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 3: Render `codeFontSize`/`codeWrap` as generated class + head style rules

**Files:**
- Modify: `kslides-core/src/main/kotlin/com/kslides/Presentation.kt` (add registry field near `slides`, line ~48)
- Modify: `kslides-core/src/main/kotlin/com/kslides/slide/Slide.kt` (`processSlide`)
- Modify: `kslides-core/src/main/kotlin/com/kslides/Page.kt` (`generatePage`, lines ~36–64)
- Test: `kslides-core/src/test/kotlin/com/kslides/SlideFontSizeTest.kt`

**Interfaces:**
- Consumes: `mergedSlideConfig.codeFontSize` / `.codeWrap` (Task 1); `Slide`'s private `presentation` constructor property; `generatePage`'s existing DOM post-processing block.
- Produces:
  - `Presentation.codeStyleClasses: LinkedHashMap<Pair<String, Boolean>, String>` (internal) — keyed by (codeFontSize, codeWrap), value = generated class name `kslides-code-<N>`; cleared at the start of every render.
  - Sections whose merged config has `codeFontSize` non-blank OR `codeWrap` true carry the generated class in addition to user classes.
  - A `<style media="screen">` element appended to `<head>` containing, per registry entry:
    `.reveal .<class> pre { font-size: <size>; }` (when size non-blank) and
    `.reveal .<class> pre code { white-space: pre-wrap; word-break: break-word; }` (when wrap true).

- [ ] **Step 1: Write the failing tests**

Add to `SlideFontSizeTest.kt`'s `init {}` block:

```kotlin
    "presentation-level codeFontSize and codeWrap emit one shared class and head rules" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig {
                codeFontSize = "0.60em"
                codeWrap = true
              }
            }
            markdownSlide { content { "# One" } }
            markdownSlide { content { "# Two" } }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain ".reveal .kslides-code-1 pre { font-size: 0.60em; }"
      html shouldContain ".reveal .kslides-code-1 pre code { white-space: pre-wrap; word-break: break-word; }"
      html shouldNotContain "kslides-code-2"
      // both slides share the single generated class
      Regex("""class="kslides-code-1"""").findAll(html).count() shouldBe 2
    }

    "slide-level codeFontSize override gets its own class and rule" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig { codeFontSize = "0.60em" }
            }
            markdownSlide { content { "# Normal" } }
            markdownSlide {
              slideConfig { codeFontSize = "0.40em" }
              content { "# Small" }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain ".reveal .kslides-code-1 pre { font-size: 0.60em; }"
      html shouldContain ".reveal .kslides-code-2 pre { font-size: 0.40em; }"
      Regex("""class="kslides-code-1"""").findAll(html).count() shouldBe 1
      Regex("""class="kslides-code-2"""").findAll(html).count() shouldBe 1
    }

    "slide-level codeWrap=false overrides presentation-level true" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig {
                codeFontSize = "0.60em"
                codeWrap = true
              }
            }
            markdownSlide { content { "# Wrapped" } }
            markdownSlide {
              slideConfig { codeWrap = false }
              content { "# Unwrapped" }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain ".reveal .kslides-code-1 pre code { white-space: pre-wrap; word-break: break-word; }"
      html shouldContain ".reveal .kslides-code-2 pre { font-size: 0.60em; }"
      html shouldNotContain ".kslides-code-2 pre code"
    }

    "generated class is appended after user classes" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig { codeFontSize = "0.60em" }
            }
            markdownSlide {
              classes = "mystyle"
              content { "# Styled" }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """class="mystyle kslides-code-1""""
    }

    "no code styling emits no class or rules" {
      val kslides =
        kslidesTest {
          presentation {
            markdownSlide { content { "# Plain" } }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldNotContain "kslides-code"
    }

    "repeated renders are deterministic (registry cleared per render)" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig { codeFontSize = "0.60em" }
            }
            markdownSlide { content { "# One" } }
          }
        }
      val p = kslides.presentation("/")
      generatePage(p) shouldBe generatePage(p)
    }
```

- [ ] **Step 2: Run tests to verify the new ones fail**

Run: `./gradlew :kslides-core:test --tests "com.kslides.SlideFontSizeTest"`
Expected: FAIL — every new test except "no code styling emits no class or rules" fails (no `kslides-code` output exists yet).

- [ ] **Step 3: Add the registry to Presentation**

In `kslides-core/src/main/kotlin/com/kslides/Presentation.kt`, after `internal val slides = mutableListOf<Slide>()` (line ~48), add:

```kotlin
  // Per-render registry mapping each distinct (codeFontSize, codeWrap) combination to its
  // generated CSS class (kslides-code-1, kslides-code-2, ... in first-appearance order).
  // Populated by Slide.processSlide during body rendering and read back by Page.generatePage
  // to build the head <style> rules; cleared at the start of every render (renders are
  // serialized on KSlides.renderLock).
  internal val codeStyleClasses = LinkedHashMap<Pair<String, Boolean>, String>()
```

- [ ] **Step 4: Register the class in `Slide.processSlide`**

In `kslides-core/src/main/kotlin/com/kslides/slide/Slide.kt`, at the end of `processSlide` (after `mergedSlideConfig.applyConfig(section)`), add:

```kotlin
    val codeFontSize = mergedSlideConfig.codeFontSize
    val codeWrap = mergedSlideConfig.codeWrap
    if (codeFontSize.isNotBlank() || codeWrap) {
      val cssClass =
        presentation.codeStyleClasses.getOrPut(codeFontSize to codeWrap) {
          "kslides-code-${presentation.codeStyleClasses.size + 1}"
        }
      section.attributes["class"] =
        listOf(section.attributes["class"].orEmpty(), cssClass)
          .filter { it.isNotBlank() }
          .joinToString(" ")
    }
```

- [ ] **Step 5: Clear the registry and insert the head `<style>` in `generatePage`**

In `kslides-core/src/main/kotlin/com/kslides/Page.kt`, inside `generatePage`:

(a) After `p.kslides.slideCount = 0` add:

```kotlin
      p.codeStyleClasses.clear()
```

(b) After the existing `data-separator` node post-processing loop (before `mergePreAndCode(htmldoc.serialize())`), add:

```kotlin
      // Per-slide slideConfig{} blocks only run during body rendering, so the code-style
      // rules they produce are patched into the already-built head here, mirroring the
      // data-separator post-processing above.
      if (p.codeStyleClasses.isNotEmpty()) {
        val headNode = htmldoc.getElementsByTagName("head").item(0)
        val styleNode =
          htmldoc.createElement("style").apply {
            setAttribute("type", "text/css")
            setAttribute("media", "screen")
            textContent =
              buildString {
                appendLine()
                p.codeStyleClasses.forEach { (values, cssClass) ->
                  val (codeFontSize, codeWrap) = values
                  if (codeFontSize.isNotBlank())
                    appendLine("\t\t\t.reveal .$cssClass pre { font-size: $codeFontSize; }")
                  if (codeWrap)
                    appendLine(
                      "\t\t\t.reveal .$cssClass pre code { white-space: pre-wrap; word-break: break-word; }",
                    )
                }
                append("\t\t")
              }
          }
        headNode.appendChild(styleNode)
      }
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `./gradlew :kslides-core:test --tests "com.kslides.SlideFontSizeTest"`
Expected: BUILD SUCCESSFUL, 11 tests passed.

- [ ] **Step 7: Run the full core test suite (regression check)**

Run: `./gradlew :kslides-core:test`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit (only if user has authorized commits)**

```bash
git add kslides-core/src/main/kotlin/com/kslides/Presentation.kt \
        kslides-core/src/main/kotlin/com/kslides/slide/Slide.kt \
        kslides-core/src/main/kotlin/com/kslides/Page.kt \
        kslides-core/src/test/kotlin/com/kslides/SlideFontSizeTest.kt
git commit -m "Emit generated CSS class and head rules for codeFontSize/codeWrap

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 4: `configBlock` parameter on `slideDefinition()`

**Files:**
- Modify: `kslides-core/src/main/kotlin/com/kslides/Presentation.kt` (both `slideDefinition` overloads, lines ~250 and ~284)
- Test: `kslides-core/src/test/kotlin/com/kslides/SlideFontSizeTest.kt`

**Interfaces:**
- Consumes: `Slide.slideConfig(block)` (existing), Task 1 properties.
- Produces: `slideDefinition(source, token, ..., configBlock: SlideConfig.() -> Unit = {})` as the LAST parameter on both overloads, enabling trailing-lambda syntax: `slideDefinition(source = s, token = t) { codeFontSize = "0.40em" }`. In the vertical overload, `configBlock` is applied AFTER the built-in `markdownNotesSeparator = "^^"` assignment so callers can override it.

- [ ] **Step 1: Write the failing tests**

Add to `SlideFontSizeTest.kt`'s `init {}` block (a nonexistent `source` file is fine — `include()` warns and yields an empty excerpt, and these tests only assert on the section class and head rule):

```kotlin
    "slideDefinition configBlock applies per-slide code font size" {
      val kslides =
        kslidesTest {
          presentation {
            slideDefinition(source = "does-not-exist.kt", token = "foo") {
              codeFontSize = "0.40em"
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """class="kslides-code-1""""
      html shouldContain ".reveal .kslides-code-1 pre { font-size: 0.40em; }"
    }

    "vertical slideDefinition configBlock applies per-slide code font size" {
      val kslides =
        kslidesTest {
          presentation {
            verticalSlides {
              slideDefinition(source = "does-not-exist.kt", token = "foo") {
                codeFontSize = "0.40em"
              }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """class="kslides-code-1""""
      html shouldContain ".reveal .kslides-code-1 pre { font-size: 0.40em; }"
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :kslides-core:test --tests "com.kslides.SlideFontSizeTest"`
Expected: compilation FAILURE — `slideDefinition` has no lambda parameter yet.

- [ ] **Step 3: Add the parameter to both overloads**

In `kslides-core/src/main/kotlin/com/kslides/Presentation.kt`:

(a) `Presentation.slideDefinition` (line ~250): add to the KDoc
`@param configBlock optional per-slide configuration (e.g. codeFontSize) applied to the generated slide.`
then add the parameter and call:

```kotlin
  fun slideDefinition(
    source: String,
    token: String,
    title: String = "Slide Definition",
    highlightPattern: String = "[]",
    id: String = "",
    classes: String = "",
    language: String = "kotlin",
    githubAccount: String = "kslides",
    githubRepo: String = "kslides",
    githubPath: String = source,
    githubBranch: String = "master",
    configBlock: SlideConfig.() -> Unit = {},
  ) {
    markdownSlide {
      if (id.isNotBlank()) this.id = id
      if (classes.isNotBlank()) this.classes += classes
      slideConfig(configBlock)
      val p = this@Presentation
      ...  // content block unchanged
```

(b) `VerticalSlidesContext.slideDefinition` (line ~284): same parameter added last; apply `configBlock` AFTER the existing `markdownNotesSeparator` default so callers can override it:

```kotlin
    markdownSlide {
      if (id.isNotBlank()) this.id = id
      if (classes.isNotBlank()) this.classes = classes
      slideConfig {
        markdownNotesSeparator = "^^"
      }
      slideConfig(configBlock)
      ...  // content block unchanged
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :kslides-core:test --tests "com.kslides.SlideFontSizeTest"`
Expected: BUILD SUCCESSFUL, 13 tests passed.

- [ ] **Step 5: Commit (only if user has authorized commits)**

```bash
git add kslides-core/src/main/kotlin/com/kslides/Presentation.kt \
        kslides-core/src/test/kotlin/com/kslides/SlideFontSizeTest.kt
git commit -m "Add configBlock parameter to slideDefinition

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 5: Migrate the example deck (dogfooding)

**Files:**
- Modify: `kslides-examples/src/main/kotlin/Slides.kt`

**Interfaces:**
- Consumes: everything from Tasks 1–4.
- Produces: an example deck that uses the new API instead of raw CSS; no `smallcode` class remains.

- [ ] **Step 1: Remove the raw code-sizing CSS blocks**

In `kslides-examples/src/main/kotlin/Slides.kt`, delete both `css += ...` blocks and their comments (currently lines 113–126):

```kotlin
    // Shrink code blocks so long lines fit the slide window (reveal's default is 0.55em). This
    // fits the ~85-92 char lines of the slideDefinition slides; the rare extra-long line (e.g. a
    // full URL) wraps instead of overflowing horizontally rather than forcing an unreadable size.
    css += """
      .reveal pre { font-size: 0.60em; }
      .reveal pre code { white-space: pre-wrap; word-break: break-word; }
    """

    // Per-slide override: the "highlighted code" slideDefinitions (classes = "smallcode") render their
    // code smaller than the global 0.60em. ".reveal .smallcode pre" (two classes) outranks ".reveal pre"
    // on specificity, so it wins regardless of order; long lines still wrap via the global pre-wrap rule.
    css += """
      .reveal .smallcode pre { font-size: 0.40em; }
    """
```

- [ ] **Step 2: Set the defaults via the new API**

In the same file, replace the empty `slideConfig {}` inside the global `presentationConfig { ... }` (currently line 188) with:

```kotlin
      slideConfig {
        // Shrink code blocks so long lines fit the slide window (reveal's default is 0.55em),
        // and wrap the rare extra-long line instead of overflowing horizontally.
        codeFontSize = "0.60em"
        codeWrap = true
      }
```

- [ ] **Step 3: Rework the smallSlideDefinition wrappers**

Replace the wrapper functions and their comment at the bottom of `Slides.kt` (currently lines 1939–1953) with:

```kotlin
// Convenience wrappers around slideDefinition() that shrink the definition slides' code below the
// deck-wide codeFontSize default. Two receiver forms mirror slideDefinition(): one for calls
// directly inside presentation{}, one for calls inside verticalSlides{}.
private fun Presentation.smallSlideDefinition(
  source: String,
  token: String,
) = slideDefinition(source = source, token = token) { codeFontSize = "0.40em" }

context(presentation: Presentation)
private fun VerticalSlidesContext.smallSlideDefinition(
  source: String,
  token: String,
) = with(presentation) {
    this@smallSlideDefinition.slideDefinition(source = source, token = token) { codeFontSize = "0.40em" }
  }
```

- [ ] **Step 4: Build everything and run all checks**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (compiles all modules, runs all tests).

Run: `./gradlew lintKotlin detekt`
Expected: BUILD SUCCESSFUL — zero findings (Detekt is fatal on findings).

- [ ] **Step 5: Commit (only if user has authorized commits)**

```bash
git add kslides-examples/src/main/kotlin/Slides.kt
git commit -m "Use the slide font-size API in the example deck

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 6: CHANGELOG and docs site

**Files:**
- Modify: `CHANGELOG.md` (Unreleased section)
- Modify: `website/kslides/docs/styling.md` (read it first; add a section that matches its heading style and voice)

**Interfaces:**
- Consumes: final API from Tasks 1–4.
- Produces: user-facing documentation.

- [ ] **Step 1: Add the CHANGELOG entry**

Under `## [Unreleased]` in `CHANGELOG.md`, add:

```markdown
### Added

- `slideConfig { }` gained three cascading font properties: `fontSize` (inline
  `font-size` on the slide's `<section>` — any CSS length), `codeFontSize`
  (font size for `<pre>` code blocks, emitted as a generated CSS class + head
  rule), and `codeWrap` (wrap long code lines instead of overflowing). All
  three resolve through the global → presentation → slide cascade, replacing
  the raw-CSS approach previously needed for code sizing.
- `slideDefinition()` (both overloads) gained a trailing
  `configBlock: SlideConfig.() -> Unit` parameter for per-slide configuration,
  e.g. `slideDefinition(source, token) { codeFontSize = "0.40em" }`.
```

- [ ] **Step 2: Document the API on the docs site**

Read `website/kslides/docs/styling.md` first. Add a "Font sizes" section (adapting heading level/format to the page's existing style) with this content:

```markdown
## Font Sizes

Slide and code font sizes are plain config values — no CSS required. All three
cascade from `kslides{}` → `presentation{}` → slide:

​```kotlin
presentationConfig {
  slideConfig {
    codeFontSize = "0.60em"   // default for all slides
    codeWrap = true           // wrap long code lines
  }
}

markdownSlide {
  slideConfig {
    fontSize = "34px"         // this slide only
    codeFontSize = "0.40em"
  }
}
​```

- `fontSize` — font size for all content on the slide (any CSS length). Themes
  size headings in `em`, so everything scales together.
- `codeFontSize` — font size for code blocks (reveal.js's default is `0.55em`).
- `codeWrap` — when `true`, long code lines wrap instead of overflowing
  horizontally. A slide can set `false` to override a presentation-wide `true`.
```

(Remove the zero-width characters before the backticks — they are only there to keep this plan's fences intact.)

- [ ] **Step 3: Check whether configuration.md lists slideConfig properties**

Read `website/kslides/docs/configuration.md`. If it enumerates `slideConfig` properties, add `fontSize`, `codeFontSize`, and `codeWrap` rows/entries in the same format. If it only shows the cascade concept, no change needed.

- [ ] **Step 4: Commit (only if user has authorized commits)**

```bash
git add CHANGELOG.md website/kslides/docs/styling.md website/kslides/docs/configuration.md
git commit -m "Document the slide font-size API

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 7: Regenerate the published example slides in /docs

**Files:**
- Modify (generated): `docs/index.html`, `docs/layouts.html`, `docs/fragments.html`, `docs/backgrounds.html`, `docs/multicols.html`, `docs/multislide.html`

**Interfaces:**
- Consumes: the fat JAR built from the migrated example deck (Task 5).
- Produces: refreshed static decks whose head contains the generated `kslides-code-*` rules and whose sections carry the generated classes.

**Caveats (read before running):**
- `Slides.kt` sets `krokiUrl = "http://localhost:8000"` expecting a local Kroki server. Without it, Kroki diagram content fails to regenerate (kslides logs warnings and the generated `docs/kroki/` files may be broken). Precedent (commit 1e12b98) staged only the six deterministic deck files.
- `docs/letsPlot/` content embeds per-run random data — never stage it for this change.

- [ ] **Step 1: Build the fat JAR**

Run: `./gradlew buildFatJar`
Expected: BUILD SUCCESSFUL; JAR at `build/libs/kslides.jar`.

- [ ] **Step 2: Run the example app to regenerate /docs, then stop it**

Run (background — the app starts a blocking HTTP server after writing files):
`java -jar build/libs/kslides.jar`
Wait until the log shows the filesystem output is written (or ~30s), then kill the process.

- [ ] **Step 3: Review the diff and stage only the deck files**

```bash
git status docs/
git diff --stat docs/
```

Verify each of the six deck files gained:
- the new `<style>` block with `.reveal .kslides-code-1 pre { font-size: 0.60em; }` (and the 0.40em rule where definition slides exist),
- `kslides-code-*` classes on the definition-slide sections,
- and LOST the old `.reveal pre { font-size: 0.60em; }` / `.smallcode` CSS.

Then restore everything that should not change:

```bash
git checkout -- docs/letsPlot docs/kroki docs/playground 2>/dev/null || true
git add docs/index.html docs/layouts.html docs/fragments.html \
        docs/backgrounds.html docs/multicols.html docs/multislide.html
```

If `docs/kroki/` or `docs/playground/` show meaningful (non-noise) diffs, stop and surface them to the user instead of staging or discarding blindly.

- [ ] **Step 4: Commit (only if user has authorized commits)**

```bash
git commit -m "Regenerate example slides with the font-size API output

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

## Verification (after all tasks)

- `./gradlew build` — full build + tests green.
- `./gradlew lintKotlin detekt` — zero findings.
- Serve the regenerated deck (`java -jar build/libs/kslides.jar`, open `http://localhost:8080`) and visually confirm: definition slides render code at the smaller size, long lines wrap, and no slide lost its styling relative to the published site.
