# Digest: Spring AI OpenAI auto-configuration property

## Findings

- claim: Spring AI's OpenAI chat auto-configuration uses the prefix `spring.ai.openai.chat`.
  source: https://github.com/spring-projects/spring-ai/blob/main/auto-configurations/models/spring-ai-autoconfigure-model-openai/src/main/java/org/springframework/ai/model/openai/autoconfigure/OpenAiChatProperties.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: configuration
- claim: Spring AI's configuration metadata marks `spring.ai.openai.chat.reasoning-effort` as the replacement for the deprecated reasoning-effort property accessor.
  source: https://github.com/spring-projects/spring-ai/blob/main/auto-configurations/models/spring-ai-autoconfigure-model-openai/src/main/java/org/springframework/ai/model/openai/autoconfigure/OpenAiChatProperties.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: configuration
- claim: The typed property should be preferred over placing `reasoning_effort` inside `spring.ai.openai.chat.extra-body` when the target server accepts the standard OpenAI Chat Completions field.
  source: https://github.com/spring-projects/spring-ai/blob/main/auto-configurations/models/spring-ai-autoconfigure-model-openai/src/main/java/org/springframework/ai/model/openai/autoconfigure/OpenAiChatProperties.java; https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatModel.java
  publisher: Spring AI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: medium
  class: recommendation

## Leads

- Fetch the exact v2.0.0 auto-configuration source under the `auto-configurations` module path.
- Compare Spring Boot binding behavior with the current application YAML before editing it.

## Looked for

- The exact property name and binding path for a typed reasoning-effort configuration.
