---
icon: lucide/list-tree
---

# Vertical slides

Reveal.js supports a 2D layout: arrow keys move horizontally between top-level slides, and ↓ drills into a vertical stack. In kslides, group vertical slides with `verticalSlides { }`:

```kotlin
--8<-- "VerticalSlides.kt:basic"
```

The block accepts any combination of `markdownSlide`, `htmlSlide`, and `dslSlide`.

## Why use vertical stacks?

- Keep deep dives off the main horizontal flow — viewers can skip them with → instead of ↓.
- Group related sub-points under one section heading.
- Provide optional "appendix" slides that are reachable but don't break the main rhythm.
