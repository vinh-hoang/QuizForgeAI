# Digest: OpenAI reasoning and structured outputs

## Findings

- claim: OpenAI's current Structured Outputs guide supports JSON Schema in both the Responses API and Chat Completions API, with model support beginning at GPT-4o and later supported models.
  source: https://developers.openai.com/api/docs/guides/structured-outputs
  publisher: OpenAI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: compatibility
- claim: OpenAI's Structured Outputs guide includes a chain-of-thought-style example whose result is a structured object containing reasoning steps and a final answer.
  source: https://developers.openai.com/api/docs/guides/structured-outputs
  publisher: OpenAI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: integration
- claim: OpenAI's reasoning guide exposes reasoning effort as a first-class parameter and recommends the Responses API for reasoning models, while noting Chat Completions remains supported.
  source: https://developers.openai.com/api/docs/guides/reasoning
  publisher: OpenAI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: compatibility
- claim: OpenAI's reasoning guide states that `none` means no reasoning, while `low`, `medium`, and higher values allocate increasing reasoning effort; supported values are model-dependent.
  source: https://developers.openai.com/api/docs/guides/reasoning
  publisher: OpenAI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: semantics
- claim: OpenAI's structured-output examples require strict JSON Schema constraints such as an object root, required fields, and `additionalProperties: false`; schema failures and incomplete responses still require handling.
  source: https://developers.openai.com/api/docs/guides/structured-outputs
  publisher: OpenAI
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: implementation

## Leads

- Determine whether LM Studio supports the Responses API `text.format` schema field, not only Chat Completions `response_format`.
- Determine whether Spring AI 2.0.0 exposes the Responses API or only maps Chat Completions options.

## Looked for

- Official compatibility between reasoning and structured outputs, the meaning of `reasoning.effort`, and schema restrictions.
