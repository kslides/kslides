---
icon: simple/html5
---

# HTML slides

`htmlSlide { }` passes raw HTML straight through to reveal.js. Use it when you need precise control over markup or when embedding media that doesn't translate cleanly from Markdown.

## Basic usage

```kotlin
--8<-- "HtmlSlides.kt:basic"
```

## Targeting CSS

Set `classes` to add CSS classes onto the slide's `<section>` element:

```kotlin
--8<-- "HtmlSlides.kt:classes"
```

You can then style it from the presentation's CSS — see [Styling](../styling.md).

## Mixing with the DSL

Need to generate just part of an HTML slide programmatically? Reach for [DSL slides](dsl.md) instead — the kotlinx.html DSL composes more cleanly than string concatenation.

## Angle brackets

`htmlSlide` content is parsed as markup — that is the point of the slide type — so every tag must
be closed and a `<` that is not opening a tag has to be escaped:

```kotlin
htmlSlide {
  content {
    """
    <p>Write generics as List&lt;String&gt;, and close void elements: <br/></p>
    """
  }
}
```

Ampersands need no such care: `&`, `&nbsp;` and `&mdash;` are repaired for you. If a `<` does slip
through, the build fails with the deck, the offending line and the fix rather than a bare parser
error.

Markdown and DSL slides have no such restriction — write `List<String>` in a `markdownSlide` and it
just works.
