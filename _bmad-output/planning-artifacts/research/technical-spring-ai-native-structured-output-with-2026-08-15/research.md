---
title: 'technical research: Spring AI native structured output with reasoning on OpenAI-compatible local endpoints'
type: 'technical'
topic: 'Spring AI native structured output with reasoning on OpenAI-compatible local endpoints'
decision: 'Determine how to preserve model reasoning while using native structured output in QuizForgeAI'
source: 'BMad Deep Recon'
status: complete
preset: 'standard'
validation: 'normal'
created: '2026-08-15'
updated: '2026-08-15'
claims_verified: 4
claims_unverified: 0
---

## Executive summary

Use Spring AI 2.0.0's typed `spring.ai.openai.chat.reasoning-effort: low` property and remove the `extra-body.reasoning_effort: none` workaround. Spring AI 2.0.0 supports native structured output and reasoning in a single Chat Completions request. Reasoning need not be disabled: the configured local `google/gemma-4-12b` endpoint returned valid schema-conforming JSON plus a `reasoning_content` field with `reasoning_effort=low` [1][2][8].

The failure is a token-budget symptom, not an inherent feature conflict. With an artificially small 256-token budget, reasoning consumed output capacity before the final JSON; with the server default or a 1024-token budget, the same schema-plus-reasoning request completed successfully [8]. The main caveat is model/server-specific support and the need to leave enough completion budget for both reasoning and the final object.

The earliest claim re-check date is 2026-09-01.

**This research addresses:** How to preserve model reasoning while using native structured output in QuizForgeAI.

## Recommendations

1. Replace `spring.ai.openai.chat.extra-body.reasoning_effort: none` with the typed property below and keep `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT` unchanged. Confidence: high, based on the exact Spring AI 2.0.0 auto-configuration and a successful local request [2][3][8].

```yaml
spring:
  ai:
    openai:
      chat:
        reasoning-effort: low
```

2. Do not add a token limit until the real quiz prompts demonstrate truncation. If they do, start with `spring.ai.openai.chat.max-completion-tokens: 1024` or higher and inspect `finish_reason`, usage, and response truncation before increasing reasoning effort. Confidence: medium, because 1024 worked for the controlled probe but quiz-generation prompts are larger [2][8].

3. Add one provider-backed smoke test or diagnostic probe that asserts valid `NewQuestion`/`Answer` JSON and records whether reasoning metadata is present. Keep the existing mocked tests for deterministic application behavior. Confidence: high, because the current unit/integration tests do not exercise the live local model [8].

### Why this works

- The framework, wire format, and tested local runtime all support reasoning plus native schema output; the workaround was masking completion-budget exhaustion rather than compensating for a Spring AI limitation [2][6][8].
- Chat Completions is sufficient for this LM Studio-style local endpoint. OpenAI recommends Responses for hosted reasoning models, while the local probe independently demonstrates that this server accepts both fields on Chat Completions [4][7][8].

### Conditions to monitor

- The endpoint behavior matches LM Studio's documented default port and API shape, but the exact LM Studio version and model runtime configuration were not retrieved. Keep the model card and server release in the next verification pass if behavior changes.
- Reasoning effort values are model/server dependent. `low` was verified locally; higher values should be evaluated against latency, token usage, and JSON completion before adoption.

## Integration and interoperability

Spring AI 2.0.0 supports both native structured output and reasoning together. `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT` marks the request for provider-native schema handling, while `OpenAiChatOptions` separately carries `reasoningEffort`; `OpenAiChatModel.createRequest()` writes both `response_format` and the reasoning-effort field into the same Chat Completions request [1][2]. The typed Spring Boot property is `spring.ai.openai.chat.reasoning-effort`, not `spring.ai.openai.chat.extra-body.reasoning_effort` [3].

The provider must still implement both features for the selected model. OpenAI documents reasoning and JSON Schema structured outputs as compatible capabilities primarily through the Responses API, and recommends Responses for hosted reasoning models; that documentation does not guarantee that every Chat Completions provider supports the combination [4][5]. LM Studio documents JSON Schema support on `/v1/chat/completions` and reasoning support on its Responses endpoint [6][7]. The local endpoint accepted the Chat Completions request with the schema payload for the configured `google/gemma-4-12b` model [8].

## Implementation reality

The local runtime probe is decisive for this setup. With `google/gemma-4-12b`, a JSON Schema `response_format` and `reasoning_effort=low` returned HTTP 200, `finish=stop`, valid JSON matching the schema, and a `reasoning_content` response field. The same request worked with no explicit token limit and with `max_tokens=1024` [8].

The observed failure was caused by an artificially small `max_tokens=256` budget: reasoning consumed the output budget, so the final JSON was incomplete or absent, even though the response still contained `reasoning_content` [8]. `reasoning_effort=none` suppresses reasoning, masking completion-budget exhaustion; it is a workaround, not a native structured-output requirement.

Spring AI maps compatible server reasoning output into response metadata as `reasoningContent`; treat it as diagnostic metadata rather than part of the quiz DTO. The final structured entity remains the normal `entity(NewQuestion::class.java)` or `entity(Answer::class.java)` result [2].

## Source appendix

| Ref | Claim or finding | Publisher | Pub date | Accessed | Confidence |
| --- | --- | --- | --- | --- | --- |
| [1] | Native structured output is a provider-native schema constraint. | [Spring AI](https://docs.spring.io/spring-ai/reference/api/chatclient.html) | not stated | 2026-08-15 | high |
| [2] | Spring AI 2.0.0 emits response format and reasoning effort in one Chat Completions request. | [Spring AI](https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatModel.java) | not stated | 2026-08-15 | high |
| [3] | `spring.ai.openai.chat.reasoning-effort` is the typed v2.0.0 property. | [Spring AI](https://raw.githubusercontent.com/spring-projects/spring-ai/v2.0.0/auto-configurations/models/spring-ai-autoconfigure-model-openai/src/main/java/org/springframework/ai/model/openai/autoconfigure/OpenAiChatProperties.java) | not stated | 2026-08-15 | high |
| [4] | OpenAI documents reasoning effort, recommends Responses for reasoning models, and does not establish this Chat Completions combination for every provider. | [OpenAI](https://developers.openai.com/api/docs/guides/reasoning) | not stated | 2026-08-15 | medium |
| [5] | OpenAI Structured Outputs supports schema-shaped reasoning examples and strict schema constraints. | [OpenAI](https://developers.openai.com/api/docs/guides/structured-outputs) | not stated | 2026-08-15 | high |
| [6] | LM Studio supports JSON Schema on Chat Completions. | [LM Studio](https://lmstudio.ai/docs/developer/openai-compat/structured-output) | not stated | 2026-08-15 | high |
| [7] | LM Studio documents reasoning on its Responses endpoint. | [LM Studio](https://lmstudio.ai/docs/developer/openai-compat/responses) | not stated | 2026-08-15 | high |
| [8] | The configured local model completed schema-plus-reasoning probes; a 256-token budget truncated final JSON. | [Local runtime](http://localhost:1234/v1/models) | 2026-08-15 | 2026-08-15 | high |

## Staleness map

Computed on 2026-08-15 from the memlog: 4 verified claims and 0 unverified claims. No claim is currently stale.

| Claim class | Re-check date | Current status |
| --- | --- | --- |
| version-compatibility, configuration, compatibility | 2026-09-01 | current |
| runtime | 2026-11-01 | current |

Earliest re-check date: 2026-09-01.
