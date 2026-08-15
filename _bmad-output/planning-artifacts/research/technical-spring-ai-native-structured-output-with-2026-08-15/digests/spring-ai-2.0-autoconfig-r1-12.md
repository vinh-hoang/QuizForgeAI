# Digest: Spring AI 2.0.0 typed reasoning property

## Findings

- claim: Spring AI 2.0.0 binds OpenAI chat properties under `spring.ai.openai.chat` and exposes a `reasoningEffort` property that is copied into `OpenAiChatOptions`.
  source: https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/auto-configurations/models/spring-ai-autoconfigure-model-openai/src/main/java/org/springframework/ai/model/openai/autoconfigure/OpenAiChatProperties.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: version-compatibility
- claim: Spring AI 2.0.0 explicitly identifies `spring.ai.openai.chat.reasoning-effort` as the replacement property for the deprecated nested options accessor.
  source: https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/auto-configurations/models/spring-ai-autoconfigure-model-openai/src/main/java/org/springframework/ai/model/openai/autoconfigure/OpenAiChatProperties.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: version-compatibility
- claim: Spring AI 2.0.0 auto-configuration passes `chatProperties.toOptions()` into `OpenAiChatModel`, so typed reasoning effort and native structured-output response format share one request-options object.
  source: https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/auto-configurations/models/spring-ai-autoconfigure-model-openai/src/main/java/org/springframework/ai/model/openai/autoconfigure/OpenAiChatAutoConfiguration.java; https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatModel.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: version-compatibility

## Leads

- Synthesize the minimal YAML change: remove `extra-body.reasoning_effort`, add `chat.reasoning-effort: low` (or another supported non-none value), and test with the actual quiz schema.
- Consider an explicit `chat.max-completion-tokens` only if production prompts still hit incomplete responses.

## Looked for

- Exact Spring AI 2.0.0 property binding and auto-configuration path.
