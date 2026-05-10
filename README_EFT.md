# EFT CDY2203 — Unidos por los Animales

Repositorio: https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra

Estructura del repo:

- `cdy2203-2026-201-main/` — frontend (Spring Boot + Thymeleaf)
- `cdy2203-backend-2026-201-main/` — backend (Spring Boot + JPA + MySQL)
- `db/` — `Dockerfile` MySQL + `docker-compose.yml` que orquesta MySQL + backend + frontend
- `db/schema.sql` — esquema SQL exportado (entregable Paso 1)
- `sonarqube/` — `docker-compose.yaml` para Sonar+Jenkins (Paso 3, SAST)
- `openvas/` — `docker-compose.yml` Greenbone Community Edition (Paso 4)
- `Generate-SCA-Reports.ps1` — script SCA (Paso 5)

## Levantar la aplicación localmente

```powershell
cd db
docker-compose up -d --build
```

Servicios:
- MySQL: `localhost:3306` (`mydatabase`, user `myuser`, pass `password`)
- Backend: `http://localhost:8081`
- Frontend: `http://localhost:8080`

Usuarios de prueba: `admin/password`, `user/password`, `manager/password`.

## Push inicial al repositorio

Ejecutar **una sola vez** desde la raíz del proyecto:

```powershell
# 1. Identidad git (cambia si quieres otro mail)
git config --global user.name "Francisco Parra"
git config --global user.email "Claude_Ean@duoc.cl"

# 2. Inicializar repo, asegurar rama main
git init
git branch -M main

# 3. Agregar archivos respetando .gitignore
git add .
git status   # revisa que NO aparezcan .zip / target/ / .m2repo

# 4. Primer commit
git commit -m "EFT CDY2203 - entrega inicial"

# 5. Conectar al remoto y pushear
git remote add origin https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra.git
git push -u origin main
```

En el `git push` te va a pedir credenciales:
- **Username**: `FcoXavierParra`
- **Password**: NO tu password de GitHub. Usa un **Personal Access Token (PAT)**.

## Generar PAT (Personal Access Token classic)

1. https://github.com/settings/tokens → "Generate new token (classic)".
2. Note: `EFT CDY2203` · Expiration: 30 days.
3. Scope: marcar **`repo`** (todas las sub-checks se marcan solas).
4. Generate token → **copiar el valor inmediatamente** (no se vuelve a mostrar).
5. Pegarlo como password en el `git push`.

Alternativa más cómoda con GitHub CLI:

```powershell
winget install --id GitHub.cli
gh auth login   # navegador → un click → token guardado
git push -u origin main
```

## Repo público vs privado

Este repo está **público** durante la entrega (decisión EFT). Eso significa:
- El docente puede inspeccionarlo sin invitación.
- **Jenkins** (Paso 3) lo clona sin token — no hay que registrar credencial en Jenkins.
- Si en algún momento se vuelve privado: registrar una credencial **Username with password** en Jenkins → username `FcoXavierParra`, password = el mismo PAT (o uno nuevo).

## Higiene de secretos

Antes de cada commit:

```powershell
# revisar que ningún archivo con secretos vaya a entrar
git status
git diff --cached
```

**Nunca commitear**:
- `*.env`, `*.pem`, `*.key`, `*.p12`
- `nvd_api_key.txt`, `github_pat.txt`
- `application-local.properties`

**Nunca compartir** un PAT / API token en:
- Chat (Slack, Teams, mensajes privados, asistentes IA, etc.)
- Issues o pull requests
- Tickets de soporte
- Capturas de pantalla (taparlos antes de subir)

Si por error se pushea o se comparte un secreto, **rotarlo inmediatamente**:
- **GitHub PAT**: `https://github.com/settings/tokens` → Delete → Generate new token.
- **NVD API key**: `https://nvd.nist.gov/developers/request-an-api-key` (renovacion).
- **Sonar token**: `http://localhost:9000` → My Account → Security → Revoke + Generate.
- **Jenkins API token**: tu user → Configure → API Token → Revoke + Generate new.

El `.gitignore` no borra historial: si se hizo push de un secreto, hay que **rotarlo aunque se elimine el archivo del repo**, porque queda en commits previos accesibles.

> Si el repo es publico y un secreto cayo ahi, asumir que ya fue indexado por bots de scraping en minutos.
