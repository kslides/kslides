---
icon: lucide/presentation
---

# Slides

kslides supports three slide types. They share the same configuration surface — pick whichever fits the content.

| Type             | Best for                              | DSL block         |
|------------------|---------------------------------------|-------------------|
| Markdown         | Prose, bullets, light formatting      | `markdownSlide`   |
| HTML             | Fine-grained markup, embedded media   | `htmlSlide`       |
| Kotlin HTML DSL  | Generated content, type-safe markup   | `dslSlide`        |

You can also nest any of them inside a `verticalSlides { }` block to make a vertical stack.

## At a glance

=== "Markdown"

    ```kotlin
    --8<-- "MarkdownSlides.kt:basic"
    ```

=== "HTML"

    ```kotlin
    --8<-- "HtmlSlides.kt:basic"
    ```

=== "DSL"

    ```kotlin
    --8<-- "DslSlides.kt:basic"
    ```

## Pick a flavor

- [Markdown slides](markdown.md)
- [HTML slides](html.md)
- [DSL slides](dsl.md)
- [Vertical slides](vertical.md)
