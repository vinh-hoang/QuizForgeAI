# QuizForgeAI

QuizForgeAI is a Spring Boot/Kotlin backend with a Vue/Vite frontend for creating and answering quizzes.

## Start the backend on Windows

Prerequisites:

- Podman with a running Podman machine
- `podman-compose` available on `PATH`
- An OpenAI-compatible local model endpoint at `http://localhost:1234`
- Podman machine server 6.1 or newer when using the WSL provider

### Upgrade the Podman machine server to 6.1

The Windows Podman CLI and the Podman machine server are upgraded
separately. To upgrade the server inside the WSL machine, run these commands
from PowerShell:

```powershell
podman machine start
podman machine ssh podman-machine-default 'sudo dnf upgrade -y podman'
podman machine stop
podman machine start
podman version
```

Confirm that the `Server` section reports version `6.1.x` or newer. The
machine restart stops running containers; start the database again with
`podman-compose up -d`.

For Windows WSL, configure the Podman machine to listen on published IPv4
ports so WSL can forward them to Windows `localhost`. Create
`%APPDATA%\containers\containers.conf.d\01-podman-wsl-port-forwarding.conf`
with:

```toml
[engine]
force_port_listen = true

[network]
default_host_ips = ["0.0.0.0"]
```

Restart the Podman machine after changing this configuration. From the
repository root, open PowerShell and run:

```powershell
podman machine start
podman-compose up -d
.\gradlew.bat bootRun
```

The backend starts at `http://localhost:8080`.

## Stop the backend

Press `Ctrl+C` in the backend terminal, then stop the database container when finished:

```powershell
podman-compose down
```

## Run the frontend

See [frontend/README.md](frontend/README.md) for frontend setup and development commands.
