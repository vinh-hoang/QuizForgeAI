- source_spec: `{project-root}/_bmad-output/implementation-artifacts/spec-update-spring-ai-version.md`
  summary: Add deterministic runtime smoke coverage for Spring AI native structured-output request and response handling.
  evidence: Existing tests mock ChatClient and do not exercise 2.0.1 request serialization, provider response handling, or typed entity conversion, so a runtime incompatibility could evade compilation and the current test suite.
- source_spec: `{project-root}/_bmad-output/implementation-artifacts/spec-change-question-count-options.md`
  summary: Fix the pre-existing low reasoning effort application test failure.
  evidence: The full backend suite still fails `QuizForgeAiApplicationTests.configuresLowReasoningEffort` at `src/test/kotlin/ai/quiz/forge/QuizForgeAiApplicationTests.kt:23`; this story does not change the application configuration or that test.
- source_spec: `{project-root}/_bmad-output/implementation-artifacts/spec-change-question-count-options.md`
  summary: Add automated frontend setup coverage for question-count labels, emitted values, and request serialization.
  evidence: The frontend has no test runner or component-test files, so the current verification relies on the type-checked build and manual browser inspection.
