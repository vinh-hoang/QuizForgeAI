---
title: 'Change supported quiz question counts'
type: 'feature'
created: '2026-08-23'
status: 'done'
review_loop_iteration: 0
baseline_commit: '80decb27b13cafb0d56eedfbaf1cfa769a681060'
context:
  - '{project-root}/frontend/AGENTS.md'
  - '{project-root}/FRONTEND_PLAN.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Quiz creation currently offers 5, 10, and 15 questions, but the desired session sizes are 3, 5, and 7. The backend and frontend must expose the same supported values so selections generate the requested number of questions.

**Approach:** Replace the backend `NumberOfQuestions` enum values and numeric conversion with `THREE`, `FIVE`, and `SEVEN`, then update the frontend type and session-length selector to send and display the same contract. Add focused backend coverage and update the shared frontend contract documentation.

## Boundaries & Constraints

**Always:** Use uppercase enum strings `THREE`, `FIVE`, and `SEVEN` on the JSON API; display numeric labels `3 questions`, `5 questions`, and `7 questions`; keep `FIVE` as the default; preserve the existing endpoint shape, question generation prompts, persistence, answer flow, and five-attempt retry loop; derive counts from the enum without a database migration; keep frontend verification network-free.

**Ask First:** If the repository reveals persisted requested-count data, a public compatibility requirement for `TEN`/`FIFTEEN`, or a migration/API change beyond replacing the supported options, stop before expanding the scope.

**Never:** Do not retain `TEN` or `FIFTEEN` as supported options; do not change the retry `repeat(5)`; do not modify generated `frontend/dist` or compiled backend output manually; do not change unrelated difficulty, scoring, database, or Spring AI behavior.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|-----------------------------|----------------|
| THREE_QUESTIONS | `numberOfQuestions: "THREE"` | Backend generates and persists exactly 3 questions; frontend can submit the same wire value | Fail the request visibly if generation cannot complete |
| FIVE_QUESTIONS | `numberOfQuestions: "FIVE"` | Backend generates exactly 5 questions and the existing default remains valid | Preserve existing generation/error behavior |
| SEVEN_QUESTIONS | `numberOfQuestions: "SEVEN"` | Backend generates and persists exactly 7 questions | Fail the request visibly if generation cannot complete |
| REMOVED_COUNT | `numberOfQuestions: "TEN"` or `"FIFTEEN"` | The enum no longer accepts the removed wire values | Request deserialization rejects the invalid value; no quiz is generated |
| FRONTEND_SELECTION | User chooses each session-length button | UI displays 3/5/7 and forwards `THREE`/`FIVE`/`SEVEN` without transformation | Type/build checks catch stale union members or options |

</frozen-after-approval>

## Code Map

- `src/main/kotlin/ai/quiz/forge/rest/model/CreateQuiz.kt:14-18` -- public request enum; replace the supported wire values.
- `src/main/kotlin/ai/quiz/forge/service/QuizService.kt:32-45` -- enum-to-number conversion and generation loop; map 3/5/7 while preserving the separate retry limit at `:81`.
- `src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt:93-110` -- existing mocked generation test; extend focused coverage to prove each supported count produces the corresponding question total.
- `frontend/src/types/quiz.ts:1-10` -- TypeScript `NumberOfQuestions` union and request payload type; keep it aligned with backend wire values.
- `frontend/src/components/QuizSetup.vue:40-44` -- session-length options and visible labels; replace the old 5/10/15 choices.
- `frontend/src/App.vue:11-14` and `frontend/src/api/quizApi.ts:33-37` -- default selection and unchanged payload forwarding; verify `FIVE` remains the default and no mapping is added.
- `FRONTEND_PLAN.md:26-39,96-103,124-129` -- shared API and flow documentation; update supported values and selector wording without changing endpoint shape.
- `bruno/quiz/create Quiz.yml:16` -- existing `FIVE` request sample remains valid; read-only compatibility reference.

## Tasks & Acceptance

**Execution:**
- [x] `src/main/kotlin/ai/quiz/forge/rest/model/CreateQuiz.kt` and `src/main/kotlin/ai/quiz/forge/service/QuizService.kt` -- replace the enum values and numeric mapping -- make backend generation support exactly 3, 5, and 7 questions.
- [x] `src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt` -- add focused count-coverage tests -- prove all supported values map to the requested question totals without changing retry behavior.
- [x] `frontend/src/types/quiz.ts` and `frontend/src/components/QuizSetup.vue` -- align the type union and selector values/labels -- keep frontend selections valid against the backend contract.
- [x] `FRONTEND_PLAN.md` -- update documented supported values and selector choices -- prevent the shared contract from advertising removed options.
- [x] Existing backend/frontend checks -- run focused Gradle tests, frontend build, and stale-value search -- verify the cross-layer contract and absence of old options.

**Acceptance Criteria:**
- Given a create request using `THREE`, `FIVE`, or `SEVEN`, when the backend processes it, then it generates exactly 3, 5, or 7 questions respectively.
- Given a create request using `TEN` or `FIFTEEN`, when JSON binding occurs, then the request is rejected because those enum values are no longer supported.
- Given the frontend setup screen, when a user selects a session length, then the only choices are 3, 5, and 7 questions and the payload contains the matching uppercase enum string.
- Given the initial setup state, when the page loads, then `FIVE` remains selected by default and no retry, persistence, answer, or endpoint behavior changes.

## Spec Change Log

## Verification

**Commands:**
- `.\gradlew.bat test --tests "ai.quiz.forge.service.QuizServiceIT" --no-daemon --console=plain` -- expected: focused backend tests pass, including 3/5/7 count coverage.
- `npm --prefix .\frontend run build` -- expected: TypeScript and Vite build succeed with the updated union/options.
- `rg -n "TEN|FIFTEEN|10 questions|15 questions|5, 10, and 15" src frontend\src FRONTEND_PLAN.md bruno` -- expected: no stale supported-count references remain.

**Results (2026-08-23):**
- Count, removed-value binding, and retry coverage passed with the focused Gradle selectors.
- The focused `QuizServiceIT` command passed after aligning stale assertions with the current request and prompt behavior; the new count, binding, and retry tests pass.
- `npm.cmd --prefix .\frontend run build` passed (PowerShell blocked the `npm` shim by execution policy).
- The stale-value search returned no matches.

## Suggested Review Order

**Backend contract and generation**

- Defines the public wire contract for supported session sizes.
  [`CreateQuiz.kt:14`](../../src/main/kotlin/ai/quiz/forge/rest/model/CreateQuiz.kt#L14)

- Converts each API enum value to the exact generation and persistence count.
  [`QuizService.kt:32`](../../src/main/kotlin/ai/quiz/forge/service/QuizService.kt#L32)

**Frontend selection**

- Keeps the TypeScript request union aligned with backend deserialization.
  [`quiz.ts:3`](../../frontend/src/types/quiz.ts#L3)

- Renders only the 3, 5, and 7 question choices with matching wire values.
  [`QuizSetup.vue:40`](../../frontend/src/components/QuizSetup.vue#L40)

**Verification and shared documentation**

- Proves supported counts persist correctly and removed values fail JSON binding.
  [`QuizServiceIT.kt:137`](../../src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt#L137)

- Documents the updated request values and setup selector choices.
  [`FRONTEND_PLAN.md:26`](../../FRONTEND_PLAN.md#L26)

**Manual checks (if no CLI):**
- Inspect the setup screen and confirm exactly three buttons labeled 3 questions, 5 questions, and 7 questions, with 5 selected initially.
