# Informe SCA - Paso 3

## 1. Introduccion

En esta actividad se aplico analisis de composicion de software (SCA) sobre las capas frontend y backend de la aplicacion desarrollada para la organizacion "Unidos por los Animales". El objetivo fue identificar dependencias de terceros con vulnerabilidades conocidas, priorizar los hallazgos criticos, aplicar medidas de mitigacion y verificar posteriormente la efectividad de los cambios mediante una reevaluacion.

La aplicacion evaluada esta compuesta por:

- Un backend desarrollado con Spring Boot, Spring Web, Spring Security, Spring Data JPA y MySQL.
- Un frontend desarrollado con Spring Boot, Spring Security y Thymeleaf.

## 2. Investigacion de herramientas SCA

### 2.1 OWASP Dependency-Check

- Nombre de la herramienta: OWASP Dependency-Check
- Organizacion o empresa que la mantiene: OWASP Foundation
- Tipo: Software Libre
- Principales caracteristicas:
  - Analiza dependencias de proyectos para detectar vulnerabilidades conocidas.
  - Soporta integracion con Maven, Gradle, CLI y pipelines CI/CD.
  - Genera reportes en formatos HTML, XML, JSON y otros.
  - Utiliza fuentes como NVD y otros analizadores por ecosistema.
- URL del sitio principal: https://owasp.org/www-project-dependency-check/

### 2.2 Snyk Open Source

- Nombre de la herramienta: Snyk Open Source
- Organizacion o empresa que la mantiene: Snyk
- Tipo: Comercial
- Principales caracteristicas:
  - Detecta vulnerabilidades y problemas de licenciamiento en dependencias open source.
  - Integra CLI, repositorios, pull requests e IDE.
  - Prioriza hallazgos y sugiere remediaciones.
  - Permite monitoreo continuo de proyectos.
- URL del sitio principal: https://docs.snyk.io/scan-with-snyk/snyk-open-source

### 2.3 Mend SCA

- Nombre de la herramienta: Mend SCA
- Organizacion o empresa que la mantiene: Mend.io
- Tipo: Comercial
- Principales caracteristicas:
  - Gestiona riesgos de seguridad y cumplimiento asociados a componentes open source.
  - Incorpora visibilidad de dependencias directas y transitivas.
  - Puede generar SBOM y apoyar procesos de priorizacion.
  - Se integra con entornos de desarrollo y pipelines.
- URL del sitio principal: https://www.mend.io/sca/

## 3. Herramienta seleccionada

La herramienta seleccionada fue **OWASP Dependency-Check**.

### 3.1 Justificacion de la seleccion

Se selecciono OWASP Dependency-Check por las siguientes razones:

- Es una herramienta de software libre, por lo que no agrega costo al desarrollo academico.
- Se integra de forma natural con proyectos Java y Maven, que es el stack utilizado en frontend y backend.
- Genera evidencia clara y exportable en reportes, lo que facilita respaldar la entrega.
- Es mantenida por OWASP, organismo ampliamente reconocido en seguridad de aplicaciones.

## 4. Evidencia de ejecucion sobre frontend y backend

### 4.1 Proyecto frontend

El frontend corresponde a una aplicacion Spring Boot con Spring Security y Thymeleaf. Sus dependencias se definen en el archivo `pom.xml`.

Ruta del proyecto:

- `cdy2203-2026-201-main/cdy2203-2026-201-main`

Elementos relevantes observados:

- Uso de Spring Boot y Thymeleaf.
- Seguridad de URLs en la configuracion `WebSecurityConfig`.
- Usuarios configurados para autenticacion.

Resultado de la primera ejecucion:

- Se genero correctamente el reporte SCA para la capa frontend.
- El reporte identifico 14 vulnerabilidades.
- En el resultado se observaron vulnerabilidades de severidad `CRITICAL`, `HIGH`, `MEDIUM` y `LOW`.
- Los hallazgos mas relevantes afectaron dependencias del ecosistema Spring Boot, Jackson, Spring Security y Tomcat embebido.

Principales hallazgos identificados:

- `jackson-core-3.0.4.jar` asociado a `CVE-2026-29062`.
- `jackson-databind-3.0.4.jar` asociado a `CVE-2026-29062`.
- `spring-boot-thymeleaf-4.0.3.jar` asociado a `CVE-2026-22731`.
- `spring-boot-web-server-4.0.3.jar` asociado a `CVE-2026-22731`.
- `spring-security-core-7.0.3.jar` asociado a `CVE-2026-22732`.
- `tomcat-embed-core-11.0.18.jar` asociado a multiples CVE.

