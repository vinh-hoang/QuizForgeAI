---
title: Harden frontend review findings
type: bugfix
created: 2026-09-06
status: done
baseline_commit: 3f48cf4e4de5e55194a0ad24e45ab3e35b37945c
review_loop_iteration: 0
context:
  - 'FRONTEND_PLAN.md'
  - 'frontend/AGENTS.md'
  - '_bmad-output/implementation-artifacts/deferred-work.md'
---

# Intent

Make the Vue frontend resilient, accessible, deployable, and testable while preserving the existing quiz flow and visual language. This implements the accepted review scope: runtime/API hardening, accessibility and responsive behavior, Markdown edge-case handling, and automated frontend verification.

# Scope

In scope are quiz request lifecycle correctness, typed API boundaries, timeout/cancellation/retry behavior, production API configuration, keyboard/focus behavior, contrast and reduced-motion support, narrow-screen text wrapping, Markdown parsing safeguards, and a Vitest-based test foundation. Do not change backend endpoints or quiz semantics.

# Inputs and outputs

| Input | Output |
| --- | --- |
| Quiz setup, answer, next-question actions | At most one active operation per action; stale responses cannot mutate the current session |
| HTTP 2xx/4xx/network/timeout responses | Validated DTOs or actionable typed errors, including status for conflict recovery |
| `VITE_API_BASE_URL` (optional) | Relative development requests by default; configurable API origin for deployed frontend |
| Markdown text from quiz content | Safe rendered text/KaTeX with literal currency, operators, and malformed markers preserved |
| Keyboard, reduced-motion, narrow viewport | Visible focus, announced screen transitions, usable wrapping, and motion reduction |

# Code map

| Area | Files | Required change |
| --- | --- | --- |
| Request boundary | `frontend/src/api/quizApi.ts`, `frontend/src/types/quiz.ts` | Add timeout/abort support, `ApiError`, runtime DTO validation, no-store quiz reads, configurable base URL, and an explicit current-index contract. |
| Quiz state | `frontend/src/composables/useQuiz.ts` | Add generation tokens/controllers, in-flight guards, stale-result suppression, and conflict-aware error recovery. |
| UI behavior | `frontend/src/App.vue`, `frontend/src/components/{QuizSetup,LoadingState,QuizStage,QuizReview}.vue` | Add focusable screen headings, transition focus, hint ARIA state, and stable accessible control labeling. |
| Styles/content | `frontend/src/style.css`, `frontend/src/components/MarkdownText.vue`, `frontend/index.html` | Fix contrast, focus visibility, reduced motion, text overflow, strict Markdown delimiters, and document title. |
| Delivery/tests | `frontend/vite.config.ts`, `frontend/package.json`, `frontend/.env.example`, `frontend/tests/**` | Enable LAN preview configuration, document API configuration, add Vitest/jsdom/Vue Test Utils, and cover API, composable, Markdown, and accessibility regressions. |

# Tasks and acceptance criteria

- [x] Harden `quizApi` and `useQuiz`. Every request has a bounded timeout and can be aborted by reset/new quiz; stale success, error, and `finally` paths are ignored. Duplicate answer/next actions are prevented. HTTP status is preserved, 409 is actionable, malformed success payloads fail safely, and quiz GETs bypass stale browser caches.
- [x] Make deployment configuration explicit. Use `VITE_API_BASE_URL` with the existing relative-path default, keep the development proxy, expose the intended Vite host for device testing, and document the variable.
- [x] Improve UI accessibility and resilience. Screen changes move focus to the new heading; hint disclosure exposes `aria-expanded`/`aria-controls`; radio cards show keyboard focus; text wraps on narrow screens; low-contrast text is corrected; reduced-motion users do not receive decorative animation or smooth scrolling.
- [x] Make Markdown conservative. Valid inline/block math continues to render through KaTeX, while currency, operators, whitespace-separated markers, and malformed delimiters remain literal and do not create unsafe HTML.
- [x] Add regression tests for request validation/status/timeout, all quiz state transitions including reset races and duplicate actions, Markdown examples, and key ARIA/focus behavior. `npm run test` and `npm run build` must pass from `frontend/`.

# Design notes and invariants

