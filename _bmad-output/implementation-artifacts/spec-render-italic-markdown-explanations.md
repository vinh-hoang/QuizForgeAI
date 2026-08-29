---
title: 'Render italic Markdown in explanations'
type: 'bugfix'
created: '2026-08-29'
status: 'done'
route: 'one-shot'
---

# Render italic Markdown in explanations

## Intent

**Problem:** Quiz explanations containing single-asterisk Markdown, such as `*Dragon Ball Super*`, displayed the asterisks literally because `MarkdownText` only interpreted bold markers.

**Approach:** Extend the existing safe text-segment parser to recognize single-asterisk emphasis and render it with semantic `<em>` markup while preserving existing bold formatting.

## Suggested Review Order

- Recognize bold and single-asterisk emphasis in the existing text parser.
  [`MarkdownText.vue:16`](../../frontend/src/components/MarkdownText.vue#L16)
- Render italic segments semantically while retaining the current bold output.
  [`MarkdownText.vue:43`](../../frontend/src/components/MarkdownText.vue#L43)
