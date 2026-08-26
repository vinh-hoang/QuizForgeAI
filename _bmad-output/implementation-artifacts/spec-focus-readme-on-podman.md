---
title: 'Focus the README on Podman localhost setup'
type: 'chore'
created: '2026-08-26'
status: 'done'
review_loop_iteration: 0
route: 'one-shot'
---

# Focus the README on Podman localhost setup

## Intent

**Problem:** The Podman WSL section included unrelated distribution-management
guidance, which obscured the commands needed to make the database available on
Windows localhost.

**Approach:** Keep the README focused on the Podman machine, its forwarding
configuration, networking reset, Compose startup, and database readiness
verification.

## Suggested Review Order

- Confirm the section stays focused on Podman machine and localhost setup.
  [`README.md:38`](../../README.md#L38)
- Confirm machine-name substitution and configuration steps remain copyable.
  [`README.md:40`](../../README.md#L40)
  [`README.md:51`](../../README.md#L51)
- Confirm readiness is checked before starting the backend.
  [`README.md:100`](../../README.md#L100)