- Treat the composable's session generation as the authority. Starting or resetting a quiz invalidates every older operation, aborts its controller, and prevents its `catch`/`finally` handlers from changing current state.
- Keep the UI-facing question index explicitly one-based because the backend mapper currently returns `currentIndex + 1`; document and test this boundary so a future DTO change cannot silently shift completion or progress behavior.
- Use a small typed `ApiError` plus manual shape guards at the HTTP boundary. Do not trust a successful status alone, and do not add a schema library for this narrow contract.
- Compose caller cancellation with the request timeout. An intentional abort is silent for stale work, while a real timeout/network failure remains visible and actionable.
- Preserve existing KaTeX rendering and `v-html` sanitization settings. Markdown recognition must be narrower, not more permissive; text that is not unambiguously a supported construct stays escaped text.
- Prefer semantic native controls and focus management over custom keyboard event handling. The new test setup should remain unit/component-level and deterministic; browser services and the backend stay outside the test boundary.

# Regression matrix

| Area | Required cases |
| --- | --- |
| API | Valid quiz/answer DTOs; null, missing, wrong-type, and out-of-range fields; non-2xx status preservation; timeout/abort; no-store GET; configured base URL. |
| Composable | Create/answer/next success; network and 409 errors; reset during each request; a late old response after a new quiz; repeated answer and next calls. |
| Markdown | Inline and display math; escaped delimiters; `$5` and operator-like asterisks; whitespace/malformed markers; long literal text; safe HTML output. |
| UI | Heading receives focus after each phase; hint attributes track state; radio-card focus is visible; reduced-motion CSS is present; build remains type-safe. |

# Non-goals

Do not redesign the quiz screens, introduce authentication, alter backend persistence or endpoint shapes, add a production reverse proxy, or make the frontend responsible for recovering an unknown server-side answer beyond presenting the preserved conflict status and a clear recovery action.

# Verification

Run `npm install`, `npm run test`, and `npm run build` in `frontend/`. Tests must use mocked `fetch` and deterministic timers; no backend, database, model, or network service may be required. Manually verify keyboard-only setup/answer/review navigation, a reduced-motion preference, a narrow viewport, and a deployed-origin API URL.

# Frozen after approval

After approval, preserve the intent, scope, I/O contract, code map, acceptance criteria, and verification commands unless a later course-correction explicitly changes this spec. Implementation may choose equivalent internal APIs while retaining the stated behavior.

## Suggested Review Order

**Request boundary and session safety**

- Start with typed errors, response guards, timeout normalization, and path safety.
  [`quizApi.ts:13`](../../frontend/src/api/quizApi.ts#L13)

- Trace cancellation and stale-response suppression through every quiz transition.
  [`useQuiz.ts:41`](../../frontend/src/composables/useQuiz.ts#L41)

- Inspect progression validation before accepting the next server question.
  [`useQuiz.ts:202`](../../frontend/src/composables/useQuiz.ts#L202)

**Accessible interaction and content rendering**

- Follow focus restoration when a reused question screen advances.
  [`App.vue:57`](../../frontend/src/App.vue#L57)

- Review radio keyboard navigation, disclosure semantics, and conflict recovery.
  [`QuizStage.vue:56`](../../frontend/src/components/QuizStage.vue#L56)

- Check conservative math/emphasis parsing and literal-content safety.
  [`MarkdownText.vue:113`](../../frontend/src/components/MarkdownText.vue#L113)

- Verify visible focus, wrapping, contrast, and reduced-motion behavior.
  [`style.css:1101`](../../frontend/src/style.css#L1101)

**Verification and delivery**

- Start with lifecycle, duplicate-action, and error-recovery regression tests.
  [`useQuiz.spec.ts:1`](../../frontend/tests/composables/useQuiz.spec.ts#L1)

- Check API contract, timeout, URL, and malformed-payload coverage.
  [`quizApi.spec.ts:1`](../../frontend/tests/api/quizApi.spec.ts#L1)

- Review component accessibility and transition-focus assertions.
  [`accessibility.spec.ts:1`](../../frontend/tests/components/accessibility.spec.ts#L1)

- Confirm LAN development proxy and deployment configuration documentation.
  [`vite.config.ts:7`](../../frontend/vite.config.ts#L7)
