param(
    [Parameter(Mandatory=$true)][ValidateSet("inicial","final")][string]$Iteration,
    [string]$ReportId = "",
    [string]$Username = "admin",
    [string]$Password = "admin",
    [string]$GvmToolsContainer = "cdy2203-openvas-gvm-tools-1"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$evidenceDir = Join-Path $root "docs\evidencias"
if (-not (Test-Path $evidenceDir)) { New-Item -ItemType Directory -Path $evidenceDir -Force | Out-Null }

# Verificar contenedor disponible
try {
    docker ps --format "{{.Names}}" | Select-String $GvmToolsContainer | Out-Null
    if (-not $?) { throw "Contenedor $GvmToolsContainer no esta corriendo." }
} catch {
    Write-Host "ERR: Contenedor gvm-tools no encontrado. Verifica con: docker ps | findstr gvm-tools" -ForegroundColor Red
    Write-Host "Si no existe, levanta el stack OpenVAS: cd openvas; docker-compose up -d" -ForegroundColor Red
    exit 1
}

$stamp = Get-Date -Format "yyyy-MM-dd"
$prefix = "04_openvas_${Iteration}_${stamp}"

# Si no se especifica ReportId, listar reportes y tomar el ultimo
if (-not $ReportId) {
    Write-Host "ReportId no especificado, listando reportes disponibles..." -ForegroundColor Cyan
    $listXml = docker exec $GvmToolsContainer gvm-cli --gmp-username $Username --gmp-password $Password socket --xml "<get_reports/>" 2>&1
    $listOut = Join-Path $evidenceDir "${prefix}_reports_list.xml"
    $listXml | Out-File -FilePath $listOut -Encoding utf8
    Write-Host "[OK] Lista de reportes -> $listOut" -ForegroundColor Green

    # Extraer el ultimo report id
    try {
        [xml]$xml = $listXml
        $latest = $xml.SelectNodes("//report") | Sort-Object { $_.creation_time } -Descending | Select-Object -First 1
        if ($latest) {
            $ReportId = $latest.id
            Write-Host "ReportId detectado: $ReportId" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "No pude parsear la lista. Define -ReportId manualmente." -ForegroundColor Red
        exit 1
    }
}

if (-not $ReportId) {
    Write-Host "Sin ReportId; abortando." -ForegroundColor Red
    exit 1
}

# Format IDs estandar de Greenbone:
$formats = @{
    "xml" = "a994b278-1f62-11e1-96ac-406186ea4fc5"
    "pdf" = "c402cc3e-b531-11e1-9163-406186ea4fc5"
    "csv" = "c1645568-627a-11e3-a660-406186ea4fc5"
    "html" = "6c248850-1f62-11e1-b082-406186ea4fc5"
}

foreach ($fmt in @("xml","pdf","html")) {
    $fmtId = $formats[$fmt]
    $outFile = Join-Path $evidenceDir "${prefix}_report.${fmt}"
    Write-Host "Exportando reporte ${fmt}..." -ForegroundColor Cyan
    try {
        docker exec $GvmToolsContainer gvm-cli --gmp-username $Username --gmp-password $Password socket --xml "<get_reports report_id=`"$ReportId`" format_id=`"$fmtId`" details=`"1`"/>" 2>&1 | Out-File -FilePath $outFile -Encoding utf8
        $size = (Get-Item $outFile).Length
        Write-Host "  [OK] ${fmt} -> $outFile ($([math]::Round($size/1KB,1)) KB)" -ForegroundColor Green
    } catch {
        Write-Host "  [ERR] ${fmt}: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Resumen markdown legible
$summaryPath = Join-Path $evidenceDir "${prefix}_RESUMEN.md"
$xmlFile = Join-Path $evidenceDir "${prefix}_report.xml"
$lines = @("# Resumen OpenVAS - reporte $ReportId ($Iteration)", "", "Fecha: $stamp", "")
if (Test-Path $xmlFile) {
    try {
        [xml]$x = Get-Content $xmlFile -Raw
        $results = $x.SelectNodes("//result")
        $high = ($results | Where-Object { [double]$_.severity -ge 7.0 -and [double]$_.severity -lt 9.0 }).Count
        $crit = ($results | Where-Object { [double]$_.severity -ge 9.0 }).Count
        $med = ($results | Where-Object { [double]$_.severity -ge 4.0 -and [double]$_.severity -lt 7.0 }).Count
        $low = ($results | Where-Object { [double]$_.severity -gt 0 -and [double]$_.severity -lt 4.0 }).Count
        $lines += "## Severidades (CVSSv2)"
        $lines += "- Critical (>=9.0): $crit"
        $lines += "- High (7.0-8.9): $high"
        $lines += "- Medium (4.0-6.9): $med"
        $lines += "- Low (0.1-3.9): $low"
        $lines += ""
    } catch {
        $lines += "(No fue posible parsear $xmlFile)"
    }
}
$lines | Out-File -FilePath $summaryPath -Encoding utf8
Write-Host "[SUMMARY] $summaryPath" -ForegroundColor Yellow

Write-Host ""
Write-Host "Evidencias OpenVAS guardadas en $evidenceDir" -ForegroundColor Green
Write-Host "Recordatorio: capturar tambien screenshot del dashboard como 04_openvas_dashboard_${Iteration}.png" -ForegroundColor DarkYellow