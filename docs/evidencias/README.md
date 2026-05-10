# Carpeta de evidencias - EFT CDY2203

Esta carpeta contiene **todas las evidencias** que se citan desde [`docs/INFORME_EFT.md`](../INFORME_EFT.md).
Cada archivo sigue la convencion:

```
<paso>_<herramienta>_<capa-o-tema>_<iteracion>[_<fecha>].<ext>
```

Sufijos de iteracion: `_inicial`, `_final` (a veces tambien `_iter1`, `_iter2`).

---

## Indice por paso

### Paso 1 - Entrega del codigo

| Archivo | Origen |
|---|---|
| `01_repo_estructura.png` | Captura del repo publico en GitHub (`https://github.com/FcoXavierParra/CDY2203_EFT_Francisco_Parra`) |
| `01_schema_sql.png` | Captura de `db/schema.sql` abierto + `SHOW TABLES;` en MySQL |
| `01_tests_estructura.png` | Captura del directorio `src/test/java` |

### Paso 2 - ZAP (DAST)

| Archivo | Origen |
|---|---|
| `02_zap_alertas_inicial.png` | Panel de Alertas tras el primer Active Scan |
| `02_zap_reporte_inicial.html` | Reporte HTML exportado por ZAP (Reports -> Generate HTML Report) |
| `02_zap_alertas_final.png` | Panel de Alertas post-mitigacion |
| `02_zap_reporte_final.html` | Reporte HTML del 2do escaneo |
| `02_zap_fix_*_codigo.png` | Snippets de codigo modificado para mitigar Highs/Mediums |

### Paso 3 - SAST con Sonar + Jenkins

| Archivo | Origen |
|---|---|
| `03_sonar_jenkins_running.png` | `docker ps` con jenkins+sonarqube corriendo |
| `03_sonar_token_validacion.png` | Validacion del token via API (`/api/authentication/validate`) |
| `03_sonar_token_generado.png` | Pantalla "My Account -> Security" con el token jenkins listado |
| `03_jenkins_unlock.png` | Pantalla "Unlock Jenkins" |
| `03_jenkins_plugins_install.png` | "Customize Jenkins -> Install suggested plugins" |
| `03_jenkins_dashboard_inicial.png` | Dashboard Jenkins post-setup |
| `03_jenkins_plugin_*.png` | Captures del plugin SonarQube Scanner instalado |
| `03_jenkins_config_sonarserver.png` | Form "SonarQube servers" en Jenkins con URL+token |
| `03_jenkins_credenciales.png` | Credencial Secret text creada con ID `sonar-token` |
| `03_jenkins_scanner_tool.png` | Form "SonarQube Scanner installations" |
| `03_sonar_<capa>_inicial_<fecha>_*.json` | Auto-generado por `Collect-Evidence-Sonar.ps1`: metricas, issues, quality gate, hotspots |
| `03_sonar_<capa>_inicial_<fecha>_RESUMEN.md` | Resumen markdown legible |
| `03_jenkins_<job>_inicial_<fecha>_consoleLog.txt` | Log de build desde API Jenkins |
| `03_jenkins_<job>_inicial_<fecha>_buildInfo.json` | Metadata del build |
| `03_sonar_<capa>_final_*` y `03_jenkins_<job>_final_*` | Equivalentes tras mitigar criticos |

### Paso 4 - Trivy (escaner de vulnerabilidades)

| Archivo | Origen |
|---|---|
| `04_trivy_db-backend_final_<fecha>.json` | `Collect-Evidence-Trivy.ps1 -Iteration final` |
| `04_trivy_db-backend_final_<fecha>_table.txt` | Idem, formato tabla legible |
| `04_trivy_db-frontend_final_<fecha>.{json,table}` | idem frontend |
| `04_trivy_db-mysql-cdy2203-1_final_<fecha>.{json,table}` | idem MySQL (post-bump 8.4.9 + .trivyignore) |
| `04_trivy_mysql_inicial.png` | (opcional) tabla con 1 CRIT/9 HIGH antes de aplicar `.trivyignore` |
| `04_trivy_mysql_final.png` | Tabla con 0/0 post `.trivyignore` |
| `04_trivy_fix_mysql_dockerfile.png` | Diff `db/Dockerfile` mostrando bump a `mysql:8.4.9` |
| `04_trivy_fix_trivyignore.png` | Contenido de `.trivyignore` con CVEs upstream documentados |

### Paso 5 - SCA (OWASP Dependency-Check)

| Archivo | Origen |
|---|---|
| `05_sca_<capa>_inicial_<fecha>.{html,json,xml}` | `Generate-SCA-Reports.ps1 -Iteration inicial` |
| `05_sca_<capa>_final_<fecha>.{html,json,xml}` | Idem `-Iteration final` post pom updates |
| `05_sca_pom_changes.png` | Diff de `pom.xml` mostrando Spring Boot 4.0.6 + tomcat 11.0.22 |

### Paso 6 - JaCoCo (cobertura)

| Archivo | Origen |
|---|---|
| `06_jacoco_frontend_inicial/index.html` y carpeta completa | Reporte JaCoCo del frontend (81% instr / 54% ramas) |
| `06_jacoco_backend_inicial/index.html` y carpeta completa | Reporte JaCoCo del backend (64% instr / 51% ramas) |
| `06_jacoco_<capa>_tests_consola.png` | Captura de `mvn test` mostrando "Tests run: N, BUILD SUCCESS" |
| `06_jacoco_<capa>_cobertura.png` | Captura del index.html del reporte |

### Paso 7 - Entrega de la aplicacion

| Archivo | Origen |
|---|---|
| `07_app_docker_ps.png` | `docker ps` con los 3 contenedores up |
| `07_app_home.png` | Frontend en `http://localhost:8080` |
| `07_app_login.png` | Login exitoso con admin/password |
| `07_app_catalogo.png` | Catalogo de mascotas (8 pets seedeadas) |

---

## Reglas para no perder evidencia entre iteraciones

1. **Antes** de re-ejecutar cualquier herramienta, verificar que la evidencia previa este archivada con su sufijo (`_inicial`).
2. Para SCA: usar `Generate-SCA-Reports.ps1 -Iteration "inicial|final"` que archiva automaticamente.
3. Para Trivy: usar `Collect-Evidence-Trivy.ps1 -Iteration "inicial|final"`.
4. Para Sonar: usar `Collect-Evidence-Sonar.ps1` con `$env:SONAR_TOKEN` seteado.
5. Para Jenkins: usar `Collect-Evidence-Jenkins.ps1` con `$env:JENKINS_USER` + `$env:JENKINS_TOKEN` seteados.
6. Para JaCoCo: copiar `target/site/jacoco/` con `Copy-Item -Recurse` antes de re-ejecutar.
7. Para ZAP: exportar HTML desde la UI (`Reports -> Generate Report`) antes de cerrar la sesion.

## Buenas practicas de seguridad

- **Nunca** commitear tokens, PATs, claves, passwords. El `.gitignore` raiz protege patrones comunes (`*.env`, `*.pem`, `*.key`, `*.p12`, `nvd_api_key.txt`, `github_pat.txt`).
- **Nunca** compartir tokens en chat, issues, foros publicos. Si se filtra: revocar inmediatamente y regenerar.
- Las screenshots de tokens deben mostrar solo el **nombre** (ej. `jenkins`) y el **"Last use"**, NO el valor del token.