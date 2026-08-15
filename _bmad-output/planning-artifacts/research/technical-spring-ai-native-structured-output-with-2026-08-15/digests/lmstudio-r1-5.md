# Digest: LM Studio Chat Completions and Responses APIs

## Findings

- claim: LM Studio's Chat Completions endpoint is `POST /v1/chat/completions`, uses port 1234 in examples, and accepts standard inference fields such as model, messages, temperature, max_tokens, and stream.
  source: https://lmstudio.ai/docs/developer/openai-compat/chat-completions
  publisher: LM Studio
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: compatibility
- claim: LM Studio's Responses endpoint explicitly supports streaming, reasoning, prior response state, and optional remote MCP tools.
  source: https://lmstudio.ai/docs/developer/openai-compat/responses
  publisher: LM Studio
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: compatibility
- claim: LM Studio's published Responses examples enable reasoning with a top-level `reasoning: { effort: "low" }` object.
  source: https://lmstudio.ai/docs/developer/openai-compat/responses
  publisher: LM Studio
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: integration
- claim: LM Studio's Chat Completions documentation points to standard OpenAI payload parameters but does not document a reasoning field in its supported-parameter list; its separate structured-output page documents `response_format` JSON schema for Chat Completions.
  source: https://lmstudio.ai/docs/developer/openai-compat/chat-completions; https://lmstudio.ai/docs/developer/openai-compat/structured-output
  publisher: LM Studio
  pub_date: not stated
  accessed: 2026-08-15
  confidence: medium
  class: source-gap

## Leads

- Fetch or test the Responses structured-output payload using `text.format` and confirm whether LM Studio supports reasoning plus schema on that endpoint.
- Identify whether the loaded Gemma model is a reasoning-capable model in LM Studio, independent of API parameter support.

## Looked for

- LM Studio endpoint semantics for reasoning and structured output, with emphasis on the difference between Chat Completions and Responses.
