---
icon: lucide/file-input
---

# Including content

Inline strings get unwieldy fast. The `include()` helper loads content from disk or a URL so your slide bodies stay short.

## From a local file

```kotlin
--8<-- "Include.kt:file"
```

Paths are resolved relative to the working directory of the running program.

## From a URL

```kotlin
--8<-- "Include.kt:url"
```

Useful for pulling READMEs or shared snippets without copy-pasting.

## With code snippets

`include()` plays well with `codeSnippet { }` — see [Code snippets](extensions/code-snippets.md):

```kotlin
--8<-- "CodeSnippets.kt:from-url"
```

## Best practices

- Keep slide bodies in source-controlled files (`src/main/resources/slides/...`) so reviewers can diff them.
- Use URL includes for content owned by other repos to avoid duplication, but be mindful of network availability at build time.
