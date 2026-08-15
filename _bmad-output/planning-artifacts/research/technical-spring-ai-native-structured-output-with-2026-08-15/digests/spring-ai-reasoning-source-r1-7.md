# Digest: Spring AI OpenAI reasoning option source

## Findings

- claim: The current Spring AI OpenAI adapter recognizes a `reasoningEffort` option and maps it to the OpenAI SDK's reasoning-effort request field.
  source: https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatModel.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: implementation
- claim: Spring AI contains OpenAI integration tests using reasoning effort values with reasoning models, demonstrating that reasoning configuration is intended to coexist with the OpenAI adapter.
  source: https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/OpenAiChatModelIT.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: medium
  class: implementation

## Leads

- Exact version check is required before recommending a typed `reasoningEffort` option for Spring AI 2.0.0.

## Looked for

- Whether reasoning is represented as a standard Spring AI OpenAI option or only as an extra-body workaround.
