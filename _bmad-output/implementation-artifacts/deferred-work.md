- source_spec: `{project-root}/_bmad-output/implementation-artifacts/spec-update-spring-ai-version.md`
  summary: Add deterministic runtime smoke coverage for Spring AI native structured-output request and response handling.
  evidence: Existing tests mock ChatClient and do not exercise 2.0.1 request serialization, provider response handling, or typed entity conversion, so a runtime incompatibility could evade compilation and the current test suite.
- source_spec: `{project-root}/_bmad-output/implementation-artifacts/spec-change-question-count-options.md`
  summary: Align the existing QuizServiceIT native-output assertions with the current QuizService request behavior.
  evidence: The focused QuizServiceIT command fails at a pre-existing request-options verification; the current service does not invoke requestSpec.options and the test also asserts older prompt wording.
