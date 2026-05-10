# Paso 4 — OpenVAS / Greenbone Community Edition

Stack tomado literal de la guía Exp2_S5. Es **pesado** (≈10 contenedores y descarga de feeds NVT/SCAP/CERT que pueden tardar 15–30 min la primera vez).

## Pre-requisitos

- Docker Desktop con al menos 6 GB asignados.
- Stack de la app (`db/docker-compose.yml`) **levantado**: necesitamos un contenedor backend al cual escanear.

## 1. Levantar Greenbone

```powershell
cd openvas
docker-compose up -d
```

La primera vez:
- Los servicios `vulnerability-tests`, `notus-data`, `scap-data`, `cert-bund-data`, `dfn-cert-data`, `data-objects`, `report-formats`, `configure-openvas` se quedan en estado `Exited` después de descargar/preparar feeds — es **lo esperado**, son init containers.
- Los servicios persistentes que deben quedar `Running`: `gvmd`, `gsa`, `openvas`, `openvasd`, `ospd-openvas`, `redis-server`, `pg-gvm`.

Verificar:
```powershell
docker ps --filter name=cdy2203-openvas
```

## 2. Conectar Greenbone con el contenedor backend

La app corre en su propia red docker-compose (`db_default` o similar). Greenbone también. Para que el escáner llegue al backend hay que conectar **un puente**.

```powershell
# 1. Crear red puente
docker network create eft_scan_net

# 2. Conectar los escáneres de Greenbone a esa red
docker network connect eft_scan_net cdy2203-openvas-openvasd-1
docker network connect eft_scan_net cdy2203-openvas-ospd-openvas-1

# 3. Conectar el backend a la misma red
docker network connect eft_scan_net db-backend-1

# 4. Obtener la IP que el backend tiene en eft_scan_net
docker inspect db-backend-1 --format "{{range .NetworkSettings.Networks}}{{println .IPAddress}}{{end}}"
```

(El nombre exacto del contenedor backend depende del compose project name; revisar con `docker ps`.)

## 3. Acceder a Greenbone Security Assistant (GSA)

http://localhost:9392 — login con **admin / admin** la primera vez.

> Nota: el puerto está bindeado a `127.0.0.1:9392` (solo localhost) por seguridad.

## 4. Crear y ejecutar Task Wizard

1. Scans → Tasks
2. Click en el icono de varita mágica → **Task Wizard**
3. IP address or hostname: la IP obtenida en paso 2.4
4. **Start Scan**

Tiempo estimado: 10–20 min para el primer escaneo (depende de cuántos plugins NVT carga).

## 5. Revisar reporte y exportar

Scans → Reports → click en el reporte:
- Tab **Results**: lista de vulnerabilidades.
- Tab **CVEs** y **Hosts**: resumen.

Exportar PDF: en el reporte, ícono de descarga → "PDF" (o XML si se prefiere para anexar).

## 6. Iteración correctiva

1. Filtrar por Severity **High** y **Critical**.
2. Para cada hallazgo, leer "Solution" e implementar:
   - Si es config inseguro (TLS débil, headers faltantes): ajustar `application.properties` o filtros.
   - Si es servicio inseguro expuesto: cerrar puerto en `db/docker-compose.yml`.
   - Si es info disclosure: agregar headers `X-Content-Type-Options`, `Strict-Transport-Security`, etc.
3. Reconstruir backend (`docker-compose build backend`) y re-ejecutar el Task.
4. Capturar reporte **después** sin críticos.

## 7. Limpieza

```powershell
docker-compose down
# Si se quiere limpiar feeds/data tambien (libera ~5GB):
# docker-compose down -v
```
