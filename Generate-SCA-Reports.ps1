param(
    [string]$NvdApiKey,
    [string]$Iteration = ""
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$projects = @(
    @{
        Name = "backend"
        Path = Join-Path $root "cdy2203-backend-2026-201-main\cdy2203-backend-2026-201-main"
    },
    @{
        Name = "frontend"
        Path = Join-Path $root "cdy2203-2026-201-main\cdy2203-2026-201-main"
    }
)

if ($NvdApiKey) {
    $env:NVD_API_KEY = $NvdApiKey
}

if (-not $env:NVD_API_KEY) {
    Write-Warning "NVD_API_KEY no esta definida. La primera carga puede fallar por limite 429 del NVD."
    Write-Warning "Puedes ejecutar: `$env:NVD_API_KEY='tu_api_key' antes de correr este script."
} elseif ($env:NVD_API_KEY -eq "TU_API_KEY") {
    throw "Debes reemplazar 'TU_API_KEY' por una API key real del NVD."
}

# Carpeta de archivado de evidencias (se crea si -Iteration fue provisto)
$evidenceDir = Join-Path $root "docs\evidencias"
if ($Iteration -and -not (Test-Path $evidenceDir)) {
    New-Item -ItemType Directory -Path $evidenceDir -Force | Out-Null
}

foreach ($project in $projects) {
    $projectName = $project.Name
    $projectPath = $project.Path
    $localRepo = Join-Path $projectPath ".m2repo"

    if (-not (Test-Path $projectPath)) {
        throw "No se encontro la ruta del proyecto: $projectPath"
    }

    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "Generando reporte SCA para $projectName" -ForegroundColor Cyan
    Write-Host "Proyecto: $projectPath"
    if ($Iteration) { Write-Host "Iteracion: $Iteration" -ForegroundColor Yellow }
    Write-Host "============================================================" -ForegroundColor Cyan

    Push-Location $projectPath
    try {
        if (-not (Test-Path $localRepo)) {
            New-Item -ItemType Directory -Path $localRepo | Out-Null
        }

        & .\mvnw.cmd org.owasp:dependency-check-maven:check "-Dmaven.repo.local=$localRepo"
        $exitCode = $LASTEXITCODE

        $reportDir = Join-Path $projectPath "target"
        $htmlReport = Join-Path $reportDir "dependency-check-report.html"
        $jsonReport = Join-Path $reportDir "dependency-check-report.json"
        $xmlReport = Join-Path $reportDir "dependency-check-report.xml"

        Write-Host ""
        if ($exitCode -ne 0) {
            Write-Host "El analisis SCA fallo para ${projectName}. Revisa el log mostrado arriba." -ForegroundColor Red
        } elseif ((Test-Path $htmlReport) -or (Test-Path $jsonReport) -or (Test-Path $xmlReport)) {
            Write-Host "Reportes generados para ${projectName}:" -ForegroundColor Green
            if (Test-Path $htmlReport) { Write-Host "HTML: $htmlReport" }
            if (Test-Path $jsonReport) { Write-Host "JSON: $jsonReport" }
            if (Test-Path $xmlReport) { Write-Host "XML:  $xmlReport" }

            # Archivado automatico cuando -Iteration fue provisto
            if ($Iteration) {
                $stamp = Get-Date -Format "yyyy-MM-dd"
                $prefix = "05_sca_${projectName}_${Iteration}_${stamp}"
                $targets = @{
                    $htmlReport = (Join-Path $evidenceDir "$prefix.html")
                    $jsonReport = (Join-Path $evidenceDir "$prefix.json")
                    $xmlReport  = (Join-Path $evidenceDir "$prefix.xml")
                }
                foreach ($src in $targets.Keys) {
                    if (Test-Path $src) {
                        Copy-Item -Path $src -Destination $targets[$src] -Force
                        Write-Host "  -> Archivado: $($targets[$src])" -ForegroundColor Green
                    }
                }
            }
        } else {
            Write-Host "El comando termino sin error, pero no se encontraron reportes para ${projectName} en target." -ForegroundColor Yellow
        }
    }
    finally {
        Pop-Location
    }
}

Write-Host ""
Write-Host "Proceso terminado." -ForegroundColor Green
if (-not $Iteration) {
    Write-Host "Tip: usa -Iteration 'inicial' o 'final' para archivar copias en docs/evidencias/." -ForegroundColor DarkYellow
}
