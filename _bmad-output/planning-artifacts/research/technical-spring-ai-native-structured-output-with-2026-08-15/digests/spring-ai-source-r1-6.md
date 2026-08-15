# Digest: Spring AI native structured output source

## Findings

- claim: Spring AI implements `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT` by setting the `STRUCTURED_OUTPUT_NATIVE` ChatClient attribute to true; the per-call alternative is `useProviderStructuredOutput()`.
  source: https://github.com/spring-projects/spring-ai/blob/main/spring-ai-client-chat/src/main/java/org/springframework/ai/chat/client/AdvisorParams.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: implementation
- claim: Spring AI's OpenAI options include a `reasoningEffort` option and its OpenAI model adapter maps a non-null value to the underlying SDK request's reasoning-effort field.
  source: https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java; https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatModel.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: implementation
- claim: Spring AI's native structured-output behavior depends on the underlying model implementing structured-output chat options; the advisor itself does not make an incompatible model capable of schema-constrained generation.
  source: https://github.com/spring-projects/spring-ai/blob/main/spring-ai-client-chat/src/main/java/org/springframework/ai/chat/client/AdvisorParams.java; https://github.com/spring-projects/spring-ai/blob/main/spring-ai-client-chat/src/main/java/org/springframework/ai/chat/client/DefaultChatClient.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: medium
  class: compatibility

## Leads

- Repeat the source inspection against the exact Spring AI 2.0.0 tag used by this project; current main may contain newer Responses API behavior.
- Inspect OpenAI model request construction around response format and reasoning fields together.

## Looked for

- Native structured-output switch implementation and typed reasoning option handling in Spring AI source.
