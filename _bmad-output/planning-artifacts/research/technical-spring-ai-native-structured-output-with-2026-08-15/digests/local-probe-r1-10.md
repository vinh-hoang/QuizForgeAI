# Digest: Local reasoning plus native schema probes

## Findings

- claim: A direct Chat Completions request to `http://localhost:1234/v1/chat/completions` with `google/gemma-4-12b`, `response_format.type=json_schema`, and `reasoning_effort=low` returned HTTP 200, `finish=stop`, valid schema-shaped JSON content, and a `reasoning_content` response field when no explicit token limit was supplied.
  source: local runtime check: POST http://localhost:1234/v1/chat/completions on 2026-08-15
  publisher: local inference server
  pub_date: 2026-08-15
  accessed: 2026-08-15
  confidence: high
  class: runtime
- claim: The same request with `max_tokens=1024` also returned valid JSON and a `reasoning_content` field.
  source: local runtime check: POST http://localhost:1234/v1/chat/completions on 2026-08-15
  publisher: local inference server
  pub_date: 2026-08-15
  accessed: 2026-08-15
  confidence: high
  class: runtime
- claim: With an artificially small `max_tokens=256` budget, `reasoning_effort=low` returned truncated JSON while the omitted-effort variant returned no final content; both still exposed `reasoning_content`.
  source: local runtime check: POST http://localhost:1234/v1/chat/completions on 2026-08-15
  publisher: local inference server
  pub_date: 2026-08-15
  accessed: 2026-08-15
  confidence: high
  class: runtime
- claim: The local runtime can therefore perform reasoning and native JSON-schema output together; the observed failure mode is output-budget exhaustion, not an inherent incompatibility in the tested model/server combination.
  source: local runtime checks: POST http://localhost:1234/v1/chat/completions on 2026-08-15
  publisher: local inference server
  pub_date: 2026-08-15
  accessed: 2026-08-15
  confidence: high
  class: decision

## Leads

- Replace the extra-body workaround with Spring AI's typed `spring.ai.openai.chat.reasoning-effort` property if the 2.0.0 auto-configuration binds it.
- Add an explicit completion-token budget only if real quiz prompts still truncate after reasoning is enabled.

## Looked for

- Direct compatibility and failure behavior for schema-constrained output with reasoning effort values and token budgets.
