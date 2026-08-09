---
icon: simple/markdown
---

# Markdown slides

`markdownSlide { }` accepts a Markdown string and hands it to reveal.js's Markdown plugin.

## Basic usage

```kotlin
--8<-- "MarkdownSlides.kt:basic"
```

The triple-quoted string is `trimIndent`-ed for you, so indentation in your Kotlin source doesn't bleed into the rendered output.

## Animated bullet points

Reveal.js's fragment classes work directly in Markdown via `<!-- .element: ... -->` comments:

```kotlin
--8<-- "MarkdownSlides.kt:bullets"
```

## Speaker notes

Anything after a line starting with `Note:` becomes speaker notes:

```kotlin
--8<-- "MarkdownSlides.kt:notes"
```

## Linkable slides

Set an `id` to link directly to a slide:

```kotlin
--8<-- "MarkdownSlides.kt:id"
```

The slide is then reachable at `/index.html#/intro`.

## Loading from a file

Prefer to keep long content out of your Kotlin source? See [Including content](../include.md).

## Special characters

Markdown content reaches reveal.js exactly as you type it, so nothing needs escaping — `<`, `&`,
generics like `List<String>`, and entities like `&nbsp;` all work, in prose and in fenced code
alike. The one sequence to avoid writing literally is `</script>`, which kslides rewrites for you.

This is not true of [HTML slides](html.md), whose content is parsed as markup.
