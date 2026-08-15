# Digest: Spring AI native structured output and OpenAI-compatible reasoning

## Findings

- claim: Spring AI native structured output sends the generated JSON schema through the provider's structured-output API instead of appending format instructions to the prompt.
  source: https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: integration
- claim: `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT` is the global ChatClient advisor switch, and the underlying model must support `StructuredOutputChatOptions`.
  source: https://docs.spring.io/spring-ai/reference/api/chatclient.html
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: compatibility
- claim: Spring AI documents OpenAI `JSON_SCHEMA` response format as the provider-native structured-output mode, while `extra-body` is flattened into the top-level request for OpenAI-compatible servers.
  source: https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: integration
- claim: Spring AI can map a compatible server's `reasoning_content` response field into `AssistantMessage` metadata under `reasoningContent`, but the field is entirely server-dependent.
  source: https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: compatibility
- claim: Spring AI warns that native structured-output support varies by model/provider and specifically documents reasoning/thinking model instability for some Ollama models; it requires testing the exact model version.
  source: https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: compatibility

## Leads

- Inspect Spring AI 2.0.0 source or versioned documentation for the exact OpenAI request serialization and whether `reasoning_effort` is passed unchanged.
- Compare native `response_format` with provider-specific reasoning fields at the local server wire level.

## Looked for

- Spring AI native structured-output behavior, OpenAI response format properties, extra-body handling, and reasoning-content mapping.