Capturas que debes insertar aqui:

- `[CAPTURA 1]` Comando o script ejecutado para analizar el frontend.
- `[CAPTURA 2]` Resumen principal del reporte HTML del frontend donde se vea el total de vulnerabilidades encontradas.
- `[CAPTURA 3]` Tabla o detalle del reporte frontend donde se aprecien las severidades `CRITICAL` y `HIGH`.

### 4.2 Proyecto backend

El backend corresponde a una aplicacion Spring Boot con Spring Web, Spring Security, Spring Data JPA y MySQL Driver. Sus dependencias tambien se definen en `pom.xml`.

Ruta del proyecto:

- `cdy2203-backend-2026-201-main/cdy2203-backend-2026-201-main`

Elementos relevantes observados:

- API de login con JWT.
- Acceso a datos mediante repositorios JPA.
- Conexion a MySQL mediante `application.properties`.

Resultado de la primera ejecucion:

- Se genero correctamente el reporte SCA para la capa backend.
- El reporte identifico 29 vulnerabilidades.
- En el resultado se observaron vulnerabilidades de severidad `CRITICAL`, `HIGH`, `MEDIUM` y `LOW`.
- Los hallazgos mas relevantes se concentraron en dependencias base y transitivas del stack Spring Boot, Spring Security, HikariCP, Jackson y Tomcat.

Capturas que debes insertar aqui:

- `[CAPTURA 4]` Comando o script ejecutado para analizar el backend.
- `[CAPTURA 5]` Resumen principal del reporte HTML del backend donde se vea el total de vulnerabilidades encontradas.
- `[CAPTURA 6]` Tabla o detalle del reporte backend donde se aprecien las severidades `CRITICAL` y `HIGH`.

## 5. Vulnerabilidades encontradas

En esta seccion se resumen los hallazgos principales del analisis SCA para ambas capas. A partir de la lectura de los reportes HTML generados por OWASP Dependency-Check, se observaron los siguientes resultados generales:

- Frontend: 14 vulnerabilidades detectadas.
- Backend: 29 vulnerabilidades detectadas.
- En ambas capas existen hallazgos de severidad critica, alta, media y baja.
- Las vulnerabilidades criticas y altas afectan principalmente bibliotecas base del stack de la aplicacion, por lo que la estrategia de correccion debe centrarse en la actualizacion de versiones en `pom.xml`.

### 5.1 Resumen inicial de hallazgos

| Capa | Dependencia | Version detectada | Vulnerabilidad | Severidad | Critica |
| --- | --- | --- | --- | --- | --- |
| Frontend | `jackson-core` | `3.0.4` | `CVE-2026-29062` | Critica | Si |
| Frontend | `jackson-databind` | `3.0.4` | `CVE-2026-29062` | Critica | Si |
| Frontend | `spring-boot-thymeleaf` | `4.0.3` | `CVE-2026-22731` | Alta | No |
| Frontend | `spring-boot-web-server` | `4.0.3` | `CVE-2026-22731` | Alta | No |
| Frontend | `spring-security-core` | `7.0.3` | `CVE-2026-22732` | Alta | No |
| Frontend | `tomcat-embed-core` | `11.0.18` | Multiples CVE | Alta/Critica | Si |
| Backend | Dependencias base y transitivas del stack Spring | Varias | Revisar reporte HTML/JSON | Critica/Alta/Media/Baja | Si, segun reporte |

### 5.2 Hallazgos criticos priorizados para mitigacion

