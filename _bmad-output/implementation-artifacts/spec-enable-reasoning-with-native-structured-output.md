---
title: 'Enable reasoning with native structured output'
type: 'bugfix'
created: '2026-08-15'
status: 'done'
review_loop_iteration: 0
baseline_commit: '0c913187fc25df9124fe387da4641b5d07398a2e'
context:
  - '{project-root}/_bmad-output/planning-artifacts/research/technical-spring-ai-native-structured-output-with-2026-08-15/research.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** `application.yaml` disables model reasoning with the untyped `extra-body.reasoning_effort: none` workaround because native structured output previously failed. This prevents the model from using reasoning even though Spring AI 2.0.0 can send reasoning effort and native schema output in the same request.

**Approach:** Replace the workaround with Spring AI's typed `spring.ai.openai.chat.reasoning-effort: low` property. Keep provider-native structured output enabled, add a configuration-level assertion, and verify that the existing network-free test suite remains self-contained.

## Boundaries & Constraints

**Always:** Use the typed Spring AI property; keep `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT`; preserve `NewQuestion` and `Answer` entity deserialization; keep the default test profile independent of Docker, PostgreSQL, and a live LLM.

**Ask First:** Do not add a two-call reasoning/formatting architecture, change the model, or add a token budget unless live quiz prompts still truncate after this change.

**Never:** Do not restore `reasoning_effort: none`; do not disable native structured output; do not expose or request raw chain-of-thought; do not add network-dependent tests; do not change `QuizService`, AI response models, or the frontend for this configuration fix.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|-----------------------------|----------------|
| CONFIGURATION | Main application profile loads | OpenAI chat options contain reasoning effort `low`; no extra-body workaround is present | Spring context fails if the property cannot bind |
| NATIVE_ENTITY_CALL | `QuizService` requests `NewQuestion` or `Answer` through `ChatClient.entity()` | Native provider schema remains enabled while reasoning is allowed | Preserve existing null/AI error handling |
| TEST_PROFILE | `@ActiveProfiles("test")` with mocked `ChatClient` | Configuration assertion and existing integration tests pass without network services | Test failure identifies binding or regression |

</frozen-after-approval>

## Code Map

- `src/main/resources/application.yaml` -- active OpenAI-compatible endpoint configuration; replace `spring.ai.openai.chat.extra-body.reasoning_effort` with `spring.ai.openai.chat.reasoning-effort: low`.
- `src/main/kotlin/ai/quiz/forge/config/ChatClientConfig.kt` -- `chatClient()` already applies `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT`; read-only, no change expected.
- `src/main/kotlin/ai/quiz/forge/service/QuizService.kt` -- `generateQuestion()` and `processQuestionAnswer()` call `.entity(NewQuestion::class.java)` and `.entity(Answer::class.java)`; preserve these boundaries.
- `src/main/kotlin/ai/quiz/forge/service/model/ai/generated/NewQuestion.kt` -- native schema target for quiz generation; read-only.
- `src/main/kotlin/ai/quiz/forge/service/model/ai/generated/Answer.kt` -- native schema target for answer evaluation; read-only.
- `src/test/kotlin/ai/quiz/forge/QuizForgeAiApplicationTests.kt` -- Spring context test; add the configuration-level reasoning-effort assertion here.
- `src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt` -- existing H2/Liquibase integration tests with mocked `ChatClient`; preserve the mock chain and network-free behavior.
- `src/main/resources/application-test.yaml` -- test datasource and local-model settings; do not make tests depend on the endpoint.
- `_bmad-output/planning-artifacts/research/technical-spring-ai-native-structured-output-with-2026-08-15/research.md` -- verified rationale and local probe evidence; use it for the token-budget caveat.

## Tasks & Acceptance

**Execution:**
- [x] `src/main/resources/application.yaml` -- replace the `extra-body` reasoning workaround with typed `reasoning-effort: low` -- enable reasoning without changing native schema behavior.
- [x] `src/test/kotlin/ai/quiz/forge/QuizForgeAiApplicationTests.kt` -- assert the configured `OpenAiChatModel` reasoning effort is `low` -- catch property binding regressions without network access.
- [x] Existing Spring test configuration -- run the focused and full test checks -- prove the change remains self-contained.

**Acceptance Criteria:**
- Given the main application configuration, when Spring AI creates `OpenAiChatModel`, then its reasoning effort is `low` and the old `extra-body.reasoning_effort: none` setting is absent.
- Given `ChatClientConfig.chatClient()`, when a service calls `.entity(NewQuestion::class.java)` or `.entity(Answer::class.java)`, then provider-native structured output remains enabled and no service/model/frontend code changes are required.
- Given the `test` profile, when the Spring context and `QuizServiceIT` execute, then all tests pass without Docker, PostgreSQL, or a live LLM.
- Given the configured local endpoint and a manual provider probe, when a schema request uses reasoning effort `low`, then the response contains valid schema-shaped JSON and may expose reasoning metadata without requiring that metadata in the DTO.

## Spec Change Log

## Design Notes

The provider-backed probe is intentionally manual. Existing tests mock `ChatClient` by design, so making them call `localhost:1234` would violate the repository's self-contained test contract. The research probe showed that a 256-token budget can truncate reasoning plus JSON; no token limit is added until the real quiz prompts demonstrate that failure.

## Verification

**Commands:**
- `.\gradlew.bat test` -- expected: BUILD SUCCESSFUL with all existing and new tests passing without external services.

**Manual checks (if no CLI):**
- With the local OpenAI-compatible endpoint available, send a small `response_format` JSON Schema request using `reasoning_effort: low`; expect valid JSON plus optional reasoning metadata, and confirm no truncation.

## Suggested Review Order

**Runtime configuration**

- Typed reasoning enables model thinking without changing the endpoint or model.
  [`application.yaml:9`](../../src/main/resources/application.yaml#L9)

- The existing advisor preserves provider-native structured output at the ChatClient boundary.
  [`ChatClientConfig.kt:16`](../../src/main/kotlin/ai/quiz/forge/config/ChatClientConfig.kt#L16)

**Verification**

- The context test proves typed binding and removes the legacy extra-body workaround.
  [`QuizForgeAiApplicationTests.kt:22`](../../src/test/kotlin/ai/quiz/forge/QuizForgeAiApplicationTests.kt#L22)

- Existing service tests preserve both structured entity deserialization paths without network access.
  [`QuizServiceIT.kt:49`](../../src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt#L49)
