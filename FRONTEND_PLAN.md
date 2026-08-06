# QuizForgeAI Frontend Plan

## Goal

Build a responsive frontend for creating AI-generated quizzes, answering one question at a time, receiving immediate feedback, and viewing a final result.

The current repository has no frontend code in `src/main/resources/static` or `src/main/resources/templates`. The frontend will run as a standalone Vite development app and proxy quiz requests to the Spring Boot backend.

## Confirmed Direction

- Vue 3 with Vite and TypeScript.
- A study-oriented interface with playful touches.
- Hints do not affect the score.
- The completion view includes a full review.
- Resume, authentication, and quiz history are out of scope for the first version.
- Chrome, Firefox, and mobile browsers are first-class targets.

## Current API Contract

| Operation | Endpoint | Request | Relevant response |
| --- | --- | --- | --- |
| Create quiz | `POST /quiz` | `{ topic, numberOfQuestions, difficulty }` | `id`, `questionCount`, `currentQuestionIndex`, `currentQuestion` |
| Load quiz | `GET /quiz/{quizId}` | None | The current unanswered question and progress |
| Answer question | `PATCH /quiz/{quizId}/question/current` | `{ selectedOption }` | `correctOption`, `explanation` |

Supported request values:

- `numberOfQuestions`: `FIVE`, `TEN`, `FIFTEEN`
- `difficulty`: `BEGINNER`, `ADVANCED`, `EXPERT`
- `selectedOption`: `OPTION_A`, `OPTION_B`, `OPTION_C`, `OPTION_D`

## Proposed User Flow

### 1. Quiz setup

- Topic text field with required validation.
- Difficulty selector with Beginner, Advanced, and Expert choices.
- Question-count selector with 5, 10, and 15 choices.
- Create button with disabled, loading, and error states.

### 2. Generation state

- Keep the setup visible while the synchronous quiz-generation request is running.
- Show a clear progress or loading state without claiming granular AI progress that the API does not provide.
- Allow retry after a failure and prevent duplicate submissions.

### 3. Question view

- Show quiz progress, question number, question text, and four answer choices.
- Let the user reveal the hint before submitting.
- Require an answer selection before submission.
- Lock the answer controls while the request is in flight.

### 4. Answer feedback

- Show whether the selected option was correct.
- Show the explanation returned by the API.
- Track the session score in the client from each answer response.
- Provide a clear next-question action.

### 5. Completion

- Show the final score and completion state.
- Offer a new quiz action.
- Show a detailed review from the question and answer data kept in the active client session.

### 6. Recovery

- Do not support refresh recovery, authentication, or quiz history in version one.
- Handle not-found, already-answered, completed, network, and server errors with an actionable message.

## Recommended Technical Shape

Unless the answers below change the direction, use:

- Vue 3, Vite, and TypeScript in a new `frontend/` directory.
- A small API client with typed request and response models.
- Composition API state for the active quiz flow; a server-state library can be added only if history or multiple concurrent views require it.
- A Vite development proxy from `localhost:5173` to the Spring Boot API at `localhost:8080`; production deployment is out of scope.
- Focused component tests for setup, question, feedback, and completion states, plus one browser-level happy-path test.

Suggested frontend boundaries:

```text
frontend/
├── src/
│   ├── api/            # Typed HTTP client and API error mapping
│   ├── components/     # Reusable controls and quiz presentation
│   ├── features/quiz/  # Setup, play, feedback, and completion flow
│   ├── composables/    # Active quiz state and workflow actions
│   ├── types/          # API and client-only models
│   └── App.vue
└── ...
```

## Backend Contract Notes

The frontend will use the existing backend without changing it:

1. `QuizDto.currentQuestionIndex` is documented as zero-based, but the current mapper returns `currentIndex + 1`. The frontend will treat the received value as one-based for display.
2. The completed quiz cannot currently be mapped by `GET /quiz/{quizId}` because the mapper requires an unanswered question. The frontend will not fetch the quiz after the final answer; it will use its in-memory answer history for the completion review.
3. The answer response contains `correctOption` and `explanation`, but not explicit progress or a correctness flag. The frontend will derive correctness by comparing `selectedOption` with `correctOption`.
4. No backend endpoints, persistence changes, CORS configuration, authentication, resume support, or history features are required for version one.

## Delivery Milestones

1. Confirm product, visual, hosting, and backend-contract decisions.
2. Scaffold the frontend and establish API configuration, typed models, and error handling.
3. Implement quiz setup and the generation state.
4. Implement the question, hint, answer submission, and feedback loop.
5. Implement completion, score presentation, and the chosen recovery behavior.
6. Add responsive polish, accessibility checks, automated tests, and production build verification.

## Acceptance Criteria

- A user can create a quiz using every supported difficulty and question-count value.
- A user can answer each question exactly once and see feedback before moving on.
- The interface clearly communicates loading, validation, network, and API error states.
- Progress and score remain correct across the entire active session.
- The final state does not make a request that the current completed-quiz API cannot satisfy.
- Keyboard navigation, visible focus, readable contrast, and mobile layouts work for the core flow.
- The frontend can be configured for both local development and the chosen production API origin.

## Confirmed Interaction Decisions

- Keep the backend unchanged and use the existing three endpoints.
- After answer submission, show whether the answer was correct, the correct option, and the explanation before allowing the user to continue with a **Next question** action.
- Use the Vite proxy for local development; no production deployment is planned.
- Keep quiz generation synchronous and show a loading state while `POST /quiz` is running. Do not implement percentage progress, polling, or server-sent events.