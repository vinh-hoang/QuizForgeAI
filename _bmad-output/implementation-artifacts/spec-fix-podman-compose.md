---
title: 'Fix Windows Podman Compose startup'
type: 'bugfix'
created: '2026-08-26'
status: 'done'
review_loop_iteration: 0
route: 'one-shot'
---

# Fix Windows Podman Compose startup

## Intent

**Problem:** The Windows setup guide requires the separately installed
`podman-compose` command even though Podman 6.1 exposes Compose through
`podman compose`. This causes the documented startup and shutdown commands to
fail when only Podman's supported Compose provider is installed.

**Approach:** Align every database lifecycle command and the prerequisite with
the provider-aware `podman compose` entry point, and explain the distinction
from the unrelated hyphenated Python tool. Preserve the existing WSL port
forwarding instructions required for published ports to reach Windows.

## Suggested Review Order

- Confirm the prerequisite names the command users can run and verifies its provider.
  [`README.md:10`](../../README.md#L10)
- Confirm startup after machine upgrades uses the same Compose entry point.
  [`README.md:35`](../../README.md#L35)
- Confirm normal startup and shutdown no longer depend on `podman-compose`.
  [`README.md:55`](../../README.md#L55)
  [`README.md:66`](../../README.md#L66)
