- source_spec: `C:\Users\Vinh\Documents\Projects\QuizForgeAI\_bmad-output\implementation-artifacts\spec-update-spring-ai-version.md`
  summary: Add deterministic runtime smoke coverage for Spring AI native structured-output request and response handling.
  evidence: Existing tests mock ChatClient and do not exercise 2.0.1 request serialization, provider response handling, or typed entity conversion, so a runtime incompatibility could evade compilation and the current test suite.
