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

## Escaping

`include()` escapes markup so the file's contents render as text rather than being interpreted.
Which escaping applies is decided by the block you call it from, not by you: a `markdownSlide`, a
`dslSlide`, and a `<code>` block each escape their own output, so kslides hands them the content
raw; an `htmlSlide` is parsed as markup, so there the content is escaped on the way in.

The overloads are resolved by the enclosing receiver, so the same call is correct everywhere. One
caveat: that resolution is lexical. If you factor a `content{}` body out into a helper function,
the call leaves the receiver and reverts to the escaping form — keep interpolated `include()` calls
inside the block.
