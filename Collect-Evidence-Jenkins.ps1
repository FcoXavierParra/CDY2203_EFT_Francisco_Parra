param(
    [Parameter(Mandatory=$true)][ValidateSet("inicial","final")][string]$Iteration,
    [Parameter(Mandatory=$true)][string]$Job,
    [int]$BuildNumber = 0,
    [string]$JenkinsUrl = "http://localhost:8090",
    [string]$User = $env:JENKINS_USER,
    [string]$Token = $env:JENKINS_TOKEN
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$evidenceDir = Join-Path $root "docs\evidencias"
if (-not (Test-Path $evidenceDir)) { New-Item -ItemType Directory -Path $evidenceDir -Force | Out-Null }

if (-not $User -or -not $Token) {
    Write-Warning "JENKINS_USER o JENKINS_TOKEN no definidos."
    Write-Warning "Para auth: en Jenkins -> User -> Configure -> API Token -> Add new -> copiar y exportar como `$env:JENKINS_TOKEN."
    Write-Warning "Tambien `$env:JENKINS_USER='admin' (o el usuario que creaste)."
}

$headers = @{}
if ($User -and $Token) {
    $b64 = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${User}:${Token}"))
    $headers["Authorization"] = "Basic $b64"
}

$stamp = Get-Date -Format "yyyy-MM-dd"
$prefix = "03_jenkins_${Job}_${Iteration}_${stamp}"

# Si no se especifica BuildNumber, tomar el ultimo build
if ($BuildNumber -eq 0) {
    try {
        $jobInfo = Invoke-WebRequest -Uri "$JenkinsUrl/job/$Job/api/json" -Headers $headers -UseBasicParsing -TimeoutSec 15
        $BuildNumber = ($jobInfo.Content | ConvertFrom-Json).lastBuild.number
        Write-Host "Ultimo build detectado: #$BuildNumber" -ForegroundColor Cyan
    } catch {
        Write-Host "No se pudo obtener lastBuild: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

# Build info JSON
try {
    $buildInfo = Invoke-WebRequest -Uri "$JenkinsUrl/job/$Job/$BuildNumber/api/json" -Headers $headers -UseBasicParsing -TimeoutSec 15
    $infoOut = Join-Path $evidenceDir "${prefix}_buildInfo.json"
    $buildInfo.Content | Out-File -FilePath $infoOut -Encoding utf8
    Write-Host "[OK] buildInfo -> $infoOut" -ForegroundColor Green
} catch {
    Write-Host "[ERR] buildInfo: $($_.Exception.Message)" -ForegroundColor Red
}

# Console log
try {
    $log = Invoke-WebRequest -Uri "$JenkinsUrl/job/$Job/$BuildNumber/consoleText" -Headers $headers -UseBasicParsing -TimeoutSec 30
    $logOut = Join-Path $evidenceDir "${prefix}_consoleLog.txt"
    $log.Content | Out-File -FilePath $logOut -Encoding utf8
    Write-Host "[OK] consoleLog -> $logOut ($([math]::Round($log.Content.Length/1KB,1)) KB)" -ForegroundColor Green
} catch {
    Write-Host "[ERR] consoleLog: $($_.Exception.Message)" -ForegroundColor Red
}

# Resumen markdown
$summaryPath = Join-Path $evidenceDir "${prefix}_RESUMEN.md"
$infoFile = Join-Path $evidenceDir "${prefix}_buildInfo.json"
$lines = @("# Resumen Jenkins build - $Job #$BuildNumber ($Iteration)", "", "Fecha: $stamp", "")
if (Test-Path $infoFile) {
    $i = Get-Content $infoFile -Raw | ConvertFrom-Json
    $lines += "## Estado"
    $lines += "- Result: $($i.result)"
    $lines += "- Duration: $([math]::Round($i.duration / 1000, 1))s"
    $lines += "- Building: $($i.building)"
    $lines += "- URL: $($i.url)"
}
$lines | Out-File -FilePath $summaryPath -Encoding utf8
Write-Host "[SUMMARY] $summaryPath" -ForegroundColor Yellow

Write-Host ""
Write-Host "Evidencias Jenkins guardadas en $evidenceDir" -ForegroundColor Green
Write-Host "Recordatorio: capturar tambien screenshot del job como 03_jenkins_${Job}_${Iteration}.png" -ForegroundColor DarkYellow
