param(
    [Parameter(Mandatory=$true)][ValidateSet("inicial","final")][string]$Iteration,
    [string]$SonarUrl = "http://localhost:9000",
    [string]$Token = $env:SONAR_TOKEN,
    [string[]]$Projects = @("cdy2203-frontend", "cdy2203-backend")
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$evidenceDir = Join-Path $root "docs\evidencias"
if (-not (Test-Path $evidenceDir)) { New-Item -ItemType Directory -Path $evidenceDir -Force | Out-Null }

if (-not $Token) {
    Write-Warning "SONAR_TOKEN no esta definida. Para autenticarse usar: `$env:SONAR_TOKEN='<token-jenkins-o-personal>'"
    Write-Warning "Continuando sin auth (puede fallar si Force authentication esta activado en Sonar)."
}

$headers = @{}
if ($Token) {
    # Sonar usa Basic con token como username y password vacio
    $b64 = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${Token}:"))
    $headers["Authorization"] = "Basic $b64"
}

$stamp = Get-Date -Format "yyyy-MM-dd"

foreach ($proj in $Projects) {
    $capa = if ($proj -match "frontend") { "frontend" } else { "backend" }
    $prefix = "03_sonar_${capa}_${Iteration}_${stamp}"

    Write-Host ""
    Write-Host "===== $proj =====" -ForegroundColor Cyan

    $endpoints = @{
        "metrics"      = "$SonarUrl/api/measures/component?component=$proj&metricKeys=alert_status,bugs,vulnerabilities,security_hotspots,code_smells,coverage,duplicated_lines_density,reliability_rating,security_rating,sqale_rating,ncloc"
        "issues"       = "$SonarUrl/api/issues/search?componentKeys=$proj&ps=500"
        "qualitygate"  = "$SonarUrl/api/qualitygates/project_status?projectKey=$proj"
        "hotspots"     = "$SonarUrl/api/hotspots/search?projectKey=$proj&ps=500"
    }

    foreach ($name in $endpoints.Keys) {
        $url = $endpoints[$name]
        $out = Join-Path $evidenceDir "${prefix}_${name}.json"
        try {
            $resp = Invoke-WebRequest -Uri $url -Headers $headers -UseBasicParsing -TimeoutSec 30
            $resp.Content | Out-File -FilePath $out -Encoding utf8
            Write-Host "  [OK] $name -> $out" -ForegroundColor Green
        } catch {
            Write-Host "  [ERR] $name -> $($_.Exception.Message)" -ForegroundColor Red
        }
    }

    # Resumen markdown legible
    $summaryPath = Join-Path $evidenceDir "${prefix}_RESUMEN.md"
    $metricsFile = Join-Path $evidenceDir "${prefix}_metrics.json"
    $qgFile = Join-Path $evidenceDir "${prefix}_qualitygate.json"
    $issuesFile = Join-Path $evidenceDir "${prefix}_issues.json"

    $lines = @("# Resumen Sonar - $proj ($Iteration)", "", "Fecha: $stamp", "")

    if (Test-Path $metricsFile) {
        $m = (Get-Content $metricsFile -Raw | ConvertFrom-Json).component.measures
        $lines += "## Metricas"
        foreach ($measure in $m) { $lines += "- $($measure.metric): $($measure.value)" }
        $lines += ""
    }
    if (Test-Path $qgFile) {
        $qg = (Get-Content $qgFile -Raw | ConvertFrom-Json).projectStatus
        $lines += "## Quality Gate: $($qg.status)"
        $lines += ""
    }
    if (Test-Path $issuesFile) {
        $iss = (Get-Content $issuesFile -Raw | ConvertFrom-Json).issues
        $bySev = $iss | Group-Object severity | Sort-Object Count -Descending
        $byType = $iss | Group-Object type | Sort-Object Count -Descending
        $lines += "## Issues por severidad"
        foreach ($g in $bySev) { $lines += "- $($g.Name): $($g.Count)" }
        $lines += ""
        $lines += "## Issues por tipo"
        foreach ($g in $byType) { $lines += "- $($g.Name): $($g.Count)" }
    }

    $lines | Out-File -FilePath $summaryPath -Encoding utf8
    Write-Host "  [SUMMARY] $summaryPath" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Evidencias Sonar guardadas en $evidenceDir" -ForegroundColor Green
Write-Host "Recordatorio: capturar tambien screenshot del dashboard como 03_sonar_<capa>_${Iteration}.png" -ForegroundColor DarkYellow
