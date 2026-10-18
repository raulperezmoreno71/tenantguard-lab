# TenantGuard Lab environments

The two Compose files use separate projects. `compose.yaml` keeps the existing development PostgreSQL database and volume and exposes it on `127.0.0.1:5433`. `compose.lab.yaml` creates separate containers, a network, and a volume; its PostgreSQL service is available only on the internal network, while the API is bound to `127.0.0.1:8080`.

Before starting either mode, keep a local `.env` file (ignored by Git) with `DB_NAME`, `DB_USER`, and `DB_PASSWORD`, plus `keys/private.pem` and `keys/public.pem` (also ignored). The two modes may use the same variable names, but they do not share database data. The key files are mounted read-only and must be readable by the API container's UID 10001.

## Development: IntelliJ + PostgreSQL in Docker

```powershell
docker compose -f compose.yaml up -d db
```

Run Spring Boot from IntelliJ with the variables from `.env`; the current configuration connects to `localhost:5433`. IntelliJ does not automatically import the Compose `.env` file into its run configuration. To stop this mode, stop the application in IntelliJ and then run:

```powershell
docker compose -f compose.yaml stop db
```

## Laboratory: API + PostgreSQL in Docker

If an IntelliJ-run Spring Boot instance is still using port 8080, stop only that instance before starting the lab.

Copy `.env.lab.example` to `.env.lab` and choose a different local demonstration password for each of `LAB_PASSWORD_ALICE`, `LAB_PASSWORD_BOB`, `LAB_PASSWORD_CHARLIE`, `LAB_PASSWORD_DAVID`, and `LAB_PASSWORD_EVE` (12–72 characters). `.env.lab` is ignored by Git. Do not use real passwords. These passwords take effect only when the lab's domain tables are empty; changing the variables later does not change existing users.

```powershell
docker compose --env-file .env --env-file .env.lab -f compose.lab.yaml up -d --build
docker compose --env-file .env --env-file .env.lab -f compose.lab.yaml ps
```

Within the lab network, the API uses `jdbc:postgresql://db:5432/<DB_NAME>`. `depends_on` waits for the PostgreSQL healthcheck; Spring Boot then runs Flyway and the `lab`-profile initializer. If any user, organization, membership, or document already exists, initialization is skipped entirely and existing data is not changed.

| User | Organization | Role |
| --- | --- | --- |
| alice | Acme Corp | ADMIN |
| bob | Acme Corp | EDITOR |
| charlie | Acme Corp | VIEWER |
| david | Globex Corp | ADMIN |
| eve | Globex Corp | VIEWER |

Acme contains documents `ACME-001` through `ACME-003`; Globex contains `GLOBEX-001` through `GLOBEX-003`. Their titles and contents identify the owning organization. The API does not expose passwords. To obtain a JWT, call the login endpoint with that user's local password, for example:

```powershell
$password = Read-Host -AsSecureString 'Local password for alice'
$plainPassword = [System.Net.NetworkCredential]::new('', $password).Password
$body = @{ username = 'alice'; password = $plainPassword } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8080/api/auth/login' -ContentType 'application/json' -Body $body).accessToken
Remove-Variable plainPassword, body
```

Use `$token` as `Authorization: Bearer <token>` in Burp Suite. Log in separately for each user; do not store tokens in Git.

To stop the lab without deleting its volume:

```powershell
docker compose --env-file .env --env-file .env.lab -f compose.lab.yaml stop
```

## Reset only the laboratory scenario

First, inspect the project, profile, volume labels, and development volume without changing anything:

```powershell
./scripts/Reset-Lab.ps1 -CheckOnly
```

Then run `./scripts/Reset-Lab.ps1`. The script requires you to type exactly `RESET tenantguard-lab-lab_lab_postgres_data` before stopping the lab containers and deleting only that volume. It then rebuilds and starts the lab, where Flyway and the initializer recreate the fictitious scenario. The development volume, `tenantguard-lab_postgres_data`, is not a target of this script. This operation deletes any changes you made to the lab data.

To confirm that development remains intact, inspect its containers and volume separately:

```powershell
docker compose -f compose.yaml ps
docker volume inspect tenantguard-lab_postgres_data --format '{{.Name}}'
```
