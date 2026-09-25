- source_spec: `{project-root}/_bmad-output/implementation-artifacts/spec-update-spring-ai-version.md`
  summary: Add deterministic runtime smoke coverage for Spring AI native structured-output request and response handling.
  evidence: Existing tests mock ChatClient and do not exercise 2.0.1 request serialization, provider response handling, or typed entity conversion, so a runtime incompatibility could evade compilation and the current test suite.
- source_spec: `{project-root}/_bmad-output/implementation-artifacts/spec-change-question-count-options.md`
  summary: Fix the pre-existing low reasoning effort application test failure.
  evidence: The full backend suite still fails `QuizForgeAiApplicationTests.configuresLowReasoningEffort` at `src/test/kotlin/ai/quiz/forge/QuizForgeAiApplicationTests.kt:23`; this story does not change the application configuration or that test.
- source_spec: `{project-root}/_bmad-output/implementation-artifacts/spec-change-question-count-options.md`
  summary: Add automated frontend setup coverage for question-count labels, emitted values, and request serialization.
  evidence: The frontend has no test runner or component-test files, so the current verification relies on the type-checked build and manual browser inspection.
- source_spec: `_bmad-output/implementation-artifacts/spec-harden-frontend-review-findings.md`
  summary: Define the production CORS or same-origin proxy contract for deployments using an absolute frontend API origin.
  evidence: `VITE_API_BASE_URL` now supports a separate API origin, but backend CORS/reverse-proxy policy is outside this frontend-only story and its non-goals.
- source_spec: `_bmad-output/implementation-artifacts/spec-harden-frontend-review-findings.md`
  summary: Make offline recovery copy environment-aware for deployed API origins.
  evidence: The existing composable message names local port 8080 even when a deployed API base is configured; changing backend/deployment-specific copy needs a separate product decision.
- source_spec: `_bmad-output/implementation-artifacts/spec-harden-frontend-review-findings.md`
  summary: Add CI automation for the frontend test and production build commands.
  evidence: No CI workflow is present in the current repository, and this story verifies the commands locally without introducing repository-wide pipeline policy.
- source_spec: `_bmad-output/implementation-artifacts/spec-topic-viability-gate.md`
  summary: Decide whether the backend should enforce the frontend's 80-character quiz-topic limit.
  evidence: The UI caps topic input at 80 characters, while the existing POST /quiz contract accepts longer strings; changing direct API behavior requires a separate API policy decision.
