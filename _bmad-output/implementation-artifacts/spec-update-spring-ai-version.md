---
title: 'Update Spring AI dependency version'
type: 'chore'
created: '2026-08-23'
status: 'done'
review_loop_iteration: 0
baseline_commit: '7d6aa1cb6d442a14e7ef6744f98edc6c9b25a2c5'
context:
  - '{project-root}/_bmad-output/planning-artifacts/research/technical-spring-ai-native-structured-output-with-2026-08-15/research.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** QuizForgeAI currently imports Spring AI 2.0.0 even though a newer stable 2.x release is available. Staying on the older BOM leaves dependency fixes and compatibility improvements unavailable to the backend.

**Approach:** Upgrade the shared Spring AI BOM version to 2.0.1, then resolve and compile the backend to prove that the existing OpenAI chat, native structured-output, vector-store, and test integrations remain compatible.

## Boundaries & Constraints

**Always:** Keep the upgrade on the Spring AI 2.x line; update the single version property used by the BOM; preserve existing Spring Boot, Kotlin, OpenAI endpoint, model, native structured-output, persistence, REST, and test behavior; use dependency resolution and the existing Gradle checks to validate the result.

**Ask First:** If Spring AI 2.0.1 cannot resolve with the current Spring Boot 4.0.3 build or requires a source/API migration beyond the dependency declaration, stop before changing Spring Boot, Kotlin, application behavior, or public APIs.

**Never:** Do not upgrade unrelated dependencies; do not replace the BOM with individually pinned Spring AI modules; do not change quiz generation, answer evaluation, database schema, frontend code, or local-model configuration merely to conceal an incompatibility; do not add network-dependent tests.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|-----------------------------|----------------|
| VERSION_RESOLUTION | Gradle build requests Spring AI BOM 2.0.1 | All Spring AI modules resolve to 2.0.1 from the shared BOM | Report dependency-resolution failure and do not broaden the upgrade |
| API_COMPATIBILITY | Existing Kotlin source and tests compile against 2.0.1 | ChatClient, OpenAiChatModel, native structured output, and vector-store references remain valid | Stop and investigate only migration errors caused by the upgrade |
| APPLICATION_VALIDATION | Existing backend checks run after dependency refresh | Compilation and the repository's applicable tests complete without new upgrade-related failures | Distinguish pre-existing environment/test failures from regressions |

</frozen-after-approval>

## Code Map

- `build.gradle.kts:23` -- the single `springAiVersion` property currently set to `2.0.0`; update this value to the selected stable release.
- `build.gradle.kts:31-33,43-46` -- Spring AI modules consume the imported BOM, so no individual module versions should be added.
- `src/main/kotlin/ai/quiz/forge/config/ChatClientConfig.kt:3-20` -- existing `ChatClient` construction and native structured-output advisor; read-only compatibility surface.
- `src/main/kotlin/ai/quiz/forge/service/QuizService.kt:80-162` -- existing `ChatClient` calls and entity conversion paths; read-only compatibility surface.
- `src/test/kotlin/ai/quiz/forge/QuizForgeAiApplicationTests.kt:8-25` and `src/test/kotlin/ai/quiz/forge/service/QuizServiceIT.kt:56-247` -- existing Spring AI configuration and service test coverage; preserve behavior and use for validation.
- `src/main/resources/application.yaml:4-10` and `src/main/resources/application-test.yaml:2-8` -- existing OpenAI-compatible runtime/test settings; do not alter for this version-only change.

## Tasks & Acceptance

**Execution:**
- [x] `build.gradle.kts` -- change `springAiVersion` from `2.0.0` to `2.0.1` -- consume the latest stable Spring AI BOM without changing unrelated dependencies.
- [x] Existing Gradle verification -- resolve the dependency graph and run compilation/tests -- prove the upgraded modules remain compatible with the current backend. Compilation passed; the two failing tests also fail against the 2.0.0 baseline and are documented below.

**Acceptance Criteria:**
- Given the Gradle build, when dependencies are resolved, then the Spring AI BOM and its managed modules use version 2.0.1.
- Given the existing backend source, when Kotlin compilation runs, then all current Spring AI integrations compile without source changes.
- Given the existing test profile, when the applicable Gradle tests run, then no failure is introduced by the Spring AI version upgrade and any baseline failure is clearly identified.

## Spec Change Log

- 2026-08-23: Updated the shared Spring AI BOM property from `2.0.0` to `2.0.1`.

## Verification

**Commands:**
- `.\gradlew.bat dependencyInsight --dependency org.springframework.ai --configuration compileClasspath --refresh-dependencies --no-daemon --console=plain` -- expected: resolved Spring AI components report version 2.0.1.
- `.\gradlew.bat compileKotlin compileTestKotlin --no-daemon --console=plain` -- expected: BUILD SUCCESSFUL.
- `.\gradlew.bat test --no-daemon --console=plain` -- expected: existing tests pass, or any unrelated baseline failure is documented separately.

**Results (2026-08-23):**

- Dependency resolution succeeded; the Spring AI components reported by `dependencyInsight` resolve to `2.0.1`.
- `compileKotlin compileTestKotlin` completed successfully.
- `test` completed 15 tests with 2 failures: `QuizForgeAiApplicationTests.configuresLowReasoningEffort` and `QuizServiceIT.createQuiz generates draft content then structures it with native output`. Both failures reproduce with `springAiVersion` set back to `2.0.0`; they are baseline failures, not regressions from this upgrade. The first observes `reasoningEffort` as `null` instead of `low`; the second expects `options(...)` interactions that the current service does not make.

**Manual checks (if no CLI):**
- Inspect the resolved dependency graph and confirm no Spring AI 2.0.0 artifacts remain.

## Suggested Review Order

- The shared property drives every Spring AI module through the imported BOM.
  [`build.gradle.kts:23`](../../build.gradle.kts#L23)
