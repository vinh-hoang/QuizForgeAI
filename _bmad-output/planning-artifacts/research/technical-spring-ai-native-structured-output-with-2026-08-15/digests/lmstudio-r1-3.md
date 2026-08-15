# Digest: LM Studio OpenAI-compatible structured output

## Findings

- claim: LM Studio's OpenAI-compatible server uses port 1234 by default and exposes `/v1/chat/completions`, matching the endpoint shape in this project.
  source: https://lmstudio.ai/docs/developer/openai-compat
  publisher: LM Studio
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: compatibility
- claim: LM Studio supports JSON-schema structured output on `/v1/chat/completions` through the OpenAI-style `response_format` field, with the schema nested under `json_schema`.
  source: https://lmstudio.ai/docs/developer/openai-compat/structured-output
  publisher: LM Studio
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: integration
- claim: LM Studio states that all parameters recognized by its chat-completions endpoint are honored, but not all models support structured output; it specifically warns that models below 7B parameters may be incapable and recommends checking the model card.
  source: https://lmstudio.ai/docs/developer/openai-compat/structured-output
  publisher: LM Studio
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: compatibility
- claim: The attempted LM Studio reasoning documentation URL returned 404 in this round, so the exact reasoning parameter name and structured-output interaction remain unresolved from LM Studio's official docs.
  source: https://lmstudio.ai/docs/developer/openai-compat/reasoning
  publisher: LM Studio
  pub_date: not stated
  accessed: 2026-08-15
  confidence: high
  class: source-gap

## Leads

- Inspect LM Studio's current chat-completions and model-specific documentation for reasoning/thinking output fields and parameters.
- Verify whether `google/gemma-4-12b` is a model identifier exposed by LM Studio and whether its loaded runtime supports grammar-constrained JSON plus reasoning.

## Looked for

- LM Studio endpoint defaults, response-format JSON schema support, model capability caveats, and reasoning documentation.
