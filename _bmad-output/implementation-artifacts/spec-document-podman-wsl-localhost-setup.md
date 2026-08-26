---
title: 'Document the Windows Podman WSL localhost setup'
type: 'chore'
created: '2026-08-26'
status: 'done'
review_loop_iteration: 0
route: 'one-shot'
---

# Document the Windows Podman WSL localhost setup

## Intent

**Problem:** The Windows setup guide did not explain the distinction between
Ubuntu and Podman's managed WSL distribution, and its localhost setup lacked
the recovery and readiness steps needed after changing WSL networking.

**Approach:** Document the separate WSL roles, discovery commands, forwarding
configuration, user-mode networking requirement, WSL reset warning, Ubuntu
default selection, and a retrying PostgreSQL connectivity check.

## Suggested Review Order

- Confirm the guide explains Podman's managed WSL machine without conflating it with Ubuntu.
  [`README.md:37`](../../README.md#L37)
- Confirm the forwarding setup and networking reset are reproducible and warn about stopping WSL.
  [`README.md:53`](../../README.md#L53)
  [`README.md:66`](../../README.md#L66)
- Confirm the readiness check waits for PostgreSQL before the backend starts.
  [`README.md:95`](../../README.md#L95)
