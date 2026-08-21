# QuizForgeAI

QuizForgeAI is a Spring Boot/Kotlin backend with a Vue/Vite frontend for creating and answering quizzes.

## Start the backend on Windows

Prerequisites:

- Podman with a running Podman machine
- `podman-compose` available on `PATH`
- An OpenAI-compatible local model endpoint at `http://localhost:1234`

From the repository root, open PowerShell and run:

```powershell
podman machine start
podman-compose up -d

$podmanHost = (wsl -d podman-machine-default ip -4 -o addr show scope global |
  Select-String -Pattern 'inet (\d{1,3}(\.\d{1,3}){3})/' |
  ForEach-Object { $_.Matches[0].Groups[1].Value } |
  Select-Object -First 1)

$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://${podmanHost}:5432/quizForge"
.\gradlew.bat bootRun
```

The backend starts at `http://localhost:8080`.

The Podman machine address is used for PostgreSQL because this Windows WSL setup does not forward the published database port to Windows `localhost`.

## Stop the backend

Press `Ctrl+C` in the backend terminal, then stop the database container when finished:

```powershell
podman-compose down
```

## Run the frontend

See [frontend/README.md](frontend/README.md) for frontend setup and development commands.
