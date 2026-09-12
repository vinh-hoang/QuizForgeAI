---
title: 'Adjust frontend request timeouts'
type: 'bugfix'
created: '2026-09-12'
status: 'done'
route: 'oneshot'
review_loop_iteration: 0
context:
  - 'FRONTEND_PLAN.md'
  - 'frontend/AGENTS.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** The frontend request durations do not match the desired quiz workflow: creation currently permits five minutes, while answer submission still uses the shorter general request timeout.

**Approach:** Reduce the quiz-creation timeout minimum to three minutes, set answer submission's default timeout to one minute, and keep next-question reads at the existing 15-second default. Preserve request cancellation and all existing timeout/error handling.

</frozen-after-approval>

## Implementation Notes

Planning facts: no user-visible intent gaps remain; the change has no irreversible effects; the footprint is limited to frontend API timeout constants/defaults, regression tests, and this implementation record. Preserve the existing `application.yaml` worktree change.

Implemented a three-minute minimum for quiz creation, a one-minute default and invalid-input fallback for answer submission, and retained the 15-second default for next-question reads. Updated API and composable regression coverage; `npm run test` passed with 47 tests and `npm run build` completed successfully.

## Review Triage Log

- medium — patched: answer-timeout normalization now uses the answer-specific one-minute fallback instead of the global 15-second fallback for invalid values.
- low — patched: the answer timeout test asserts the literal 60,000 ms value in addition to using the exported constant.
- low — patched: added direct answer-submission cancellation coverage for the wrapped options path.
- medium — patched: added composable coverage proving an answer timeout leaves the question retryable and clears `isSubmitting`.
- low — patched: made the composable timeout fixture generic so it does not duplicate the API duration policy.
- low — patched: finalized this implementation record with status `done`, implementation notes, and verification results.
