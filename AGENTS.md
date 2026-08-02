# QuizForgeAI

Spring Boot and Kotlin application for generating and answering AI-powered quizzes.

## Technology

- Kotlin 2.3.10
- Spring Boot 4.0.3
- Gradle 9.5.1 via the checked-in Gradle wrapper
- Java 25
- PostgreSQL with Liquibase in the application environment
- H2 with Liquibase in tests
- Spring AI 2.0.0 using an OpenAI-compatible local endpoint
- Bruno collections in `bruno/quiz/`

## Repository Layout

```text
src/main/kotlin/ai/quiz/forge/
├── QuizForgeAiApplication.kt
├── config/                         # Spring and AI configuration
├── persistence/
│   ├── model/                      # JPA entities
│   └── repository/                 # Spring Data repositories
├── rest/
│   ├── QuizController.kt           # HTTP endpoints
│   └── model/                      # Request and response DTOs
├── service/
│   ├── QuizService.kt              # Quiz generation and answer workflows
│   ├── mapper/                     # Domain/entity/DTO mappers
│   └── model/                      # Domain and AI response models
└── shared/                         # Shared value types and enums
```

## Development

The application profile expects PostgreSQL and a local OpenAI-compatible model.
Start PostgreSQL with:

```powershell
docker compose up -d
```

The configured database is `quizForge` on `localhost:5432` with the credentials
defined in `docker-compose.yml`. The configured AI endpoint is
`http://localhost:1234`; update `src/main/resources/application.yaml` if it
differs in your environment.

Always use the Gradle wrapper rather than a system Gradle installation:

```powershell
.\gradlew.bat bootRun
.\gradlew.bat test
.\gradlew.bat clean bootJar
```

On Unix-like systems, use the equivalent `./gradlew` commands.

## Testing

`.\gradlew.bat test` is self-contained:

- Tests use an in-memory H2 database.
- Liquibase applies the normal changelog to the test database.
- AI calls are replaced with deterministic Mockito responses.
- Docker, PostgreSQL, and a running local LLM are not required.

The main Spring integration tests are `QuizForgeAiApplicationTests` and
`QuizServiceIT`. Mapper tests are kept under `src/test/kotlin`.

## Configuration and Database

- Application configuration: `src/main/resources/application.yaml`
- Test configuration: `src/main/resources/application-test.yaml`
- Liquibase changelog: `src/main/resources/db/changelog/`
- Build and dependency versions: `build.gradle.kts`

Add database changes through Liquibase rather than relying on Hibernate schema
generation. Keep production and test schema changes compatible.

## Conventions

- Keep REST DTOs in `rest.model`, domain models in `service.model`, and persistence
  models in `persistence.model`.
- Keep entity/DTO mappers in `service.mapper`.
- Prefer constructor injection and Kotlin null-safety.
- Preserve the existing `-Xjsr305=strict` compiler setting.
- Use `@ActiveProfiles("test")` for Spring tests that need the test database.
- Mock external AI calls in tests; do not make tests depend on network services.
- Make focused changes and add or update tests when behavior changes.