| Capa | Dependencia | Version afectada | Vulnerabilidad | Severidad | Mitigacion propuesta |
| --- | --- | --- | --- | --- | --- |
| Frontend | `spring-security-core` | `7.0.3` | `CVE-2026-22732` | Critica | Actualizar Spring Boot de `4.0.3` a `4.0.4`, lo que incorpora Spring Security `7.0.4`, version corregida. |
| Frontend | `tomcat-embed-core` | `11.0.18` | Multiples CVE criticas, entre ellas `CVE-2026-29146` | Critica | Forzar `tomcat.version=11.0.20` en el `pom.xml`, ya que con Spring Boot `4.0.4` el reporte seguia detectando `11.0.18`. |
| Backend | `spring-security-core` | `7.0.3` | `CVE-2026-22732` | Critica | Actualizar Spring Boot de `4.0.3` a `4.0.4`, lo que incorpora Spring Security `7.0.4`, version corregida. |
| Backend | `tomcat-embed-core` | `11.0.18` | Multiples CVE criticas, entre ellas `CVE-2026-29146` | Critica | Forzar `tomcat.version=11.0.20` en el `pom.xml`, ya que con Spring Boot `4.0.4` el reporte seguia detectando `11.0.18`. |

Capturas que debes insertar aqui:

- `[CAPTURA 7]` Vista general del reporte frontend con el total de 14 vulnerabilidades.
- `[CAPTURA 8]` Vista general del reporte backend con el total de 29 vulnerabilidades.
- `[CAPTURA 9]` Fragmento del detalle donde se vean vulnerabilidades criticas del frontend.
- `[CAPTURA 10]` Fragmento del detalle donde se vean vulnerabilidades criticas del backend.

## 6. Correccion de vulnerabilidades criticas

Luego del analisis, se definio una estrategia de correccion en dos etapas. La primera consistio en actualizar la version padre de Spring Boot en ambas capas desde `4.0.3` a `4.0.4`, ya que los hallazgos mas relevantes estaban concentrados en dependencias transitivas administradas por este framework.

La razon de esta correccion es que los principales hallazgos criticos y altos se concentran en dependencias transitivas administradas por Spring Boot, especialmente:

- `spring-security-core:7.0.3`, afectada por `CVE-2026-22732`.
- `tomcat-embed-core:11.0.18`, afectada por multiples CVE criticas y altas.
- componentes Spring Boot `4.0.3`, asociados a `CVE-2026-22731`.

Con esta actualizacion coordinada se busco:

- incorporar Spring Security `7.0.4`, version corregida segun el aviso oficial del proyecto Spring.
- incorporar una version corregida de Spring Boot para el hallazgo `CVE-2026-22731`.
- arrastrar versiones mas recientes de Tomcat y Jackson desde el BOM administrado por Spring Boot.
- reducir el riesgo sin introducir overrides manuales innecesarios dependencia por dependencia.

Despues de la primera reevaluacion posterior al cambio a `Spring Boot 4.0.4`, se verifico que:

- los hallazgos asociados a `Spring Security 7.0.3` dejaron de aparecer, lo que indica mitigacion del hallazgo critico `CVE-2026-22732`.
- tambien dejaron de aparecer los hallazgos previos asociados a `Spring Boot 4.0.3` y `Jackson 3.0.4`.
- el hallazgo critico remanente quedo concentrado en `tomcat-embed-core 11.0.18`.

Debido a lo anterior, fue necesario un segundo ajuste complementario: fijar la propiedad `tomcat.version` en `11.0.20` en ambas capas, con el objetivo de abordar los hallazgos remanentes asociados a Tomcat embebido.

### 6.1 Evidencia de los cambios realizados

| Archivo | Cambio aplicado | Motivo |
| --- | --- | --- |
| `pom.xml` frontend | Se actualiza `spring-boot-starter-parent` de `4.0.3` a `4.0.4` | Mitigar hallazgos criticos y altos asociados a Spring Security, Spring Boot y Tomcat |
| `pom.xml` frontend | Se agrega `tomcat.version=11.0.20` | Mitigar CVE criticos remanentes en `tomcat-embed-core 11.0.18` |
| `pom.xml` backend | Se actualiza `spring-boot-starter-parent` de `4.0.3` a `4.0.4` | Mitigar hallazgos criticos y altos asociados a Spring Security, Spring Boot y Tomcat |
| `pom.xml` backend | Se agrega `tomcat.version=11.0.20` | Mitigar CVE criticos remanentes en `tomcat-embed-core 11.0.18` |
| `db/Dockerfile` | Se agrega archivo de construccion para MySQL | Cumplimiento de entregable |

Redaccion sugerida:

