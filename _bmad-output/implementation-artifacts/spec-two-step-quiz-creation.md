---
title: 'Create quizzes through two-step LLM generation and structuring'
type: 'feature'
created: '2026-08-22'
status: 'done'
review_loop_iteration: 0
baseline_commit: '2a5d8ecb765b6585924a1556d29201b7b70c29ac'
context:
  - '{project-root}/_bmad-output/planning-artifacts/research/technical-spring-ai-native-structured-output-with-2026-08-15/research.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Quiz generation currently asks the model to reason about a question and satisfy provider-native structured output constraints in one request. That coupling can weaken or truncate question content.

**Approach:** Generate each question with the existing prompt as an unstructured response. Send that text to a second call whose formatting instruction is to place the draft into native `NewQuestion` output, without changing the quiz API or persistence.

## Boundaries & Constraints

**Always:** Apply the two calls sequentially per question; keep the existing generation prompt and request behavior; pass complete first-call text to the second call; use native structured output only for step 2; preserve `NewQuestion` deserialization, persistence, logging, and five-attempt retries; keep tests network-free.

**Ask First:** If Spring AI cannot disable native structured output only for step 1, stop before changing `ChatClientConfig` and ask whether to add a second qualified `ChatClient`. If live probing shows truncation, ask before changing token or model configuration.

**Never:** Do not expose raw draft text or chain-of-thought; manually parse step 2 as JSON; duplicate its schema in the prompt; change `CreateQuiz`, `QuizDto`, the database, answer evaluation, frontend, or reasoning-effort configuration; or require live services in tests.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|-----------------------------|----------------|
| HAPPY_PATH | Valid request and non-empty step-1 draft | Step 2 returns native `NewQuestion`; existing API and persistence are used | N/A |
| EMPTY_DRAFT | Step 1 returns null or blank content | No step-2 call is made; the question attempt is retried | Log the attempt and preserve final failure behavior |
| STRUCTURING_FAILURE | Step 1 succeeds but step 2 fails | The pair is one failed attempt; no partial question is persisted | Reuse retry logging and failure propagation |

</frozen-after-approval>

## Code Map

- `src/main/kotlin/ai/quiz/forge/service/QuizService.kt:36-113` -- `createQuiz()`, `generateQuestion()`, and the two generation helpers are the generation boundary; preserve the loop, prompt inputs, five-attempt retry, and final save.
- `src/main/kotlin/ai/quiz/forge/config/ChatClientConfig.kt:12-20` -- global `ChatClient` configuration applies `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT`; read-only unless the dependency lacks the supported per-request opt-out for step 1.
- `src/main/kotlin/ai/quiz/forge/service/model/ai/generated/NewQuestion.kt:3-10` -- step 2 native structured-output target; no new DTO or manual JSON parser is needed.
- `src/main/kotlin/ai/quiz/forge/service/QuizPersistenceService.kt:20-23` -- unchanged persistence boundary after generation.
- `src/main/kotlin/ai/quiz/forge/rest/QuizController.kt:20-24`, `src/main/kotlin/ai/quiz/forge/rest/model/CreateQuiz.kt`, and `src/main/kotlin/ai/quiz/forge/rest/model/QuizDto.kt` -- unchanged HTTP contract and mapping.
- `src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt:30-247` -- H2/Liquibase integration tests and chained `ChatClient` mocks; cover `content()` then native `entity(NewQuestion::class.java)`, advisor opt-out, handoff, blank drafts, and retries.

## Tasks & Acceptance

**Execution:**
- [x] `src/main/kotlin/ai/quiz/forge/service/QuizService.kt` -- refactor each question attempt into raw generation then native `NewQuestion` structuring, passing delimited raw content and preserving atomic retries -- separate reasoning from formatting.
- [x] `src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt` -- mock and verify both calls, prompt handoff, blank-draft handling, and retry after either call fails -- prove the contract without a live model.
- [x] Existing backend test configuration -- run focused and full Gradle tests -- verify self-contained tests and unchanged answer behavior.

**Acceptance Criteria:**
- Given a valid `CreateQuiz`, when a question is generated, then the existing prompt produces unstructured text before a native structured-output call converts it to `NewQuestion`.
- Given the second request, when built, then it contains the complete first response and a concise formatting instruction; its schema comes from `NewQuestion`, not prompt-written JSON rules.
- Given either call fails or the first response is blank, when retried, then no partial question is saved and the existing maximum-attempt behavior is preserved.
- Given the test profile, when focused and full Gradle tests run, then they pass without Docker, PostgreSQL, network access, or a live LLM, with REST and persistence unchanged.

## Spec Change Log

- 2026-08-22: Implemented two-step question generation (raw draft + native structuring), added blank-draft and structuring-failure retry handling, and expanded `QuizServiceIT` coverage for handoff and matrix scenarios.

## Design Notes

This follows the linked decoupled-prompt pattern: step 1 can produce a normal draft, while step 2 performs conversion under the provider's native schema constraint. The raw response is internal only. A retry repeats the pair so a failed formatter cannot use stale draft content.

## Verification

**Commands:**
- `.\gradlew.bat test --tests "ai.quiz.forge.service.QuizServiceIT"` -- expected: focused quiz creation and answer tests pass with mocked two-step calls.
- `.\gradlew.bat test` -- expected: BUILD SUCCESSFUL with all existing tests passing without external services.

**Results:**
- 2026-08-22: The focused `QuizServiceIT` command passed after the review fixes.
- 2026-08-22: The full suite reached 15 tests but one pre-existing configuration assertion failed because the current `application.yaml` uses `reasoning-effort: on` while `QuizForgeAiApplicationTests` expects `low`; this configuration commit is outside this feature's working changes.

## Suggested Review Order

**Two-stage generation**

- `generateQuestion` now makes an atomic raw-draft then native-schema attempt.
  [`QuizService.kt:76`](../../src/main/kotlin/ai/quiz/forge/service/QuizService.kt#L76)

- Step 1 disables provider-native schema decoding while preserving the existing generation prompt.
  [`QuizService.kt:90`](../../src/main/kotlin/ai/quiz/forge/service/QuizService.kt#L90)

- Step 2 receives the exact draft with a narrow structuring instruction.
  [`QuizService.kt:107`](../../src/main/kotlin/ai/quiz/forge/service/QuizService.kt#L107)

**Behavior verification**

- Test setup executes the step-one advisor override and shared request chain.
  [`QuizServiceIT.kt:59`](../../src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt#L59)

- The happy path verifies two calls, prompt handoff, and structured fields.
  [`QuizServiceIT.kt:92`](../../src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt#L92)

- Blank and null drafts retry without invoking structuring on failed attempts.
  [`QuizServiceIT.kt:122`](../../src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt#L122)
  [`QuizServiceIT.kt:138`](../../src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt#L138)

- Structuring failures repeat the pair and preserve failure atomicity.
  [`QuizServiceIT.kt:154`](../../src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt#L154)
  [`QuizServiceIT.kt:181`](../../src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt#L181)

**Related runtime configuration**

- This baseline-adjacent setting is separate from two-step code and explains the known full-suite failure.
  [`application.yaml:9`](../../src/main/resources/application.yaml#L9)
