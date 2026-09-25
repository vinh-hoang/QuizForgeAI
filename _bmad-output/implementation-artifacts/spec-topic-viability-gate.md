---
title: 'Reject nonviable quiz topics before generation'
type: 'feature'
created: '2026-09-25'
status: 'done'
route: 'dispatch'
review_loop_iteration: 0
baseline_commit: '5ab64f48cf7044d4da8fba08a35ab41a8749fd62'
context:
  - '{project-root}/FRONTEND_PLAN.md'
  - '{project-root}/_bmad-output/implementation-artifacts/spec-two-step-quiz-creation.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** The quiz generator accepts arbitrary strings and may spend several model calls generating questions for input that cannot support a meaningful quiz.

**Approach:** Before generating questions, have the LLM make one structured viability decision with a single Boolean field. Continue through the existing quiz flow when viable; reject nonviable topics before generation or persistence.

## Boundaries & Constraints

**Always:** A topic is viable when it is non-empty, not obvious gibberish, names a recognizable subject or activity, and can support at least one meaningful question without invented context. Preserve the existing `POST /quiz` request and success response. A rejected topic returns HTTP 422; the frontend keeps the entered text, shows concise recovery guidance, focuses the topic field, and selects its text for replacement. If repeated generation failures occur after approval, tune the viability check based on those cases.

**Never:** Do not add a database change, a separate validation endpoint, reason codes, suggested topics, or alternate generation fallback. Do not generate or persist a quiz after a negative viability result. Keep model failures distinct from a negative viability decision.

## Code Map

- `src/main/kotlin/ai/quiz/forge/service/QuizService.kt` -- `createQuiz()` is the pre-generation boundary. Insert the viability call before question generation and before the existing persistence path; reuse the injected `ChatClient` and preserve the current generation/retry behavior.
- `src/main/kotlin/ai/quiz/forge/service/model/ai/generated/` -- add a one-field structured response model for the viability Boolean, following the existing AI response model conventions.
- `src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt` -- its shared `ChatClient` mock setup serves all service integration scenarios; make the new preflight response viable by default and account for its additional prompt in existing call-count assertions. Keep tests network-free.
- `frontend/src/composables/useQuiz.ts` -- `startQuiz()` already returns to setup and retains the topic on request failure; map HTTP 422 to concise topic-specific guidance.
- `frontend/src/App.vue` -- `errorStatus` is already available. On the setup screen after HTTP 422, focus and select `#topic` instead of focusing the screen heading.
- `frontend/src/components/QuizSetup.vue` and `frontend/src/api/quizApi.ts` -- the topic input, `role="alert"` error display, and `ApiError.status` handling already exist; preserve their current contracts.

## Tasks & Acceptance

**Execution:**
- [x] `src/main/kotlin/ai/quiz/forge/service/model/ai/generated/TopicViability.kt` -- add a Boolean-only structured response type -- keep the LLM result minimal.
- [x] `src/main/kotlin/ai/quiz/forge/service/QuizService.kt` -- validate once at the start of `createQuiz()` and return HTTP 422 on `false` -- prevent question-generation calls and persistence for rejected topics.
- [x] `src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt` -- update the existing shared mock fixture and call-count expectations -- keep current service scenarios compatible with the preflight call.
- [x] `frontend/src/composables/useQuiz.ts` -- map 422 to user-facing topic guidance -- make rejection actionable.
- [x] `frontend/src/App.vue` -- focus and select the retained topic after a 422 returns to setup -- enable immediate replacement.

**Acceptance Criteria:**
- Given a quiz creation request, when `createQuiz()` begins, then one structured viability check runs before question generation.
- Given the viability result is true, when the request continues, then the existing question generation, persistence, and success response remain unchanged.
- Given the viability result is false, when the request completes, then it returns HTTP 422 without generating questions or persisting a quiz.
- Given the validator cannot return a result, when the model call fails, then the request follows the server error path and does not treat the failure as a user topic rejection.
- Given the UI receives HTTP 422, when it returns to setup, then the typed topic remains visible and selected, an accessible inline message explains that a usable topic is needed, and the user can replace it and submit again.

## Implementation Notes

- Added a `TopicViability` structured response and run its single LLM check before generating any quiz questions. A negative result maps to HTTP 422; a missing or failed model response remains a server error.
- Whitespace-only topics are rejected even if the model returns `viable=true`; topic text is XML-escaped before interpolation into the viability prompt.
- The frontend keeps the rejected topic, renders inline guidance, then focuses and selects the topic after the setup screen enters. The user can replace it and retry.
- Focused backend integration test passed: 18 tests via `test --tests "ai.quiz.forge.service.QuizServiceIT"`.
- Frontend suite passed: 49 tests across 5 files. `npm run build` passed.

## Review Triage Log

| Finding | Verdict and evidence | Disposition |
|---|---|---|
| Blind hunter: whitespace-only topic may pass if the model says viable. | **Medium** — the reviewed version relied on the model Boolean; the current code also rejects `topic.isBlank()`, covered by a test where the model returns true. | **Patch** — guarded after the required viability check; no quiz generation or persistence follows rejection. |
| Blind hunter: topic can close the XML element and inject prompt instructions. | **Medium** — raw topic interpolation could create a second `</quiz-topic>` and alter prompt structure; the topic is now XML-escaped and a test asserts only one literal closing tag. | **Patch** — escape `&`, `<`, and `>` before interpolation. Grouped with the edge-case hunter's duplicate finding below. |
| Blind hunter: backend has no matching 80-character topic limit. | **Medium** — direct API callers can submit longer strings, and this feature sends them through an additional model call; imposing a limit changes existing API behavior and needs a separate policy decision. | **Defer** — recorded in `deferred-work.md`; retain the existing API contract for this feature. |
| Blind hunter: missing test for a null structured viability result. | **Medium** — the Elvis branch throws a server error, but had no regression test; a new test confirms the error is not treated as a 422 and generation/persistence do not occur. | **Patch** — added explicit null-result coverage. Grouped with the verification-gap review's duplicate below. |
| Blind hunter: verification command list omits the tests reported in implementation notes. | **Low** — the commands section is narrower than the checks recorded above it, but correcting this finding requires editing this build's spec. | **Rejected per build-review rule** — findings whose fix edits this build's spec are not applied. |
| Edge-case hunter: XML-formatted topic may terminate the wrapper and steer the viability answer. | **Medium** — this is the same prompt-boundary defect as the blind hunter's XML-injection finding; escaping and the focused test now prevent the extra closing element. | **Patch** — same root-cause group as blind-hunter XML finding; fixed once. |
| Verification-gap reviewer: null viability response path lacks a test. | **Medium** — the existing null fallback needed direct verification; the new test covers the server-error path and verifies no question generation or quiz persistence. | **Patch** — same test-coverage gap as blind-hunter's null-result finding; fixed once. |

## Design Notes

Use the HTTP status already retained by `ApiError` to distinguish topic rejection in the client. The model response contains only the Boolean; the application owns the recovery message and focus behavior. The topic is sent as data to the validator, which should assess it rather than follow instructions embedded in it.

## Verification

**Commands:**
- `.\\gradlew.bat compileKotlin` -- expected: backend production sources compile.
- `npm run build` from `frontend/` -- expected: TypeScript and Vite production build succeed.