"Para la mitigacion de los hallazgos criticos se actualizo la version padre de Spring Boot en frontend y backend desde 4.0.3 a 4.0.4. Esta decision se justifico porque los hallazgos mas importantes no correspondian a una unica libreria declarada manualmente, sino a dependencias transitivas administradas por Spring Boot, como Spring Security y Tomcat embebido. Posteriormente, al detectar que `tomcat-embed-core 11.0.18` seguia presente en la reevaluacion, se agrego la propiedad `tomcat.version=11.0.20` en ambos proyectos para corregir especificamente los CVE criticos remanentes."

Capturas que debes insertar aqui:

- `[CAPTURA 11]` Fragmento del `pom.xml` frontend antes o despues de actualizar dependencias.
- `[CAPTURA 12]` Fragmento del `pom.xml` backend antes o despues de actualizar dependencias.

## 7. Reevaluacion posterior

Despues de aplicar las correcciones, se debe volver a ejecutar la herramienta seleccionada sobre frontend y backend para comparar el estado inicial con las reevaluaciones posteriores.

Comandos sugeridos para la reevaluacion:

```powershell
$env:NVD_API_KEY="AQUI_TU_API_KEY_REAL"
.\Generate-SCA-Reports.ps1
```

Para no perder la trazabilidad de las evidencias, se recomienda mover cada conjunto de reportes a carpetas separadas. En este caso se conservaron tres momentos:

- `antes`
- `despues_Reevaluacion1`
- reporte final en `target`

Ejemplo de respaldo de archivos:

```powershell
Move-Item ".\cdy2203-2026-201-main\cdy2203-2026-201-main\target\dependency-check-report.html" ".\cdy2203-2026-201-main\cdy2203-2026-201-main\target\front_dependency-check-report-despues-2026-17-04.html"
Move-Item ".\cdy2203-backend-2026-201-main\cdy2203-backend-2026-201-main\target\dependency-check-report.html" ".\cdy2203-backend-2026-201-main\cdy2203-backend-2026-201-main\target\back_dependency-check-report-despues-2026-17-04.html"
```

Redaccion sugerida:

"Luego de la actualizacion de dependencias y de la nueva ejecucion de OWASP Dependency-Check, los hallazgos criticos inicialmente detectados dejaron de aparecer en el reporte. Con ello se evidencia una mejora en la postura de seguridad de ambas capas de la aplicacion."

Si despues de la reevaluacion final permanecen hallazgos altos o medios, se debe indicar expresamente que los hallazgos criticos fueron mitigados, pero que aun existen riesgos residuales que requieren una mejora posterior.

### 7.1 Resultado de la primera reevaluacion

- Frontend: el reporte dejo de listar `spring-security-core 7.0.3`, `jackson-core 3.0.4`, `jackson-databind 3.0.4` y los componentes de Spring Boot `4.0.3`.
- Backend: el reporte tambien quedo concentrado en dependencias remanentes, principalmente `tomcat-embed-core 11.0.18` y componentes embebidos en `swagger-ui`.
- En ambas capas persistio `tomcat-embed-core 11.0.18`, por lo que se aplico una segunda correccion especifica fijando `tomcat.version=11.0.20`.

Capturas que debes insertar aqui:

- `[CAPTURA 15]` Segundo escaneo despues de actualizar a Spring Boot `4.0.4`, donde se vea que ya no aparece `spring-security-core 7.0.3`.
- `[CAPTURA 16]` Hallazgo remanente de `tomcat-embed-core 11.0.18`.
- `[CAPTURA 17]` Fragmento del `pom.xml` con la propiedad `tomcat.version=11.0.20`.

### 7.2 Resultado de la segunda reevaluacion

- Frontend: `spring-security-core 7.0.4` ya aparece como dependencia no vulnerable en el reporte.
- Frontend: `tomcat-embed-core` paso de `11.0.18` a `11.0.20`.
- Frontend: ya no se observan hallazgos criticos; los hallazgos remanentes asociados a Tomcat quedaron en severidad alta.
- Backend: `spring-security-core 7.0.4` tambien aparece como dependencia no vulnerable.
- Backend: `tomcat-embed-core` paso de `11.0.18` a `11.0.20`.
- Backend: persisten hallazgos asociados a `swagger-ui 4.18.2`, especificamente componentes que incluyen `DOMPurify 3.0.1`.
- Backend: al igual que en frontend, ya no se observan los hallazgos criticos iniciales asociados a `spring-security-core 7.0.3`.

Redaccion sugerida:

