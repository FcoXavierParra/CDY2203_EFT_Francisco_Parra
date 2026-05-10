# Paso 3 — SAST con Sonar + Jenkins (Docker)

Stack basado en guía Exp2_S4. Diferencia clave: Jenkins en **8090** porque 8080 lo usa el frontend.

## 1. Levantar el stack

```powershell
cd sonarqube
docker-compose up -d
```

Verificar:
- Jenkins: http://localhost:8090   (la primera vez pide unlock)
- SonarQube: http://localhost:9000   (admin / admin)

## 2. Desbloquear Jenkins (primera vez)

```powershell
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Pegar el valor en el navegador → "Install suggested plugins" → crear usuario admin.

## 3. Instalar Maven y JDK dentro del contenedor Jenkins

```powershell
docker exec -u root jenkins bash -c "apt-get update && apt-get install -y maven default-jdk"
```

(El proyecto necesita Java 21; default-jdk en Debian bookworm trae Java 17. Si Sonar reclama, reemplazar por `openjdk-21-jdk` desde backports — mantener el comando preparado pero comenzar con default).

## 4. Cambiar password admin de SonarQube

http://localhost:9000 → login admin/admin → forzar cambio de password.

## 5. Generar token en SonarQube

User (esquina sup. derecha) → My Account → Security → Generate Token
- Name: `jenkins`
- Type: `Global Analysis Token`
- Expira: 30 days

**Copiar el token** (no se vuelve a mostrar).

## 6. Instalar plugin SonarQube Scanner en Jenkins

Manage Jenkins → Plugins → Available → buscar **"SonarQube Scanner"** → Install + restart.

## 7. Conectar Jenkins ↔ SonarQube

Manage Jenkins → System → SonarQube servers → Add SonarQube
- Name: `sonarqube`
- Server URL: `http://sonarqube:9000`   (nombre del servicio en la red docker)
- Server authentication token → Add → Jenkins → Kind: **Secret text** → Secret: el token del paso 5 → Save.

## 8. Configurar el SonarQube Scanner

Manage Jenkins → Tools → SonarQube Scanner installations → Add
- Name: `sonarqube`
- Install automatically → Install from Maven Central → version más reciente.

## 9. Crear el Job de análisis (Freestyle)

New Item → nombre `sast-cdy2203-frontend` → Freestyle project → OK.

- **Source Code Management** → Git → Repository URL: `https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra.git`
- Branch Specifier: `*/main`
- (repo público → no requiere credenciales)

- **Build Steps** → Add → Execute shell:
  ```bash
  cd cdy2203-2026-201-main/cdy2203-2026-201-main
  mvn -DskipTests clean install -Dmaven.repo.local=.m2repo
  ```
- **Build Steps** → Add → Execute SonarQube Scanner:
  - Analysis properties:
    ```
    sonar.projectKey=cdy2203-frontend
    sonar.projectName=CDY2203 Frontend
    sonar.sources=cdy2203-2026-201-main/cdy2203-2026-201-main/src/main/java
    sonar.java.binaries=cdy2203-2026-201-main/cdy2203-2026-201-main/target/classes
    sonar.java.source=21
    ```

Repetir el job cambiando paths/projectKey para el backend (`sast-cdy2203-backend`).

## 10. Ejecutar y revisar

`Build Now` en cada job → al terminar:
- Logs en Jenkins: ver "SonarQube task..." con URL al dashboard.
- SonarQube → Projects → `cdy2203-frontend` y `cdy2203-backend`.
- Capturar **dashboard inicial** (Bugs / Vulnerabilities / Security Hotspots / Code Smells).

## 11. Iteración correctiva

1. Ordenar por **Severity Critical/Blocker** y **Vulnerabilities**.
2. Corregir mínimo **2 hallazgos críticos** (un buen ratio antes/después es lo que apunta a CL).
3. Commitear los fixes en el repo.
4. `Build Now` otra vez → capturar **dashboard final** sin críticas.

Adjuntar ambos dashboards (antes/después) al informe.
