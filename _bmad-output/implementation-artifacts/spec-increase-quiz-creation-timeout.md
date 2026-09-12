---
title: 'Increase frontend quiz-creation timeout'
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

**Problem:** The frontend stops waiting too soon when the backend is generating a quiz, so valid but slow quiz-creation requests can be reported as failures.

**Approach:** Set the default timeout for the frontend quiz-creation request to five minutes (300,000 ms), while preserving the existing shorter timeout behavior for subsequent quiz reads and answer submissions.

</frozen-after-approval>

## Implementation Notes

Planning facts: no user-visible intent gaps remain; the change has no irreversible effects; the footprint is limited to the frontend API timeout constant/creation-request default and its regression tests. Preserve the existing `application.yaml` worktree change.

Implemented `QUIZ_CREATION_TIMEOUT_MS` as 300,000 ms and applied it as a minimum to `createQuiz()` requests. Kept the general 15-second default for quiz reads and answers, and updated API/composable tests to cover slow creation, the creation minimum, and unchanged non-creation behavior. `npm run test` passed with 45 tests and `npm run build` completed successfully.

## Review Triage Log

- medium — patched: the creation regression now asserts the literal 300,000 ms value and proves a response arriving after 15 seconds still succeeds.
- low — patched: added coverage that `getQuiz()` and `answerQuestion()` retain the 15-second default.
- medium — patched: creation timeout normalization clamps invalid or too-short values to the five-minute minimum, with regression coverage for a short value.
- false — the loading message is intentionally generic and the persistent header brand already provides the existing reset/new-quiz action; adding a new loading control is outside this timeout-only fix.
- false — the one-shot route intentionally uses the minimal spec shape without separate acceptance and verification sections, as permitted by the build workflow for small changes.
