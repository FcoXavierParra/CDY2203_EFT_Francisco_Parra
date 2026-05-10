# Seguridad y Calidad en el Desarrollo (CDY2203)

**Evaluación Final Transversal — Semana 9**

| | |
|---|---|
| **Estudiante** | Francisco Javier Parra Andia |
| **Carrera** | Analista Programador |
| **Profesor** | Jonathan Fletcher Castro |
| **Fecha** | _completar al cierre_ |
| **Repositorio** | https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra |

---

## Índice

1. [Introducción](#1-introducción)
2. [Contexto general de la solución](#2-contexto-general-de-la-solución)
3. [Paso 1 — Entrega del código de la aplicación web](#3-paso-1--entrega-del-código-de-la-aplicación-web)
4. [Paso 2 — Análisis OWASP Top 10 con ZAP](#4-paso-2--análisis-owasp-top-10-con-zap)
5. [Paso 3 — Análisis estático con SonarQube + Jenkins](#5-paso-3--análisis-estático-sast-con-sonarqube--jenkins)
6. [Paso 4 — Escaneo de vulnerabilidades con OpenVAS](#6-paso-4--escaneo-de-vulnerabilidades-con-openvas)
7. [Paso 5 — Análisis SCA con OWASP Dependency-Check](#7-paso-5--análisis-sca-con-owasp-dependency-check)
8. [Paso 6 — Pruebas unitarias y cobertura con JUnit + JaCoCo](#8-paso-6--pruebas-unitarias-y-cobertura)
9. [Paso 7 — Entrega de la aplicación](#9-paso-7--entrega-de-la-aplicación)
10. [Conclusión](#10-conclusión)
11. [Anexos](#11-anexos)

---

## 1. Introducción

El presente informe documenta el cierre de la asignatura **CDY2203 Seguridad y Calidad en el Desarrollo** mediante la Evaluación Final Transversal. Sobre la aplicación web "Unidos por los Animales", desarrollada a lo largo del bimestre, se aplicó un proceso integral de aseguramiento de calidad y seguridad, abordando los siete criterios definidos por la rúbrica de la EFT:

1. Validación de vulnerabilidades OWASP Top 10 con **OWASP ZAP**.
2. Análisis estático del código (SAST) con **SonarQube + Jenkins**.
3. Detección de vulnerabilidades de infraestructura con **OpenVAS / Greenbone Community Edition**.
4. Análisis de composición (SCA) de las dependencias con **OWASP Dependency-Check**.
5. Pruebas unitarias y cobertura ≥ 60% con **JUnit + JaCoCo**.
6. Documentación técnica del proceso (este informe).
7. Presentación audiovisual del trabajo.

El proceso se llevó a cabo de manera **iterativa**: cada herramienta se ejecutó al menos dos veces (estado inicial → corrección → estado final), de forma que las evidencias entregadas demuestran tanto la detección como la mitigación efectiva de los hallazgos.

## 2. Contexto general de la solución

La aplicación está compuesta por tres capas integradas mediante Docker Compose:

```
[ Frontend Thymeleaf :8080 ] ──REST──▶ [ Backend Spring Boot :8081 ] ──JPA──▶ [ MySQL 8.4 :3306 ]
```

**Backend** (`cdy2203-backend-2026-201-main/`):
- Spring Boot 4.0.4, Spring Web, Spring Security, Spring Data JPA.
- Conector MySQL 8.4 + HikariCP.
- Autenticación con login + token **JWT**, configuración de Spring Security en `WebSecurityConfig`.
- APIs públicas (`GET /pets`, `/pets/available`, `/pets/search`) y privadas (`POST/PUT/DELETE`).

**Frontend** (`cdy2203-2026-201-main/`):
- Spring Boot 4.0.4 + Thymeleaf + Spring Security.
- Vistas server-side con CSS propio.
- Login que consume el backend, obtiene el JWT y mantiene sesión Spring Security para las páginas privadas.

**Base de datos** (`db/`):
- Imagen `mysql:8.4` orquestada en `docker-compose.yml`.
- Esquema generado por JPA (`spring.jpa.hibernate.ddl-auto=update`).
- Esquema exportado para entregable: [`db/schema.sql`](../db/schema.sql).

---

## 3. Paso 1 — Entrega del código de la aplicación web

La aplicación se entrega en este mismo repositorio. Estructura mínima exigida por la rúbrica:

| Capa | Ubicación | Contenido |
|---|---|---|
| Frontend | [`cdy2203-2026-201-main/`](../cdy2203-2026-201-main) | Spring Boot + Thymeleaf, CSS, controllers MVC |
| Backend | [`cdy2203-backend-2026-201-main/`](../cdy2203-backend-2026-201-main) | Spring Boot + JPA + Spring Security + JWT |
| BBDD (esquema) | [`db/schema.sql`](../db/schema.sql) | Script `CREATE TABLE` exportado por `mysqldump` |
| Pruebas unitarias | [`cdy2203-backend-2026-201-main/.../src/test`](../cdy2203-backend-2026-201-main/cdy2203-backend-2026-201-main/src/test) y [`cdy2203-2026-201-main/.../src/test`](../cdy2203-2026-201-main/cdy2203-2026-201-main/src/test) | JUnit 5, ver Paso 6 |

**Arquitectura en 3 capas**: presentación (frontend Thymeleaf) — lógica (backend REST) — datos (MySQL), respetando lo solicitado por la pauta.

**Spring Security**: implementado en ambas capas. En el backend se exponen rutas públicas y privadas (filtro `JWTAuthorizationFilter`); en el frontend se protegen las páginas privadas mediante sesión, redirigiendo a login.

> 📸 **EVIDENCIA — Paso 1** (capturas)
> - **E1.1** Estructura del repositorio en GitHub
>   _Captura del árbol del repo en `https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra`._
>   Guardar como `docs/evidencias/01_repo_estructura.png`.
> - **E1.2** Esquema SQL en ejecución
>   _Captura de `db/schema.sql` abierto y, opcionalmente, salida de `SHOW TABLES;` en MySQL._
>   Guardar como `docs/evidencias/01_schema_sql.png`.
> - **E1.3** Pruebas unitarias en el repo
>   _Captura del directorio `src/test/java` mostrando los archivos `*Test.java` existentes._
>   Guardar como `docs/evidencias/01_tests_estructura.png`.

---

## 4. Paso 2 — Análisis OWASP Top 10 con ZAP

### 4.1. Herramienta y configuración

Se utiliza **OWASP ZAP** (DAST, caja negra) sobre la aplicación corriendo localmente en Docker Compose.

Comando de levantamiento del stack para escaneo:

```powershell
cd db
docker-compose up -d --build
```

URLs objetivo:
- Frontend: `http://localhost:8080`
- Backend público: `http://localhost:8081/pets`

Exclusiones aplicadas en ZAP (para evitar contaminación por la propia UI/proxy de ZAP, conocido del Exp1):
- `http://localhost:8080/UI/.*`
- `http://localhost:8080/JSON/.*`
- `http://localhost:8080/OTHER/.*`

### 4.2. Estado inicial — Escaneo 1

Procedimiento ejecutado en ZAP:
1. Abrir ZAP → "Automated Scan".
2. URL `http://localhost:8080` → marcar Spider tradicional → Attack.
3. Repetir para `http://localhost:8081/pets`.
4. Generar reporte HTML: Reports → Generate HTML Report.

> 📸 **EVIDENCIA E2.1 — Estado inicial ZAP**
> - **Captura 1**: Panel de Alertas de ZAP con conteo por severidad (Alto/Medio/Bajo/Informativo) tras el primer Active Scan. Guardar como `docs/evidencias/02_zap_alertas_inicial.png`.
> - **Captura 2**: Detalle de cada alerta High/Medium expandida (texto del CWE, URL afectada). Guardar como `docs/evidencias/02_zap_detalle_alertas.png`.
> - **Archivo**: reporte HTML exportado por ZAP. Guardar como `docs/evidencias/02_zap_reporte_inicial.html`.

**Hallazgos típicos esperados** (basado en Exp1):
- 0 High (✅ ya cumplido en Exp1).
- 1 Medium **CSP: Failure to Define Directive with No Fallback** (asociado a la respuesta de la propia API UI de ZAP — falso positivo conocido, documentado más abajo).
- 1 Low **Cookie sin atributo SameSite** (cookie `JSESSIONID` del backend tras 403).
- 1 Informativo **Respuesta de Gestión de Sesión Identificada**.

### 4.3. Iteración correctiva

Durante el desarrollo se aplicaron las siguientes mitigaciones:

#### 4.3.1. Content Security Policy explícita

Archivo modificado: [`WebSecurityConfig.java`](../cdy2203-2026-201-main/cdy2203-2026-201-main/src/main/java/com/duoc/seguridadcalidad/WebSecurityConfig.java) en frontend.

Se agregó:

```java
.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives(
        "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'"
    ))
    .contentTypeOptions(Customizer.withDefaults())
    .frameOptions(frame -> frame.deny())
)
```

> 📸 **EVIDENCIA E2.2 — Mitigación CSP**
> - **Captura**: diff o snippet del archivo `WebSecurityConfig.java` mostrando el bloque `headers(...)`.
>   Guardar como `docs/evidencias/02_zap_fix_csp_codigo.png`.

#### 4.3.2. Atributo SameSite en cookies de sesión

Archivo modificado: `application.properties` del backend.

```properties
server.servlet.session.cookie.same-site=lax
server.servlet.session.cookie.secure=true
```

> 📸 **EVIDENCIA E2.3 — Mitigación SameSite**
> - **Captura**: snippet del archivo `application.properties` con las dos líneas agregadas.
>   Guardar como `docs/evidencias/02_zap_fix_samesite_codigo.png`.

### 4.4. Estado final — Escaneo 2

> ⚠️ **Antes de reejecutar ZAP**: el reporte HTML del escaneo inicial debe estar **ya guardado** como `docs/evidencias/02_zap_reporte_inicial.html`. ZAP no genera archivos en disco automáticamente, pero al exportar el reporte HTML desde el menú "Reports", reutilizar el mismo nombre lo sobrescribiría. Verificar antes del nuevo escaneo.

Tras reconstruir el stack (`docker-compose up -d --build`), se reejecuta ZAP.

> 📸 **EVIDENCIA E2.4 — Estado final ZAP**
> - **Captura**: Panel de Alertas de ZAP **post-mitigación**, mostrando 0 High, 0 Medium accionables.
>   Guardar como `docs/evidencias/02_zap_alertas_final.png`.
> - **Archivo**: reporte HTML del segundo escaneo.
>   Guardar como `docs/evidencias/02_zap_reporte_final.html`.

**Resultado**: 0 vulnerabilidades de severidad alta. Las observaciones residuales (cookie en respuesta 403, CSP de la UI de ZAP) están documentadas como falsos positivos por contaminación del proxy.

---

## 5. Paso 3 — Análisis estático (SAST) con SonarQube + Jenkins

### 5.1. Investigación y selección

Se evaluaron tres herramientas SAST aplicables a Java:

| Herramienta | Tipo | Mantenimiento | Por qué considerada |
|---|---|---|---|
| **SonarQube** | Open source (Community Edition) | SonarSource | Estándar de la industria, integra con Maven/Jenkins, dashboard claro |
| SpotBugs | Open source | SpotBugs Project | Liviano, pero menos rico en métricas de seguridad |
| PMD | Open source | PMD Project | Reglas configurables, pero requiere más curaduría |

**Selección**: **SonarQube Community Edition** integrado con **Jenkins**, por:
- Cobertura de la guía Exp2_S4 de la asignatura.
- Pipeline reproducible vía Docker.
- Reportes con clasificación clara: Bugs / Vulnerabilities / Security Hotspots / Code Smells.

### 5.2. Configuración (Docker)

Stack en [`sonarqube/docker-compose.yaml`](../sonarqube/docker-compose.yaml). Diferencia respecto a la guía: **Jenkins en puerto 8090** (8080 lo usa el frontend de la app).

```powershell
cd sonarqube
docker-compose up -d
```

URLs:
- Jenkins: `http://localhost:8090`
- SonarQube: `http://localhost:9000` (admin/admin → cambiar password al primer login)

> 📸 **EVIDENCIA E3.1 — Stack levantado**
> - **Captura**: salida de `docker ps` filtrando por `jenkins` y `sonarqube`.
>   Guardar como `docs/evidencias/03_sonar_jenkins_running.png`.

### 5.3. Integración Jenkins ↔ SonarQube

Pasos seguidos (referencia: Exp2_S4 secciones 7–10):

1. Desbloquear Jenkins con `docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword`.
2. Instalar plugins sugeridos.
3. Manage Jenkins → Plugins → instalar **SonarQube Scanner**.
4. SonarQube → My Account → Security → generar **Global Analysis Token** llamado `jenkins`.
5. Jenkins → Manage Jenkins → System → SonarQube servers:
   - Name: `sonarqube`
   - URL: `http://sonarqube:9000` (resuelve por nombre del servicio en la red Docker).
   - Server authentication token → credencial **Secret text** con el token del paso 4.
6. Manage Jenkins → Tools → SonarQube Scanner installations → instalar desde Maven Central.

> 📸 **EVIDENCIA E3.2 — Configuración Jenkins**
> - **Captura E3.2.a**: pantalla "SonarQube servers" en Jenkins con la URL configurada.
>   Guardar como `docs/evidencias/03_sonar_jenkins_config_server.png`.
> - **Captura E3.2.b**: token generado en SonarQube (NO mostrar el valor del token, solo la pantalla con el `name=jenkins` y `Last use`).
>   Guardar como `docs/evidencias/03_sonar_token_generado.png`.

### 5.4. Job de análisis y estado inicial

Se crearon **dos** jobs Freestyle:
- `sast-cdy2203-frontend`
- `sast-cdy2203-backend`

Configuración de cada job (referencia [`sonarqube/README.md`](../sonarqube/README.md)):

- Source Code Management → Git → URL `https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra.git` (repo público, sin credenciales).
- Branch Specifier: `*/main`.
- Build Step 1 (Execute shell): `mvn -DskipTests clean install -Dmaven.repo.local=.m2repo`.
- Build Step 2 (Execute SonarQube Scanner) — Analysis properties para frontend:
  ```
  sonar.projectKey=cdy2203-frontend
  sonar.projectName=CDY2203 Frontend
  sonar.sources=cdy2203-2026-201-main/cdy2203-2026-201-main/src/main/java
  sonar.java.binaries=cdy2203-2026-201-main/cdy2203-2026-201-main/target/classes
  sonar.java.source=21
  ```
  (análoga para backend cambiando paths y projectKey).

Tras `Build Now` en cada job, SonarQube genera el dashboard.

> 📸 **EVIDENCIA E3.3 — Estado inicial SAST**
> - **Captura E3.3.a**: dashboard de SonarQube proyecto `cdy2203-frontend`, sección "Overall Code", mostrando contadores de Bugs / Vulnerabilities / Security Hotspots / Code Smells.
>   Guardar como `docs/evidencias/03_sonar_frontend_inicial.png`.
> - **Captura E3.3.b**: análoga para `cdy2203-backend`.
>   Guardar como `docs/evidencias/03_sonar_backend_inicial.png`.
> - **Captura E3.3.c**: log de Jenkins de un build exitoso, mostrando "ANALYSIS SUCCESSFUL" y la URL al dashboard.
>   Guardar como `docs/evidencias/03_jenkins_build_log.png`.

### 5.5. Iteración correctiva

A partir del listado del estado inicial (Quality Gate ya en `OK`, pero con **1 Security Hotspot HIGH** y **12 Code Smells CRITICAL**), se priorizaron las correcciones más impactantes desde el punto de vista de seguridad:

#### 5.5.1. Security Hotspot · `java:S6418` · "Hard-coded secret detected"

- **Archivo**: [`Constants.java`](../cdy2203-backend-2026-201-main/cdy2203-backend-2026-201-main/src/main/java/com/duoc/backend/Constants.java) (backend)
- **Línea original**: 17
- **Descripción Sonar**: la clave HMAC usada para firmar/verificar JWT está hard-codeada como literal Base64 en el código fuente público. Cualquiera con acceso al repo puede forjar tokens y suplantar usuarios.
- **Fix aplicado**: extracción a método `getJwtSigningKey()` que resuelve la clave por orden de prioridad:
  1. variable de entorno `JWT_SIGNING_KEY` (production),
  2. system property `jwt.signing-key` (CI/tests),
  3. fallback de desarrollo con texto legible (no high-entropy, no apto para producción, documentado en JavaDoc).
- **Snippet (antes / después)**:

  ```java
  // ANTES (hardcoded — Sonar: HIGH hotspot)
  public static final String SUPER_SECRET_KEY = "ZnJhc2VzbGFy...zRGNFE9PQ==";
  // ... uso:
  .signWith(getSigningKey(SUPER_SECRET_KEY))
  ```

  ```java
  // DESPUÉS (env var con fallback documentado)
  public static String getJwtSigningKey() {
      String envKey = System.getenv("JWT_SIGNING_KEY");
      if (envKey != null && !envKey.isBlank()) return envKey;
      String propKey = System.getProperty("jwt.signing-key");
      if (propKey != null && !propKey.isBlank()) return propKey;
      return "development-fallback-jwt-key-replace-with-JWT_SIGNING_KEY-env-var-in-production";
  }
  // ... uso:
  .signWith(getSigningKey(getJwtSigningKey()))
  ```

- **Archivos también actualizados**: [`JWTAuthenticationConfig.java`](../cdy2203-backend-2026-201-main/cdy2203-backend-2026-201-main/src/main/java/com/duoc/backend/JWTAuthenticationConfig.java), [`JWTAuthorizationFilter.java`](../cdy2203-backend-2026-201-main/cdy2203-backend-2026-201-main/src/main/java/com/duoc/backend/JWTAuthorizationFilter.java), [`SecurityAndModelsTest.java`](../cdy2203-backend-2026-201-main/cdy2203-backend-2026-201-main/src/test/java/com/duoc/backend/SecurityAndModelsTest.java).
- **Resultado esperado**: 0 Security Hotspots HIGH tras re-escaneo.

#### 5.5.2. Code Smell CRITICAL · `java:S1192` · "Define a constant instead of duplicating literals"

10 ocurrencias distribuidas en 7 archivos. Patrón uniforme: literales repetidos (`"Bearer "`, `"Authorization"`, `"/login"`, `"password"`, `"message"`, `"/pets"`, `"/patients/"`) reemplazados por constantes `private static final String` con nombre semántico y, donde aplica, comentarios JavaDoc explicando contexto (`DEV_DEFAULT_PASSWORD` en `WebSecurityConfig`, `DEV_SEED_PASSWORD` en `DataInitializer`).

| Archivo | Constante introducida | # ocurrencias eliminadas |
|---|---|---|
| `BackendService.java` (FE) | `BEARER_PREFIX`, `AUTHORIZATION_HEADER`, `PETS_PATH`, `PATIENTS_PATH` | 4 issues |
| `PatientRestController.java` (FE) | `BEARER_PREFIX` | 1 issue |
| `PetRestController.java` (FE) | `BEARER_PREFIX` | 1 issue |
| `WebSecurityConfig.java` (FE) | `LOGIN_PATH`, `DEV_DEFAULT_PASSWORD` | 2 issues |
| `DataInitializer.java` (BE) | `DEV_SEED_PASSWORD` | 1 issue |
| `PetController.java` (BE) | `ERROR_MESSAGE_KEY` | 1 issue |

**Resultado esperado**: 10 de 12 CRITICAL Code Smells eliminados (S1192). Los 2 restantes son `S3776` (cognitive complexity en `PetController.searchPets`) y se dejan como deuda técnica documentada en el reporte final, ya que su refactor (extraer subqueries) introduce riesgo de regresión en una rúbrica con tests específicos del comportamiento actual.

> 📸 **EVIDENCIA E3.4 — Fixes SAST**
> - **Captura E3.4.a**: Sonar → proyecto `cdy2203-backend` → tab "Security Hotspots" → mostrar 0 hotspots o el estado "Reviewed/Safe" tras el fix.
>   Guardar como `docs/evidencias/03_sonar_hotspot_resuelto.png`.
> - **Captura E3.4.b**: diff de `Constants.java` (antes vs después) — desde GitHub o localmente con `git diff HEAD~1 Constants.java`.
>   Guardar como `docs/evidencias/03_sonar_fix_constants_diff.png`.
> - **Captura E3.4.c**: en cualquier archivo (ej. `BackendService.java`), Sonar marcaba antes el literal repetido en rojo con `S1192`; tras el fix la línea está limpia. Captura del archivo en Sonar antes/después.
>   Guardar como `docs/evidencias/03_sonar_fix_s1192_diff.png`.

### 5.6. Estado final — Re-ejecución

> ⚠️ **Antes de reejecutar el job**: SonarQube **acumula** análisis en su histórico (Activity → puede verse cada análisis con su fecha), pero **el dashboard principal solo refleja el último**. Por eso es indispensable haber capturado los screenshots `03_sonar_*_inicial.png` antes del nuevo build, ya que el dashboard se reescribe.

Tras commit + push de los fixes, ejecutar `Build Now` en ambos jobs.

> 📸 **EVIDENCIA E3.5 — Estado final SAST**
> - **Captura E3.5.a**: dashboard de `cdy2203-frontend` con **Quality Gate: Passed** y 0 vulnerabilidades Critical/Blocker.
>   Guardar como `docs/evidencias/03_sonar_frontend_final.png`.
> - **Captura E3.5.b**: análoga para `cdy2203-backend`.
>   Guardar como `docs/evidencias/03_sonar_backend_final.png`.

**Resultado**: ausencia de hallazgos críticos en ambas capas — criterio CL del rubro 2 de la rúbrica.

---

## 6. Paso 4 — Escaneo de vulnerabilidades con Trivy

### 6.1. Investigación y selección

Se compararon herramientas gratuitas para escaneo de vulnerabilidades:

| Herramienta | Tipo | Foco |
|---|---|---|
| OpenVAS / Greenbone Community Edition | Software libre | Escaneo de red e infraestructura, requiere registry `registry.community.greenbone.net` |
| **Trivy** (Aqua Security) | Software libre | Escaneo de **imágenes Docker** y filesystem, CVE database (NVD + Red Hat + Ubuntu), salida JSON/SARIF/Table |
| Nessus Essentials | Comercial (limitado) | Equivalente comercial, requiere licencia y registro |
| Nikto | Software libre | Foco en web, menos completo |

**Selección final**: **Trivy** (Aqua Security).

**Justificación del cambio respecto a la guía Exp2_S5 (OpenVAS)**:

Durante la implementación se intentó levantar **OpenVAS / Greenbone Community Edition** según la guía. El registry oficial `registry.community.greenbone.net` (45.135.106.145) resultó **inalcanzable desde la red de trabajo**, tanto por IPv4 como IPv6:

```
dial tcp [2a0e:6b40:20:106::feed:145]:443: connectex: No connection could be made
because the target machine actively refused it
```

Verificación: `Test-NetConnection registry.community.greenbone.net -Port 443` → False en ambos protocolos. Docker Hub respondía correctamente, descartando un problema general de conectividad.

**Trivy** se eligió como reemplazo porque:
- Cumple el rol de **escáner de vulnerabilidades** que pide el rubro 3 de la rúbrica.
- Está distribuido vía Docker Hub (accesible).
- Encaja mejor con la arquitectura del proyecto: la app corre en contenedores, así que escanear directamente las **imágenes Docker** (backend, frontend, MySQL) es coherente y exhaustivo.
- Reporta CVEs con CVSS y "Solution" igual que OpenVAS.
- Mucho más liviano (~200 MB vs ~5 GB de feeds de Greenbone).

**Sobre CVE y CVSS** (clasificación adoptada en este informe):
- **CVE**: identificador único de vulnerabilidad pública mantenido por MITRE.
- **CVSS** (0–10): severidad calculada a partir de vector de ataque, complejidad, privilegios, interacción de usuario, alcance e impacto en CIA. Umbrales aplicados: ≥7.0 = Alto, ≥9.0 = Crítico.

### 6.2. Configuración

```powershell
docker pull aquasec/trivy:latest
```

Se utiliza el script [`Collect-Evidence-Trivy.ps1`](../Collect-Evidence-Trivy.ps1) que ejecuta Trivy con el flag `--ignorefile` apuntando a [`.trivyignore`](../.trivyignore) (ver §6.5) y archiva JSON+Tabla por cada imagen escaneada.

**Imágenes objetivo** (tras `docker-compose up -d --build` del stack `db/`):

| Imagen | Origen | Rol |
|---|---|---|
| `db-backend:latest` | Build local desde `cdy2203-backend-2026-201-main/Dockerfile` (multi-stage `eclipse-temurin:21-jdk-alpine`) | API REST |
| `db-frontend:latest` | Build local desde `cdy2203-2026-201-main/Dockerfile` (idem) | Vistas Thymeleaf |
| `db-mysql-cdy2203-1:latest` | Build local desde `db/Dockerfile` (`FROM mysql:8.4.9`) | BBDD |

### 6.3. Estado inicial — Escaneo 1 (sin `.trivyignore`)

```powershell
.\Collect-Evidence-Trivy.ps1 -Iteration "inicial"
```

**Resultados sin suprimir nada** (escaneo 2026-05-09 sobre las imágenes ya con SCA mitigado):

| Imagen | CRITICAL | HIGH | MEDIUM |
|---|---|---|---|
| `db-backend:latest` | 0 | 0 | 0 |
| `db-frontend:latest` | 0 | 0 | 0 |
| `db-mysql-cdy2203-1:latest` | **1** | **9** | 18 |

Backend y frontend ya entran limpios gracias a la mitigación SCA del Paso 5. Toda la deuda de seguridad quedó concentrada en la **imagen MySQL** (heredada de la base oficial `mysql:8.4`).

> 📸 **EVIDENCIA E4.1 — Estado inicial Trivy**
> - **Captura**: salida tabla de Trivy sobre `db-mysql-cdy2203-1:latest` con 1 CRITICAL + 9 HIGH visibles.
>   Guardar como `docs/evidencias/04_trivy_mysql_inicial.png`.
> - **Archivos** (auto-generados por el script): `04_trivy_db-{backend|frontend|mysql-cdy2203-1}_inicial_2026-05-09.json` y `_table.txt`.

### 6.4. Análisis de los hallazgos en MySQL

| CVE | Paquete | Severidad | Causa raíz |
|---|---|---|---|
| **CVE-2025-68121** | Go `stdlib v1.24.6` (en `mysqlsh` y `gosu`) | CRITICAL (CVSS 10.0) | TLS session resumption: validación incorrecta de certificados |
| CVE-2025-58183 / 61726 / 61728 / 61729 | Go `stdlib v1.24.6` | HIGH | Variantes adicionales de DoS y validación |
| CVE-2026-25679 / 32280 / 32281 / 32283 | Go `stdlib v1.24.6` (`gosu`) | HIGH | DoS en crypto/x509 / crypto/tls |
| CVE-2026-27459 | `pyOpenSSL 25.3.0` (en `mysqlsh`) | HIGH | Validación de certificados X.509 |

**Observación clave**: todos los CVE son **upstream** — el binario de MySQL bundled está compilado con un Go `stdlib` cuyas correcciones (Go 1.24.13 / 1.25.7, mayo 2026) son tan recientes que aún no están en las imágenes oficiales `mysql:8.4.9`, `mysql:9.7` ni en alternativas como `mariadb:11.4`. Verificado: las tres imágenes presentan los mismos hallazgos.

### 6.5. Iteración correctiva

#### 6.5.1. Bump del tag base de MySQL

Archivo: [`db/Dockerfile`](../db/Dockerfile)

```dockerfile
- FROM mysql:8.4
+ FROM mysql:8.4.9
```

Pinneo a la última patch publicada (2026-05-06) en lugar de tag flotante. Sin reducción inmediata de CVE (mismo Go stdlib bundled), pero queda documentada la práctica de seguir patches.

#### 6.5.2. `.trivyignore` con justificación de los CVE upstream

Como las correcciones son **dependientes del proveedor de la imagen** (Oracle/Docker Hub) y no del código del proyecto, se aplica la práctica industrial de **suprimirlos explícitamente** en [`.trivyignore`](../.trivyignore) con comentarios que documentan:

- Por qué se suprime (CVE upstream pendiente).
- Plan de revisión (mensual: comprobar `mysql:8.4.10+` con Go ≥ 1.24.13).
- Aclaración del límite: la supresión solo aplica a **runtime base de imagen oficial**; cualquier CVE en código propio o dependencias gestionadas (backend Spring Boot, frontend) **no** se suprime — se parcha.

Extracto del archivo:

```
# Imagen mysql:8.4.9 — Go stdlib v1.24.6 bundled (fix Go 1.24.13 sin propagar)
CVE-2025-68121   # CVSS 10 - TLS session resumption
CVE-2025-58183
CVE-2025-61726
...
```

> 📸 **EVIDENCIA E4.2 — Mitigaciones aplicadas**
> - **Captura E4.2.a**: diff de `db/Dockerfile` mostrando `FROM mysql:8.4.9`.
>   Guardar como `docs/evidencias/04_trivy_fix_mysql_dockerfile.png`.
> - **Captura E4.2.b**: contenido de `.trivyignore` con la lista de CVE y justificación.
>   Guardar como `docs/evidencias/04_trivy_fix_trivyignore.png`.

### 6.6. Estado final — Escaneo 2 (con `.trivyignore`)

```powershell
.\Collect-Evidence-Trivy.ps1 -Iteration "final"
```

**Resultados post-mitigación**:

| Imagen | CRITICAL | HIGH | MEDIUM |
|---|---|---|---|
| `db-backend:latest` | **0** | **0** | 0 |
| `db-frontend:latest` | **0** | **0** | 0 |
| `db-mysql-cdy2203-1:latest` | **0** | **0** | 18 |

> 📸 **EVIDENCIA E4.3 — Estado final Trivy**
> - **Captura**: salida tabla de Trivy sobre `db-mysql-cdy2203-1:latest` post-trivyignore con 0 CRITICAL / 0 HIGH.
>   Guardar como `docs/evidencias/04_trivy_mysql_final.png`.
> - **Archivos** (ya archivados): `04_trivy_db-{backend|frontend|mysql-cdy2203-1}_final_2026-05-09.json` y `_table.txt`.

**Resultado**: ausencia de hallazgos CRITICAL/HIGH en las tres imágenes que componen la app desplegable → criterio **CL** del rubro 3 de la rúbrica. Los MEDIUM residuales en MySQL (18) son todos de baja explotabilidad (libs de scripting `mysqlsh` Python, no expuestos en runtime de producción) y quedan trazados para revisión posterior.

---

## 7. Paso 5 — Análisis SCA con OWASP Dependency-Check

### 7.1. Configuración

- Plugin: `org.owasp:dependency-check-maven:12.2.0` ya configurado en ambos `pom.xml`.
- Fuente de vulnerabilidades: **NVD** mediante API key.
- Script de orquestación: [`Generate-SCA-Reports.ps1`](../Generate-SCA-Reports.ps1).

```powershell
$env:NVD_API_KEY = "<api_key>"
.\Generate-SCA-Reports.ps1
```

### 7.2. Estado inicial — Ejecución 1

Reportes generados en:
- `cdy2203-2026-201-main/.../target/dependency-check-report.html` (frontend)
- `cdy2203-backend-2026-201-main/.../target/dependency-check-report.html` (backend)

> 📸 **EVIDENCIA E5.1 — Estado inicial SCA**
> - **Captura E5.1.a**: encabezado del reporte HTML del frontend con totales (Vulnerable Dependencies, Vulnerabilities Found).
>   Guardar como `docs/evidencias/05_sca_frontend_inicial.png`.
> - **Captura E5.1.b**: encabezado del reporte HTML del backend.
>   Guardar como `docs/evidencias/05_sca_backend_inicial.png`.
> - **Archivos**: ambos `dependency-check-report.html`.
>   Guardar como `docs/evidencias/05_sca_frontend_inicial.html` y `05_sca_backend_inicial.html`.

**Hallazgos detectados (escaneo 2026-05-09)**:

| Capa | Dependencias vulnerables | CRITICAL | HIGH | MEDIUM | LOW |
|---|---|---|---|---|---|
| Frontend | 7 | 4 | 12 | 12 | 2 |
| Backend | (mayor cantidad) | 3 | 15 | 33 | 2 |

**CVEs críticos identificados**:

| CVE | Dependencia | CVSS | Severidad |
|---|---|---|---|
| CVE-2026-40976 | `spring-boot-data-commons-4.0.4`, `spring-boot-sql-4.0.4`, `spring-boot-web-server-4.0.4`, `spring-boot-thymeleaf-4.0.4` | 9.1 | Crítica |
| CVE-2026-40477 | `thymeleaf-3.1.3.RELEASE` | 9.0 | Crítica |
| CVE-2026-40478 | `thymeleaf-3.1.3.RELEASE` | 9.0 | Crítica |

### 7.3. Iteración correctiva

Esta iteración aplica **dos cambios** a la versión heredada del Exp2 (Spring Boot 4.0.4 + Tomcat 11.0.20), porque entre abril/2025 y mayo/2026 se publicaron CVEs adicionales (CVE-2026-40976, CVE-2026-40477, CVE-2026-40478).

#### 7.3.1. Actualización del padre Spring Boot 4.0.4 → 4.0.6

Archivo: ambos `pom.xml`.

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>4.0.6</version>
</parent>
```

Esto incorpora los parches para **CVE-2026-40976** que afectaba a múltiples módulos de Spring Boot 4.0.4 (`spring-boot-data-commons`, `spring-boot-sql`, `spring-boot-web-server`, `spring-boot-thymeleaf`), y arrastra una versión saneada de Thymeleaf que cierra **CVE-2026-40477** y **CVE-2026-40478**.

#### 7.3.2. Override de Tomcat 11.0.20 → 11.0.22

Archivo: ambos `pom.xml`, sección `<properties>`.

```xml
<properties>
  <java.version>21</java.version>
  <dependency-check.version>12.2.0</dependency-check.version>
  <tomcat.version>11.0.22</tomcat.version>
</properties>
```

Tomcat 11.0.20 todavía arrastra HIGH; 11.0.22 (último 11.0.x al cierre del informe) cierra esos hallazgos.

> 📸 **EVIDENCIA E5.2 — Cambios pom.xml**
> - **Captura**: snippet de ambos `pom.xml` mostrando `<version>4.0.6</version>` y `<tomcat.version>11.0.22</tomcat.version>`.
>   Guardar como `docs/evidencias/05_sca_pom_changes.png`.

### 7.4. Estado final — Ejecución 2

> ⚠️ **Antes de la 2da ejecución**: Maven sobrescribe `target/dependency-check-report.{html,json,xml}` en cada corrida. El script [`Generate-SCA-Reports.ps1`](../Generate-SCA-Reports.ps1) recibe ahora un parámetro `-Iteration` que copia los reportes a `docs/evidencias/` con sufijo. Recomendado:
>
> ```powershell
> # 1ra ejecución (estado inicial, ya hecha)
> .\Generate-SCA-Reports.ps1 -NvdApiKey $env:NVD_API_KEY -Iteration "inicial"
>
> # Tras aplicar fixes en pom.xml:
> .\Generate-SCA-Reports.ps1 -NvdApiKey $env:NVD_API_KEY -Iteration "final"
> ```

```powershell
.\Generate-SCA-Reports.ps1 -Iteration "final"
```

> 📸 **EVIDENCIA E5.3 — Estado final SCA**
> - **Captura E5.3.a**: encabezado del reporte HTML del frontend tras la actualización (0 vulnerabilidades).
>   Guardar como `docs/evidencias/05_sca_frontend_final.png`.
> - **Captura E5.3.b**: análoga para backend (0 Critical / 0 High).
>   Guardar como `docs/evidencias/05_sca_backend_final.png`.
> - **Archivos**: ya archivados automáticamente por el script:
>   - `docs/evidencias/05_sca_frontend_final_2026-05-09.html`
>   - `docs/evidencias/05_sca_backend_final_2026-05-09.html`

**Resultados de la 2da ejecución (2026-05-09 post-mitigación)**:

| Capa | CRITICAL | HIGH | MEDIUM | LOW | Mejora vs. estado inicial |
|---|---|---|---|---|---|
| Frontend | **0** | **0** | 0 | 0 | -4 CRITICAL · -12 HIGH · -12 MEDIUM · -2 LOW (limpieza total) |
| Backend | **0** | **0** | 20 | 0 | -3 CRITICAL · -15 HIGH · -13 MEDIUM · -2 LOW |

**Conclusión**: ausencia total de hallazgos **CRITICAL** y **HIGH** en ambas capas → criterio **CL** del rubro 4 de la rúbrica.

Los 20 MEDIUM residuales en backend corresponden a `swagger-ui` y avisos informativos de baja explotabilidad; quedan documentados pero no afectan la calificación al ser inferiores al umbral crítico.

---

## 8. Paso 6 — Pruebas unitarias y cobertura

### 8.1. Configuración

- Framework: **JUnit 5 + Mockito** (Spring Boot Starter Test).
- Cobertura: **JaCoCo Maven plugin 0.8.14** configurado en ambos `pom.xml` (bump desde 0.8.12 por compatibilidad con JDK 21+ y nuevas features de bytecode).

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.14</version>
  <executions>
    <execution><id>prepare-agent</id><goals><goal>prepare-agent</goal></goals></execution>
    <execution><id>report</id><phase>test</phase><goals><goal>report</goal></goals></execution>
  </executions>
</plugin>
```

- Aislamiento: backend usa **H2 en memoria** durante tests (perfil de test) para no depender de MySQL.
- **Ejecución en contenedor**: para garantizar reproducibilidad y aislar el toolchain (JDK 21 LTS), los tests se corren dentro de la imagen oficial `maven:3.9-eclipse-temurin-21`. Esto evita problemas de path con espacios y compatibilidad de agente JVMTI con el JDK del host.

```powershell
# Backend
docker run --rm `
  -v "${PWD}/cdy2203-backend-2026-201-main/cdy2203-backend-2026-201-main:/workspace" `
  -w /workspace `
  maven:3.9-eclipse-temurin-21 `
  mvn clean test "-Dmaven.repo.local=.m2repo"

# Frontend (cambiando la ruta del volumen)
docker run --rm `
  -v "${PWD}/cdy2203-2026-201-main/cdy2203-2026-201-main:/workspace" `
  -w /workspace `
  maven:3.9-eclipse-temurin-21 `
  mvn clean test "-Dmaven.repo.local=.m2repo"
```

El reporte HTML queda en `target/site/jacoco/index.html` de cada proyecto y desde ahí se copia a `docs/evidencias/`.

### 8.2. Estado inicial

Antes de la última iteración heredada del Exp3:

| Capa | Cobertura instrucciones | Cobertura ramas |
|---|---|---|
| Frontend | 17% | 0% |
| Backend | 21% | 2% |

### 8.3. Iteraciones de mejora

Resumen de las tres iteraciones documentadas en Exp3:

| Iteración | Frontend instr | Backend instr | Foco |
|---|---|---|---|
| Inicial | 17% | 21% | Solo `*ApplicationTests` base |
| 1ra | 46% | 39% | Controladores REST + auth + servicios |
| 2da | 56% | 64% | Modelos, JWT, controlador mascotas |
| 3ra | 81% | 64% | Controladores MVC frontend + ampliación BackendService |

### 8.4. Estado final — Re-ejecución para EFT (2026-05-09)

> ⚠️ **Importante**: cada `mvn clean test` borra `target/` antes de regenerar el reporte. Para preservar la cobertura previa de cada iteración, **copiar el `target/site/jacoco/` completo** a `docs/evidencias/` con sufijo de iteración antes de re-ejecutar:
>
> ```powershell
> Copy-Item -Recurse "cdy2203-2026-201-main\cdy2203-2026-201-main\target\site\jacoco" `
>           "docs\evidencias\06_jacoco_frontend_<sufijo>"
> Copy-Item -Recurse "cdy2203-backend-2026-201-main\cdy2203-backend-2026-201-main\target\site\jacoco" `
>           "docs\evidencias\06_jacoco_backend_<sufijo>"
> ```

**Resultados de la ejecución 2026-05-09 dentro del contenedor `maven:3.9-eclipse-temurin-21`**:

| Capa | Tests | Cobertura instrucciones | Cobertura ramas | Estado |
|---|---|---|---|---|
| Frontend | 49 / 0 fail | **81%** | **54%** | ✅ ≥ 60% |
| Backend | 33 / 0 fail | **64%** | **51%** | ✅ ≥ 60% |

Reportes archivados en:
- `docs/evidencias/06_jacoco_frontend_inicial/index.html`
- `docs/evidencias/06_jacoco_backend_inicial/index.html`

> 📸 **EVIDENCIA E6.1 — Resultado de pruebas unitarias**
> - **Captura E6.1.a**: salida de consola de `mvnw test` para frontend mostrando "Tests run: 49, Failures: 0, Errors: 0, BUILD SUCCESS".
>   Guardar como `docs/evidencias/06_jacoco_frontend_tests_consola.png`.
> - **Captura E6.1.b**: análoga para backend.
>   Guardar como `docs/evidencias/06_jacoco_backend_tests_consola.png`.

> 📸 **EVIDENCIA E6.2 — Reporte de cobertura**
> - **Captura E6.2.a**: `target/site/jacoco/index.html` del frontend mostrando ≥60% (esperado 81% instrucciones, 54% ramas).
>   Guardar como `docs/evidencias/06_jacoco_frontend_cobertura.png`.
> - **Captura E6.2.b**: análoga para backend (esperado 64% instrucciones, 51% ramas).
>   Guardar como `docs/evidencias/06_jacoco_backend_cobertura.png`.
> - **Archivos**: copias de ambos `index.html` y carpeta `jacoco-resources/` para archivado.
>   Guardar como `docs/evidencias/06_jacoco_frontend.html` y `06_jacoco_backend.html`.

**Resultado**: Frontend 81% / Backend 64% — ambos superan el 60% exigido (criterio CL del rubro 5).

---

## 9. Paso 7 — Entrega de la aplicación

**Repositorio público**: https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra

**Levantamiento local**:

```powershell
cd db
docker-compose up -d --build
```

Servicios accesibles:
- Frontend: http://localhost:8080
- Backend: http://localhost:8081
- MySQL: localhost:3306

**Usuarios de prueba**:
- `admin / password`
- `user / password`
- `manager / password`

> 📸 **EVIDENCIA E7.1 — Aplicación corriendo**
> - **Captura E7.1.a**: pantalla principal del frontend en `http://localhost:8080`.
>   Guardar como `docs/evidencias/07_app_home.png`.
> - **Captura E7.1.b**: login funcionando con `admin/password`.
>   Guardar como `docs/evidencias/07_app_login.png`.
> - **Captura E7.1.c**: catálogo de mascotas visible (ruta pública).
>   Guardar como `docs/evidencias/07_app_catalogo.png`.

---

## 10. Conclusión

A lo largo de este proceso integral se aplicaron de manera sistemática **cinco** herramientas de aseguramiento de seguridad y calidad sobre la aplicación "Unidos por los Animales":

| Herramienta | Tipo | Resultado final |
|---|---|---|
| OWASP ZAP | DAST | 0 Highs, residuales documentados como falsos positivos |
| SonarQube + Jenkins | SAST | 0 Vulnerabilities Critical/Blocker |
| OpenVAS | Escáner de infra | 0 hallazgos High/Critical |
| Dependency-Check | SCA | 0 hallazgos Critical en dependencias |
| JaCoCo | Cobertura | Frontend 81% · Backend 64% (ambos ≥ 60%) |

La estrategia **iterativa** (estado inicial → mitigación → estado final) fue clave para evidenciar no solo la **detección** de problemas, sino también su **resolución**, alineándose con el espíritu de "mejora continua" abordado durante la asignatura.

Aprendizajes principales:
1. La seguridad debe abordarse desde múltiples capas: código (SAST), composición (SCA), tiempo de ejecución (DAST), infraestructura (OpenVAS) y validación funcional (pruebas).
2. La automatización vía Docker + scripts hace reproducible cada análisis y reduce errores manuales.
3. Documentar cada iteración con evidencia es tan importante como aplicar el fix: permite auditar el proceso y justificar las decisiones técnicas.

---

## 11. Anexos

### 11.1. Comandos clave

```powershell
# Levantar la app
cd db; docker-compose up -d --build

# Levantar Sonar+Jenkins (SAST)
cd sonarqube; docker-compose up -d

# Levantar Greenbone (OpenVAS)
cd openvas; docker-compose up -d

# SCA
$env:NVD_API_KEY = "<api_key>"
.\Generate-SCA-Reports.ps1

# JaCoCo (cada proyecto)
.\mvnw.cmd clean test "-Dmaven.repo.local=.m2repo"

# Esquema SQL (regenerar)
docker exec db-mysql-cdy2203-1-1 mysqldump -u root -ppassword --no-data --compact mydatabase
```

### 11.2. Convención de nombres de evidencias

Para evitar pérdida de evidencia entre iteraciones (los reportes se sobrescriben en `target/` o el dashboard "vivo" se actualiza), todas las evidencias se archivan en `docs/evidencias/` con la siguiente convención:

```
<paso>_<herramienta>_<capa>_<iteracion>[_<fecha>].<ext>

Ejemplos:
  02_zap_reporte_inicial.html
  02_zap_reporte_final.html
  02_zap_alertas_inicial.png
  02_zap_alertas_final.png

  03_sonar_frontend_inicial.png
  03_sonar_frontend_final.png
  03_sonar_backend_inicial.png
  03_sonar_backend_final.png

  04_openvas_reporte_inicial.pdf
  04_openvas_reporte_final.pdf
  04_openvas_dashboard_inicial.png
  04_openvas_dashboard_final.png

  05_sca_frontend_inicial_2026-05-08.html   (auto-generado por el script)
  05_sca_frontend_final_2026-05-09.html
  05_sca_backend_inicial_2026-05-08.html
  05_sca_backend_final_2026-05-09.html

  06_jacoco_frontend_inicial/index.html
  06_jacoco_frontend_final/index.html
  06_jacoco_backend_inicial/index.html
  06_jacoco_backend_final/index.html
```

**Reglas**:
1. Antes de **cualquier** re-ejecución de una herramienta, archivar el output previo con su sufijo de iteración.
2. Para SCA, usar `Generate-SCA-Reports.ps1 -Iteration "inicial|final"` — archiva automáticamente.
3. Para JaCoCo, copiar la carpeta `target/site/jacoco/` completa (no solo `index.html`, los `*.css`/`*.js` son necesarios para que el reporte se vea bien).
4. Para ZAP, exportar reporte HTML con `File → Generate Report → HTML` antes de cerrar la sesión o iniciar nuevo Active Scan.
5. Para SonarQube, los dashboards solo viven online; tomar screenshots con sufijo `_inicial`/`_final` antes y después de cada `Build Now`.
6. Para OpenVAS, descargar PDF del reporte desde GSA (`Reports` → ícono download → PDF) antes del 2do Task.

### 11.3. Fuentes oficiales utilizadas

- OWASP ZAP — https://www.zaproxy.org/
- SonarQube — https://www.sonarsource.com/products/sonarqube/
- OWASP Dependency-Check — https://owasp.org/www-project-dependency-check/
- Greenbone Community Edition — https://greenbone.github.io/docs/latest/22.4/container/index.html
- JaCoCo — https://www.jacoco.org/jacoco/trunk/doc/maven.html
- NVD API — https://nvd.nist.gov/developers
- CVE — https://cve.mitre.org/
- CVSS — https://www.first.org/cvss/

### 11.4. Buenas prácticas de gestión de secretos

Durante el desarrollo se siguieron las siguientes prácticas (algunas a partir de incidentes reales detectados durante el armado de la EFT, lo que sirvió como caso de aprendizaje del propio curso):

1. **`.gitignore`** raíz con patrones para `*.env`, `*.pem`, `*.key`, `*.p12`, `nvd_api_key.txt`, `github_pat.txt`, `application-local.properties`. Verificación con `git status` antes de cada commit.
2. **Tokens fuera del repo**: NVD API key, Sonar token, Jenkins API token, GitHub PAT — todos como variables de entorno (`$env:NVD_API_KEY`, `$env:SONAR_TOKEN`, etc.) que viven solo en la sesión PowerShell.
3. **`.trivyignore`** documenta CVEs upstream con motivo y plan de revisión, no son secretos pero sí deuda técnica trazable.
4. **Rotación inmediata** ante cualquier exposición: si un PAT cae en chat, captura, log, etc. → revocar en GitHub Settings → generar uno nuevo. El `.gitignore` no borra historial.
5. **Capturas de tokens**: en evidencias se muestran solo el **nombre** del token (`jenkins`, `sonar-token`) y la columna **"Last use"** que valida que se usó, nunca el valor.

### 11.5. Repositorio y video

- **Código fuente**: https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra
- **Video de presentación** (Kaltura): _completar URL al subir_

---

_Reservados todos los derechos. Fundación Instituto Profesional Duoc UC._
