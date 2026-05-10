# Cómo retomar la EFT — paso a paso

Guía corta para cuando vuelvas a esta tarea. Asume que detuviste con `docker-compose stop` (volúmenes y datos preservados).

---

## 0. Pre-requisito

**Docker Desktop**: System Tray → click derecho → Start Docker Desktop. Esperar que el ícono pare de animar.

```powershell
docker info --format "Server: {{.ServerVersion}}"
# Debe responder con la versión, sin error.
```

---

## 1. Stack de la aplicación (mysql + backend + frontend)

```powershell
cd "C:\Users\fparraa\Documents\Estudio\2026_B6\CDY2203 SEGURIDAD Y CALIDAD EN EL DESARROLLO\S3_Sumativa1\Ayuda_Sumativa"
cd db
docker-compose start
cd ..
```

**Verificación** (esperar ~30s tras start para que backend conecte a MySQL):

```powershell
docker ps --filter "name=db-" --format "table {{.Names}}\t{{.Status}}"
# Tres contenedores: db-frontend-1, db-backend-1, db-mysql-cdy2203-1-1 (todos Up)

# Test funcional rápido:
(Invoke-WebRequest http://localhost:8080 -UseBasicParsing -TimeoutSec 5).StatusCode  # → 200
(Invoke-WebRequest http://localhost:8081/pets -UseBasicParsing -TimeoutSec 5).StatusCode  # → 200
```

Si el backend tarda en arrancar y muestra "Exited (1)" por race con MySQL:

```powershell
docker start db-backend-1
# Esperar 25s y reintentar el test
```

**URLs**:
- Frontend: http://localhost:8080
- Backend: http://localhost:8081/pets
- MySQL: localhost:3306 (`mydatabase` / `myuser` / `password`)

**Usuarios de prueba**: `admin / password`, `user / password`, `manager / password`.

---

## 2. Stack SAST (Sonar + Jenkins)

```powershell
cd sonarqube
docker-compose start
cd ..
```

**Verificación** (Sonar tarda ~2 min en estar UP):

```powershell
docker ps --filter "name=jenkins" --filter "name=sonarqube" --format "table {{.Names}}\t{{.Status}}"

# Cuando Sonar esté arriba:
(Invoke-WebRequest http://localhost:9000/api/system/status -UseBasicParsing).Content
# Debe retornar {"status":"UP"}

(Invoke-WebRequest http://localhost:8090/login -UseBasicParsing).StatusCode
# → 200
```

**URLs**:
- Jenkins: http://localhost:8090
- SonarQube: http://localhost:9000

### Credenciales

**Si NO completaste el setup inicial todavía** (primera vez):

```powershell
# Password inicial Jenkins (si aún no creaste tu admin):
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Sonar primer login: `admin / admin` (te pedirá cambiar password).

**Si YA completaste el setup**: usa el admin/password que creaste tú. (Recomendado: anotalos en un password manager).

---

## 3. Variables de entorno a reactivar

Cada vez que abres una **nueva** terminal PowerShell, antes de correr scripts:

```powershell
# Para SCA (Dependency-Check)
$env:NVD_API_KEY = "34607290-88d4-4c44-bf12-0982ce87ee4c"

# Para Collect-Evidence-Sonar.ps1 (cuando tengas el token)
$env:SONAR_TOKEN = "<el-token-que-generaste-en-sonar>"

# Para Collect-Evidence-Jenkins.ps1 (cuando tengas el API token)
$env:JENKINS_USER = "<tu-usuario-jenkins>"
$env:JENKINS_TOKEN = "<tu-api-token-jenkins>"
```

JDK 21 (para tests JaCoCo locales, **opcional** porque ya corremos en Docker):

```powershell
$env:JAVA_HOME = "C:\Users\fparraa\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

---

## 4. Lo que ya está hecho y archivado

Todo en `docs/evidencias/` con la convención `<paso>_<herramienta>_<capa>_<iter>_<fecha>`:

| Paso | Herramienta | Resultado |
|---|---|---|
| 1 | SQL schema | `db/schema.sql` (4.4 KB, 9 tablas) |
| 4 | Trivy | FE 0/0, BE 0/0, MySQL 0/0 (con `.trivyignore`) |
| 5 | SCA Dependency-Check | FE 0/0/0/0, BE 0/0/20/0 |
| 6 | JaCoCo | FE 81% / BE 64% (≥60% requerido) |

Mitigaciones aplicadas en `pom.xml`:
- Spring Boot 4.0.4 → **4.0.6**
- Tomcat 11.0.20 → **11.0.22**
- JaCoCo 0.8.12 → **0.8.14**
- MySQL Dockerfile: `mysql:8.4` → **mysql:8.4.9**

