# QuizForgeAI

Spring Boot + Kotlin project for AI-powered quiz generation.

## Tech Stack

- **Language**: Kotlin 2.3.10
- **Framework**: Spring Boot 4.0.3
- **Build**: Gradle 9.5.1 (wrapper: `.\gradlew`)
- **Runtime**: Java 25
- **Database**: PostgreSQL (production), H2 (tests)
- **Migrations**: Liquibase
- **AI**: Spring AI 2.0.0-M2 (OpenAI, configured for local LLM at `localhost:1234`)
- **API Testing**: Bruno (`bruno/quiz/`)

## Project Structure

```
src/main/kotlin/ai/quiz/forge/
├── QuizForgeAiApplication.kt    # Application entry point
├── persistence/
│   ├── model/                    # JPA entities (QuizEntity, QuestionEntity)
│   └── repository/               # Spring Data JPA repositories
├── rest/
│   ├── QuizController.kt         # REST endpoints
│   └── model/                    # DTOs (QuizDto, AnswerDto, CreateQuiz)
├── service/
│   ├── QuizService.kt            # Business logic
│   ├── mapper/                   # Entity/DTO mappers
│   └── model/
│       ├── Quiz.kt, Question.kt  # Domain models
│       └── ai/generated/         # AI-generated DTOs (NewQuiz, NewQuestion, Answer)
└── shared/
    └── Option.kt                 # Shared types
```

## Quick Start

1. **Start PostgreSQL** via Docker Compose:
   ```
   docker-compose up -d
   ```

2. **Run the app**:
   ```
   .\gradlew bootRun
   ```

3. **Run tests**:
   ```
   .\gradlew test
   ```

4. **Clean build**:
   ```
   .\gradlew clean bootJar
   ```

## Key Config

- **App config**: `src/main/resources/application.yaml`
- **Test config**: `src/main/resources/application-test.yaml`
- **DB migrations**: `src/main/resources/db/changelog/`
- **Liquibase**: enabled by default (`spring.liquibase.enabled=true`)

## Dependencies

Spring AI dependencies are managed via BOM in `build.gradle.kts` (`springAiVersion = "2.0.0-M2"`):

- `spring-ai-advisors-vector-store` — vector store retrieval augmentation
- `spring-ai-starter-model-openai` — OpenAI chat model adapter
- `spring-ai-starter-vector-store-pgvector` — PostgreSQL vector store

## Conventions

- Use `.\gradlew` (not `gradle`) — the system `gradle` command is not on PATH.
- Entity/DTO mappers live in `service.mapper` package.
- Testcontainers PostgreSQL is used for integration tests (`QuizServiceIT`).
- H2 is the default test database (via `spring-boot-starter-data-jpa` auto-config).
- Kotlin compiler flags: `-Xjsr305=strict` for null-safety.
