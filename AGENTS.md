<!-- bmad:context -->
<!-- Verified 2026-08-15 against d55feb1. Managed by bmad-project-context; edits inside this block are replaced on refresh. Keep anything you want preserved outside the markers. -->

## QuizForgeAI

QuizForgeAI is a full-stack quiz application with a Spring Boot/Kotlin backend and a standalone Vue/Vite/TypeScript frontend. The backend generates and answers quizzes through Spring AI, persists application state with PostgreSQL and Liquibase, and uses H2 plus mocked AI responses in tests. Frontend work lives in `frontend/`; cross-scope API decisions live in `FRONTEND_PLAN.md`, and BMAD planning artifacts live in `_bmad-output/` when present.

## Where things are

- Backend entry point and HTTP surface: `src/main/kotlin/ai/quiz/forge/QuizForgeAiApplication.kt` and `src/main/kotlin/ai/quiz/forge/rest/QuizController.kt`
- AI integration and model configuration: `src/main/kotlin/ai/quiz/forge/config/ChatClientConfig.kt` and `src/main/resources/application.yaml`; consult the [Spring AI OpenAI chat reference](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html) for AI integration changes
- Database schema and environment-specific settings: `src/main/resources/db/changelog/`, `src/main/resources/application.yaml`, and `src/main/resources/application-test.yaml`
- Frontend context: `frontend/AGENTS.md`; shared API decisions: `FRONTEND_PLAN.md`

## Running and verifying

- Use the checked-in Gradle wrapper for backend tasks: `.\gradlew.bat` on Windows or `./gradlew` on Unix-like systems.
- Backend tests are self-contained: `.\gradlew.bat test` uses H2, Liquibase, and mocked AI responses, so Docker, PostgreSQL, and a local model are not required.
- Running the application requires PostgreSQL and an OpenAI-compatible model endpoint configured in `src/main/resources/application.yaml`; the default local database is provided by `docker compose.yml` and the default model endpoint is `http://localhost:1234`.

## Conventions that differ from defaults

- Add schema changes through Liquibase changesets; Hibernate schema generation is disabled in the application and test profiles.
- Keep application and test schema changes compatible.
<!-- /bmad:context -->
