---
title: 'Verify and repair generated quiz questions'
type: 'feature'
created: '2026-09-28'
status: 'done'
route: 'oneshot'
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Generated question drafts can contain factual errors, ambiguity, or conflicting answer choices and hints. The current flow formats and persists each draft without a separate review step.

**Approach:** Add an LLM review-and-repair pass for each draft before the existing native `NewQuestion` structuring call. The review should check that the content is accurate, clear, and has exactly one defensible correct option, repairing the draft when needed while preserving the existing API and persisted fields.

</frozen-after-approval>

## Implementation Notes

- Added an LLM review and repair call after raw draft generation and before conversion to the existing native `NewQuestion` shape. `QuestionReviewResult` carries the verified draft and a validity signal; the review checks factual accuracy, clarity, exactly four distinct choices with one defensible correct option, and hint consistency, repairing or replacing a flawed item.
- Missing, blank, or invalid review output, plus incomplete or duplicate fields from final structuring, uses the existing five-attempt question retry path. The API, persisted question fields, and database schema remain unchanged.
- The review receives earlier questions so replacement drafts avoid duplicates. Prompt tag content is XML-escaped, and the structuring prompt directs the model to decode the entities and preserve the reviewed meaning.
- Updated the existing `QuizServiceIT` mock interaction counts and prompt order for the additional review call. No new test case was added.
- `compileKotlin` passed. Tests were not run.

## Review Triage Log

- Patch — the existing happy-path mock now returns its custom reviewed draft, and its assertions match the new review-to-structuring prompt sequence.
- Patch — the final structuring prompt treats the reviewed draft as authoritative, and the resulting `NewQuestion` is retried if required fields are blank or choices duplicate.
- Patch — the final structuring prompt explicitly decodes `&amp;`, `&lt;`, and `&gt;` so escaped content is stored with its original characters.
- Patch — the review receives previously accepted questions and is told not to duplicate them when replacing a flawed draft.