---

## 5. Lo que falta hacer (manual)

Orden sugerido (ver propuesta detallada en mensajes de chat anteriores):

### [A] Push inicial a GitHub (~5 min) — **bloqueante para SAST**

```powershell
# Solo la primera vez:
git config --global user.name "Francisco Parra"
git config --global user.email "Claude_Ean@duoc.cl"

# Init + push:
git init
git branch -M main
git add .
git status   # verificar que .zip / target/ / .m2repo NO aparezcan
git commit -m "EFT CDY2203 - entrega inicial"
git remote add origin https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra.git
git push -u origin main
# Username: FcoXavierParra · Password: tu PAT (NO password de cuenta)
```

PAT: https://github.com/settings/tokens → Generate new token (classic) → scope `repo`.

### [B] ZAP DAST (~30–45 min)

Con app arriba:
1. Abrir OWASP ZAP → desactivar HUD si estaba activo.
2. Quick Start → Automated Scan → URL `http://localhost:8080` → Attack.
3. Capturar `02_zap_alertas_inicial.png` y exportar reporte HTML como `02_zap_reporte_inicial.html`.
4. Repetir con `http://localhost:8081/pets`.
5. Si hay High → mitigar y re-escanear (capturas `_final`).

### [C] Sonar+Jenkins SAST (~60–90 min)

Con stacks arriba y push hecho:
1. **Setup Jenkins**: http://localhost:8090 → unlock → install suggested plugins → crear admin.
2. **Setup Sonar**: http://localhost:9000 → admin/admin → cambiar pass → My Account → Security → generar Global Analysis Token llamado `jenkins`.
3. **Plugin Sonar Scanner** en Jenkins → Manage Jenkins → Plugins → install + restart.
4. **Configurar conexión**: Manage Jenkins → System → SonarQube servers (URL `http://sonarqube:9000`, secret text con el token).
5. **Configurar tool**: Manage Jenkins → Tools → SonarQube Scanner installations → Install from Maven Central.
6. **Maven en Jenkins**:
   ```powershell
   docker exec -u root jenkins bash -c "apt-get update -qq && apt-get install -y -qq maven"
   ```
7. **Crear 2 jobs Freestyle** (`sast-cdy2203-frontend`, `sast-cdy2203-backend`) — config detallada en [`sonarqube/README.md`](sonarqube/README.md).
8. Build Now en cada uno → screenshot dashboard Sonar.
9. Generar API token Jenkins (User → Configure → API Token) y avisarme.
10. Si hay críticos → mitigar → push → Build Now → screenshot final.

---

## 6. Detener todo cuando termines la sesión

```powershell
cd db ; docker-compose stop ; cd ..
cd sonarqube ; docker-compose stop ; cd ..
docker ps   # debe estar vacío
```

`stop` (no `down`) preserva volúmenes y configuración. Para limpiar todo (perdería datos):

```powershell
# SOLO si quieres empezar de cero:
cd db ; docker-compose down -v ; cd ..
cd sonarqube ; docker-compose down -v ; cd ..
```

---

## 7. Atajos rápidos

| Lo que quieres hacer | Comando |
|---|---|
| Re-correr JaCoCo backend | `docker run --rm -v "${PWD}/cdy2203-backend-2026-201-main/cdy2203-backend-2026-201-main:/workspace" -w /workspace maven:3.9-eclipse-temurin-21 mvn clean test "-Dmaven.repo.local=.m2repo"` |
| Re-correr SCA | `$env:NVD_API_KEY="34607290-88d4-4c44-bf12-0982ce87ee4c" ; .\Generate-SCA-Reports.ps1 -NvdApiKey $env:NVD_API_KEY -Iteration "final"` |
| Re-correr Trivy | `.\Collect-Evidence-Trivy.ps1 -Iteration "final"` |
| Regenerar `schema.sql` | `docker exec db-mysql-cdy2203-1-1 mysqldump -u root -ppassword --no-data --compact mydatabase` |
| Convertir informe a PDF | Markdown PDF (extensión VS Code) o `pandoc docs/INFORME_EFT.md -o INFORME.pdf` |

---

**Estado del informe**: `docs/INFORME_EFT.md` ya tiene §1–§3, §6 (Trivy), §7 (SCA), §8 (JaCoCo), §9–§11 completos con cifras reales. §4 (ZAP) y §5 (SAST) tienen estructura + marcadores de evidencia esperando que captures las pantallas.