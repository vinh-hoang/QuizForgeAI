---
title: 'Persist the Copilot subagent model preference'
type: 'chore'
created: '2026-08-26'
status: 'done'
review_loop_iteration: 0
route: 'one-shot'
---

# Persist the Copilot subagent model preference

## Intent

**Problem:** The preference to use Luna at maximum reasoning effort was only
present in one chat, so future agents could miss it or silently choose another
model.

**Approach:** Record the preference in the repository-level `AGENTS.md`,
including the canonical model identifiers, all subagent surfaces it covers, and
the required behavior when the launcher cannot honor the requested parameters.

## Suggested Review Order

- Confirm the repository-wide instruction covers every subagent entry point and preserves the no-fallback rule.
  [`AGENTS.md:29`](../../AGENTS.md#L29)
