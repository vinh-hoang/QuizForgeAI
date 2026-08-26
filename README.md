# QuizForgeAI

QuizForgeAI is a Spring Boot/Kotlin backend with a Vue/Vite frontend for creating and answering quizzes.

## Start the backend on Windows

Prerequisites:

- Podman with a running Podman machine
- A Compose provider available to `podman compose` (verify with
  `podman compose version`)
- An OpenAI-compatible local model endpoint at `http://localhost:1234`
- Podman machine server 6.1 or newer when using the WSL provider

Use `podman compose` (with a space) for this project. It is Podman's
provider-aware Compose entry point; `podman-compose` (with a hyphen) is a
separate Python tool and is not required.

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
`podman compose up -d`.

## Podman and WSL distributions

Podman Desktop uses its own WSL distribution, `podman-machine-default`, to run
containers. It is separate from an Ubuntu distribution used as your normal
Linux shell. Setting Ubuntu as the default WSL distribution changes what plain
`wsl` opens, but it does not make Podman run inside Ubuntu. Do not unregister
`podman-machine-default` while using the Windows Podman client.

Use the exact distribution and machine names shown by these commands if yours
are different:

```powershell
wsl --list --verbose
podman machine list
```

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

If the machine has user-mode networking enabled, disable it for this
configuration. After changing the networking mode, shut down WSL once so it
regenerates its networking and DNS state. This stops every running WSL
distribution, including Ubuntu:

```powershell
podman machine stop
podman machine set --user-mode-networking=false podman-machine-default
wsl --shutdown
podman machine start
```

You can make Ubuntu the default WSL distribution independently:

```powershell
wsl --set-default Ubuntu
```

Replace `Ubuntu` with the exact distribution name from `wsl --list --verbose`
when it is versioned, such as `Ubuntu-24.04`.

From the repository root, open PowerShell and run:

```powershell
podman machine start
podman compose up -d
.\gradlew.bat bootRun
```

Verify that the database is reachable from Windows before starting the backend:

```powershell
for ($attempt = 1; $attempt -le 30; $attempt++) {
    podman exec quiz_forge_db pg_isready -U admin -d quizForge *> $null
    if ($LASTEXITCODE -eq 0) { break }
    if ($attempt -eq 30) { throw "PostgreSQL did not become ready." }
    Start-Sleep -Seconds 1
}
Test-NetConnection 127.0.0.1 -Port 5432
```

`Test-NetConnection` should report `TcpTestSucceeded : True`. If it reports
`False`, confirm that the Podman machine is running and that the forwarding
configuration file exists at the path above.

The backend starts at `http://localhost:8080`.

## Stop the backend

Press `Ctrl+C` in the backend terminal, then stop the database container when finished:

```powershell
podman compose down
```

## Run the frontend

See [frontend/README.md](frontend/README.md) for frontend setup and development commands.