"En la segunda reevaluacion, realizada despues de fijar la propiedad `tomcat.version=11.0.20`, se comprobo que las vulnerabilidades criticas inicialmente detectadas en Spring Security dejaron de aparecer en ambas capas. Asimismo, se verifico el reemplazo de `tomcat-embed-core 11.0.18` por `11.0.20`. No obstante, OWASP Dependency-Check todavia reporto algunos hallazgos de severidad alta sobre Tomcat 11.0.20 y, en el backend, hallazgos asociados a recursos web incluidos en `swagger-ui 4.18.2` con `DOMPurify 3.0.1`. Por ello, se concluye que los hallazgos criticos fueron mitigados, aunque permanecen riesgos residuales altos que pueden ser abordados en una mejora posterior."

## 8. Evidencia de correccion de hallazgos criticos

Los principales cambios observables en el proyecto para respaldar la mejora de seguridad son los siguientes:

- Dependencias administradas en archivos `pom.xml`.
- Configuracion de proteccion de URLs en frontend y backend.
- Uso de JWT en backend para acceso a APIs privadas.
- Definicion de una imagen de base de datos mediante `db/Dockerfile`.

Tambien es importante dejar constancia de aspectos que aun pueden mejorarse:

- Las credenciales por defecto no deberian mantenerse en ambientes reales.
- La configuracion deberia externalizar secretos mediante variables de entorno.
- Se recomienda fijar versiones estables en la imagen de MySQL y evitar `latest`.

### 8.1 Balance de mitigacion logrado

- Hallazgos criticos mitigados: `spring-security-core 7.0.3` y dependencias Spring/Jackson antiguas observadas en la primera ejecucion.
- Hallazgos altos residuales en frontend: `tomcat-embed-core 11.0.20`.
- Hallazgos altos residuales en backend: `tomcat-embed-core 11.0.20` y recursos incluidos en `swagger-ui 4.18.2` relacionados con `DOMPurify 3.0.1`.

### 8.2 Mejora posterior recomendada

- evaluar una actualizacion de `springdoc-openapi-starter-webmvc-ui`, ya que el backend aun incluye recursos de `swagger-ui 4.18.2` que Dependency-Check reporta como vulnerables.

Capturas que debes insertar aqui:

- `[CAPTURA 13]` Comparacion entre primer y segundo escaneo del frontend.
- `[CAPTURA 14]` Comparacion entre primer y segundo escaneo del backend.
- `[CAPTURA 18]` Resultado final del frontend, donde se vea `spring-security-core 7.0.4` como no vulnerable y `tomcat-embed-core 11.0.20` como hallazgo residual alto.
- `[CAPTURA 19]` Resultado final del backend, donde se vea `spring-security-core 7.0.4` como no vulnerable y los hallazgos residuales en `tomcat-embed-core 11.0.20` y `swagger-ui`.

## 9. Conclusion

La aplicacion desarrollada cumple con la separacion de capas frontend y backend solicitada por la actividad, incorpora mecanismos de autenticacion y proteccion de rutas, y puede fortalecerse mediante el uso sistematico de herramientas SCA. La seleccion de OWASP Dependency-Check permitio obtener evidencia real sobre el estado de las dependencias utilizadas por ambas capas del sistema. En la primera ejecucion se detectaron 14 vulnerabilidades en frontend y 29 en backend, incluyendo hallazgos criticos y altos. Posteriormente, mediante una estrategia de mitigacion iterativa basada en la actualizacion a Spring Boot `4.0.4` y en la fijacion de `tomcat.version=11.0.20`, los hallazgos criticos iniciales dejaron de aparecer. La evaluacion final evidencia una mejora concreta en la postura de seguridad del proyecto, aunque permanecen algunos hallazgos altos residuales en Tomcat y en recursos de `swagger-ui` del backend, los cuales pueden ser tratados en una etapa posterior de endurecimiento.

## 10. Fuentes

- OWASP Dependency-Check: https://owasp.org/www-project-dependency-check/
- Snyk Open Source: https://docs.snyk.io/scan-with-snyk/snyk-open-source
- Mend SCA: https://www.mend.io/sca/
- Spring Security Advisory `CVE-2026-22732`: https://spring.io/security/cve-2026-22732
- Spring Boot Advisory `CVE-2026-22731`: https://spring.io/security/cve-2026-22731
- Apache Tomcat 11 Vulnerabilities: https://tomcat.apache.org/security-11
