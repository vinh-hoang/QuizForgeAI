<!-- bmad:context -->
<!-- Verified 2026-08-15 against d55feb1. Managed by bmad-project-context; edits inside this block are replaced on refresh. Keep anything you want preserved outside the markers. -->

## QuizForgeAI frontend

The frontend is a standalone Vue 3, Vite, and TypeScript client for the Spring Boot quiz API. It owns quiz setup, question, feedback, and in-memory review screens. API contract decisions shared with backend work are documented in `../FRONTEND_PLAN.md`.

## Where things are

- API client and typed contract: `src/api/quizApi.ts` and `src/types/quiz.ts`
- Active quiz state and transitions: `src/composables/useQuiz.ts`
- UI screens: `src/App.vue` and `src/components/`
- Local development proxy: `vite.config.ts`
- Before changing API or completion behavior, read `../FRONTEND_PLAN.md`; it records the current backend/frontend contract

## Running and verifying

- Start the Spring Boot backend before testing browser API flows; the Vite development server proxies `/quiz` requests to `http://localhost:8080`, so frontend CORS configuration is not expected.
<!-- /bmad:context -->
