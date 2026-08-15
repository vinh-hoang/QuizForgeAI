# Digest: Spring AI 2.0.0 exact OpenAI adapter

## Findings

- claim: Spring AI 2.0.0's `OpenAiChatOptions` implements both `StructuredOutputChatOptions` and tool-calling options, and includes a typed `reasoningEffort` field plus builder method.
  source: https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: version-compatibility
- claim: Spring AI 2.0.0's `OpenAiChatModel.createRequest()` independently writes `responseFormat` as JSON schema and `reasoningEffort` into the same OpenAI Chat Completions request.
  source: https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatModel.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: version-compatibility
- claim: Spring AI 2.0.0's `extraBody` is also serialized as additional top-level body properties, so the current `extra-body.reasoning_effort` workaround is an untyped provider-specific path rather than a native structured-output requirement.
  source: https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatModel.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: version-compatibility
- claim: Spring AI 2.0.0 is implemented on the OpenAI Chat Completions client path, not the Responses API path.
  source: https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatModel.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: version-compatibility

## Leads

- Verify the exact Spring Boot property name for the typed reasoning option (`reasoning-effort`) in the 2.0.0 auto-configuration.
- Send controlled Chat Completions requests to the local endpoint with schema plus reasoning omitted/none/low to isolate server/model behavior.

## Looked for

- Exact version compatibility between native structured output and reasoning options in Spring AI 2.0.0.
