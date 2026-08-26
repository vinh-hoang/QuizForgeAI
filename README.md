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
from PowerShell. The example uses the default machine name; replace it if
`podman machine list` shows a different name:

```powershell
podman machine start podman-machine-default
podman machine ssh podman-machine-default 'sudo dnf upgrade -y podman'
podman machine stop podman-machine-default
podman machine start podman-machine-default
podman version
```

Confirm that the `Server` section reports version `6.1.x` or newer. The
machine restart stops running containers; start the database again with
`podman compose up -d`.

## Podman WSL localhost setup

Podman Desktop uses its own WSL machine, `podman-machine-default`, to run
containers. Do not unregister this machine while using the Windows Podman
client. If your machine has another name, use the name shown by:

```powershell
podman machine list
```

Replace `podman-machine-default` with the actual machine name in the commands
below if it differs.

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

This bind setting is for WSL port forwarding. Keep this development database
on a trusted machine and use Windows Firewall rules if external access must be
restricted.

Create the configuration directory first if it does not exist:

```powershell
New-Item -ItemType Directory -Force "$env:APPDATA\containers\containers.conf.d" |
    Out-Null
```

Open the configuration file in an editor and save the TOML shown above:

```powershell
notepad "$env:APPDATA\containers\containers.conf.d\01-podman-wsl-port-forwarding.conf"
```

Run the following sequence after saving the file. It disables user-mode
networking if enabled, refreshes WSL networking and DNS, and reloads the
Podman configuration. This stops every running WSL distribution:

```powershell
podman machine stop podman-machine-default
podman machine set --user-mode-networking=false podman-machine-default
wsl --shutdown
podman machine start podman-machine-default
```

From the repository root, open PowerShell and run:

```powershell
podman machine start podman-machine-default
podman compose up -d
```

Verify that the database is reachable from Windows before starting the backend:

```powershell
for ($attempt = 1; $attempt -le 30; $attempt++) {
    podman exec quiz_forge_db pg_isready -U admin -d quizForge *> $null
    if ($LASTEXITCODE -eq 0) { break }
    if ($attempt -eq 30) {
        podman compose ps
        podman compose logs db
        throw "PostgreSQL did not become ready."
    }
    Start-Sleep -Seconds 1
}
$databaseConnection = Test-NetConnection 127.0.0.1 -Port 5432 `
    -WarningAction SilentlyContinue
if (-not $databaseConnection.TcpTestSucceeded) {
    podman compose ps
    podman compose logs db
    throw "Windows cannot reach PostgreSQL on 127.0.0.1:5432."
}
$databaseConnection
```

`Test-NetConnection` should report `TcpTestSucceeded : True`. If it reports
`False`, confirm that the Podman machine is running and that the forwarding
configuration file exists at the path above.

After the database check succeeds, start the backend:

```powershell
.\gradlew.bat bootRun
```

The backend starts at `http://localhost:8080`.

## Stop the backend

Press `Ctrl+C` in the backend terminal, then stop the database container when finished:

```powershell
podman compose down
```

## Run the frontend

See [frontend/README.md](frontend/README.md) for frontend setup and development commands.
