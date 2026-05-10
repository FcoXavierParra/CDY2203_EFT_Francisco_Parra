param(
    [Parameter(Mandatory=$true)][ValidateSet("inicial","final")][string]$Iteration,
    [string[]]$Images = @("db-backend:latest", "db-frontend:latest", "db-mysql-cdy2203-1:latest")
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$evidenceDir = Join-Path $root "docs\evidencias"
if (-not (Test-Path $evidenceDir)) { New-Item -ItemType Directory -Path $evidenceDir -Force | Out-Null }

$stamp = Get-Date -Format "yyyy-MM-dd"

foreach ($img in $Images) {
    $tag = ($img -replace ":latest$","") -replace ":","_" -replace "/","_"
    $prefix = "04_trivy_${tag}_${Iteration}_${stamp}"

    Write-Host ""
    Write-Host "===== Trivy: $img =====" -ForegroundColor Cyan

    # JSON detallado
    $jsonOut = Join-Path $evidenceDir "${prefix}.json"
    docker run --rm `
        -v //var/run/docker.sock:/var/run/docker.sock `
        -v trivy-cache:/root/.cache/ `
        -v "${root}:/work" `
        -w /work `
        aquasec/trivy:latest image `
        --ignorefile /work/.trivyignore `
        --severity CRITICAL,HIGH,MEDIUM `
        --format json `
        --quiet `
        $img 2>$null | Out-File -FilePath $jsonOut -Encoding utf8
    Write-Host "  [OK] JSON -> $jsonOut" -ForegroundColor Green

    # Tabla legible
    $tableOut = Join-Path $evidenceDir "${prefix}_table.txt"
    docker run --rm `
        -v //var/run/docker.sock:/var/run/docker.sock `
        -v trivy-cache:/root/.cache/ `
        -v "${root}:/work" `
        -w /work `
        aquasec/trivy:latest image `
        --ignorefile /work/.trivyignore `
        --severity CRITICAL,HIGH `
        --format table `
        --quiet `
        $img 2>$null | Out-File -FilePath $tableOut -Encoding utf8
    Write-Host "  [OK] Table -> $tableOut" -ForegroundColor Green

    # Conteo por severidad
    if (Test-Path $jsonOut) {
        try {
            $j = Get-Content $jsonOut -Raw | ConvertFrom-Json
            $crit=0; $high=0; $med=0
            if ($j.Results) {
                foreach ($r in $j.Results) {
                    if ($r.Vulnerabilities) {
                        foreach ($v in $r.Vulnerabilities) {
                            switch ($v.Severity) {
                                "CRITICAL" { $crit++ }
                                "HIGH"     { $high++ }
                                "MEDIUM"   { $med++ }
                            }
                        }
                    }
                }
            }
            Write-Host "  CRITICAL=$crit HIGH=$high MEDIUM=$med" -ForegroundColor $(if ($crit -gt 0) {"Red"} elseif ($high -gt 0) {"Yellow"} else {"Green"})
        } catch {
            Write-Host "  [WARN] No fue posible parsear JSON ($_)" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "Evidencias Trivy guardadas en $evidenceDir" -ForegroundColor Green