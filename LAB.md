# Entornos de TenantGuard Lab

Los dos archivos Compose usan proyectos distintos. `compose.yaml` conserva PostgreSQL de desarrollo en `127.0.0.1:5433` y su volumen actual. `compose.lab.yaml` crea contenedores, red y volumen propios; su PostgreSQL solo escucha en la red interna y la API se publica en `127.0.0.1:8080`.

Antes de arrancar, conserva un archivo `.env` local (ignorado por Git) con `DB_NAME`, `DB_USER` y `DB_PASSWORD`, y los archivos `keys/private.pem` y `keys/public.pem` (también ignorados). Ambos modos pueden usar las mismas variables, pero no comparten datos. Los archivos de claves se montan por separado y en modo de solo lectura; deben ser legibles para el UID 10001 del contenedor de la API.

## Desarrollo: IntelliJ + PostgreSQL en Docker

```powershell
docker compose -f compose.yaml up -d db
```

Ejecuta Spring Boot desde IntelliJ con las variables de `.env`; la configuración actual conecta con `localhost:5433`. Para detener este modo, detén la aplicación en IntelliJ y luego ejecuta:

```powershell
docker compose -f compose.yaml stop db
```

## Laboratorio: API + PostgreSQL en Docker

Si Spring Boot sigue usando el puerto 8080 desde IntelliJ, detén solo esa ejecución antes de arrancar el laboratorio.

Copia `.env.lab.example` a `.env.lab` y asigna una contraseña local distinta a cada `LAB_PASSWORD_ALICE`, `LAB_PASSWORD_BOB`, `LAB_PASSWORD_CHARLIE`, `LAB_PASSWORD_DAVID` y `LAB_PASSWORD_EVE` (12 a 72 caracteres). `.env.lab` está ignorado por Git. No uses contraseñas reales. Las contraseñas solo se aplican cuando las tablas de dominio del laboratorio están vacías; cambiar variables después no modifica usuarios existentes.

```powershell
docker compose --env-file .env --env-file .env.lab -f compose.lab.yaml up -d --build
docker compose --env-file .env --env-file .env.lab -f compose.lab.yaml ps
```

La API usa `jdbc:postgresql://db:5432/<DB_NAME>` dentro de la red del laboratorio. `depends_on` espera a que el healthcheck de PostgreSQL esté preparado; Spring Boot ejecuta Flyway y luego la inicialización del perfil `lab`. Si ya existe cualquier usuario, organización, membresía o documento, la inicialización se omite completa y no modifica esos datos.

| Usuario | Organización | Rol |
| --- | --- | --- |
| alice | Acme Corp | ADMIN |
| bob | Acme Corp | EDITOR |
| charlie | Acme Corp | VIEWER |
| david | Globex Corp | ADMIN |
| eve | Globex Corp | VIEWER |

Acme contiene los documentos `ACME-001` a `ACME-003`; Globex contiene `GLOBEX-001` a `GLOBEX-003`. Sus títulos y contenidos identifican claramente la organización. La API no expone las contraseñas. Para obtener un JWT, usa el endpoint de login con la contraseña local del usuario correspondiente, por ejemplo:

```powershell
$password = Read-Host -AsSecureString 'Contraseña local de alice'
$plainPassword = [System.Net.NetworkCredential]::new('', $password).Password
$body = @{ username = 'alice'; password = $plainPassword } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8080/api/auth/login' -ContentType 'application/json' -Body $body).accessToken
Remove-Variable plainPassword, body
```

Usa `$token` como `Authorization: Bearer <token>` en Burp Suite. Repite el login para cada usuario; no guardes tokens en Git.

Para detener el laboratorio sin eliminar el volumen:

```powershell
docker compose --env-file .env --env-file .env.lab -f compose.lab.yaml stop
```

## Restaurar solo el escenario de laboratorio

Primero comprueba el proyecto, el perfil, las etiquetas del volumen y el volumen de desarrollo sin cambiar nada:

```powershell
./scripts/Reset-Lab.ps1 -CheckOnly
```

Después ejecuta `./scripts/Reset-Lab.ps1`. El script exige escribir exactamente `RESET tenantguard-lab-lab_lab_postgres_data` antes de detener los contenedores del laboratorio y eliminar únicamente ese volumen. A continuación vuelve a construir y arrancar el laboratorio, que aplica Flyway y recrea los datos ficticios. El volumen de desarrollo `tenantguard-lab_postgres_data` no es objetivo del script. Esta operación elimina los cambios que hayas hecho en los datos del laboratorio.

Para comprobar que desarrollo sigue intacto, consulta sus contenedores y el volumen por separado:

```powershell
docker compose -f compose.yaml ps
docker volume inspect tenantguard-lab_postgres_data --format '{{.Name}}'
```
